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

public class DeleteOperation<T> extends AbstractListOperation<T> {
  public static final int TYPE = 6;

  public static <T> DeleteOperation<T> parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    return new DeleteOperation<T>(parseId(serialized), (int) serialized.getNumber(2),
        (int) serialized.getNumber(3));
  }

  public DeleteOperation(String id, int startIndex, T values) {
    super(id, startIndex, values, length(values));
  }

  private DeleteOperation(String id, int startIndex, int length) {
    super(id, startIndex, null, length);
  }

  @Override
  public void apply(ListTarget<T> target) {
    target.delete(startIndex, length);
  }

  @Override
  public int getType() {
    return TYPE;
  }

  @Override
  public Operation<ListTarget<T>> invert() {
    return new InsertOperation<T>(id, startIndex, values);
  }

  @Override
  public int transformIndexReference(int index, boolean rigthSide, boolean canBeDeleted) {
    if (startIndex + length <= index) {
      return index - length;
    } else if (startIndex <= index) {
      return canBeDeleted ? -1 : startIndex;
    } else {
      return index;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public DeleteOperation<T>[] transformWith(Operation<ListTarget<T>> operation, boolean arrivedAfter) {
    assert isSameId(operation) && operation instanceof AbstractListOperation;
    AbstractListOperation<T> op = (AbstractListOperation<T>) operation;
    int transformedStart = op.transformIndexReference(startIndex, true, false);
    int endIndex = startIndex + length;
    int transformedEnd = op.transformIndexReference(endIndex, false, false);
    if (transformedStart == startIndex && transformedEnd == endIndex) {
      return new DeleteOperation[] {this};
    }
    int type = op.getType();
    switch (type) {
      case InsertOperation.TYPE:
        if (op.startIndex <= startIndex) {
          return new DeleteOperation[] {new DeleteOperation<T>(id, transformedStart, transformedEnd
              - transformedStart)};
        } else {
          assert op.startIndex < endIndex && startIndex == transformedStart;
          int len1 = op.startIndex - startIndex;
          return new DeleteOperation[] {
              new DeleteOperation<T>(id, startIndex, len1),
              new DeleteOperation<T>(id, startIndex + op.length, length - len1)};
        }
      case DeleteOperation.TYPE:
        return transformedStart == transformedEnd ? null
            : new DeleteOperation[] {new DeleteOperation<T>(id, transformedStart, transformedEnd
                - transformedStart)};
      default:
        throw new RuntimeException("Unsupported List Operation type: " + type);
    }
  }
}
