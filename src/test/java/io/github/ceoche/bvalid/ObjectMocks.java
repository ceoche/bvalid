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

import java.util.*;
import java.util.concurrent.LinkedTransferQueue;

public class ObjectMocks {

   public static DefaultValidableMock instantiateValid() {
      DefaultValidableMock object = new DefaultValidableMock();
      object.setMandatoryAttribute("name");
      object.getOneOrMoreAssociation().add("one association");
      return object;
   }

   public static DefaultValidableMock instantiateInvalid() {
      DefaultValidableMock object = new DefaultValidableMock();
      object.setMandatoryAttribute(null);
      object.setOptionalAttribute("   ");
      return object;
   }

   public static WithInheritance instantiateInheritanceWithInvalidParent() {
      WithInheritance object = new WithInheritance();
      object.setChildAttribute("defined sub type");
      object.setMandatoryAttribute(null);
      return object;
   }

   public static DefaultValidableMock instantiateInheritanceWithoutAnnotationValid() {
      WithInheritanceButWithoutAnnotation object = new WithInheritanceButWithoutAnnotation();
      object.setMandatoryAttribute("name");
      object.getOneOrMoreAssociation().add("one association");
      return object;
   }

   public static IllegalBusinessObject instantiateWithoutAssertions() {
      IllegalBusinessObject object = new IllegalBusinessObject();
      object.setName("value");
      return object;
   }

   public static OnlyBusinessMember instantiateBusinessMemberInvalid() {
      OnlyBusinessMember onlyBusinessMember = new OnlyBusinessMember();
      onlyBusinessMember.setValidableMock((DefaultValidableMock) instantiateInvalid());
      return onlyBusinessMember;
   }

   public static OnlyBusinessMember instantiateBusinessMemberNull() {
      return new OnlyBusinessMember();
   }

   public static CollectionBusinessMembers instantiateBusinessMemberCollection() {
      DefaultValidableMock validMock = (DefaultValidableMock) instantiateValid();
      DefaultValidableMock invalidMock = (DefaultValidableMock) instantiateInvalid();
      CollectionBusinessMembers collecBusinessMember = new CollectionBusinessMembers();
      collecBusinessMember.setValidableMockList(Arrays.asList(new DefaultValidableMock[]{validMock, invalidMock}));
      collecBusinessMember.setValidableMockSet(
            new HashSet<DefaultValidableMock>(collecBusinessMember.getValidableMockList()));
      collecBusinessMember.setValidableMockQueue(
            new LinkedTransferQueue<DefaultValidableMock>(collecBusinessMember.getValidableMockList()));
      return collecBusinessMember;
   }

   public static ArrayBusinessMember instantiateBusinessMemberArray() {
      DefaultValidableMock validMock = (DefaultValidableMock) instantiateValid();
      DefaultValidableMock invalidMock = (DefaultValidableMock) instantiateInvalid();
      ArrayBusinessMember arrayBusinessMember = new ArrayBusinessMember();
      arrayBusinessMember.setValidableMockArray(new DefaultValidableMock[]{validMock, invalidMock});
      return arrayBusinessMember;
   }

   public static IllegalBusinessRuleObject instantiateIllegalBusinessRule() {
      return new IllegalBusinessRuleObject();
   }

   public static IllegalBusinessMemberObject instantiateIllegalBusinessMember() {
      return new IllegalBusinessMemberObject();
   }

   public static ExceptionBusinessRuleObject instantiateExceptionBusinessRule() {
      return new ExceptionBusinessRuleObject();
   }

   public static ExceptionBusinessMemberObject instantiateExceptionBusinessMember() {
      return new ExceptionBusinessMemberObject();
   }

   public static BusinessObjectWithNoAnnotation instantiateBusinessObjectWithNoAnnotation() {
      BusinessObjectWithNoAnnotation object = new BusinessObjectWithNoAnnotation();
      object.setName("noAnnotation");
      object.setMandatoryAttribute("mandatory");
      object.getOneOrMoreAssociation().add("one association");
      return object;
   }

   public static MemberIsNotBO instantiateMemberIsNotBO() {
      return new MemberIsNotBO().setMember(
            new NotABusinessObject().setAttribute("attribute")
      );
   }

   public static ParentHasMember instantiateParentHasMember() {
      return (ParentHasMember) new ParentHasMember().setValidableMock(instantiateValid());
   }

   @BusinessObject(name = "validable-mock")
   public static class DefaultValidableMock {

      private String mandatoryAttribute;
      private String optionalAttribute;
      private List<String> oneOrMoreAssociation = new ArrayList<>();

      public String getMandatoryAttribute() {
         return mandatoryAttribute;
      }

      public DefaultValidableMock setMandatoryAttribute(String mandatoryAttribute) {
         this.mandatoryAttribute = mandatoryAttribute;
         return this;
      }

      public String getOptionalAttribute() {
         return optionalAttribute;
      }

      public DefaultValidableMock setOptionalAttribute(String optionalAttribute) {
         this.optionalAttribute = optionalAttribute;
         return this;
      }

