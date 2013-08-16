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

import com.goodow.realtime.operation.AbstractOperation;
import com.goodow.realtime.operation.list.string.StringDeleteOperation;
import com.goodow.realtime.operation.list.string.StringInsertOperation;
import com.goodow.realtime.operation.list.string.StringReplaceOperation;

import junit.framework.TestCase;

import elemental.json.Json;
import elemental.json.JsonArray;

public class ReplaceOperationTest extends TestCase {
  private AbstractOperation<?> expected0;
  private AbstractOperation<?> expected1;

  public void testParseFromJson() {
    AbstractReplaceOperation<String> op = new StringReplaceOperation(null, 2, "abc", "123");
    assertEquals(op, StringReplaceOperation.parse((JsonArray) Json.instance().parse(op.toString())));
    op = new StringReplaceOperation("id", 0, "abcd", "true");
    assertEquals(op, StringReplaceOperation.parse((JsonArray) Json.instance().parse(op.toString())));
  }

  public void testReplaceVsDelete() {
    // A's deletion spatially before B's replacement
    AbstractReplaceOperation<String> op1 = new StringReplaceOperation(null, 2, "abc", "234");
    AbstractDeleteOperation<String> op2 = new StringDeleteOperation(null, 0, "0");
    AbstractReplaceOperation<String>[] transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 1, "abc", "234");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's deletion spatially adjacent to and before B's replacement
    op2 = new StringDeleteOperation(null, 0, "01");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 0, "abc", "234");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's deletion overlaps B's replacement
    op2 = new StringDeleteOperation(null, 1, "12");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 1, "bc", "34");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's deletion overlaps B's replacement
    op2 = new StringDeleteOperation(null, 3, "3456");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 2, "a", "2");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's deletion a subset of B's replacement
    op2 = new StringDeleteOperation(null, 3, "3");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 2, "ac", "24");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's deletion identical to B's replacement
    op2 = new StringDeleteOperation(null, 2, "234");
    transformedOp = op1.transformWith(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);
  }

  public void testReplaceVsInsert() {
    // A's insertion spatially before B's replacement
    AbstractReplaceOperation<String> op1 = new StringReplaceOperation(null, 3, "abc", "345");
    AbstractInsertOperation<String> op2 = new StringInsertOperation(null, 2, "de");
    AbstractReplaceOperation<String>[] transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 5, "abc", "345");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's insertion spatially at the start of B's replacement
    op2 = new StringInsertOperation(null, 3, "de");
    transformedOp = op1.transformWith(op2, true);
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);

    // A's insertion spatially inside B's replacement
    op2 = new StringInsertOperation(null, 4, "de");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 3, "a", "3");
    expected1 = new StringReplaceOperation(null, 6, "bc", "45");
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);
    transformedOp = op1.transformWith(op2, false);
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);

    // A's insertion spatially at the end of B's replacement
    op2 = new StringInsertOperation(null, 6, "de");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's insertion spatially after B's replacement
    op2 = new StringInsertOperation(null, 7, "de");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);
  }

  public void testReplaceVsReplace() {
    // A's replacement spatially before B's replacement
    AbstractReplaceOperation<String> op1 = new StringReplaceOperation(null, 2, "abc", "234");
    AbstractReplaceOperation<String> op2 = new StringReplaceOperation(null, 6, "a", "6");
    AbstractReplaceOperation<String>[] transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertSame(op2, transformedOp[0]);

    // A's replacement spatially adjacent to and before B's replacement
    op2 = new StringReplaceOperation(null, 5, "ab", "56");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertSame(op2, transformedOp[0]);

    // A's replacement overlaps B's replacement
    op2 = new StringReplaceOperation(null, 3, "abcd", "3456");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 2, "a34", "234");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    expected0 = new StringReplaceOperation(null, 2, "a", "2");
    equals(expected0, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    expected0 = new StringReplaceOperation(null, 3, "34cd", "3456");
    equals(expected0, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    expected0 = new StringReplaceOperation(null, 5, "cd", "56");
    equals(expected0, transformedOp[0]);

    // A's replacement a subset of B's replacement
    op2 = new StringReplaceOperation(null, 1, "abcdef", "123456");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 2, "234", "234");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);

    transformedOp = op2.transformWith(op1, true);
    expected0 = new StringReplaceOperation(null, 1, "a234ef", "123456");
    equals(expected0, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    expected0 = new StringReplaceOperation(null, 1, "a", "1");
    expected1 = new StringReplaceOperation(null, 5, "ef", "56");
    equals(expected0, transformedOp[0]);
    equals(expected1, transformedOp[1]);

    // A's replacement identical to B's replacement
    op2 = new StringReplaceOperation(null, 2, "abc", "234");
    transformedOp = op1.transformWith(op2, true);
    expected0 = new StringReplaceOperation(null, 2, "234", "234");
    equals(expected0, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);
  }

  void equals(AbstractOperation<?> op0, AbstractOperation<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }
}
