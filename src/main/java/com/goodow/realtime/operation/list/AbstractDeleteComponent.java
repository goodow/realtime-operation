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

public abstract class AbstractDeleteComponent<T> extends AbstractListComponent<T> {
  public static final int TYPE = 6;

  protected AbstractDeleteComponent(String id, int startIndex, int length) {
    super(TYPE, id, startIndex, null, length);
  }

  protected AbstractDeleteComponent(String id, int startIndex, T values) {
    super(TYPE, id, startIndex, values, -1);
  }

  @Override
  public void apply(ListTarget<T> target) {
    target.delete(startIndex, length);
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
          // ...|.......
          return canUndo() ? create(startIndex + op.length, values)
                           : create(startIndex + op.length, length);
        } else if (op.startIndex < endIndex0) {
          // ....[...]....
          // ......|.....
          return super.transform(other, applied);
        } else {
          // ....[...]....
          // .........|..
          return this;
        }
      case AbstractDeleteComponent.TYPE:
        if (endIndex1 <= startIndex) {
          // ....[...]....
          // .[.]...
          return canUndo() ? create(startIndex - op.length, values)
                           : create(startIndex - op.length, length);
        } else if (op.startIndex >= endIndex0) {
          // ....[...]....
          // .........[.].
          return this;
        } else if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[..]...]
          int len = endIndex0 - endIndex1;
          return endIndex1 < endIndex0 ? (canUndo() ? create(op.startIndex, getHelper()
              .subset(values, endIndex1 - startIndex, len)) : create(op.startIndex, len)) : null;
        } else {
          // ....[...]....
          // .....[.]..]
          if (endIndex1 < endIndex0) {
            return canUndo() ? create(startIndex, getHelper()
                .replaceWith(values, op.startIndex - startIndex, op.length, null))
                : create(startIndex, length - op.length);
          } else {
            return canUndo() ? create(startIndex, getHelper()
                .subset(values, 0, op.startIndex - startIndex))
                : create(startIndex, op.startIndex - startIndex);
          }
        }
      case AbstractReplaceComponent.TYPE:
        if (!canUndo() || endIndex1 <= startIndex || op.startIndex >= endIndex0) {
          // ....[...]....
          // .{.}. OR .{.}.
          return this;
        } else if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...{..}...}
          if (endIndex1 < endIndex0) {
            return create(startIndex, getHelper().subset(op.values, startIndex - op.startIndex,
                endIndex1 - startIndex, values, endIndex1 - startIndex, endIndex0 - endIndex1));
          } else {
            return create(startIndex, getHelper().subset(op.values, startIndex - op.startIndex,
                length));
          }
        } else {
          // ....[...]....
          // .....{.}..}
          if (endIndex1 < endIndex0) {
            return create(startIndex, getHelper().replaceWith(values, op.startIndex - startIndex,
                op.length, op.values));
          } else {
            return create(startIndex, getHelper().subset(values, 0, op.startIndex - startIndex,
                op.values, 0, endIndex0 - op.startIndex));
          }
        }
      default:
        throw new RuntimeException("Unsupported List Operation type: " + op.type);
    }
  }

  @Override
  public AbstractDeleteComponent<T>[] transformComponent(OperationComponent<ListTarget<T>> other,
      boolean applied) {
    AbstractListComponent<T> op = (AbstractListComponent<T>) other;
    int endIndex0 = startIndex + length;
    assert op.type == AbstractInsertComponent.TYPE && op.startIndex > startIndex
        && op.startIndex < endIndex0;
    // ....[...]....
    // ......|.....
    int len0 = op.startIndex - startIndex;
    int len1 = length - len0;
    return asArray(canUndo() ? create(startIndex, getHelper().subset(values, 0, len0))
                             : create(startIndex, len0),
                  canUndo() ? create(startIndex + op.length, getHelper().subset(values, len0, len1))
                            : create(startIndex + op.length, len1));
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

  protected abstract AbstractDeleteComponent<T> create(int startIndex, int length);

  protected abstract AbstractDeleteComponent<T> create(int startIndex, T values);

  private boolean canUndo() {
    return values != null;
  }
}
