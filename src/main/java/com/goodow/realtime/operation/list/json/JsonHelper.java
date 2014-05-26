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
package com.goodow.realtime.operation.list.json;

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonElement;
import com.goodow.realtime.operation.list.ListHelper;

public class JsonHelper implements ListHelper<JsonArray> {
  public static final int TYPE = 0;
  static final JsonHelper INSTANCE = new JsonHelper();

  @Override
  public int length(JsonArray values) {
    return values.length();
  }

  @Override
  public JsonArray parseValues(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE;
    int length = serialized.length();
    assert length >= 2;
    JsonArray values = Json.createArray();
    for (int i = 1; i < length; i++) {
      values.push(serialized.<JsonElement>get(i));
    }
    return values;
  }

  @Override
  public JsonArray replaceWith(JsonArray values, int startIndex, int length,
      JsonArray replacement) {
    final JsonArray array = Json.createArray();
    for (int i = 0; i < startIndex; i++) {
      array.push(values.get(i));
    }
    if (replacement != null) {
      replacement.forEach(new JsonArray.ListIterator<Object>() {
        @Override
        public void call(int index, Object value) {
          array.push(value);
        }
      });
    }
    for (int i = startIndex + length, len = values.length(); i < len; i++) {
      array.push(values.get(i));
    }
    return array;
  }

  @Override
  public JsonArray subset(JsonArray values, int startIndex, int length) {
    return subset(values, startIndex, length, 0, 0);
  }

  @Override
  public JsonArray subset(JsonArray values, int startIndex0, int length0, int startIndex1,
      int length1) {
    return subset(values, startIndex0, length0, values, startIndex1, length1);
  }

  @Override
  public JsonArray subset(JsonArray values0, int startIndex0, int length0,
      JsonArray values1, int startIndex1, int length1) {
    JsonArray array = Json.createArray();
    for (int i = startIndex0; i < startIndex0 + length0; i++) {
      array.push(values0.get(i));
    }
    for (int i = startIndex1; i < startIndex1 + length1; i++) {
      array.push(values1.get(i));
    }
    return array;
  }

  @Override
  public JsonArray toJson(JsonArray values) {
    final JsonArray json = Json.createArray().push(TYPE);
    values.forEach(new JsonArray.ListIterator<Object>() {
      @Override
      public void call(int index, Object value) {
        json.push(value);
      }
    });
    return json;
  }
}