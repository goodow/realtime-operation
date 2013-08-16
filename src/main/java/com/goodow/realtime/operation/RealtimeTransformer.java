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

import com.goodow.realtime.operation.list.AbstractDeleteOperation;
import com.goodow.realtime.operation.list.AbstractInsertOperation;
import com.goodow.realtime.operation.list.AbstractReplaceOperation;
import com.goodow.realtime.operation.list.json.JsonDeleteOperation;
import com.goodow.realtime.operation.list.json.JsonHelper;
import com.goodow.realtime.operation.list.json.JsonInsertOperation;
import com.goodow.realtime.operation.list.json.JsonReplaceOperation;
import com.goodow.realtime.operation.list.string.StringDeleteOperation;
import com.goodow.realtime.operation.list.string.StringHelper;
import com.goodow.realtime.operation.list.string.StringInsertOperation;
import com.goodow.realtime.operation.map.AbstractMapOperation;
import com.goodow.realtime.operation.map.json.JsonMapOperation;
import com.goodow.realtime.operation.util.Pair;

import java.util.ArrayList;
import java.util.List;

import elemental.json.JsonArray;
import elemental.json.JsonValue;

public class RealtimeTransformer implements Transformer<AbstractOperation<?>> {

  @Override
  public AbstractOperation<?> createOperation(JsonValue serialized, String userId, String sessionId) {
    AbstractOperation<?> op = createOperation((JsonArray) serialized);
    op.setUserAndSessionId(userId, sessionId);
    return op;
  }

  public AbstractOperation<?> invert(AbstractOperation<?> operation) {
    AbstractOperation<?> invertOp = operation.invert();
    invertOp.setUserAndSessionId(operation.getUserId(), operation.getSessionId());
    return invertOp;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public void transform(List<AbstractOperation<?>> results, AbstractOperation<?> op1,
      List<AbstractOperation<?>> ops2, int startIndex, boolean arrivedAfter) {
    assert op1 != null;
    if (startIndex == ops2.size()) {
      results.add(op1);
      return;
    }
    AbstractOperation op2 = ops2.get(startIndex);
    assert op2 != null;
    if (!op1.isSameId(op2)) {
      transform(results, op1, ops2, startIndex++, arrivedAfter);
      return;
    }
    AbstractOperation<?>[] transformedOps2 = op2.transformWith(op1, !arrivedAfter);
    ops2.remove(startIndex);
    if (transformedOps2 != null) {
      for (AbstractOperation op : transformedOps2) {
        op.setUserAndSessionId(op2.getUserId(), op2.getSessionId());
        ops2.add(startIndex++, op);
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

  @Override
  public Pair<List<AbstractOperation<?>>, List<AbstractOperation<?>>> transform(
      List<AbstractOperation<?>> serverOps, List<AbstractOperation<?>> clientOps) {
    List<AbstractOperation<?>> transformedClientOps = new ArrayList<AbstractOperation<?>>();
    for (AbstractOperation<?> clientOp : clientOps) {
      transform(transformedClientOps, clientOp, serverOps, 0, true);
    }
    return Pair.of(serverOps, transformedClientOps);
  }

  private AbstractOperation<?> createOperation(JsonArray serialized) {
    AbstractOperation<?> op = null;
    switch ((int) serialized.getNumber(0)) {
      case CreateOperation.TYPE:
        op = CreateOperation.parse(serialized);
        break;
      case AbstractMapOperation.TYPE:
        op = JsonMapOperation.parse(serialized);
        break;
      case AbstractInsertOperation.TYPE:
        switch ((int) serialized.getArray(3).getNumber(0)) {
          case JsonHelper.TYPE:
            op = JsonInsertOperation.parse(serialized);
            break;
          case StringHelper.TYPE:
            op = StringInsertOperation.parse(serialized);
            break;
          default:
            throw new UnsupportedOperationException("Unknow insert operation sub-type: "
                + serialized.toJson());
        }
      case AbstractDeleteOperation.TYPE:
        switch ((int) serialized.getArray(3).getNumber(0)) {
          case JsonHelper.TYPE:
            op = JsonDeleteOperation.parse(serialized);
            break;
          case StringHelper.TYPE:
            op = StringDeleteOperation.parse(serialized);
            break;
          default:
            throw new UnsupportedOperationException("Unknow delete operation sub-type: "
                + serialized.toJson());
        }
      case AbstractReplaceOperation.TYPE:
        op = JsonReplaceOperation.parse(serialized);
        break;
      case ReferenceShiftedOperation.TYPE:
        op = ReferenceShiftedOperation.parse(serialized);
        break;
      default:
        throw new UnsupportedOperationException("Unknow operation type: " + serialized.toJson());
    }
    return op;
  }
}
