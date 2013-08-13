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

public class ReplaceOperationTest extends TestCase {
  public void testParseFromJson() {
    ReplaceOperation<String> op = new ReplaceOperation<String>(null, 2, "abc", "123");
    assertEquals(op, ReplaceOperation.parse((JsonArray) Json.instance().parse(op.toString())));
    op = new ReplaceOperation<String>("id", 0, "abcd", "true");
    assertEquals(op, ReplaceOperation.parse((JsonArray) Json.instance().parse(op.toString())));
  }

  public void testReplaceVsDelete() {
    // A's deletion spatially before B's replacement
    ReplaceOperation<String> op1 = new ReplaceOperation<String>(null, 2, "abc", "234");
    DeleteOperation<String> op2 = new DeleteOperation<String>(null, 0, "0");
    ReplaceOperation<String>[] transformedOp = op1.transformWith(op2, true);
    ReplaceOperation<String> expected = new ReplaceOperation<String>(null, 1, "abc", "234");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    // A's deletion spatially adjacent to and before B's replacement
    op2 = new DeleteOperation<String>(null, 0, "01");
    transformedOp = op1.transformWith(op2, true);
    expected = new ReplaceOperation<String>(null, 0, "abc", "234");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    // A's deletion overlaps B's replacement
    op2 = new DeleteOperation<String>(null, 1, "12");
    transformedOp = op1.transformWith(op2, true);
    expected = new ReplaceOperation<String>(null, 1, "ab", "34");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    // A's deletion overlaps B's replacement
    op2 = new DeleteOperation<String>(null, 3, "3456");
    transformedOp = op1.transformWith(op2, true);
    expected = new ReplaceOperation<String>(null, 2, "a", "2");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    // A's deletion a subset of B's replacement
    op2 = new DeleteOperation<String>(null, 3, "3");
    transformedOp = op1.transformWith(op2, true);
    expected = new ReplaceOperation<String>(null, 2, "ab", "24");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    // A's deletion identical to B's replacement
    op2 = new DeleteOperation<String>(null, 2, "234");
    transformedOp = op1.transformWith(op2, true);
    assertNull(transformedOp);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);
  }

  public void testReplaceVsInsert() {
    // A's insertion spatially before B's replacement
    ReplaceOperation<String> op1 = new ReplaceOperation<String>(null, 3, "abc", "345");
    InsertOperation<String> op2 = new InsertOperation<String>(null, 2, "de");
    ReplaceOperation<String>[] transformedOp = op1.transformWith(op2, true);
    ReplaceOperation<String> expected = new ReplaceOperation<String>(null, 5, "abc", "345");
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    // A's insertion spatially at the start of B's replacement
    op2 = new InsertOperation<String>(null, 3, "de");
    transformedOp = op1.transformWith(op2, true);
    assertEquals(expected, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);

    // A's insertion spatially inside B's replacement
    op2 = new InsertOperation<String>(null, 4, "de");
    transformedOp = op1.transformWith(op2, true);
    expected = new ReplaceOperation<String>(null, 3, "a", "3");
    ReplaceOperation<String> expected2 = new ReplaceOperation<String>(null, 6, "bc", "45");
    assertEquals(expected, transformedOp[0]);
    assertEquals(expected2, transformedOp[1]);
    transformedOp = op1.transformWith(op2, false);
    assertEquals(expected, transformedOp[0]);
    assertEquals(expected2, transformedOp[1]);

    // A's insertion spatially at the end of B's replacement
    op2 = new InsertOperation<String>(null, 6, "de");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    // A's insertion spatially after B's replacement
    op2 = new InsertOperation<String>(null, 7, "de");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);
  }

  public void testReplaceVsReplace() {
    // A's replacement spatially before B's replacement
    ReplaceOperation<String> op1 = new ReplaceOperation<String>(null, 2, "abc", "234");
    ReplaceOperation<String> op2 = new ReplaceOperation<String>(null, 6, "a", "6");
    ReplaceOperation<String>[] transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertSame(op2, transformedOp[0]);

    // A's replacement spatially adjacent to and before B's replacement
    op2 = new ReplaceOperation<String>(null, 5, "ab", "56");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertSame(op1, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    assertSame(op2, transformedOp[0]);

    // A's replacement overlaps B's replacement
    op2 = new ReplaceOperation<String>(null, 3, "abcd", "3456");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    ReplaceOperation<String> expected = new ReplaceOperation<String>(null, 2, "a", "2");
    assertEquals(expected, transformedOp[0]);

    transformedOp = op2.transformWith(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    expected = new ReplaceOperation<String>(null, 5, "ab", "56");
    assertEquals(expected, transformedOp[0]);

    // A's replacement a subset of B's replacement
    op2 = new ReplaceOperation<String>(null, 1, "abcdef", "123456");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);

    transformedOp = op2.transformWith(op1, true);
    assertSame(op2, transformedOp[0]);
    transformedOp = op2.transformWith(op1, false);
    expected = new ReplaceOperation<String>(null, 1, "a", "1");
    ReplaceOperation<String> expected1 = new ReplaceOperation<String>(null, 5, "ab", "56");
    assertEquals(expected, transformedOp[0]);
    assertEquals(expected1, transformedOp[1]);

    // A's replacement identical to B's replacement
    op2 = new ReplaceOperation<String>(null, 2, "abc", "234");
    transformedOp = op1.transformWith(op2, true);
    assertSame(op1, transformedOp[0]);
    transformedOp = op1.transformWith(op2, false);
    assertNull(transformedOp);
  }
}
