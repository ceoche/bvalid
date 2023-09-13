/*
 * Copyright 2022-2023 Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * ou may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ceoche.bvalid;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicRulesTest {

   @Test
   void testStringDefined() {
      assertTrue(BasicRules.isDefined("This a defined string"));
   }

   @ParameterizedTest
   @NullAndEmptySource
   void testStringNotDefined(String nullOrEmpty) {
      assertFalse(BasicRules.isDefined(nullOrEmpty));
   }

   @Test
   void testObjectDefined() {
      assertTrue(BasicRules.isDefined(new StringBuilder("defined")));
   }

   @Test
   void testObjectNotDefined() {
      StringBuilder sb = null;
      assertFalse(BasicRules.isDefined(sb));
   }

   @Test
   void testOptionalString() {
      assertTrue(BasicRules.isDefinedIfPresent("Optional String"));
      assertTrue(BasicRules.isDefinedIfPresent(null));
   }

   @Test
   void testWrongOptionalString() {
      assertFalse(BasicRules.isDefinedIfPresent("  "));
   }

   @Test
   void testOneOrMoreElementArrayValid() {
      assertTrue(BasicRules.hasOneOrMoreElements(new Integer[]{1, null, 3}));
   }

   @Test
   void testOneOrMoreElementArrayInvalid() {
      assertFalse(BasicRules.hasOneOrMoreElements(new Integer[]{}));
      assertFalse(BasicRules.hasOneOrMoreElements((Object[]) null));
   }

   @Test
   void testOneOrMoreElementListValid() {
      assertTrue(BasicRules.hasOneOrMoreElements(Arrays.asList(1, null, 3)));
   }

   @Test
   void testOneOrMoreElementListInvalid() {
      assertFalse(BasicRules.hasOneOrMoreElements(new ArrayList<>()));
      assertFalse(BasicRules.hasOneOrMoreElements((Collection<?>) null));
   }

   @Test
   void testHasDefinedElementList() {
      assertTrue(BasicRules.hasDefinedElements(Arrays.asList(1, 2, 3)));
      assertTrue(BasicRules.hasDefinedElements(
            new NonNullList<>(Arrays.asList(1, 2, 3))
      ));
   }

   @Test
   void testHasDefinedElementListInvalid() {
      assertFalse(BasicRules.hasDefinedElements((Collection<?>) null));
      assertFalse(BasicRules.hasDefinedElements(Arrays.asList(1, null, 3)));
   }

   @Test
   void testHasDefinedElementArray() {
      assertTrue(BasicRules.hasDefinedElements(new Integer[]{1, 2, 3}));
   }

   @Test
   void testHasDefinedElementArrayInvalid() {
      assertFalse(BasicRules.hasDefinedElements(new Integer[]{1, 2, null}));
   }

   @Test
   void testHasOneOrMoreDefinedElementArrayValid() {
      assertTrue(BasicRules.hasOneOrMoreDefinedElements(new Integer[]{1, 2, 3}));
   }

   @Test
   void testHasOneOrMoreDefinedElementArrayInvalid() {
      assertFalse(BasicRules.hasOneOrMoreDefinedElements(new Integer[]{}));
      assertFalse(BasicRules.hasOneOrMoreDefinedElements(new Integer[]{1, null, 3}));
   }

   @Test
   void testHasOneOrMoreDefinedElementListValid() {
      assertTrue(BasicRules.hasOneOrMoreDefinedElements(Arrays.asList(1, 2, 3)));
   }

   @Test
   void testHasOneOrMoreDefinedElementListInvalid() {
      assertFalse(BasicRules.hasOneOrMoreDefinedElements(new ArrayList<>()));
      assertFalse(BasicRules.hasOneOrMoreDefinedElements(Arrays.asList(1, null, 3)));
   }

   @Test
   void testPatternMatches() {
      assertTrue(BasicRules.matches("^[^\\s\\.{}\\$]+$", "validId"));
   }

   @ParameterizedTest
   @ValueSource(strings = {"idWith space", "idWith.dot", "idWith{bracket", "idWithBracket}",
         "idWith$"})
   void testPatternMatchesInvalid(String subject) {
      assertFalse(BasicRules.matches("^[^\\s\\.{}\\$]+$", subject));
   }

   @Test
   void testPatternMatchesError() {
      assertFalse(BasicRules.matches(null, "toto"));
      assertFalse(BasicRules.matches(".*", null));
   }

}
