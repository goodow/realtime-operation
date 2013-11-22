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

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.AbstractOperation;

public abstract class AbstractListOperation<T> extends AbstractOperation<ListTarget<T>> {
  protected static int parseStartIndex(JsonArray serialized) {
    return (int) serialized.getNumber(2);
  }

  protected final int startIndex;
  protected final T values;
  protected final int length;

  protected AbstractListOperation(int type, String id, int startIndex, T values, int length) {
    super(type, id);
    assert startIndex >= 0;
    this.startIndex = startIndex;
    this.values = values;
    assert values != null || length > 0;
    this.length = length < 0 ? getHelper().length(values) : length;
    assert values == null || getHelper().length(values) == this.length;
    assert this.length > 0;
  }

  /**
   * @param index
   * @param rigthSide Which 'side' of the gap the cursor is in.
   * @param canBeDeleted
   * @return
   */
  public abstract int transformIndexReference(int index, boolean rigthSide, boolean canBeDeleted);

  protected abstract ListHelper<T> getHelper();

  @Override
  protected void toJson(JsonArray json) {
    json.push(startIndex);
    if (this instanceof AbstractDeleteOperation) {
      json.push(length);
    } else {
      json.push(getHelper().toJson(values));
    }
  }
}