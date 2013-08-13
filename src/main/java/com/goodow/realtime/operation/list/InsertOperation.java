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
package com.goodow.realtime.operation.list;

import com.goodow.realtime.operation.Operation;

import elemental.json.JsonArray;

public class InsertOperation<T> extends AbstractListOperation<T> {
  public static final int TYPE = 5;

  public static <T> InsertOperation<T> parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    T values = parseValues(serialized.getArray(3));
    return new InsertOperation<T>(parseId(serialized), (int) serialized.getNumber(2), values);
  }

  public InsertOperation(String id, int startIndex, T values) {
    super(id, startIndex, values, length(values));
  }

  @Override
  public void apply(ListTarget<T> target) {
    target.insert(startIndex, values);
  }

  @Override
  public int getType() {
    return TYPE;
  }

  @Override
  public Operation<ListTarget<T>> invert() {
    return new DeleteOperation<T>(id, startIndex, values);
  }

  @Override
  public int transformIndexReference(int index, boolean rigthSide, boolean canBeDeleted) {
    if (rigthSide ? startIndex <= index : startIndex < index) {
      return index + length;
    }
    return index;
  }

  @SuppressWarnings("unchecked")
  @Override
  public InsertOperation<T>[] transformWith(Operation<ListTarget<T>> operation, boolean arrivedAfter) {
    assert isSameId(operation) && operation instanceof AbstractListOperation;
    AbstractListOperation<T> op = (AbstractListOperation<T>) operation;
    int transformedStart = op.transformIndexReference(startIndex, arrivedAfter, false);
    return transformedStart == startIndex ? new InsertOperation[] {this}
        : new InsertOperation[] {new InsertOperation<T>(id, transformedStart, values)};
  }
}