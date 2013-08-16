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
package com.goodow.realtime.operation.list;

import elemental.json.JsonArray;

public interface ListHelper<T> {
  int length(T values);

  T parse(JsonArray serialized);

  T replaceWith(T values, int startIndex, int length, T replacement);

  StringBuilder serialize(T values);

  T subset(T values, int startIndex, int length);

  T subset(T values, int startIndex0, int length0, int startIndex1, int length1);

  T subset(T values0, int startIndex0, int length0, T values1, int startIndex1, int length1);
}