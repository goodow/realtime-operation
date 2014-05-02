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

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.list.AbstractReplaceComponent;

public class StringReplaceComponent extends AbstractReplaceComponent<String> {
  public static StringReplaceComponent parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    String values = StringHelper.INSTANCE.parseValues(serialized.getArray(3));
    return new StringReplaceComponent(parseId(serialized), parseStartIndex(serialized), null,
        values);
  }

  public StringReplaceComponent(String id, int startIndex, String oldValues, String newValues) {
    super(id, startIndex, oldValues, newValues);
  }

  @Override
  public StringReplaceComponent invert() {
    assert oldValues != null;
    return new StringReplaceComponent(id, startIndex, values, oldValues);
  }

  @Override
  protected AbstractReplaceComponent<String> create(int startIndex, String oldValues,
      String newValues) {
    return new StringReplaceComponent(id, startIndex, oldValues, newValues);
  }

  @Override
  protected StringHelper getHelper() {
    return StringHelper.INSTANCE;
  }
}