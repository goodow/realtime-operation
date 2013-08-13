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

import junit.framework.TestCase;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

public class MapOperationTest extends TestCase {
  public void testParseFromJson() {
    MapOperation op = new MapOperation(null, "key", null, Json.create("abc"));
    assertEquals(op, MapOperation.parse((JsonArray) Json.instance().parse(op.toString())));

    JsonObject obj = Json.createObject();
    obj.put("k", true);

    op = new MapOperation("id", "key", Json.createNull(), obj);
    assertEquals(op, MapOperation.parse((JsonArray) Json.instance().parse(op.toString())));

    op = new MapOperation("id", "key", obj, Json.createNull());
    assertEquals(op, MapOperation.parse((JsonArray) Json.instance().parse(op.toString())));
  }

  public void testTransformDifferentKey() {
    MapOperation op1 = new MapOperation(null, "key1", null, Json.create("abc"));
    MapOperation op2 = new MapOperation(null, "key2", Json.create(3), Json.create(true));

    MapOperation[] transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertSame(op2, transformedOp[0]);
  }

  public void testTransformNoOp() {
    MapOperation op1 = new MapOperation(null, "key", null, Json.create("abc"));
    MapOperation op2 = new MapOperation(null, "key", Json.create(3), Json.create("abc"));

    MapOperation[] transformedOp = op1.transformWith(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);
  }

  public void testTransformSameKey() {
    MapOperation op1 = new MapOperation(null, "key", null, Json.create("abc"));
    MapOperation op2 = new MapOperation(null, "key", Json.create(3), Json.create(true));

    MapOperation[] transformedOp = op1.transformWith(op2, true);
    MapOperation expected = new MapOperation(null, "key", Json.create(true), Json.create("abc"));
    assertEquals(expected, transformedOp[0]);

    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);

    transformedOp = op2.transformWith(op1, true);
    expected = new MapOperation(null, "key", Json.create("abc"), Json.create(true));
    assertEquals(expected, transformedOp[0]);

    transformedOp = op2.transformWith(op1, false);
    assertNull(transformedOp);
  }
}
