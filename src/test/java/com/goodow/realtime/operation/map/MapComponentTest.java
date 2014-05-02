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
package com.goodow.realtime.operation.map;

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonElement;
import com.goodow.realtime.json.JsonObject;
import com.goodow.realtime.operation.impl.AbstractComponent;
import com.goodow.realtime.operation.map.json.JsonMapComponent;

import junit.framework.TestCase;

public class MapComponentTest extends TestCase {
  private AbstractMapComponent<JsonElement> expected;

  public void testParseFromJson() {
    AbstractMapComponent<JsonElement> op =
        new JsonMapComponent(null, "key", null, Json.<JsonObject> parse("{\"k\":\"abc\"}"));
    assertEquals(op, JsonMapComponent.parse(Json.<JsonArray> parse(op.toString())));

    JsonObject obj = Json.createObject();
    obj.set("k", true);

    op = new JsonMapComponent("id", "key", Json.createArray(), obj);
    assertEquals(op, JsonMapComponent.parse(Json.<JsonArray> parse(op.toString())));

    op = new JsonMapComponent("id", "key", obj, Json.createArray());
    assertEquals(op, JsonMapComponent.parse(Json.<JsonArray> parse(op.toString())));
  }

  public void testTransformDifferentKey() {
    AbstractMapComponent<JsonElement> op1 =
        new JsonMapComponent(null, "key1", null, Json.<JsonObject> parse("{\"k\":\"abc\"}"));
    AbstractMapComponent<JsonElement> op2 =
        new JsonMapComponent(null, "key2", Json.<JsonObject> parse("{\"k\":3}"), Json
            .<JsonObject> parse("{\"k\":true}"));

    AbstractMapComponent<JsonElement> transformedOp = op1.transform(op2, true);
    assertSame(op1, transformedOp);
    transformedOp = op1.transform(op2, false);
    assertSame(op1, transformedOp);

    transformedOp = op2.transform(op1, true);
    assertSame(op2, transformedOp);
    transformedOp = op2.transform(op1, false);
    assertSame(op2, transformedOp);
  }

  public void testTransformNoOp() {
    AbstractMapComponent<JsonElement> op1 =
        new JsonMapComponent(null, "key", null, Json.<JsonObject> parse("{\"k\":\"abc\"}"));
    AbstractMapComponent<JsonElement> op2 =
        new JsonMapComponent(null, "key", Json.<JsonObject> parse("{\"k\":3}"), Json
            .<JsonObject> parse("{\"k\":\"abc\"}"));

    AbstractMapComponent<JsonElement> transformedOp = op1.transform(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transform(op2, false);
    assertNull(transformedOp);
  }

  public void testTransformSameKey() {
    AbstractMapComponent<JsonElement> op1 =
        new JsonMapComponent(null, "key", null, Json.<JsonObject> parse("{\"k\":\"abc\"}"));
    AbstractMapComponent<JsonElement> op2 =
        new JsonMapComponent(null, "key", Json.<JsonObject> parse("{\"k\":3}"), Json
            .<JsonObject> parse("{\"k\":true}"));

    AbstractMapComponent<JsonElement> transformedOp = op1.transform(op2, false);
    expected =
        new JsonMapComponent(null, "key", Json.<JsonObject> parse("{\"k\":true}"), Json
            .<JsonObject> parse("{\"k\":\"abc\"}"));
    equals(expected, transformedOp);

    transformedOp = op1.transform(op2, true);
    assertNull(transformedOp);

    transformedOp = op2.transform(op1, false);
    expected =
        new JsonMapComponent(null, "key", Json.<JsonObject> parse("{\"k\":\"abc\"}"), Json
            .<JsonObject> parse("{\"k\":true}"));
    equals(expected, transformedOp);

    transformedOp = op2.transform(op1, true);
    assertNull(transformedOp);
  }

  void equals(AbstractComponent<?> op0, AbstractComponent<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }
}
