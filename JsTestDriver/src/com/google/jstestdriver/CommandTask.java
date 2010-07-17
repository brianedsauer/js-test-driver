/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver;

import static java.lang.String.format;

import com.google.gson.Gson;
import com.google.jstestdriver.model.RunData;
import com.google.jstestdriver.util.StopWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles the communication of a command to the JsTestDriverServer from the
 * JsTestDriverClient.
 *
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class CommandTask {

  static final Logger logger = LoggerFactory.getLogger(CommandTask.class);

  static final List<String> EMPTY_ARRAYLIST = new ArrayList<String>();

  public static final int CHUNK_SIZE = 50;

  private final Gson gson = new Gson();

  private final ResponseStream stream;
  private final String baseUrl;
  private final Server server;
  private final Map<String, String> params;
  private final boolean upload;

  private final StopWatch stopWatch;

  private final FileUploader fileUploader;


  public CommandTask(JsTestDriverFileFilter filter, ResponseStream stream, String baseUrl,
      Server server, Map<String, String> params, FileLoader fileLoader,
      boolean upload, StopWatch stopWatch) {
    this.stream = stream;
    this.baseUrl = baseUrl;
    this.server = server;
    this.params = params;
    this.upload = upload;
    this.stopWatch = stopWatch;
    fileUploader = new FileUploader(stopWatch, server, baseUrl, fileLoader, filter);
  }
  
  /**
   * Throws an exception if the expected browser is not available for this task.
   */
  private void checkBrowser() {
    String alive = server.fetch(baseUrl + "/heartbeat?id=" + params.get("id"));

    if (!alive.equals("OK")) {
      throw new FailureException(
          format("Browser is not available\n {} \nfor\n {}", alive, params));
    }
  }

  // TODO(corysmith): remove this function once FileInfo is used exclusively.
  // Hate static crap.
  public static FileSource fileInfoToFileSource(FileInfo info) {
    if (info.getFilePath().startsWith("http://")) {
      return new FileSource(info.getFilePath(), info.getTimestamp());
    }
    return new FileSource("/test/" + info.getFilePath(), info.getTimestamp());
  }

  public void run(RunData runData) {
    String browserId = params.get("id");
    try {
      checkBrowser();

      logger.debug("Starting upload for {}", browserId);
      if (upload) {
        fileUploader.uploadFileSet(browserId, runData.getFileSet(), stream);
      }
      logger.debug("Finished upload for {}", browserId);
      server.post(baseUrl + "/cmd", params);
      StreamMessage streamMessage = null;

      stopWatch.start("execution %s", params.get("data"));
      logger.debug("Starting {} for {}", params.get("data"), browserId);
      do {
        String response = server.fetch(baseUrl + "/cmd?id=" + browserId);
        streamMessage = gson.fromJson(response, StreamMessage.class);
        Response resObj = streamMessage.getResponse();
        stream.stream(resObj);
      } while (!streamMessage.isLast());
      stopWatch.stop("execution %s", params.get("data"));
    } finally {
      logger.debug("finished {} for {}", params.get("data"), browserId);
    }
  }
}
