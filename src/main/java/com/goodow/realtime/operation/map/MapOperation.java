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
package com.goodow.realtime.operation.map;

import com.goodow.realtime.operation.AbstractOperation;
import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.util.JsonUtility;

import elemental.json.JsonArray;
import elemental.json.JsonValue;

public class MapOperation extends AbstractOperation<MapTarget> {
  public static final int TYPE = 8;

  public static MapOperation parse(JsonArray serialized) {
    int length = serialized.length();
    assert serialized.getNumber(0) == TYPE && (length == 3 || length == 4);
    return new MapOperation(parseId(serialized), serialized.getString(2), null, length == 3 ? null
        : serialized.get(3));
  }

  private final String key;
  private final JsonValue oldValue;

  private final JsonValue newValue;

  public MapOperation(String id, String key, JsonValue oldValue, JsonValue newValue) {
    super(id);
    assert key != null : "Null key";
    assert (oldValue == null && newValue == null) || !JsonUtility.jsonEqual(oldValue, newValue);
    this.key = key;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public void apply(MapTarget target) {
    target.set(key, newValue);
  }

  @Override
  public int getType() {
    return TYPE;
  }

  @Override
  public Operation<MapTarget> invert() {
    return new MapOperation(id, key, newValue, oldValue);
  }

  @Override
  public MapOperation[] transformWith(Operation<MapTarget> operation, boolean arrivedAfter) {
    assert isSameId(operation) && operation instanceof MapOperation;
    MapOperation op = (MapOperation) operation;
    if (!key.equals(op.key)) {
      return new MapOperation[] {this};
    }
    if (!arrivedAfter || JsonUtility.jsonEqual(newValue, op.newValue)) {
      return null;
    }
    return new MapOperation[] {new MapOperation(id, key, op.newValue, newValue)};
  }

  @Override
  protected void toString(StringBuilder sb) {
    sb.append('\'').append(key).append('\'');
    if (!JsonUtility.isNull(newValue)) {
      sb.append(',').append(newValue.toJson());
    }
  }
}