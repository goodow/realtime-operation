/*
 * Copyright 2012 Goodow.com
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
package com.goodow.realtime.operation;

import com.goodow.realtime.operation.list.DeleteOperation;
import com.goodow.realtime.operation.list.InsertOperation;
import com.goodow.realtime.operation.list.ReplaceOperation;
import com.goodow.realtime.operation.map.MapOperation;
import com.goodow.realtime.operation.util.Pair;

import elemental.json.JsonArray;
import elemental.json.JsonValue;
import elemental.util.ArrayOf;
import elemental.util.Collections;

public class RealtimeTransformer implements Transformer<AbstractOperation<?>> {

  @Override
  public AbstractOperation<?> createOperation(JsonValue serialized, String userId, String sessionId) {
    AbstractOperation<?> op = createOperation((JsonArray) serialized);
    op.setUserAndSessionId(userId, sessionId);
    return op;
  }

  @Override
  public Pair<ArrayOf<AbstractOperation<?>>, ArrayOf<AbstractOperation<?>>> transform(
      ArrayOf<AbstractOperation<?>> serverOps, ArrayOf<AbstractOperation<?>> clientOps) {
    ArrayOf<AbstractOperation<?>> transformedClientOps = Collections.arrayOf();
    for (int i = 0, len = clientOps.length(); i < len; i++) {
      transform(transformedClientOps, clientOps.get(i), serverOps, 0, true);
    }
    return Pair.of(serverOps, transformedClientOps);
  }

  private AbstractOperation<?> createOperation(JsonArray serialized) {
    AbstractOperation<?> op = null;
    switch ((int) serialized.getNumber(0)) {
      case CreateOperation.TYPE:
        op = CreateOperation.parse(serialized);
        break;
      case MapOperation.TYPE:
        op = MapOperation.parse(serialized);
        break;
      case InsertOperation.TYPE:
        op = InsertOperation.parse(serialized);
        break;
      case DeleteOperation.TYPE:
        op = InsertOperation.parse(serialized);
        break;
      case ReplaceOperation.TYPE:
        op = InsertOperation.parse(serialized);
        break;
      case ReferenceShiftedOperation.TYPE:
        op = ReferenceShiftedOperation.parse(serialized);
        break;
      default:
        throw new UnsupportedOperationException("Unknow operation type: " + serialized.toJson());
    }
    return op;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void transform(ArrayOf<AbstractOperation<?>> results, AbstractOperation<?> op1,
      ArrayOf<AbstractOperation<?>> ops2, int startIndex, boolean arrivedAfter) {
    assert op1 != null;
    if (startIndex == ops2.length()) {
      results.push(op1);
      return;
    }
    AbstractOperation op2 = ops2.get(startIndex);
    assert op2 != null;
    if (!op1.isSameId(op2)) {
      transform(results, op1, ops2, startIndex++, arrivedAfter);
      return;
    }
    AbstractOperation<?>[] transformedOps2 = op2.transformWith(op1, !arrivedAfter);
    ops2.removeByIndex(startIndex);
    if (transformedOps2 != null) {
      for (AbstractOperation op : transformedOps2) {
        op.setUserAndSessionId(op2.getUserId(), op2.getSessionId());
        ops2.insert(startIndex++, op);
      }
    }
    AbstractOperation<?>[] transformedOps1 = op1.transformWith(op2, arrivedAfter);
    if (transformedOps1 == null) {
      return;
    } else {
      for (AbstractOperation op : transformedOps1) {
        op.setUserAndSessionId(op1.getUserId(), op1.getSessionId());
        transform(results, op, ops2, startIndex, arrivedAfter);
      }
    }
  }
}
