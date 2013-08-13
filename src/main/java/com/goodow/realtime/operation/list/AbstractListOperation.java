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

import elemental.json.JsonArray;
import elemental.json.JsonValue;

public abstract class AbstractListOperation<T> extends AbstractOperation<ListTarget<T>> {
  protected static <T> int length(T values) {
    assert values != null;
    if (values instanceof String) {
      return ((String) values).length();
    } else {
      assert values.getClass().isArray();
      return ((Object[]) values).length;
    }
  }

  @SuppressWarnings("unchecked")
  protected static <T> T parseValues(JsonArray array) {
    int subType = (int) array.getNumber(0);
    int length = array.length();
    switch (subType) {
      case 0:
        assert length >= 2;
        JsonValue[] val = new JsonValue[length - 1];
        for (int i = 1; i < length; i++) {
          val[i - 1] = array.get(i);
        }
        return (T) val;
      case 1:
        assert length == 2;
        String string = array.getString(1);
        assert !string.isEmpty();
        return (T) string;
      default:
        throw new RuntimeException("Unsupported List Operation sub-type: " + subType);
    }
  }

  @SuppressWarnings("unchecked")
  protected static <T> T subset(T values, int startIndex1, int length1, int startIndex2, int length2) {
    assert values != null;
    if (length1 == -1) {
      length1 = length(values) - startIndex1;
    }
    if (values instanceof String) {
      String substring = ((String) values).substring(startIndex1, startIndex1 + length1);
      return length2 == 0 ? (T) substring : (T) (substring + ((String) values).substring(
          startIndex2, startIndex2 + length2));
    }
    T toRtn;
    if (values instanceof String[]) {
      toRtn = (T) new String[length1 + length2];
    } else {
      assert values instanceof JsonValue[];
      toRtn = (T) new JsonValue[length1 + length2];
    }
    System.arraycopy(values, startIndex1, toRtn, 0, length1);
    if (length2 > 0) {
      System.arraycopy(values, startIndex2, toRtn, length1, length2);
    }
    return toRtn;
  }

  protected final int startIndex;
  protected final T values;

  protected final int length;

  protected AbstractListOperation(String id, int startIndex, T values, int length) {
    super(id);
    assert startIndex >= 0 && length > 0;
    this.startIndex = startIndex;
    this.values = values;
    this.length = length;
  }

  /**
   * @param index
   * @param rigthSide Which 'side' of the gap the cursor is in.
   * @param canBeDeleted
   * @return
   */
  public abstract int transformIndexReference(int index, boolean rigthSide, boolean canBeDeleted);

  @Override
  protected void toString(StringBuilder sb) {
    sb.append(startIndex).append(',');
    if (this instanceof DeleteOperation) {
      sb.append(length);
    } else {
      sb.append('[');
      if (values instanceof String) {
        sb.append(1).append(",\'").append(values).append('\'');
      } else {
        sb.append(0);
        if (values instanceof String[]) {
          for (String value : (String[]) values) {
            sb.append(',').append(value);
          }
        } else {
          assert values instanceof JsonValue[];
          for (JsonValue value : (JsonValue[]) values) {
            sb.append(',').append(value.toJson());
          }
        }
      }
      sb.append(']');
    }
  }
}
