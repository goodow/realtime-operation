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

import elemental.json.JsonArray;
import elemental.json.JsonType;

public abstract class AbstractOperation<T> implements Operation<T> {
  protected static String parseId(JsonArray serialized) {
    return serialized.get(1).getType() == JsonType.NULL ? null : serialized.getString(1);
  }

  protected final String id;
  private String userId;
  private String sessionId;

  protected AbstractOperation(String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object obj) {
    return toString().equals(obj.toString());
  }

  @Override
  public String getId() {
    return id;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getUserId() {
    return userId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + toString().hashCode();
    return result;
  }

  public void setUserAndSessionId(String userId, String sessionId) {
    this.userId = userId;
    this.sessionId = sessionId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder().append('[');
    sb.append(getType()).append(',');
    if (id == null) {
      sb.append((String) null);
    } else {
      sb.append('\'').append(id).append('\'');
    }
    sb.append(',');
    toString(sb);
    sb.append(']');
    return sb.toString();
  }

  @Override
  public abstract AbstractOperation<T>[] transformWith(Operation<T> operation, boolean arrivedAfter);

  protected boolean isSameId(Operation<?> operation) {
    String id2 = operation.getId();
    return id == null ? id2 == null : id.equals(id2);
  }

  protected abstract void toString(StringBuilder sb);
}