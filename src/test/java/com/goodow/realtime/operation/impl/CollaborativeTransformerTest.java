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
import com.goodow.realtime.operation.impl.AbstractComponent;
import com.goodow.realtime.operation.impl.CollaborativeTransformer;
import com.goodow.realtime.operation.list.AbstractDeleteComponent;
import com.goodow.realtime.operation.list.AbstractInsertComponent;
import com.goodow.realtime.operation.list.AbstractReplaceComponent;
import com.goodow.realtime.operation.list.string.StringDeleteComponent;
import com.goodow.realtime.operation.list.string.StringInsertComponent;
import com.goodow.realtime.operation.list.string.StringReplaceComponent;
import com.goodow.realtime.operation.util.Pair;

import junit.framework.TestCase;

public class CollaborativeTransformerTest extends TestCase {
  CollaborativeTransformer transformer = new CollaborativeTransformer();

  public void testTransformClientOpToMultipleOps() {
    AbstractReplaceComponent<String> clientOp0 = new StringReplaceComponent(null, 2, "abc", "234");
    AbstractDeleteComponent<String> clientOp1 = new StringDeleteComponent(null, 2, "234");
    AbstractInsertComponent<String> serverOp0 = new StringInsertComponent(null, 3, "ab");
    AbstractReplaceComponent<String> serverOp1 = new StringReplaceComponent(null, 3, "abc", "def");
    JsonArray clientOps = Json.createArray().push(clientOp0).push(clientOp1);
    JsonArray serverOps = Json.createArray().push(serverOp0).push(serverOp1);
    Pair<JsonArray, JsonArray> pair = transformer.transform(clientOps, serverOps);
    AbstractComponent<?> expected;

    assertEquals(4, pair.first.length());
    expected = new StringReplaceComponent(null, 2, "a", "2");
    equals(expected, pair.first.<AbstractComponent<?>> get(0));
    expected = new StringReplaceComponent(null, 5, "fc", "34");
    equals(expected, pair.first.<AbstractComponent<?>> get(1));
    expected = new StringDeleteComponent(null, 2, "2");
    equals(expected, pair.first.<AbstractComponent<?>> get(2));
    expected = new StringDeleteComponent(null, 4, "34");
    equals(expected, pair.first.<AbstractComponent<?>> get(3));

    assertEquals(2, pair.second.length());
    expected = new StringInsertComponent(null, 2, "ab");
    equals(expected, pair.second.<AbstractComponent<?>> get(0));
    expected = new StringReplaceComponent(null, 2, "ab", "de");
    equals(expected, pair.second.<AbstractComponent<?>> get(1));
  }

  public void testTransformDeleteWithDelete() {
    //          0123456
    // clientOp  -  --
    // serverOp    --
    AbstractDeleteComponent<String> clientOp0 = new StringDeleteComponent(null, 1, "1");
    AbstractDeleteComponent<String> clientOp1 = new StringDeleteComponent(null, 3, "45");
    AbstractDeleteComponent<String> serverOp = new StringDeleteComponent(null, 3, "34");
    JsonArray serverOps = Json.createArray().push(serverOp);
    JsonArray clientOps = Json.createArray().push(clientOp0).push(clientOp1);
    Pair<JsonArray, JsonArray> pair = transformer.transform(clientOps, serverOps);
    AbstractComponent<?> expected;

    assertEquals(2, pair.first.length());
    equals(clientOp0, pair.first.<AbstractComponent<?>>get(0));
    expected = new StringDeleteComponent(null, 2, "5");
    equals(expected, pair.first.<AbstractComponent<?>> get(1));

    assertEquals(1, pair.second.length());
    expected = new StringDeleteComponent(null, 2, "3");
    equals(expected, pair.second.<AbstractComponent<?>> get(0));
  }

  public void testTransformServerOpToMultipleOps() {
    AbstractInsertComponent<String> clientOp0 = new StringInsertComponent(null, 3, "ab");
    AbstractReplaceComponent<String> clientOp1 = new StringReplaceComponent(null, 3, "abc", "def");
    AbstractReplaceComponent<String> serverOp0 = new StringReplaceComponent(null, 2, "abc", "234");
    AbstractDeleteComponent<String> serverOp1 = new StringDeleteComponent(null, 2, "234");
    JsonArray serverOps = Json.createArray().push(serverOp0).push(serverOp1);
    JsonArray clientOps = Json.createArray().push(clientOp0).push(clientOp1);
    Pair<JsonArray, JsonArray> pair = transformer.transform(clientOps, serverOps);
    AbstractComponent<?> expected;

    assertEquals(2, pair.first.length());
    expected = new StringInsertComponent(null, 2, "ab");
    equals(expected, pair.first.<AbstractComponent<?>> get(0));
    expected = new StringReplaceComponent(null, 2, "ab", "de");
    equals(expected, pair.first.<AbstractComponent<?>> get(1));

    assertEquals(4, pair.second.length());
    expected = new StringReplaceComponent(null, 2, "a", "2");
    equals(expected, pair.second.<AbstractComponent<?>> get(0));
    expected = new StringReplaceComponent(null, 6, "c", "4");
    equals(expected, pair.second.<AbstractComponent<?>> get(1));
    expected = new StringDeleteComponent(null, 2, "2");
    equals(expected, pair.second.<AbstractComponent<?>> get(2));
    expected = new StringDeleteComponent(null, 4, "f4");
    equals(expected, pair.second.<AbstractComponent<?>> get(3));
  }

  public void testTransformServerOpToNoOp() {

  }

  void equals(AbstractComponent<?> op0, AbstractComponent<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }
}