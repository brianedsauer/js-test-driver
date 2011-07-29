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


/**
 * @fileDesciption
 * Provides the namespaces and necessary function to enable migration to the
 * Google JsCompiler.
 *
 * @author Cory Smith (corbinrsmith@gmail.com)
 */

var jstestdriver = {};
jstestdriver.plugins = {};
jstestdriver.plugins.async = {};

jstestdriver.JSON = JSON || {
  stringify : function(msg, opt_args){}
};

var goog = window.goog || {
  provide : function(symbol){},
  require : function(symbol){}
};

