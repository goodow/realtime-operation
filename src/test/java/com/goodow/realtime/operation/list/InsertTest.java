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

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.operation.OperationComponent;
import com.goodow.realtime.operation.list.string.StringDeleteComponent;
import com.goodow.realtime.operation.list.string.StringInsertComponent;

import junit.framework.TestCase;

public class InsertTest extends TestCase {
  private AbstractInsertComponent<String> expected0;

  /**
   * Performs tests for transforming text insertions against text deletions.
   */
  public void testInsertVsDelete() {
    // A's insertion spatially before B's deletion
    AbstractInsertComponent<String> op1 = new StringInsertComponent(null, 2, "abc");
    AbstractDeleteComponent<String> op2 = new StringDeleteComponent(null, 3, "def");
    OperationComponent<ListTarget<String>> transformedOp = op1.transform(op2, true);
    assertSame(op1, transformedOp);
    transformedOp = op1.transform(op2, false);
    assertSame(op1, transformedOp);

    // A's insertion spatially at the start of B's deletion
    op2 = new StringDeleteComponent(null, 2, "def");
    transformedOp = op1.transform(op2, true);
    assertSame(op1, transformedOp);
    transformedOp = op1.transform(op2, false);
    assertSame(op1, transformedOp);

    // A's insertion spatially inside B's deletion
    op2 = new StringDeleteComponent(null, 1, "def");
    transformedOp = op1.transform(op2, true);
    expected0 = new StringInsertComponent(null, 1, "abc");
    equals(expected0, transformedOp);
    transformedOp = op1.transform(op2, false);
    equals(expected0, transformedOp);

    // A's insertion spatially at the end of B's deletion
    op2 = new StringDeleteComponent(null, 0, "de");
    transformedOp = op1.transform(op2, true);
    expected0 = new StringInsertComponent(null, 0, "abc");
    equals(expected0, transformedOp);
    transformedOp = op1.transform(op2, false);
    equals(expected0, transformedOp);

    // A's insertion spatially after B's deletion
    op2 = new StringDeleteComponent(null, 0, "d");
    transformedOp = op1.transform(op2, true);
    expected0 = new StringInsertComponent(null, 1, "abc");
    equals(expected0, transformedOp);
    transformedOp = op1.transform(op2, false);
    equals(expected0, transformedOp);
  }

  /**
   * Performs tests for transforming text insertions against text insertions.
   */
  public void testInsertVsInsert() {
    // op1's insertion spatially before op2's insertion
    AbstractInsertComponent<String> op1 = new StringInsertComponent(null, 1, "abc");
    AbstractInsertComponent<String> op2 = new StringInsertComponent(null, 2, "def");
    OperationComponent<ListTarget<String>> transformedOp = op1.transform(op2, true);
    assertSame(op1, transformedOp);
    transformedOp = op1.transform(op2, false);
    assertSame(op1, transformedOp);

    expected0 = new StringInsertComponent(null, 2 + 3, "def");
    transformedOp = op2.transform(op1, true);
    equals(expected0, transformedOp);
    transformedOp = op2.transform(op1, false);
    equals(expected0, transformedOp);

    // op1's insertion spatially at the same location as op2's insertion
    op2 = new StringInsertComponent(null, 1, "def");
    transformedOp = op1.transform(op2, false);
    expected0 = new StringInsertComponent(null, 1 + 3, "abc");
    equals(expected0, transformedOp);

    transformedOp = op1.transform(op2, true);
    assertSame(op1, transformedOp);
  }

  public void testParseFromJson() {
    AbstractInsertComponent<String> op = new StringInsertComponent(null, 2, "123");
    equals(op, StringInsertComponent.parse(Json.<JsonArray> parse(op.toString())));
    op = new StringInsertComponent("id", 0, "true");
    equals(op, StringInsertComponent.parse(Json.<JsonArray> parse(op.toString())));
  }

  public void testTransformIndexReference() {
    AbstractInsertComponent<String> op = new StringInsertComponent(null, 1, "abc");
    // cursor before AbstractInsertComponent's startIndex
    int transformIndex = op.transformIndexReference(0, true, true);
    assertEquals(0, transformIndex);
    transformIndex = op.transformIndexReference(0, false, true);
    assertEquals(0, transformIndex);

    // cursor at the same location as AbstractInsertComponent's startIndex
    transformIndex = op.transformIndexReference(1, true, true);
    assertEquals(1 + 3, transformIndex);
    transformIndex = op.transformIndexReference(1, false, true);
    assertEquals(1, transformIndex);

    // cursor after AbstractInsertComponent's startIndex
    transformIndex = op.transformIndexReference(2, true, true);
    assertEquals(2 + 3, transformIndex);
    transformIndex = op.transformIndexReference(2, false, true);
    assertEquals(2 + 3, transformIndex);
  }

  void equals(OperationComponent<?> op0, OperationComponent<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }
}