package io.github.ceoche.bvalid;

import io.github.ceoche.bvalid.mock.Address;
import io.github.ceoche.bvalid.mock.City;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static io.github.ceoche.bvalid.ManualInheritenceTest.getWithInheritanceBValidatorBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BValidatorBuilderTest {

   @Test
   void testBValidatorBuilder() {
      BValidatorBuilder<Address> builder = new BValidatorBuilder<>(Address.class)
            .setObjectName("address")
            .addAssertion("CityValid", Address::isCityValid, "City must not be null")
            .addAssertion("StreetValid", Address::isStreetValid, "Street must not be empty")
            .addMember("city", Address::getCity, new BValidatorBuilder<>(City.class)
                  .setObjectName("city")
                  .addAssertion("cityNameValid", City::isNamesValid, "City name must not be empty")
                  .addAssertion("cityZipcodeValid", City::isZipCodeValid, "City zipcode must be valid")
            );
      assertEquals(2, builder.getRulesCount());
      assertEquals(1, builder.getMembersCount());
      assertEquals(Address.class, builder.getType());
   }

   @Test
   void testNullAssertionBValidatorBuilder() {
      BValidatorBuilder<Address> builder = new BValidatorBuilder<>(Address.class)
            .setObjectName("address");
      assertThrows(IllegalArgumentException.class, () -> builder.addAssertion("CityValid", null, "City must not be null"));
   }

   @Test
   void testNullMemberBValidatorBuilder() {
      BValidatorBuilder<Address> builder = new BValidatorBuilder<>(Address.class)
            .setObjectName("address");
      assertThrows(IllegalArgumentException.class, () -> builder.addMember("CityValid", null));
   }

   @Test
   void test() {
      BValidatorBuilder<ObjectMocks.OnlyBusinessMember> builder = ManualSimpleTest.getOnlyBusinessMembersBValidatorBuilder();
      assertThrows(NoSuchElementException.class, () -> builder.addMemberSubTypes("wrong-member", getWithInheritanceBValidatorBuilder()));
   }
}
