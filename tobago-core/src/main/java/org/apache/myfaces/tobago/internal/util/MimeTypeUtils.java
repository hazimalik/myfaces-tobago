package org.apache.myfaces.tobago.internal.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class MimeTypeUtils {

  private MimeTypeUtils() {
    // utils class
  }

  // todo: maybe support more extensions (configurable?)  
  public static String getMimeTypeForFile(String file) {
    if (file.endsWith(".gif")) {
      return "image/gif";
    } else if (file.endsWith(".png")) {
      return "image/png";
    } else if (file.endsWith(".jpg")) {
      return "image/jpeg";
    } else if (file.endsWith(".js")) {
      return "text/javascript";
    } else if (file.endsWith(".css")) {
      return "text/css";
    } else if (file.endsWith(".ico")) {
      return "image/vnd.microsoft.icon";
    } else if (file.endsWith(".html") || file.endsWith(".htm")) {
      return "text/html";
    }
    return null;
  }
}
