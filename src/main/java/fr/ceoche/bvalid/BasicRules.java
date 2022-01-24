/*
 * Copyright 2021 Cédric Eoche-Duval
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
package fr.ceoche.bvalid;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * {@link BasicRules} provides basic expressions methods for business model constraints about
 * mandatory fields and cardinalities.
 *
 * @author ceoche
 */
public class BasicRules {

   private BasicRules() {
   }

   /**
    * Verify whether a string attribute is defined, aka not null nor blank.
    *
    * @param attributeValue to verify
    * @return true if the attribute is not null and not blank (trimmed and not empty)
    */
   public static boolean isDefined(String attributeValue) {
      return attributeValue != null && !attributeValue.trim().isEmpty();
   }

   /**
    * Verify whether an object attribute is defined, aka not null.
    * <p>
    * To verify the validity of the attribute itself, use {@link BusinessMember} instead.
    *
    * @param attributeValue to verify
    * @return true if the attribute is not null, false otherwise
    */
   public static boolean isDefined(Object attributeValue) {
      return attributeValue != null;
   }


   /**
    * Verify whether an optional string attribute is defined if present, aka not blank.
    *
    * @param attributeValue to verify
    * @return true if the attribute is null or if it is not blank (trimmed and not empty), false
    * otherwise
    */
   public static boolean isDefinedIfPresent(String attributeValue) {
      return attributeValue == null || !attributeValue.trim().isEmpty();
   }

   /**
    * Verify whether a collection attribute as at least one element.
    *
    * @param collection to verify
    * @return true if the collection is not null and has one or more elements, false otherwise.
    */
   public static boolean hasOneOrMoreElements(Collection<?> collection) {
      return collection != null && !collection.isEmpty();
   }

   /**
    * Verify whether an array attribute as at lease one element.
    *
    * @param objects array to verify
    * @return true if the array is not null and has one or more element, false otherwise.
    */
   public static boolean hasOneOrMoreElements(Object[] objects) {
      return objects != null && objects.length > 0;
   }

   /**
    * Verify whether a collection has non-null elements.
    *
    * @param collection collection to verify
    * @return true if none of the elements of the collection is null or if the collection is empty,
    * false otherwise.
    */
   public static boolean hasDefinedElements(Collection<?> collection) {
      if (collection != null) {
         try {
            return !collection.contains(null);
         } catch (NullPointerException e) {
            return true;
         }
      } else {
         return false;
      }
   }

   /**
    * Verify whether an array has non-null elements
    *
    * @param objects array to verify
    * @return true if none of the elements of the array is null or if the array is empty, false
    * otherwise.
    */
   public static boolean hasDefinedElements(Object[] objects) {
      return hasDefinedElements(Arrays.asList(objects));
   }

   /**
    * Verify whether a collection attribute as at least one element and does not have undefined
    * elements (null elements).
    *
    * @param collection collection to verify
    * @return true if the collection has at least one element and if none of them is null,
    * false otherwise.
    */
   public static boolean hasOneOrMoreDefinedElements(Collection<?> collection) {
      return hasOneOrMoreElements(collection) && hasDefinedElements(collection);
   }

   /**
    * Verify whether an array as at least one element and does not have undefined
    * elements (null elements).
    *
    * @param objects array to verify
    * @return true if the array has at least one element and if none of them is null,
    * false otherwise.
    */
   public static boolean hasOneOrMoreDefinedElements(Object[] objects) {
      return hasOneOrMoreElements(objects) && hasDefinedElements(objects);
   }

   /**
    * Verify whether a String is matching the given pattern.
    *
    * @param regexp  the regular expression to match
    * @param subject the subject of the pattern test.
    * @return true if the subject is matching the regexp, false if not or if either regexp or
    * subject is null.
    */
   public static boolean matches(String regexp, String subject) {
      if (regexp != null && subject != null) {
         return Pattern.compile(regexp).matcher(subject).matches();
      } else {
         return false;
      }
   }
}
