/*
 * Copyright 2013 Goodow.com
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
package com.goodow.realtime.operation.list.string;

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.list.ListHelper;

public class StringHelper implements ListHelper<String> {
  public static final int TYPE = 1;
  static final StringHelper INSTANCE = new StringHelper();

  @Override
  public int length(String values) {
    return values.length();
  }

  @Override
  public String parseValues(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 2;
    String string = serialized.getString(1);
    assert !string.isEmpty();
    return string;
  }

  @Override
  public String replaceWith(String values, int startIndex, int length, String replacement) {
    return values.substring(0, startIndex) + (replacement == null ? "" : replacement)
        + values.substring(startIndex + length);
  }

  @Override
  public String subset(String values, int startIndex, int length) {
    return values.substring(startIndex, startIndex + length);
  }

  @Override
  public String subset(String values, int startIndex0, int length0, int startIndex1, int length1) {
    return subset(values, startIndex0, length0, values, startIndex1, length1);
  }

  @Override
  public String subset(String values0, int startIndex0, int length0, String values1,
      int startIndex1, int length1) {
    return subset(values0, startIndex0, length0) + subset(values1, startIndex1, length1);
  }

  @Override
  public JsonArray toJson(String values) {
    return Json.createArray().push(TYPE).push(values);
  }
}