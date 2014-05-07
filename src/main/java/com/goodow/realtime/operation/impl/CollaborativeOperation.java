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

public class CollaborativeOperation implements Operation<Object> {
  public static final String UID = "uid";
  public static final String SID = "sid";
  public static final String OP = "op";
  public final String userId;
  public final String sessionId;
  public final JsonArray components; // List<OperationComponent>

  public CollaborativeOperation(String userId, String sessionId, JsonArray components) {
    assert components != null && components.indexOf(null) == -1;
    this.userId = userId;
    this.sessionId = sessionId;
    this.components = components;
  }

  @Override
  public void apply(final Object target) {
    components.forEach(new ListIterator<OperationComponent<Object>>() {
      @Override
      public void call(int index, OperationComponent<Object> component) {
        component.apply(target);
      }
    });
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CollaborativeOperation)) {
      return false;
    }
    CollaborativeOperation other = (CollaborativeOperation) obj;
    if (components == null) {
      if (other.components != null) {
        return false;
      }
    } else if (!components.equals(other.components)) {
      return false;
    }
    if (sessionId == null) {
      if (other.sessionId != null) {
        return false;
      }
    } else if (!sessionId.equals(other.sessionId)) {
      return false;
    }
    if (userId == null) {
      if (other.userId != null) {
        return false;
      }
    } else if (!userId.equals(other.userId)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((components == null) ? 0 : components.hashCode());
    result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
    result = prime * result + ((userId == null) ? 0 : userId.hashCode());
    return result;
  }

  @Override
  public CollaborativeOperation invert() {
    int size = components.length();
    JsonArray invertedComponents = Json.createArray();
    for (int i = size - 1; i >= 0; i--) {
      Operation<?> inverted = components.<Operation<?>> get(i).invert();
      // assert invertOp != null;
      if (inverted != null) {
        invertedComponents.push(inverted);
      }
    }
    return new CollaborativeOperation(userId, sessionId, invertedComponents);
  }

  @Override
  public JsonObject toJson() {
    final JsonArray op = Json.createArray();
    components.forEach(new ListIterator<Operation<Object>>() {
      @Override
      public void call(int index, Operation<Object> component) {
        op.push(component.toJson());
      }
    });
    return Json.createObject().set(UID, userId).set(SID, sessionId).set(OP, op);
  }

  @Override
  public String toString() {
    return toJson().toJsonString();
  }

  @Override
  public CollaborativeOperation transform(Operation<Object> other, boolean applied) {
    assert other instanceof CollaborativeOperation;
    CollaborativeTransformer transformer = new CollaborativeTransformer();
    return applied ? transformer.transform((CollaborativeOperation) other, this).second
        : transformer.transform(this, (CollaborativeOperation) other).first;
  }
}