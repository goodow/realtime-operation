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
package com.goodow.realtime.operation;

import com.goodow.realtime.operation.create.CreateOperation;
import com.goodow.realtime.operation.cursor.ReferenceShiftedOperation;
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
import java.util.LinkedList;
import java.util.List;

import elemental.json.JsonArray;
import elemental.json.JsonValue;

public class TransformerImpl<T extends Operation<?>> implements Transformer<T> {

  @SuppressWarnings("unchecked")
  @Override
  public T createOperation(String userId, String sessionId, JsonValue serialized) {
    JsonArray ops = (JsonArray) serialized;
    int length = ops.length();
    assert length > 0;
    List<AbstractOperation<?>> operations = new ArrayList<AbstractOperation<?>>(length);
    for (int i = 0; i < length; i++) {
      operations.add(createOperation(ops.getArray(i)));
    }
    return (T) new RealtimeOperation(userId, sessionId, operations);
  }

  @Override
  public Pair<List<T>, List<T>> transform(List<T> clientOps, List<T> serverOps) {
    List<T> transformedClientOps = new ArrayList<T>();
    List<T> transformedServerOps = new LinkedList<T>(serverOps);
    for (T clientOp : clientOps) {
      transform(transformedClientOps, clientOp, transformedServerOps, 0, true);
    }
    return Pair.of(transformedClientOps, transformedServerOps);
  }

  public void transform(List<T> results, T operation, List<T> operations, int startIndex,
      boolean arrivedAfter) {
    assert operation != null;
    if (startIndex == operations.size()) {
      results.add(operation);
      return;
    }
    @SuppressWarnings("rawtypes")
    Operation op1 = operations.get(startIndex);
    assert op1 != null;
    if (operation instanceof AbstractOperation
        && !((AbstractOperation<?>) operation).isSameId((AbstractOperation<?>) op1)) {
      transform(results, operation, operations, ++startIndex, arrivedAfter);
      return;
    }
    @SuppressWarnings("unchecked")
    Pair<T[], T[]> pair =
        arrivedAfter ? operation.transformWith(op1) : op1.transformWith(operation);
    T[] transformedOps1 = arrivedAfter ? pair.second : pair.first;
    operations.remove(startIndex);
    if (transformedOps1 != null) {
      for (T op : transformedOps1) {
        operations.add(startIndex++, op);
      }
    }
    T[] transformedOps0 = arrivedAfter ? pair.first : pair.second;
    if (transformedOps0 == null) {
      return;
    } else {
      for (T op : transformedOps0) {
        transform(results, op, operations, startIndex, arrivedAfter);
      }
    }
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
