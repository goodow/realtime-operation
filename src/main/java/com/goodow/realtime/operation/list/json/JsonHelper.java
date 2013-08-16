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

import com.goodow.realtime.operation.list.ListHelper;

import elemental.json.JsonArray;
import elemental.json.JsonValue;

public class JsonHelper implements ListHelper<JsonValue[]> {
  public static final int TYPE = 0;
  static final JsonHelper INSTANCE = new JsonHelper();

  @Override
  public int length(JsonValue[] values) {
    return values.length;
  }

  @Override
  public JsonValue[] parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE;
    int length = serialized.length();
    assert length >= 2;
    JsonValue[] values = create(length - 1);
    for (int i = 1; i < length; i++) {
      values[i - 1] = serialized.get(i);
    }
    return values;
  }

  @Override
  public JsonValue[] replaceWith(JsonValue[] values, int startIndex, int length,
      JsonValue[] replacement) {
    int len = replacement == null ? 0 : replacement.length;
    JsonValue[] array = create(values.length - length + len);
    System.arraycopy(values, 0, array, 0, startIndex);
    if (replacement != null) {
      System.arraycopy(replacement, 0, array, startIndex, len);
    }
    System.arraycopy(values, startIndex + length, array, startIndex + len, values.length
        - startIndex - length);
    return array;
  }

  @Override
  public StringBuilder serialize(JsonValue[] values) {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    sb.append(TYPE);
    for (JsonValue value : values) {
      sb.append(',').append(value == null ? (String) null : value.toJson());
    }
    sb.append(']');
    return sb;
  }

  @Override
  public JsonValue[] subset(JsonValue[] values, int startIndex, int length) {
    JsonValue[] array = create(length);
    System.arraycopy(values, startIndex, array, 0, length);
    return array;
  }

  @Override
  public JsonValue[] subset(JsonValue[] values, int startIndex0, int length0, int startIndex1,
      int length1) {
    return subset(values, startIndex0, length0, values, startIndex1, length1);
  }

  @Override
  public JsonValue[] subset(JsonValue[] values0, int startIndex0, int length0, JsonValue[] values1,
      int startIndex1, int length1) {
    JsonValue[] array = create(length0 + length1);
    System.arraycopy(values0, startIndex0, array, 0, length0);
    System.arraycopy(values1, startIndex1, array, length0, length1);
    return array;
  }

  protected JsonValue[] create(int length) {
    return new JsonValue[length];
  }
}