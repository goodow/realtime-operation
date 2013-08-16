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
package com.goodow.realtime.operation;

import com.goodow.realtime.operation.list.AbstractDeleteOperation;
import com.goodow.realtime.operation.list.AbstractInsertOperation;
import com.goodow.realtime.operation.list.AbstractReplaceOperation;
import com.goodow.realtime.operation.list.string.StringDeleteOperation;
import com.goodow.realtime.operation.list.string.StringInsertOperation;
import com.goodow.realtime.operation.list.string.StringReplaceOperation;
import com.goodow.realtime.operation.util.Pair;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RealtimeTransformerTest extends TestCase {
  RealtimeTransformer transformer = new RealtimeTransformer();

  public void testTransformClientOpToMultipleOps() {
    AbstractInsertOperation<String> serverOp0 = new StringInsertOperation(null, 3, "ab");
    AbstractReplaceOperation<String> serverOp1 = new StringReplaceOperation(null, 3, "abc", "def");
    AbstractReplaceOperation<String> clientOp0 = new StringReplaceOperation(null, 2, "abc", "234");
    AbstractDeleteOperation<String> clientOp1 = new StringDeleteOperation(null, 2, "234");
    List<AbstractOperation<?>> serverOps = new LinkedList<AbstractOperation<?>>();
    List<AbstractOperation<?>> clientOps = new ArrayList<AbstractOperation<?>>();
    serverOps.add(serverOp0);
    serverOps.add(serverOp1);
    clientOps.add(clientOp0);
    clientOps.add(clientOp1);
    Pair<List<AbstractOperation<?>>, List<AbstractOperation<?>>> pair =
        transformer.transform(serverOps, clientOps);
    AbstractOperation<?> expected;

    assertEquals(4, pair.second.size());
    expected = new StringReplaceOperation(null, 2, "a", "2");
    equals(expected, pair.second.get(0));
    expected = new StringReplaceOperation(null, 5, "fc", "34");
    equals(expected, pair.second.get(1));
    expected = new StringDeleteOperation(null, 2, "2");
    equals(expected, pair.second.get(2));
    expected = new StringDeleteOperation(null, 4, "34");
    equals(expected, pair.second.get(3));

    assertEquals(2, pair.first.size());
    expected = new StringInsertOperation(null, 2, "ab");
    equals(expected, pair.first.get(0));
    expected = new StringReplaceOperation(null, 2, "ab", "de");
    equals(expected, pair.first.get(1));
  }

  public void testTransformServerOpToMultipleOps() {
    AbstractReplaceOperation<String> serverOp0 = new StringReplaceOperation(null, 2, "abc", "234");
    AbstractDeleteOperation<String> serverOp1 = new StringDeleteOperation(null, 2, "234");
    AbstractInsertOperation<String> clientOp0 = new StringInsertOperation(null, 3, "ab");
    AbstractReplaceOperation<String> clientOp1 = new StringReplaceOperation(null, 3, "abc", "def");
    List<AbstractOperation<?>> serverOps = new LinkedList<AbstractOperation<?>>();
    List<AbstractOperation<?>> clientOps = new ArrayList<AbstractOperation<?>>();
    serverOps.add(serverOp0);
    serverOps.add(serverOp1);
    clientOps.add(clientOp0);
    clientOps.add(clientOp1);
    Pair<List<AbstractOperation<?>>, List<AbstractOperation<?>>> pair =
        transformer.transform(serverOps, clientOps);
    AbstractOperation<?> expected;

    assertEquals(2, pair.second.size());
    expected = new StringInsertOperation(null, 2, "ab");
    equals(expected, pair.second.get(0));
    expected = new StringReplaceOperation(null, 2, "ab", "de");
    equals(expected, pair.second.get(1));

    assertEquals(4, pair.first.size());
    expected = new StringReplaceOperation(null, 2, "a", "2");
    equals(expected, pair.first.get(0));
    expected = new StringReplaceOperation(null, 6, "c", "4");
    equals(expected, pair.first.get(1));
    expected = new StringDeleteOperation(null, 2, "2");
    equals(expected, pair.first.get(2));
    expected = new StringDeleteOperation(null, 4, "f4");
    equals(expected, pair.first.get(3));

  }

  public void testTransformServerOpToNoOp() {

  }

  void equals(AbstractOperation<?> op0, AbstractOperation<?> op1) {
    assertEquals(op0, op1);
    assertEquals(op0.invert(), op1.invert());
  }

}
