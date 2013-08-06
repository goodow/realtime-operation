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

import com.goodow.realtime.operation.basic.NoOp;
import com.goodow.realtime.operation.list.ArrayOp;
import com.goodow.realtime.operation.list.StringOp;
import com.goodow.realtime.operation.list.algorithm.ListOp;
import com.goodow.realtime.operation.list.algorithm.ListOpCollector;
import com.goodow.realtime.operation.map.MapOp;
import com.goodow.realtime.operation.util.Pair;

import elemental.json.JsonArray;
import elemental.json.JsonValue;
import elemental.util.ArrayOf;
import elemental.util.Collections;

public class RealtimeTransformer implements Transformer<RealtimeOperation<?>> {
  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public ArrayOf<RealtimeOperation<?>> compose(ArrayOf<RealtimeOperation<?>> ops) {
    if (ops.isEmpty()) {
      return ops;
    }
    ArrayOf<RealtimeOperation<?>> toRtn = Collections.arrayOf();
    RealtimeOperation<?> current = null;
    ListOpCollector<?> collector = null;
    for (int i = 0, len = ops.length(); i < len; i++) {
      RealtimeOperation op = ops.get(i);
      assert !op.isNoOp();
      if (current == null || !op.getId().equals(current.getId())
          || !op.sessionId.equals(current.sessionId) || !op.userId.equals(current.userId)
          || op.getType() != current.getType()) {
        if (collector != null) {
          composeListOps(toRtn, collector);
          collector = null;
        }
        toRtn.push(op);
      } else {
        assert toRtn.peek().getType() == op.getType();
        if (op.getOp() instanceof ListOp) {
          if (collector == null) {
            collector = ((ListOp) op.getOp()).createOpCollector();
            collector.add((ListOp) toRtn.peek().getOp());
          }
          collector.add((ListOp) op.getOp());
        } else {
          RealtimeOperation<?> composition = toRtn.pop().composeWith(op);
          if (!composition.isNoOp()) {
            toRtn.push(composition);
          }
        }
      }
      current = op;
    }
    if (collector != null) {
      composeListOps(toRtn, collector);
      collector = null;
    }
    return toRtn;
  }

  public Operation<?> createOp(JsonArray serialized) {
    Operation<?> op = null;
    String id = serialized.getString(1);
    switch ((int) serialized.getNumber(0)) {
      case CreateOperation.TYPE:
        op = new CreateOperation(serialized);
        break;
      case MapOp.TYPE:
        op = new MapOp(serialized.getArray(2));
        break;
      case StringOp.TYPE:
        op = new StringOp(serialized.getArray(2));
        break;
      case ArrayOp.TYPE:
        op = new ArrayOp(serialized.getArray(2));
        break;
      case ReferenceShiftedOperation.TYPE:
        op = new ReferenceShiftedOperation(serialized.getArray(2));
        break;
      case NoOp.TYPE:
        op = NoOp.get();
        break;
      default:
        throw new UnsupportedOperationException("Unknow operation type: " + serialized.toJson());
    }
    op.setId(id);
    return op;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public RealtimeOperation<?> createOperation(JsonValue serialized, String userId, String sessionId) {
    Operation<?> op = createOp((JsonArray) serialized);
    return new RealtimeOperation(op, userId, sessionId);
  }

  @Override
  public Pair<ArrayOf<RealtimeOperation<?>>, ArrayOf<RealtimeOperation<?>>> transform(
      ArrayOf<RealtimeOperation<?>> serverOps, ArrayOf<RealtimeOperation<?>> clientOps) {
    ArrayOf<RealtimeOperation<?>> sOps =
        Collections.<RealtimeOperation<?>> arrayOf().concat(serverOps);
    ArrayOf<RealtimeOperation<?>> cOps =
        Collections.<RealtimeOperation<?>> arrayOf().concat(clientOps);
    sLoop : for (int i = 0; i < sOps.length(); i++) {
      RealtimeOperation<?> serverOp = sOps.get(i);
      assert !serverOp.isNoOp();
      for (int j = 0; j < cOps.length(); j++) {
        RealtimeOperation<?> clientOp = cOps.get(j);
        assert !clientOp.isNoOp();
        Pair<? extends RealtimeOperation<?>, ? extends RealtimeOperation<?>> pair =
            serverOp.transformWith(clientOp);
        serverOp = pair.first;
        clientOp = pair.second;
        if (clientOp.isNoOp()) {
          cOps.removeByIndex(j--);
        } else {
          cOps.set(j, clientOp);
        }
        if (serverOp.isNoOp()) {
          sOps.removeByIndex(i--);
          continue sLoop;
        }
      }
      sOps.set(i, serverOp);
    }
    return Pair.of(sOps, cOps);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void composeListOps(ArrayOf<RealtimeOperation<?>> ops, ListOpCollector<?> collector) {
    ListOp<?> composition = collector.composeAll();
    if (!composition.isNoOp()) {
      RealtimeOperation<?> peek = ops.peek();
      composition.setId(peek.getId());
      ops.set(ops.length() - 1, new RealtimeOperation(composition, peek.userId, peek.sessionId));
    } else {
      ops.removeByIndex(ops.length() - 1);
    }
  }
}
