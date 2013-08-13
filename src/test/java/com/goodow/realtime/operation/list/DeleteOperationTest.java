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

import junit.framework.TestCase;

import elemental.json.Json;
import elemental.json.JsonArray;

public class DeleteOperationTest extends TestCase {
  /**
   * Performs tests for transforming text deletions against text deletions.
   */
  public void testDeleteVsDelete() {
    // A's deletion spatially before B's deletion
    DeleteOperation<String> op1 = new DeleteOperation<String>(null, 2, "234");
    DeleteOperation<String> op2 = new DeleteOperation<String>(null, 6, "6");
    DeleteOperation<String>[] transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    DeleteOperation<String> expected = new DeleteOperation<String>(null, 3, "6");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertEquals(expected, transformedOp[0]);

    // A's deletion spatially adjacent to and before B's deletion
    op2 = new DeleteOperation<String>(null, 5, "56");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    expected = new DeleteOperation<String>(null, 2, "56");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertEquals(expected, transformedOp[0]);

    // A's deletion overlaps B's deletion
    op2 = new DeleteOperation<String>(null, 3, "345");
    transformedOp = op1.transformWith(op2, true);
    expected = new DeleteOperation<String>(null, 2, "2");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    expected = new DeleteOperation<String>(null, 2, "5");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertEquals(expected, transformedOp[0]);

    // A's deletion a subset of B's deletion
    op2 = new DeleteOperation<String>(null, 1, "123456");
    transformedOp = op1.transformWith(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);

    transformedOp = op2.transformWith(op1, true);
    expected = new DeleteOperation<String>(null, 1, "156");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertEquals(expected, transformedOp[0]);

    // A's deletion identical to B's deletion
    op2 = new DeleteOperation<String>(null, 2, "234");
    transformedOp = op1.transformWith(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);
  }

  /**
   * Performs tests for transforming text insertions against text deletions.
   */
  public void testDeleteVsInsert() {
    // A's insertion spatially before B's deletion
    DeleteOperation<String> op1 = new DeleteOperation<String>(null, 3, "abc");
    InsertOperation<String> op2 = new InsertOperation<String>(null, 2, "de");
    DeleteOperation<String>[] transformedOp = op1.transformWith(op2, true);
    DeleteOperation<String> expected = new DeleteOperation<String>(null, 5, "abc");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    // A's insertion spatially at the start of B's deletion
    op2 = new InsertOperation<String>(null, 3, "de");
    transformedOp = op1.transformWith(op2, true);
    expected = new DeleteOperation<String>(null, 5, "abc");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    // A's insertion spatially inside B's deletion
    op2 = new InsertOperation<String>(null, 4, "de");
    transformedOp = op1.transformWith(op2, true);
    expected = new DeleteOperation<String>(null, 3, "a");
    DeleteOperation<String> expected2 = new DeleteOperation<String>(null, 5, "bc");
    assertEquals(expected, transformedOp[0]);
    assertEquals(expected2, transformedOp[1]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);
    assertEquals(expected2, transformedOp[1]);

    // A's insertion spatially at the end of B's deletion
    op2 = new InsertOperation<String>(null, 6, "de");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's insertion spatially after B's deletion
    op2 = new InsertOperation<String>(null, 7, "de");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);
  }

  public void testParseFromJson() {
    DeleteOperation<String> op = new DeleteOperation<String>(null, 2, "123");
    assertEquals(op, DeleteOperation.parse((JsonArray) Json.instance().parse(op.toString())));
    op = new DeleteOperation<String>("id", 0, "true");
    assertEquals(op, DeleteOperation.parse((JsonArray) Json.instance().parse(op.toString())));
  }

  public void testTransformIndexReference() {
    DeleteOperation<String> op = new DeleteOperation<String>(null, 1, "abc");
    int transformIndex = op.transformIndexReference(0, true, true);
    assertEquals(0, transformIndex);
    transformIndex = op.transformIndexReference(0, true, false);
    assertEquals(0, transformIndex);

    transformIndex = op.transformIndexReference(1, true, true);
    assertEquals(-1, transformIndex);
    transformIndex = op.transformIndexReference(1, true, false);
    assertEquals(1, transformIndex);

    transformIndex = op.transformIndexReference(3, true, true);
    assertEquals(-1, transformIndex);
    transformIndex = op.transformIndexReference(3, true, false);
    assertEquals(1, transformIndex);

    transformIndex = op.transformIndexReference(4, true, true);
    assertEquals(1, transformIndex);
    transformIndex = op.transformIndexReference(4, true, false);
    assertEquals(1, transformIndex);
  }
}
