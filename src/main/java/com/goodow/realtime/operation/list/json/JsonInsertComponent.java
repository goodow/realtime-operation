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
import com.goodow.realtime.operation.list.AbstractInsertComponent;

public class JsonInsertComponent extends AbstractInsertComponent<JsonElement[]> {
  public static JsonInsertComponent parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    JsonElement[] values = JsonHelper.INSTANCE.parseValues(serialized.getArray(3));
    return new JsonInsertComponent(parseId(serialized), parseStartIndex(serialized), values);
  }

  public JsonInsertComponent(String id, int startIndex, JsonElement[] values) {
    super(id, startIndex, values);
  }

  @Override
  public JsonDeleteComponent invert() {
    return new JsonDeleteComponent(id, startIndex, values);
  }

  @Override
  protected JsonInsertComponent create(int startIndex, JsonElement[] values) {
    return new JsonInsertComponent(id, startIndex, values);
  }

  @Override
  protected JsonHelper getHelper() {
    return JsonHelper.INSTANCE;
  }
}