/*
 * Copyright 2022-2025. Cédric Eoche-Duval
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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

public class DefaultAssertionsTest {

   @Test
   void testStringDefined() {
      assertTrue(DefaultAssertions.isDefined("This a defined string"));
   }

   @ParameterizedTest
   @NullAndEmptySource
   void testStringNotDefined(String nullOrEmpty) {
      assertFalse(DefaultAssertions.isDefined(nullOrEmpty));
   }

   @Test
   void testObjectDefined() {
      assertTrue(DefaultAssertions.isDefined(new StringBuilder("defined")));
   }

   @Test
   void testObjectNotDefined() {
      StringBuilder sb = null;
      assertFalse(DefaultAssertions.isDefined(sb));
   }

   @Test
   void testOptionalString() {
      assertTrue(DefaultAssertions.isDefinedIfPresent("Optional String"));
      assertTrue(DefaultAssertions.isDefinedIfPresent(null));
   }

   @Test
   void testWrongOptionalString() {
      assertFalse(DefaultAssertions.isDefinedIfPresent("  "));
   }

   @Test
   void testOneOrMoreElementArrayValid() {
      assertTrue(DefaultAssertions.hasOneOrMoreElements(new Integer[]{1, null, 3}));
   }

   @Test
   void testOneOrMoreElementArrayInvalid() {
      assertFalse(DefaultAssertions.hasOneOrMoreElements(new Integer[]{}));
      assertFalse(DefaultAssertions.hasOneOrMoreElements((Object[]) null));
   }

   @Test
   void testOneOrMoreElementListValid() {
      assertTrue(DefaultAssertions.hasOneOrMoreElements(Arrays.asList(1, null, 3)));
   }

   @Test
   void testOneOrMoreElementListInvalid() {
      assertFalse(DefaultAssertions.hasOneOrMoreElements(new ArrayList<>()));
      assertFalse(DefaultAssertions.hasOneOrMoreElements((Collection<?>) null));
   }

   @Test
   void testHasDefinedElementList() {
      assertTrue(DefaultAssertions.hasDefinedElements(Arrays.asList(1, 2, 3)));
      assertTrue(DefaultAssertions.hasDefinedElements(
            new NonNullList<>(Arrays.asList(1, 2, 3))
      ));
   }

   @Test
   void testHasDefinedElementListInvalid() {
      assertFalse(DefaultAssertions.hasDefinedElements((Collection<?>) null));
      assertFalse(DefaultAssertions.hasDefinedElements(Arrays.asList(1, null, 3)));
   }

   @Test
   void testHasDefinedElementArray() {
      assertTrue(DefaultAssertions.hasDefinedElements(new Integer[]{1, 2, 3}));
   }

   @Test
   void testHasDefinedElementArrayInvalid() {
      assertFalse(DefaultAssertions.hasDefinedElements(new Integer[]{1, 2, null}));
   }

   @Test
   void testHasOneOrMoreDefinedElementArrayValid() {
      assertTrue(DefaultAssertions.hasOneOrMoreDefinedElements(new Integer[]{1, 2, 3}));
   }

   @Test
   void testHasOneOrMoreDefinedElementArrayInvalid() {
      assertFalse(DefaultAssertions.hasOneOrMoreDefinedElements(new Integer[]{}));
      assertFalse(DefaultAssertions.hasOneOrMoreDefinedElements(new Integer[]{1, null, 3}));
   }

   @Test
   void testHasOneOrMoreDefinedElementListValid() {
      assertTrue(DefaultAssertions.hasOneOrMoreDefinedElements(Arrays.asList(1, 2, 3)));
   }

   @Test
   void testHasOneOrMoreDefinedElementListInvalid() {
      assertFalse(DefaultAssertions.hasOneOrMoreDefinedElements(new ArrayList<>()));
      assertFalse(DefaultAssertions.hasOneOrMoreDefinedElements(Arrays.asList(1, null, 3)));
   }

   @Test
   void testPatternMatches() {
      assertTrue(DefaultAssertions.matches("^[^\\s\\.{}\\$]+$", "validId"));
   }

   @ParameterizedTest
   @ValueSource(strings = {"idWith space", "idWith.dot", "idWith{bracket", "idWithBracket}",
         "idWith$"})
   void testPatternMatchesInvalid(String subject) {
      assertFalse(DefaultAssertions.matches("^[^\\s\\.{}\\$]+$", subject));
   }

   @Test
   void testPatternMatchesError() {
      assertFalse(DefaultAssertions.matches(null, "toto"));
      assertFalse(DefaultAssertions.matches(".*", null));
   }

}
