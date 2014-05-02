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
import com.goodow.realtime.operation.OperationComponent;

public abstract class AbstractReplaceComponent<T> extends AbstractListComponent<T> {
  public static final int TYPE = 11;

  protected final T oldValues;

  protected AbstractReplaceComponent(String id, int startIndex, T oldValues, T newValues) {
    super(TYPE, id, startIndex, newValues, -1);
    this.oldValues = oldValues;
    assert oldValues == null || getHelper().length(oldValues) == length;
  }

  @Override
  public void apply(ListTarget<T> target) {
    target.replace(startIndex, values);
  }

  @Override
  public Operation<ListTarget<T>> transform(Operation<ListTarget<T>> other, boolean applied) {
    assert other instanceof AbstractListComponent && isSameId(other);
    AbstractListComponent<T> op = (AbstractListComponent<T>) other;
    int endIndex0 = startIndex + length;
    int endIndex1 = op.startIndex + op.length;
    switch (op.type) {
      case AbstractInsertComponent.TYPE:
        if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[.......
          return create(startIndex + op.length, oldValues, values);
        } else if (op.startIndex < endIndex0) {
          // ....[...]....
          // ......[.....
          return super.transform(other, applied);
        } else {
          // ....[...]....
          // .........[.].
          return this;
        }
      case AbstractDeleteComponent.TYPE:
        if (op.startIndex >= endIndex0) {
          // ....[...]....
          // .........[.].
          return this;
        } else if (endIndex1 <= startIndex) {
          // ....[...]....
          // .[.]...
          return create(startIndex - op.length, oldValues, values);
        } else if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[..]...]
          return endIndex1 < endIndex0 ? create(op.startIndex, oldValues == null ? null
              : getHelper().subset(oldValues, endIndex1 - startIndex, endIndex0 - endIndex1),
              getHelper().subset(values, endIndex1 - startIndex, endIndex0 - endIndex1)) : null;
        } else {
          // ....[...]....
          // .....[.]..]
          return endIndex1 < endIndex0 ? create(startIndex, oldValues == null ? null : getHelper()
              .replaceWith(oldValues, op.startIndex - startIndex, op.length, null), getHelper()
              .replaceWith(values, op.startIndex - startIndex, op.length, null)) : create(
              startIndex, oldValues == null ? null : getHelper().subset(oldValues, 0,
                  op.startIndex - startIndex), getHelper().subset(values, 0,
                  op.startIndex - startIndex));
        }
      case AbstractReplaceComponent.TYPE:
        if (endIndex1 <= startIndex || op.startIndex >= endIndex0
            || (!applied && oldValues == null)) {
          // ....[...]....
          // .[.]. OR .[.].
          return this;
        } else if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[..]...]
          if (!applied) {
            T transformedOldValues =
                endIndex1 < endIndex0 ? getHelper().subset(op.values, startIndex - op.startIndex,
                    endIndex1 - startIndex, oldValues, endIndex1 - startIndex,
                    endIndex0 - endIndex1) : getHelper().subset(op.values,
                    startIndex - op.startIndex, length);
            return create(startIndex, transformedOldValues, values);
          } else {
            return endIndex1 < endIndex0 ? create(endIndex1, oldValues == null ? null : getHelper()
                .subset(oldValues, endIndex1 - startIndex, endIndex0 - endIndex1), getHelper()
                .subset(values, endIndex1 - startIndex, endIndex0 - endIndex1)) : null;
          }
        } else {
          // ....[...]....
          // .....[.]..]
          if (!applied) {
            T transformedOldValues =
                endIndex1 < endIndex0 ? getHelper().replaceWith(oldValues,
                    op.startIndex - startIndex, op.length, op.values) : getHelper().subset(
                    oldValues, 0, op.startIndex - startIndex, op.values, 0,
                    endIndex0 - op.startIndex);
            return create(startIndex, transformedOldValues, values);
          } else if (endIndex1 >= endIndex0) {
            // ....[...]....
            // .....[....]
            return create(startIndex, oldValues == null ? null : getHelper().subset(oldValues, 0,
                op.startIndex - startIndex), getHelper().subset(values, 0,
                op.startIndex - startIndex));
          } else {
            return super.transform(other, applied);
          }
        }
      default:
        throw new RuntimeException("Unsupported List Operation type: " + op.type);
    }
  }

  @Override
  public int transformIndexReference(int index, boolean rigthSide, boolean canBeDeleted) {
    return index;
  }

  @Override
  public OperationComponent<ListTarget<T>>[] transformComponent(OperationComponent<ListTarget<T>> other,
      boolean applied) {
    AbstractListComponent<T> op = (AbstractListComponent<T>) other;
    int endIndex0 = startIndex + length;
    int endIndex1 = op.startIndex + op.length;
    switch (op.type) {
      case AbstractInsertComponent.TYPE:
        if (op.startIndex > startIndex && op.startIndex < endIndex0) {
          // ....[...]....
          // ......[.....
          int len0 = op.startIndex - startIndex;
          return asArray(create(startIndex, oldValues == null ? null : getHelper().subset(
              oldValues, 0, len0), getHelper().subset(values, 0, len0)), create(endIndex1,
              oldValues == null ? null : getHelper().subset(oldValues, len0, length - len0),
              getHelper().subset(values, len0, length - len0)));
        }
      case AbstractReplaceComponent.TYPE:
        // ....[...]....
        // .....[.]...
        if (applied && op.startIndex > startIndex && endIndex1 < endIndex0) {
          AbstractReplaceComponent<T> op1 =
              create(startIndex, oldValues == null ? null : getHelper().subset(oldValues, 0,
                  op.startIndex - startIndex), getHelper().subset(values, 0,
                  op.startIndex - startIndex));
          return asArray(op1, create(endIndex1, oldValues == null ? null : getHelper().subset(
              oldValues, endIndex1 - startIndex, endIndex0 - endIndex1), getHelper().subset(values,
              endIndex1 - startIndex, endIndex0 - endIndex1)));
        }
      default:
        throw new RuntimeException("Unsupported List Operation type: " + op.type);
    }
  }

  protected abstract AbstractReplaceComponent<T> create(int startIndex, T oldValues, T newValues);
}
