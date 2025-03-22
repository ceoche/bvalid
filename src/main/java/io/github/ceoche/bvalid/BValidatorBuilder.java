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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Builder for a {@link BValidator}.
 *
 * @param <T> the type of the business object to validate
 */
public class BValidatorBuilder<T> {

   private final Class<T> type;

   private String objectName = "";

   private final Set<BAssertion<? super T>> rules = new LinkedHashSet<>();
   private final Map<String, BusinessMemberBuilder<? super T, ?>> memberBuilders = new LinkedHashMap<>();

   /**
    * Constructor of BValidatorManualBuilder
    *
    * @param type the type of the business object to build a validator for.
    *
    * @throws IllegalArgumentException if the type is null
    */
   public BValidatorBuilder(Class<T> type) {
      if (type != null) {
         this.type = type;
         this.objectName = type.getSimpleName();
      } else {
         throw new IllegalArgumentException("Type must not be null.");
      }
   }

   /**
    * Constructor to extends a BValidatorManualBuilder of a supertype.
    *
    * @param type    the type of the business object to build a validator for.
    * @param builder the BValidator builder of the business object supertype.
    *
    * @throws IllegalArgumentException if the type is null
    * @throws NullPointerException     if the builder is null (use the constructor without builder in this case)
    */
   public BValidatorBuilder(Class<T> type, BValidatorBuilder<? super T> builder) {
      this(type);
      rules.addAll(builder.rules);
      memberBuilders.putAll(builder.memberBuilders);
   }

   /**
    * Get the type of the business object to validate.
    *
    * @return the type of the business object.
    */
   public Class<T> getType() {
      return type;
   }

   /**
    * Define the name of the business object to build a validator for. It is recommended to set the name of the root
    * object for a better reporting.
    *
    * @param objectName name of the business object.
    *
    * @return this instance of BValidatorBuilder.
    */
   public BValidatorBuilder<T> setObjectName(String objectName) {
      this.objectName = objectName;
      return this;
   }

   /**
    * Add a rule in form of Java Predicate {@link Predicate<T>} to the validator.
    *
    * @param id          the requirement id of the rule
    * @param rule        the rule to add
    * @param description the description of the rule
    *
    * @return the builder
    *
    * @throws IllegalArgumentException if the rule is null
    */
   public BValidatorBuilder<T> addAssertion(String id, Predicate<T> rule, String description) {
      if (rule == null) {
         throw new IllegalArgumentException("Rule predicate must not be null");
      }
      rules.add(new BAssertion<>(id, rule, description));
      return this;
   }

   /**
    * Add a rule in form of Java Predicate {@link Predicate<T>} to the validator.
    *
    * @param rule        the rule to add
    * @param description the description of the rule
    *
    * @return the builder
    *
    * @throws IllegalArgumentException if the rule is null
    */
   public BValidatorBuilder<T> addAssertion(Predicate<T> rule, String description) {
      addAssertion("", rule, description);
      return this;
   }


   /**
    * Add a member to the validator.
    *
    * @param name               the name of the field
    * @param getter             the getter of the field in form of a Java Function {@link Function}
    * @param bValidatorBuilders All the validators builders of the possible subtypes of the field
    * @param <M>                the type of the field
    *
    * @return the builder
    *
    * @throws IllegalArgumentException if the name, getter or bValidatorBuilders are null
    */
   @SafeVarargs
   public final <M> BValidatorBuilder<T> addMember(String name, Function<T, ?> getter,
                                                   BValidatorBuilder<? extends M>... bValidatorBuilders) {
      if (name == null || getter == null || isThereNullBuilder(bValidatorBuilders)) {
         throw new IllegalArgumentException("Name, getter and bValidatorBuilders must not be null");
      }
      memberBuilders.put(name, new BusinessMemberBuilder<>(name, getter, Set.of(bValidatorBuilders)));
      return this;
   }

   /**
    * Add subtypes validators to a member of the business object.
    *
    * @param name               the name of the member to complete
    * @param bValidatorBuilders the validators builders of the possible subtypes of the field to add
    *
    * @return the builder
    *
    * @throws NoSuchElementException if the member with the given name is not found in the builder.
    * @throws ClassCastException     if the type covered by the given bValidatorBuilders are not subtypes of the member
    *                                type.
    */
   public BValidatorBuilder<T> addMemberSubTypes(String memberName,
                                                 BValidatorBuilder<?>... bValidatorBuilders) {
      BusinessMemberBuilder<? super T, ?> businessMemberBuilder = getMemberBuilders(memberName);
      for (BValidatorBuilder<?> bValidatorBuilder : bValidatorBuilders) {
         businessMemberBuilder.addValidatorBuilder(bValidatorBuilder);
      }
      return this;
   }

