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
package com.goodow.realtime.operation.cursor;

import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.impl.AbstractComponent;
import com.goodow.realtime.operation.list.AbstractListComponent;

public class ReferenceShiftedComponent extends AbstractComponent<Void> {
  public static final int TYPE = 25;

  public static ReferenceShiftedComponent parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 6;
    return new ReferenceShiftedComponent(parseId(serialized), serialized.getString(2),
        (int) serialized.getNumber(3), serialized.getBoolean(4), (int) serialized.getNumber(5));
  }

  public final String referencedObjectId;
  public final int newIndex;
  public final boolean canBeDeleted;

  public final int oldIndex;

  public ReferenceShiftedComponent(String id, String referencedObjectId, int newIndex,
      boolean canBeDeleted, int oldIndex) {
    super(TYPE, id);
    this.referencedObjectId = referencedObjectId;
    this.newIndex = newIndex;
    this.canBeDeleted = canBeDeleted;
    this.oldIndex = oldIndex;
  }

  @Override
  public void apply(Void target) {
    throw new IllegalStateException();
  }

  @Override
  public ReferenceShiftedComponent invert() {
    return new ReferenceShiftedComponent(id, referencedObjectId, oldIndex, canBeDeleted, newIndex);
  }

  @Override
  public ReferenceShiftedComponent transform(Operation<Void> other, boolean applied) {
    if (other instanceof ReferenceShiftedComponent) {
      assert isSameId(other);
      ReferenceShiftedComponent op = (ReferenceShiftedComponent) other;
      assert referencedObjectId.equals(op.referencedObjectId);
      return applied ? null : new ReferenceShiftedComponent(id, referencedObjectId, newIndex,
          canBeDeleted, op.newIndex);
    } else {
      assert other instanceof AbstractListComponent &&
             referencedObjectId.equals(((AbstractListComponent) other).id);
      AbstractListComponent op = (AbstractListComponent) other;
      return new ReferenceShiftedComponent(id, referencedObjectId,
          op.transformIndexReference(newIndex, true, canBeDeleted), canBeDeleted,
          op.transformIndexReference(oldIndex, true, canBeDeleted));
    }
  }

  @Override
  protected void toJson(JsonArray json) {
    json.push(referencedObjectId).push(newIndex).push(canBeDeleted).push(oldIndex);
  }
}