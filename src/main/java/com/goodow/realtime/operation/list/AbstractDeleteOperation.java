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

public abstract class AbstractDeleteOperation<T> extends AbstractListOperation<T> {
  public static final int TYPE = 6;

  protected AbstractDeleteOperation(String id, int startIndex, int length) {
    super(id, startIndex, null, length);
  }

  protected AbstractDeleteOperation(String id, int startIndex, T values) {
    super(id, startIndex, values, -1);
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
  public AbstractDeleteOperation<T>[] transformWith(AbstractOperation<ListTarget<T>> operation,
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
          return new AbstractDeleteOperation[] {values == null ? create(startIndex + op.length,
              length) : create(startIndex + op.length, values)};
        } else if (op.startIndex < endIndex0) {
          // ....[...]....
          // ......[.....
          int len0 = op.startIndex - startIndex;
          int len1 = length - len0;
          return new AbstractDeleteOperation[] {
              values == null ? create(startIndex, len0) : create(startIndex, getHelper().subset(
                  values, 0, len0)),
              values == null ? create(startIndex + op.length, len1) : create(
                  startIndex + op.length, getHelper().subset(values, len0, len1))};
        } else {
          // ....[...]....
          // .........[.].
          return new AbstractDeleteOperation[] {this};
        }
      case AbstractDeleteOperation.TYPE:
        if (endIndex1 <= startIndex) {
          // ....[...]....
          // .[.]...
          return new AbstractDeleteOperation[] {values == null ? create(startIndex - op.length,
              length) : create(startIndex - op.length, values)};
        } else if (op.startIndex >= endIndex0) {
          // ....[...]....
          // .........[.].
          return new AbstractDeleteOperation[] {this};
        } else if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[..]...]
          int len = endIndex0 - endIndex1;
          return endIndex1 < endIndex0 ? new AbstractDeleteOperation[] {values == null ? create(
              op.startIndex, len) : create(op.startIndex, getHelper().subset(values,
              endIndex1 - startIndex, len))} : null;
        } else {
          // ....[...]....
          // .....[.]..]
          if (endIndex1 < endIndex0) {
            return new AbstractDeleteOperation[] {values == null ? create(startIndex, length
                - op.length) : create(startIndex, getHelper().replaceWith(values,
                op.startIndex - startIndex, op.length, null))};
          } else {
            return new AbstractDeleteOperation[] {values == null ? create(startIndex, op.startIndex
                - startIndex) : create(startIndex, getHelper().subset(values, 0,
                op.startIndex - startIndex))};
          }
        }
      case AbstractReplaceOperation.TYPE:
        if (values == null || endIndex1 <= startIndex || op.startIndex >= endIndex0) {
          // ....[...]....
          // .[.]. OR .[.].
          return new AbstractDeleteOperation[] {this};
        } else if (op.startIndex <= startIndex) {
          // ....[...]....
          // ...[..]...]
          if (endIndex1 < endIndex0) {
            return new AbstractDeleteOperation[] {create(startIndex, getHelper().subset(op.values,
                startIndex - op.startIndex, endIndex1 - startIndex, values, endIndex1 - startIndex,
                endIndex0 - endIndex1))};
          } else {
            return new AbstractDeleteOperation[] {create(startIndex, getHelper().subset(op.values,
                startIndex - op.startIndex, length))};
          }
        } else {
          // ....[...]....
          // .....[.]..]
          if (endIndex1 < endIndex0) {
            return new AbstractDeleteOperation[] {create(startIndex, getHelper().replaceWith(
                values, op.startIndex - startIndex, op.length, op.values))};
          } else {
            return new AbstractDeleteOperation[] {create(startIndex, getHelper().subset(values, 0,
                op.startIndex - startIndex, op.values, 0, endIndex0 - op.startIndex))};
          }
        }
      default:
        throw new RuntimeException("Unsupported List Operation type: " + op.getType());
    }
  }

  protected abstract AbstractDeleteOperation<T> create(int startIndex, int length);

  protected abstract AbstractDeleteOperation<T> create(int startIndex, T values);
}
