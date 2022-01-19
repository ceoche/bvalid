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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;

public class BusinessObjectMocks {

   public static Object instantiateValid() {
      DefaultValidableMock object = new DefaultValidableMock();
      object.setMandatoryAttribute("name");
      object.getOneOrMoreAssociation().add("one association");
      return object;
   }

   public static Object instantiateInvalid() {
      DefaultValidableMock object = new DefaultValidableMock();
      object.setMandatoryAttribute(null);
      object.setOptionalAttribute("   ");
      return object;
   }

   public static Object instantiateInheritanceWithInvalidParent() {
      WithInheritance object = new WithInheritance();
      object.setSubtype("defined sub type");
      object.setMandatoryAttribute(null);
      return object;
   }

   public static Object instantiateInheritanceWithoutAnnotationValid() {
      WithInheritanceButWithoutAnnotation object = new WithInheritanceButWithoutAnnotation();
      object.setMandatoryAttribute("name");
      object.getOneOrMoreAssociation().add("one association");
      return object;
   }

   public static Object instantiateWithoutAssertions() {
      IllegalBusinessObject object= new IllegalBusinessObject();
      object.setName("value");
      return object;
   }

   public static Object instantiateBusinessMemberInvalid() {
      OnlyBusinessMembers onlyBusinessMembers = new OnlyBusinessMembers();
      onlyBusinessMembers.setValidableMock((DefaultValidableMock) instantiateInvalid());
      return onlyBusinessMembers;
   }

   public static Object instantiateBusinessMemberNull() {
      return new OnlyBusinessMembers();
   }

   public static Object instantiateBusinessMemberCollection() {
      DefaultValidableMock validMock = (DefaultValidableMock) instantiateValid();
      DefaultValidableMock invalidMock = (DefaultValidableMock) instantiateInvalid();
      CollectionBusinessMembers collecBusinessMember = new CollectionBusinessMembers();
      collecBusinessMember.setValidableMockList(Arrays.asList(new DefaultValidableMock[]{validMock, invalidMock}));
      collecBusinessMember.setValidableMockSet(new HashSet<DefaultValidableMock>(collecBusinessMember.getValidableMockList()));
      collecBusinessMember.setValidableMockQueue(new LinkedTransferQueue<DefaultValidableMock>(collecBusinessMember.getValidableMockList()));
      return collecBusinessMember;
   }

   public static Object instantiateBusinessMemberArray() {
      DefaultValidableMock validMock = (DefaultValidableMock) instantiateValid();
      DefaultValidableMock invalidMock = (DefaultValidableMock) instantiateInvalid();
      ArrayBusinessMember arrayBusinessMember = new ArrayBusinessMember();
      arrayBusinessMember.setValidableMockArray(new DefaultValidableMock[]{validMock, invalidMock});
      return arrayBusinessMember;
   }

   public static Object instantiateIllegalBusinessRule() {
      return new IllegalBusinessRuleObject();
   }

   public static Object instantiateIllegalBusinessMember() {
      return new IllegalBusinessMemberObject();
   }

   public static Object instantiateExceptionBusinessRule() {
      return new ExceptionBusinessRuleObject();
   }

   public static Object instantiateExceptionBusinessMember() {
      return new ExceptionBusinessMemberObject();
   }

   @BusinessObject(name = "validable-mock")
   public static class DefaultValidableMock {

      private String mandatoryAttribute;
      private String optionalAttribute;
      private List<String> oneOrMoreAssociation = new ArrayList<>();

      public String getMandatoryAttribute() {
         return mandatoryAttribute;
      }

      public void setMandatoryAttribute(String mandatoryAttribute) {
         this.mandatoryAttribute = mandatoryAttribute;
      }

      public String getOptionalAttribute() {
         return optionalAttribute;
      }

      public void setOptionalAttribute(String optionalAttribute) {
         this.optionalAttribute = optionalAttribute;
      }

      public List<String> getOneOrMoreAssociation() {
         return oneOrMoreAssociation;
      }

