package io.github.ceoche.bvalid;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionAndArrayTest {

   @Test
   void testCollectionOfBO() {
      List<BusinessObjectMocks.DefaultValidableMock> objects = new ArrayList<>();
      objects.add(BusinessObjectMocks.instantiateValid());
      objects.add(BusinessObjectMocks.instantiateValid());
      objects.add(BusinessObjectMocks.instantiateValid());

      List<BReport> results = buildObjectValidator(BusinessObjectMocks.DefaultValidableMock.class).validate(objects);

      assertFalse(results.isEmpty());
      for (int index = 0; index < results.size(); index++) {
         assertTrue(results.get(index).isValid());
         assertEquals("validable-mock[" + index + "]", results.get(index).getBusinessObjectName(),
               "Results' name of BusinessObjects in a collection should be incremented like an " +
                     "array.");
      }
   }

   @Test
   void testBusinessObjectCollectionMember() {
      BusinessObjectMocks.CollectionBusinessMembers object = BusinessObjectMocks.instantiateBusinessMemberCollection();
      BReport report = buildObjectValidator(BusinessObjectMocks.CollectionBusinessMembers.class).validate(object);
      assertFalse(report.isValid());
   }

   @Test
   void testArrayOfBO() {
      BusinessObjectMocks.DefaultValidableMock[] objects = new BusinessObjectMocks.DefaultValidableMock[]{
            BusinessObjectMocks.instantiateValid(),
            BusinessObjectMocks.instantiateValid()
      };
      List<BReport> results = buildObjectValidator(BusinessObjectMocks.DefaultValidableMock.class).validate(objects);
      assertFalse(results.isEmpty());
      for (BReport result : results) {
         assertTrue(result.isValid());
      }
   }

   @Test
   void testBusinessObjectArrayMember() {
      BusinessObjectMocks.ArrayBusinessMember object = BusinessObjectMocks.instantiateBusinessMemberArray();
      BReport report = buildObjectValidator(BusinessObjectMocks.ArrayBusinessMember.class).validate(object);
      assertFalse(report.isValid());
   }

   private <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
      return new AnnotationResolver<>(clazz).buildValidator();
   }

}