   /**
    * Check if the builder is empty (i.e. no rules or members).
    *
    * @return true if the builder is empty, false otherwise
    */
   public boolean isEmpty() {
      return rules.isEmpty() && memberBuilders.isEmpty();
   }

   /**
    * Get the number of rules in this builder.
    *
    * @return the number of rules.
    */
   public int getRulesCount() {
      return rules.size();
   }

   /**
    * Get the number of members in this builder.
    *
    * @return the number of members.
    */
   public int getMembersCount() {
      return memberBuilders.size();
   }

   /**
    * Build the {@link BValidator} from the builder.
    *
    * @return the {@link BValidator} intialized with assertions and members defined in the builder.
    *
    * @throws IllegalStateException if a member does not have at least one associated validator builder or if the
    *                               builder is empty (i.e. no rules or members)
    */
   public BValidator<T> build() {
      BuildingCache cache = new BuildingCache();
      return build(cache);
   }

   BValidatorBuilder<T> addAllMembers(Map<String, BusinessMemberBuilder<? super T, ?>> members) {
      this.memberBuilders.putAll(members);
      return this;
   }

   BValidatorBuilder<T> addAllRules(Set<BAssertion<? super T>> rules) {
      this.rules.addAll(rules);
      return this;
   }

   private BusinessMemberBuilder<? super T, ?> getMemberBuilders(String memberName) {
      return Optional.ofNullable(memberBuilders.get(memberName))
            .orElseThrow(
                  () -> new NoSuchElementException("No member with name " + memberName + " found in the builder.")
            );
   }

   private BValidator<T> build(BuildingCache cache) {
      assertBuilderNotEmpty();

      if (cache.contains(this)) {
         return cache.get(this);
      } else {
         Set<BMember<? super T, ?>> membersPlaceholder = new LinkedHashSet<>();
         BValidator<T> validator = new BValidator<>(
               this.type,
               this.objectName,
               this.rules,
               membersPlaceholder
         );
         cache.put(this, validator);
         membersPlaceholder.addAll(
               this.memberBuilders.values().stream()
                     .map(businessMemberBuilder -> buildMember(businessMemberBuilder, cache))
                     .collect(Collectors.toSet())
         );
         return validator;
      }
   }

   private BMember<? super T, ?> buildMember(BusinessMemberBuilder<? super T, ?> businessMemberBuilder,
                                             BuildingCache cache) {
      return new BMember<>(
            businessMemberBuilder.getName(),
            businessMemberBuilder.getGetter(),
            businessMemberBuilder.getValidatorBuilders().stream()
                  .map(
                        bValidatorBuilder -> {
                           if (cache.contains(bValidatorBuilder)) {
                              return cache.get(bValidatorBuilder);
                           } else {
                              return bValidatorBuilder.build(cache);
                           }
                        }
                  )
                  .collect(Collectors.toSet())
      );
   }

   private void assertBuilderNotEmpty() {
      if (isEmpty()) {
         throw new IllegalStateException(
               "At least one rule or one member must be provided to build a validator.");
      }
   }


   private boolean isThereNullBuilder(BValidatorBuilder<?>... bValidatorBuilders) {
      for (BValidatorBuilder<?> bValidatorBuilder : bValidatorBuilders) {
         if (bValidatorBuilder == null) {
            return true;
         }
      }
      return false;
   }

   private static class BuildingCache {
      private final Map<BValidatorBuilder<?>, BValidator<?>> cache = new HashMap<>();

      public <T> boolean contains(BValidatorBuilder<T> bValidatorBuilder) {
         return cache.containsKey(bValidatorBuilder);
      }

      public <T> BValidator<T> get(BValidatorBuilder<T> bValidatorBuilder) {
         return (BValidator<T>) cache.get(bValidatorBuilder);
      }

      public <T> void put(BValidatorBuilder<T> bValidatorBuilder, BValidator<T> bValidator) {
         cache.put(bValidatorBuilder, bValidator);
      }

   }

}
