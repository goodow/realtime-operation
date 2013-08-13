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

public class InsertOperationTest extends TestCase {
  /**
   * Performs tests for transforming text insertions against text deletions.
   */
  public void testInsertVsDelete() {
    // A's insertion spatially before B's deletion
    InsertOperation<String> op1 = new InsertOperation<String>(null, 2, "abc");
    DeleteOperation<String> op2 = new DeleteOperation<String>(null, 3, "def");
    InsertOperation<String> transformedOp = op1.transformWith(op2, true)[0];
    assertSame(op1, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    assertSame(op1, transformedOp);

    // A's insertion spatially at the start of B's deletion
    op2 = new DeleteOperation<String>(null, 2, "def");
    transformedOp = op1.transformWith(op2, true)[0];
    assertSame(op1, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    assertSame(op1, transformedOp);

    // A's insertion spatially inside B's deletion
    op2 = new DeleteOperation<String>(null, 1, "def");
    transformedOp = op1.transformWith(op2, true)[0];
    InsertOperation<String> expected = new InsertOperation<String>(null, 1, "abc");
    assertEquals(expected, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    assertEquals(expected, transformedOp);

    // A's insertion spatially at the end of B's deletion
    op2 = new DeleteOperation<String>(null, 0, "de");
    transformedOp = op1.transformWith(op2, true)[0];
    expected = new InsertOperation<String>(null, 0, "abc");
    assertEquals(expected, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    assertEquals(expected, transformedOp);

    // A's insertion spatially after B's deletion
    op2 = new DeleteOperation<String>(null, 0, "d");
    transformedOp = op1.transformWith(op2, true)[0];
    expected = new InsertOperation<String>(null, 1, "abc");
    assertEquals(expected, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    assertEquals(expected, transformedOp);
  }

  /**
   * Performs tests for transforming text insertions against text insertions.
   */
  public void testInsertVsInsert() {
    // op1's insertion spatially before op2's insertion
    InsertOperation<String> op1 = new InsertOperation<String>(null, 1, "abc");
    InsertOperation<String> op2 = new InsertOperation<String>(null, 2, "def");
    InsertOperation<String> transformedOp = op1.transformWith(op2, true)[0];
    assertSame(op1, transformedOp);
    transformedOp = op1.transformWith(op2, false)[0];
    assertSame(op1, transformedOp);

    InsertOperation<String> expected = new InsertOperation<String>(null, 2 + 3, "def");
    transformedOp = op2.transformWith(op1, true)[0];
    assertEquals(expected, transformedOp);
    transformedOp = op2.transformWith(op1, false)[0];
    assertEquals(expected, transformedOp);

    // op1's insertion spatially at the same location as op2's insertion
    op2 = new InsertOperation<String>(null, 1, "def");
    transformedOp = op1.transformWith(op2, true)[0];
    expected = new InsertOperation<String>(null, 1 + 3, "abc");
    assertEquals(expected, transformedOp);

    transformedOp = op1.transformWith(op2, false)[0];
    assertSame(op1, transformedOp);
  }

  public void testParseFromJson() {
    InsertOperation<String> op = new InsertOperation<String>(null, 2, "123");
    assertEquals(op, InsertOperation.parse((JsonArray) Json.instance().parse(op.toString())));
    op = new InsertOperation<String>("id", 0, "true");
    assertEquals(op, InsertOperation.parse((JsonArray) Json.instance().parse(op.toString())));
  }

  public void testTransformIndexReference() {
    InsertOperation<String> op = new InsertOperation<String>(null, 1, "abc");
    // cursor before InsertOperation's startIndex
    int transformIndex = op.transformIndexReference(0, true, true);
    assertEquals(0, transformIndex);
    transformIndex = op.transformIndexReference(0, false, true);
    assertEquals(0, transformIndex);

    // cursor at the same location as InsertOperation's startIndex
    transformIndex = op.transformIndexReference(1, true, true);
    assertEquals(1 + 3, transformIndex);
    transformIndex = op.transformIndexReference(1, false, true);
    assertEquals(1, transformIndex);

    // cursor after InsertOperation's startIndex
    transformIndex = op.transformIndexReference(2, true, true);
    assertEquals(2 + 3, transformIndex);
    transformIndex = op.transformIndexReference(2, false, true);
    assertEquals(2 + 3, transformIndex);
  }

}
