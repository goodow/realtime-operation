/*
 * Copyright 2012 Goodow.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.goodow.realtime.operation.util;

import elemental.json.JsonType;
import elemental.json.JsonValue;

public class JsonUtility {
  public static boolean isNull(JsonValue json) {
    return json == null || JsonType.NULL == json.getType();
  }

  public static boolean jsonEqual(JsonValue a, JsonValue b) {
    if (isNull(a)) {
      return isNull(b);
    } else {
      return isNull(b) ? false : a.toJson().equals(b.toJson());
    }
  }
}
