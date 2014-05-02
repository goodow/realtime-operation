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
import com.goodow.realtime.operation.Operation;
import com.goodow.realtime.operation.OperationComponent;

public abstract class AbstractComponent<T> extends OperationComponent<T> {
  protected static String parseId(JsonArray serialized) {
    return serialized.getString(1);
  }

  public final int type;
  public final String id;

  protected AbstractComponent(int type, String id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public boolean equals(Object obj) {
    return toString().equals(obj.toString());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + toString().hashCode();
    return result;
  }

  @Override
  public abstract AbstractComponent<T> invert();

  @Override
  public JsonArray toJson() {
    JsonArray json = Json.createArray().push(type).push(id);
    toJson(json);
    return json;
  }

  @Override
  public String toString() {
    return toJson().toJsonString();
  }

  @SafeVarargs
  protected final <O> O[] asArray(O... operations) {
    return operations;
  }

  protected boolean isSameId(Operation<?> operation) {
    String id2 = ((AbstractComponent<?>) operation).id;
    return id == null ? id2 == null : id.equals(id2);
  }

  protected abstract void toJson(JsonArray json);
}