      public void setOneOrMoreAssociation(List<String> oneOrMoreAssociation) {
         this.oneOrMoreAssociation = oneOrMoreAssociation;
      }

      @BusinessRule(id = "rule01", description = "mandatoryAttribute must be defined.")
      public boolean isMandatoryAttributeValid() {
         return BasicRules.isDefined(mandatoryAttribute);
      }

      @BusinessRule(description = "optionalAttribute must be defined if present.")
      public boolean isOptionalAttributeValid() {
         return BasicRules.isDefinedIfPresent(optionalAttribute);
      }

      @BusinessRule(description = "oneOrMoreAssociation must have at least one element.")
      public boolean isOneOrMoreAssociationValid() {
         return BasicRules.hasOneOrMoreElements(oneOrMoreAssociation);
      }
   }

   @BusinessObject(name = "With-inheritance")
   public static class WithInheritance extends DefaultValidableMock {
      private String subtype;

      public String getSubtype() {
         return subtype;
      }

      public void setSubtype(String subtype) {
         this.subtype = subtype;
      }

      @BusinessRule(description = "Sub type must be defined.")
      public boolean isSubtypeValid() {
         return BasicRules.isDefined(subtype);
      }
   }

   public static class WithInheritanceButWithoutAnnotation extends DefaultValidableMock {

   }

   @BusinessObject
   public static class OnlyBusinessMembers {

      private DefaultValidableMock validableMock;

      @BusinessMember(name = "my-only-member")
      public DefaultValidableMock getValidableMock() {
         return validableMock;
      }

      public void setValidableMock(DefaultValidableMock validableMock) {
         this.validableMock = validableMock;
      }
   }

   @BusinessObject
   public static class CollectionBusinessMembers {

      private List<DefaultValidableMock> validableMockList;
      private Set<DefaultValidableMock> validableMockSet;
      private Queue<DefaultValidableMock> validableMockQueue;

      @BusinessMember(name = "list")
      public List<DefaultValidableMock> getValidableMockList() {
         return validableMockList;
      }

      public void setValidableMockList(
            List<DefaultValidableMock> validableMockList) {
         this.validableMockList = validableMockList;
      }

      @BusinessMember
      public Set<DefaultValidableMock> getValidableMockSet() {
         return validableMockSet;
      }

      public void setValidableMockSet(
            Set<DefaultValidableMock> validableMockSet) {
         this.validableMockSet = validableMockSet;
      }

      @BusinessMember
      public Queue<DefaultValidableMock> getValidableMockQueue() {
         return validableMockQueue;
      }

      public void setValidableMockQueue(
            Queue<DefaultValidableMock> validableMockQueue) {
         this.validableMockQueue = validableMockQueue;
      }
   }

   @BusinessObject
   public static class ArrayBusinessMember {

      private DefaultValidableMock[] validableMockArray;

      @BusinessMember
      public DefaultValidableMock[] getValidableMockArray() {
         return validableMockArray;
      }

      public void setValidableMockArray(DefaultValidableMock[] validableMockArray) {
         this.validableMockArray = validableMockArray;
      }
   }

   @BusinessObject(name = "without-assertions")
   public static class IllegalBusinessObject {
      private String name;

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public boolean isNameValid() {
         return BasicRules.isDefined(name);
      }
   }

   @BusinessObject(name = "illegalBusinessRule")
   public static class IllegalBusinessRuleObject {

      @BusinessRule(description = "The object must be defined")
      public boolean isValid(Object object) {
         return object != null;
      }
   }

   @BusinessObject
   public static class IllegalBusinessMemberObject {

      @BusinessMember
      public void getNotABusinessObject(String nope) {
         nope = "tadaaa !";
      }
   }

   @BusinessObject
   public static class ExceptionBusinessRuleObject {

      @BusinessRule(description = "To test InvocationException")
      public boolean getAnException() {
         throw new IllegalStateException();
      }
   }

   @BusinessObject
   private static class ExceptionBusinessMemberObject {

      @BusinessMember
      public DefaultValidableMock getMember() {
         throw new IllegalStateException();
      }
   }
}
