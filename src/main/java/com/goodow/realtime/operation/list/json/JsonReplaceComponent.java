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
import com.goodow.realtime.operation.list.AbstractReplaceComponent;

public class JsonReplaceComponent extends AbstractReplaceComponent<JsonArray> {
  public static JsonReplaceComponent parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    JsonArray values = JsonHelper.INSTANCE.parseValues(serialized.getArray(3));
    return new JsonReplaceComponent(parseId(serialized), parseStartIndex(serialized), null, values);
  }

  public JsonReplaceComponent(String id, int startIndex, JsonArray oldValues,
      JsonArray newValues) {
    super(id, startIndex, oldValues, newValues);
  }

  @Override
  public JsonReplaceComponent invert() {
    assert oldValues != null;
    return new JsonReplaceComponent(id, startIndex, values, oldValues);
  }

  @Override
  protected JsonReplaceComponent create(int startIndex, JsonArray oldValues,
      JsonArray newValues) {
    return new JsonReplaceComponent(id, startIndex, oldValues, newValues);
  }

  @Override
  protected JsonHelper getHelper() {
    return JsonHelper.INSTANCE;
  }
}