      public List<String> getOneOrMoreAssociation() {
         return oneOrMoreAssociation;
      }

      public DefaultValidableMock setOneOrMoreAssociation(List<String> oneOrMoreAssociation) {
         this.oneOrMoreAssociation = oneOrMoreAssociation;
         return this;
      }

      @BusinessAssertion(id = "rule01", description = "mandatoryAttribute must be defined.")
      public boolean isMandatoryAttributeValid() {
         return DefaultAssertions.isDefined(mandatoryAttribute);
      }

      @BusinessAssertion(description = "optionalAttribute must be defined if present.")
      public boolean isOptionalAttributeValid() {
         return DefaultAssertions.isDefinedIfPresent(optionalAttribute);
      }

      @BusinessAssertion(description = "oneOrMoreAssociation must have at least one element.")
      public boolean isOneOrMoreAssociationValid() {
         return DefaultAssertions.hasOneOrMoreElements(oneOrMoreAssociation);
      }
   }

   @BusinessObject(name = "With-inheritance")
   public static class WithInheritance extends DefaultValidableMock {
      private String childAttribute;

      public String getChildAttribute() {
         return childAttribute;
      }

      public void setChildAttribute(String childAttribute) {
         this.childAttribute = childAttribute;
      }

      @BusinessAssertion(description = "Child attribute must be defined.")
      public boolean isSubtypeValid() {
         return DefaultAssertions.isDefined(childAttribute);
      }
   }

   public static class WithInheritanceButWithoutAnnotation extends DefaultValidableMock {

   }

   @BusinessObject
   public static class OnlyAttributes {
      private String stringAttribute;
      private Integer integerAttribute;

      public String getStringAttribute() {
         return stringAttribute;
      }

      public OnlyAttributes setStringAttribute(String stringAttribute) {
         this.stringAttribute = stringAttribute;
         return this;
      }

      public Integer getIntegerAttribute() {
         return integerAttribute;
      }

      public OnlyAttributes setIntegerAttribute(Integer integerAttribute) {
         this.integerAttribute = integerAttribute;
         return this;
      }

      @BusinessAssertion(id = "R-12", description = "stringAttribute must be defined.")
      public boolean isMandatoryAttributeValid() {
         return DefaultAssertions.isDefined(stringAttribute);
      }

      @BusinessAssertion(description = "integerAttribute must be defined and positive.")
      public boolean isOptionalAttributeValid() {
         return DefaultAssertions.isDefined(integerAttribute) && integerAttribute > 0;
      }
   }

   @BusinessObject
   public static class OnlyBusinessMember {

      private DefaultValidableMock validableMock;

      @BusinessMember(name = "my-only-member")
      public DefaultValidableMock getValidableMock() {
         return validableMock;
      }

      public OnlyBusinessMember setValidableMock(DefaultValidableMock validableMock) {
         this.validableMock = validableMock;
         return this;
      }
   }

   public static class ParentHasMember extends OnlyBusinessMember {
      @BusinessAssertion(description = "Member must be defined.")
      public boolean isAttributeDefined() {
         return DefaultAssertions.isDefined(getValidableMock());
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

   @BusinessObject(name = "recursive")
   public static class Recursive {
      private String name;
      private Recursive reference;

      public String getName() {
         return name;
      }

      public Recursive setName(String name) {
         this.name = name;
         return this;
      }

      @BusinessMember(name = "reference")
      public Recursive getReference() {
         return reference;
      }

      public Recursive setReference(Recursive reference) {
         this.reference = reference;
         return this;
      }

      @BusinessAssertion(description = "Name must be defined.")
      public boolean isNameDefined() {
         return DefaultAssertions.isDefined(name);
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
         return DefaultAssertions.isDefined(name);
      }
   }

   @BusinessObject(name = "illegalBusinessRule")
   public static class IllegalBusinessRuleObject {

      @BusinessAssertion(description = "An assertion must not take any parameter.")
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

      @BusinessAssertion(description = "To test InvocationException")
      public boolean isThrowingAnException() {
         throw new IllegalStateException();
      }
   }

   @BusinessObject
   public static class ExceptionBusinessMemberObject {

      @BusinessMember
      public DefaultValidableMock getMemberException() {
         throw new IllegalStateException();
      }
   }

   public static class BusinessObjectWithNoAnnotation extends DefaultValidableMock {

      private String name;

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      @BusinessAssertion(description = "The object name must be defined")
      public boolean isNameValid() {
         return name != null && !name.isEmpty();
      }

   }

   @BusinessObject
   public static class MemberIsNotBO {
      private NotABusinessObject member;

      @BusinessMember
      public NotABusinessObject getMember() {
         return member;
      }

      public MemberIsNotBO setMember(NotABusinessObject member) {
         this.member = member;
         return this;
      }
   }

   public static class NotABusinessObject {
      private String attribute;

      public String getAttribute() {
         return attribute;
      }

      public NotABusinessObject setAttribute(String attribute) {
         this.attribute = attribute;
         return this;
      }
   }
}
