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
package com.goodow.realtime.operation.list;

import com.goodow.realtime.operation.list.algorithm.ListOp;

import junit.framework.TestCase;

import elemental.json.Json;
import elemental.json.JsonArray;

public class StringOpTest extends TestCase {
  public void testParseFromJson() {
    ListOp<String> op = new StringOp().retain(1).insert("abc").retain(2).delete("efg");
    assertEquals(op, new StringOp((JsonArray) Json.instance().parse(op.toString())));
  }
}
