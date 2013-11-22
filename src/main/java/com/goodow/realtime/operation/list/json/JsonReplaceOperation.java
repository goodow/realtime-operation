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

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonElement;
import com.goodow.realtime.operation.list.AbstractReplaceOperation;

public class JsonReplaceOperation extends AbstractReplaceOperation<JsonElement[]> {
  public static JsonReplaceOperation parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    JsonElement[] values = JsonHelper.INSTANCE.parseValues(serialized.getArray(3));
    return new JsonReplaceOperation(parseId(serialized), parseStartIndex(serialized), null, values);
  }

  public JsonReplaceOperation(String id, int startIndex, JsonElement[] oldValues,
      JsonElement[] newValues) {
    super(id, startIndex, oldValues, newValues);
  }

  @Override
  public JsonReplaceOperation invert() {
    assert oldValues != null;
    return new JsonReplaceOperation(id, startIndex, values, oldValues);
  }

  @Override
  protected JsonReplaceOperation create(int startIndex, JsonElement[] oldValues,
      JsonElement[] newValues) {
    return new JsonReplaceOperation(id, startIndex, oldValues, newValues);
  }

  @Override
  protected JsonHelper getHelper() {
    return JsonHelper.INSTANCE;
  }
}