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
package com.goodow.realtime.operation.impl;

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonArray.ListIterator;
import com.goodow.realtime.json.JsonObject;
import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.OperationComponent;
import com.goodow.realtime.operation.Transformer;
import com.goodow.realtime.operation.create.CreateComponent;
import com.goodow.realtime.operation.cursor.ReferenceShiftedComponent;
import com.goodow.realtime.operation.list.AbstractDeleteComponent;
import com.goodow.realtime.operation.list.AbstractInsertComponent;
import com.goodow.realtime.operation.list.AbstractReplaceComponent;
import com.goodow.realtime.operation.list.SimpleDeleteComponent;
import com.goodow.realtime.operation.list.json.JsonHelper;
import com.goodow.realtime.operation.list.json.JsonInsertComponent;
import com.goodow.realtime.operation.list.json.JsonReplaceComponent;
import com.goodow.realtime.operation.list.string.StringHelper;
import com.goodow.realtime.operation.list.string.StringInsertComponent;
import com.goodow.realtime.operation.map.AbstractMapComponent;
import com.goodow.realtime.operation.map.json.JsonMapComponent;
import com.goodow.realtime.operation.util.Pair;

public class CollaborativeTransformer implements Transformer<CollaborativeOperation> {

  @Override
  public CollaborativeOperation compose(JsonArray operations) {
    assert operations.length() > 0 && operations.indexOf(null) == -1;
    final CollaborativeOperation first = operations.<CollaborativeOperation> get(0);
    if (operations.length() == 1) {
      return first;
    }
    final JsonArray components = Json.createArray();
    operations.forEach(new ListIterator<CollaborativeOperation>() {
      @SuppressWarnings("rawtypes")
      @Override
      public void call(int index, CollaborativeOperation operation) {
        operation.components.forEach(new ListIterator<OperationComponent>() {
          @Override
          public void call(int index, OperationComponent component) {
            components.push(component);
          }
        });
      }
    });
    return new CollaborativeOperation(first.userId, first.sessionId, components);
  }

  public AbstractComponent<?> createComponent(JsonArray serialized) {
    AbstractComponent<?> component = null;
    switch ((int) serialized.getNumber(0)) {
      case CreateComponent.TYPE:
        component = CreateComponent.parse(serialized);
        break;
      case AbstractMapComponent.TYPE:
        component = JsonMapComponent.parse(serialized);
        break;
      case AbstractInsertComponent.TYPE:
        switch ((int) serialized.getArray(3).getNumber(0)) {
          case JsonHelper.TYPE:
            component = JsonInsertComponent.parse(serialized);
            break;
          case StringHelper.TYPE:
            component = StringInsertComponent.parse(serialized);
            break;
          default:
            throw new UnsupportedOperationException("Unknow insert operation sub-type: "
                + serialized.toJsonString());
        }
        break;
      case AbstractDeleteComponent.TYPE:
        component = SimpleDeleteComponent.parse(serialized);
        break;
      case AbstractReplaceComponent.TYPE:
        component = JsonReplaceComponent.parse(serialized);
        break;
      case ReferenceShiftedComponent.TYPE:
        component = ReferenceShiftedComponent.parse(serialized);
        break;
      default:
        throw new UnsupportedOperationException("Unknow operation type: "
            + serialized.toJsonString());
    }
    return component;
  }

  @Override
  public CollaborativeOperation createOperation(JsonObject opData) {
    assert opData.getArray(CollaborativeOperation.OP).length() > 0;
    final JsonArray components = Json.createArray();
    opData.getArray(CollaborativeOperation.OP).forEach(new ListIterator<JsonArray>() {
      @Override
      public void call(int index, JsonArray component) {
        components.push(createComponent(component));
      }
    });
    return new CollaborativeOperation(opData.getString(CollaborativeOperation.UID), opData
        .getString(CollaborativeOperation.SID), components);
  }

  @Override
  public Pair<CollaborativeOperation, CollaborativeOperation> transform(
      CollaborativeOperation operation, CollaborativeOperation appliedOperation) {
    Pair<JsonArray, JsonArray> pair = transform(operation.components, appliedOperation.components);
    CollaborativeOperation transformed =
        new CollaborativeOperation(operation.userId, operation.sessionId, pair.first);
    CollaborativeOperation transformedApplied =
        new CollaborativeOperation(appliedOperation.userId, appliedOperation.sessionId, pair.second);
    return Pair.of(transformed, transformedApplied);
  }

  protected Pair<JsonArray, JsonArray> transform(JsonArray operations, JsonArray appliedOperations) {
    assert operations.indexOf(null) == -1 && appliedOperations.indexOf(null) == -1;
    final JsonArray transformed = Json.createArray();
    final JsonArray transformedApplied = appliedOperations.copy();
    operations.forEach(new ListIterator<Operation<?>>() {
      @Override
      public void call(int index, Operation<?> operation) {
        transform(transformed, operation, transformedApplied, 0, false);
      }
    });
    return Pair.of(transformed, transformedApplied);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  protected void transform(JsonArray transformedResults, Operation<?> operation, JsonArray others,
      int startIndex, boolean applied) {
    assert operation != null;
    if (startIndex == others.length()) {
      transformedResults.push(operation);
      return;
    }
    Operation other = others.get(startIndex);
    assert other != null;
    if (operation instanceof AbstractComponent
        && !((AbstractComponent<?>) operation).isSameId(other)) {
      transform(transformedResults, operation, others, ++startIndex, applied);
      return;
    }
    try {
      Operation<?> transformed = other.transform(operation, !applied);
      others.remove(startIndex);
      if (transformed != null) {
        others.insert(startIndex++, transformed);
      }
    } catch (UnsupportedOperationException e) {
      if (!(other instanceof OperationComponent)) {
        throw e;
      }
      OperationComponent<?>[] transformed =
          ((OperationComponent) other).transformComponent((OperationComponent) operation, !applied);
      others.remove(startIndex);
      if (transformed != null) {
        for (OperationComponent<?> component : transformed) {
          others.insert(startIndex++, component);
        }
      }
    }

    try {
      Operation<?> transformed = operation.transform(other, applied);
      if (transformed == null) {
        return;
      } else {
        transform(transformedResults, transformed, others, startIndex, applied);
      }
    } catch (UnsupportedOperationException e) {
      if (!(operation instanceof OperationComponent)) {
        throw e;
      }
      OperationComponent<?>[] transformed =
          ((OperationComponent) operation).transformComponent((OperationComponent) other, applied);
      if (transformed == null) {
        return;
      } else {
        for (OperationComponent<?> component : transformed) {
          transform(transformedResults, component, others, startIndex, applied);
        }
      }
    }
  }
}
