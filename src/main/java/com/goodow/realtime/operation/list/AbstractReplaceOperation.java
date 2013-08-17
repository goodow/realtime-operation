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

import com.goodow.realtime.operation.AbstractOperation;

public abstract class AbstractReplaceOperation<T> extends AbstractListOperation<T> {
  public static final int TYPE = 11;

  protected final T oldValues;

  protected AbstractReplaceOperation(String id, int startIndex, T oldValues, T newValues) {
    super(id, startIndex, newValues, -1);
    this.oldValues = oldValues;
    assert oldValues == null || getHelper().length(oldValues) == length;
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
  public int transformIndexReference(int index, boolean rigthSide, boolean canBeDeleted) {
    return index;
  }

  @Override
  @SuppressWarnings("unchecked")
  public AbstractReplaceOperation<T>[] transformWith(AbstractOperation<ListTarget<T>> operation,
      boolean arrivedAfter) {
    assert operation instanceof AbstractListOperation && isSameId(operation);
    AbstractListOperation<T> op = (AbstractListOperation<T>) operation;
    int endIndex0 = startIndex + length;
    int endIndex1 = op.startIndex + op.length;
    switch (op.getType()) {
      case AbstractInsertOperation.TYPE:
        if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[.......
          return new AbstractReplaceOperation[] {create(startIndex + op.length, oldValues, values)};
        } else if (op.startIndex < endIndex0) {
          // ....[...]....
          // ......[.....
          int len0 = op.startIndex - startIndex;
          return new AbstractReplaceOperation[] {
              create(startIndex, oldValues == null ? null : getHelper().subset(oldValues, 0, len0),
                  getHelper().subset(values, 0, len0)),
              create(endIndex1, oldValues == null ? null : getHelper().subset(oldValues, len0,
                  length - len0), getHelper().subset(values, len0, length - len0))};
        } else {
          // ....[...]....
          // .........[.].
          return new AbstractReplaceOperation[] {this};
        }
      case AbstractDeleteOperation.TYPE:
        if (op.startIndex >= endIndex0) {
          // ....[...]....
          // .........[.].
          return new AbstractReplaceOperation[] {this};
        } else if (endIndex1 <= startIndex) {
          // ....[...]....
          // .[.]...
          return new AbstractReplaceOperation[] {create(startIndex - op.length, oldValues, values)};
        } else if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[..]...]
          return endIndex1 < endIndex0 ? new AbstractReplaceOperation[] {create(op.startIndex,
              oldValues == null ? null : getHelper().subset(oldValues, endIndex1 - startIndex,
                  endIndex0 - endIndex1), getHelper().subset(values, endIndex1 - startIndex,
                  endIndex0 - endIndex1))} : null;
        } else {
          // ....[...]....
          // .....[.]..]
          return endIndex1 < endIndex0 ? new AbstractReplaceOperation[] {create(startIndex,
              oldValues == null ? null : getHelper().replaceWith(oldValues,
                  op.startIndex - startIndex, op.length, null), getHelper().replaceWith(values,
                  op.startIndex - startIndex, op.length, null))}
              : new AbstractReplaceOperation[] {create(startIndex, oldValues == null ? null
                  : getHelper().subset(oldValues, 0, op.startIndex - startIndex), getHelper()
                  .subset(values, 0, op.startIndex - startIndex))};
        }
      case AbstractReplaceOperation.TYPE:
        if (endIndex1 <= startIndex || op.startIndex >= endIndex0
            || (arrivedAfter && oldValues == null)) {
          // ....[...]....
          // .[.]. OR .[.].
          return new AbstractReplaceOperation[] {this};
        } else if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[..]...]
          if (arrivedAfter) {
            T transformedOldValues =
                endIndex1 < endIndex0 ? getHelper().subset(op.values, startIndex - op.startIndex,
                    endIndex1 - startIndex, oldValues, endIndex1 - startIndex,
                    endIndex0 - endIndex1) : getHelper().subset(op.values,
                    startIndex - op.startIndex, length);
            return new AbstractReplaceOperation[] {create(startIndex, transformedOldValues, values)};
          } else {
            return endIndex1 < endIndex0 ? new AbstractReplaceOperation[] {create(endIndex1,
                oldValues == null ? null : getHelper().subset(oldValues, endIndex1 - startIndex,
                    endIndex0 - endIndex1), getHelper().subset(values, endIndex1 - startIndex,
                    endIndex0 - endIndex1))} : null;
          }
        } else {
          // ....[...]....
          // .....[.]..]
          if (arrivedAfter) {
            T transformedOldValues =
                endIndex1 < endIndex0 ? getHelper().replaceWith(oldValues,
                    op.startIndex - startIndex, op.length, op.values) : getHelper().subset(
                    oldValues, 0, op.startIndex - startIndex, op.values, 0,
                    endIndex0 - op.startIndex);
            return new AbstractReplaceOperation[] {create(startIndex, transformedOldValues, values)};
          } else {
            AbstractReplaceOperation<T> op1 =
                create(startIndex, oldValues == null ? null : getHelper().subset(oldValues, 0,
                    op.startIndex - startIndex), getHelper().subset(values, 0,
                    op.startIndex - startIndex));
            return endIndex1 < endIndex0 ? new AbstractReplaceOperation[] {
                op1,
                create(endIndex1, oldValues == null ? null : getHelper().subset(oldValues,
                    endIndex1 - startIndex, endIndex0 - endIndex1), getHelper().subset(values,
                    endIndex1 - startIndex, endIndex0 - endIndex1))}
                : new AbstractReplaceOperation[] {op1};
          }
        }
      default:
        throw new RuntimeException("Unsupported List Operation type: " + op.getType());
    }
  }

  protected abstract AbstractReplaceOperation<T> create(int startIndex, T oldValues, T newValues);
}
