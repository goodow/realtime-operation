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

public class ReplaceOperation<T> extends AbstractListOperation<T> {
  public static final int TYPE = 11;

  public static <T> ReplaceOperation<T> parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 4;
    T values = parseValues(serialized.getArray(3));
    return new ReplaceOperation<T>(parseId(serialized), (int) serialized.getNumber(2), values);
  }

  private final T oldValues;

  public ReplaceOperation(String id, int startIndex, T oldValues, T newValues) {
    super(id, startIndex, newValues, length(newValues));
    this.oldValues = oldValues;
    assert oldValues != null && length(oldValues) == length;
  }

  private ReplaceOperation(String id, int startIndex, T newValues) {
    super(id, startIndex, newValues, length(newValues));
    this.oldValues = null;
  }

  @Override
  public void apply(ListTarget<T> target) {
    target.replace(startIndex, values);
  }

  @Override
  public int getType() {
    return TYPE;
  }

  @Override
  public Operation<ListTarget<T>> invert() {
    return new ReplaceOperation<T>(id, startIndex, values, oldValues);
  }

  @Override
  public int transformIndexReference(int index, boolean rigthSide, boolean canBeDeleted) {
    return index;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ReplaceOperation<T>[] transformWith(Operation<ListTarget<T>> operation,
      boolean arrivedAfter) {
    assert isSameId(operation) && operation instanceof AbstractListOperation;
    AbstractListOperation<T> op = (AbstractListOperation<T>) operation;
    int type = op.getType();
    int endIndex1 = startIndex + length;
    int endIndex2 = op.startIndex + op.length;
    switch (type) {
      case InsertOperation.TYPE:
        if (op.startIndex <= startIndex) {
          return new ReplaceOperation[] {new ReplaceOperation<T>(id, startIndex + op.length, values)};
        } else if (op.startIndex < endIndex1) {
          int len1 = op.startIndex - startIndex;
          return new ReplaceOperation[] {
              new ReplaceOperation<T>(id, startIndex, subset(values, 0, len1, -1, 0)),
              new ReplaceOperation<T>(id, endIndex2, subset(values, len1, length - len1, -1, 0))};
        } else {
          return new ReplaceOperation[] {this};
        }
      case DeleteOperation.TYPE:
        if (op.startIndex >= endIndex1) {
          // ....[...]....
          // .........[.].
          return new ReplaceOperation[] {this};
        } else if (endIndex2 <= startIndex) {
          // ....[...]....
          // .[.]...
          return new ReplaceOperation[] {new ReplaceOperation<T>(id, startIndex - op.length, values)};
        }
        if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[..]...]
          return endIndex2 < endIndex1 ? new ReplaceOperation[] {new ReplaceOperation<T>(id,
              op.startIndex, subset(values, endIndex2 - startIndex, -1, -1, 0))} : null;
        } else {
          // ....[...]....
          // .....[.]..]
          return endIndex2 < endIndex1 ? new ReplaceOperation[] {new ReplaceOperation<T>(id,
              startIndex, subset(values, 0, op.startIndex - startIndex, endIndex2 - startIndex,
                  endIndex1 - endIndex2))} : new ReplaceOperation[] {new ReplaceOperation<T>(id,
              startIndex, subset(values, 0, op.startIndex - startIndex, -1, 0))};
        }
      case ReplaceOperation.TYPE:
        if (arrivedAfter || endIndex2 <= startIndex || op.startIndex >= endIndex1) {
          // ....[...]....
          // .[.]. OR .[.].
          return new ReplaceOperation[] {this};
        }
        if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[..]...]
          return endIndex2 < endIndex1 ? new ReplaceOperation[] {new ReplaceOperation<T>(id,
              endIndex2, subset(values, endIndex2 - startIndex, -1, -1, 0))} : null;
        } else {
          // ....[...]....
          // .....[.]..]
          ReplaceOperation<T> op1 =
              new ReplaceOperation<T>(id, startIndex, subset(values, 0, op.startIndex - startIndex,
                  -1, 0));
          return endIndex2 < endIndex1 ? new ReplaceOperation[] {
              op1,
              new ReplaceOperation<T>(id, endIndex2, subset(values, endIndex2 - startIndex, -1, -1,
                  0))} : new ReplaceOperation[] {op1};
        }
      default:
        throw new RuntimeException("Unsupported List Operation type: " + type);
    }
  }
}
