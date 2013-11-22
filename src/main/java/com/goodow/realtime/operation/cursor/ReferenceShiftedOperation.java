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
import com.goodow.realtime.operation.AbstractOperation;

public class ReferenceShiftedOperation extends AbstractOperation<Void> {
  public static final int TYPE = 25;

  public static ReferenceShiftedOperation parse(JsonArray serialized) {
    assert serialized.getNumber(0) == TYPE && serialized.length() == 6;
    return new ReferenceShiftedOperation(parseId(serialized), serialized.getString(2),
        (int) serialized.getNumber(3), serialized.getBoolean(4), (int) serialized.getNumber(5));
  }

  public final String referencedObjectId;
  public final int newIndex;
  public final boolean canBeDeleted;

  public final int oldIndex;

  public ReferenceShiftedOperation(String id, String referencedObjectId, int newIndex,
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
  public ReferenceShiftedOperation invert() {
    return new ReferenceShiftedOperation(id, referencedObjectId, oldIndex, canBeDeleted, newIndex);
  }

  @Override
  public ReferenceShiftedOperation[] transformWith(AbstractOperation<Void> operation,
      boolean arrivedAfter) {
    assert operation instanceof ReferenceShiftedOperation && isSameId(operation);
    ReferenceShiftedOperation op = (ReferenceShiftedOperation) operation;
    assert referencedObjectId.equals(op.referencedObjectId);
    return arrivedAfter ? asArray(new ReferenceShiftedOperation(id, referencedObjectId, newIndex,
        canBeDeleted, op.newIndex)) : null;
  }

  @Override
  protected void toJson(JsonArray json) {
    json.push(referencedObjectId);
    json.push(newIndex);
    json.push(canBeDeleted);
    json.push(oldIndex);
  }
}