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

import com.goodow.realtime.operation.AbstractOperation;
import com.goodow.realtime.operation.map.json.JsonMapOperation;

import junit.framework.TestCase;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

public class MapOperationTest extends TestCase {
  private AbstractMapOperation<JsonValue> expected;

  public void testParseFromJson() {
    AbstractMapOperation<JsonValue> op =
        new JsonMapOperation(null, "key", null, Json.create("abc"));
    assertEquals(op, JsonMapOperation.parse((JsonArray) Json.instance().parse(op.toString())));

    JsonObject obj = Json.createObject();
    obj.put("k", true);

    op = new JsonMapOperation("id", "key", Json.createArray(), obj);
    assertEquals(op, JsonMapOperation.parse((JsonArray) Json.instance().parse(op.toString())));

    op = new JsonMapOperation("id", "key", obj, Json.createArray());
    assertEquals(op, JsonMapOperation.parse((JsonArray) Json.instance().parse(op.toString())));
  }

  public void testTransformDifferentKey() {
    AbstractMapOperation<JsonValue> op1 =
        new JsonMapOperation(null, "key1", null, Json.create("abc"));
    AbstractMapOperation<JsonValue> op2 =
        new JsonMapOperation(null, "key2", Json.create(3), Json.create(true));

    AbstractMapOperation<JsonValue>[] transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertSame(op2, transformedOp[0]);
  }

  public void testTransformNoOp() {
    AbstractMapOperation<JsonValue> op1 =
        new JsonMapOperation(null, "key", null, Json.create("abc"));
    AbstractMapOperation<JsonValue> op2 =
        new JsonMapOperation(null, "key", Json.create(3), Json.create("abc"));

    AbstractMapOperation<JsonValue>[] transformedOp = op1.transformWith(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);
  }

  public void testTransformSameKey() {
    AbstractMapOperation<JsonValue> op1 =
        new JsonMapOperation(null, "key", null, Json.create("abc"));
    AbstractMapOperation<JsonValue> op2 =
        new JsonMapOperation(null, "key", Json.create(3), Json.create(true));

    AbstractMapOperation<JsonValue>[] transformedOp = op1.transformWith(op2, true);
    expected = new JsonMapOperation(null, "key", Json.create(true), Json.create("abc"));
    equals(expected, transformedOp[0]);

    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);

    transformedOp = op2.transformWith(op1, true);
    expected = new JsonMapOperation(null, "key", Json.create("abc"), Json.create(true));
    equals(expected, transformedOp[0]);

    transformedOp = op2.transformWith(op1, false);
    assertNull(transformedOp);
  }

  void equals(AbstractOperation<?> op0, AbstractOperation<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }
}
