package io.github.ceoche.bvalid;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ManualMapTest {

    private final ManualSimpleTest manualSimpleTest = new ManualSimpleTest();

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testMapMemberWithNonStringKeyIsRejected() {
        ObjectMocks.MapBusinessMember object = ObjectMocks.instantiateMapBusinessMember();
        Map rawMap = object.getValidableMockMap();
        rawMap.clear();
        rawMap.put(1, ObjectMocks.instantiateValid());
        BValidator<ObjectMocks.MapBusinessMember> validator = buildObjectValidator(ObjectMocks.MapBusinessMember.class);

        assertThrows(IllegalBusinessObjectException.class,
                () -> validator.validate(object));
    }

    @Test
    void testBusinessObjectValidMapMember() {
        ObjectMocks.MapBusinessMember object = ObjectMocks.instantiateMapBusinessMember();
        BReport report = buildObjectValidator(ObjectMocks.MapBusinessMember.class).validate(object);
        assertTrue(report.isValid());
    }

    @Test
    void testBusinessObjectInvalidMapMember() {
        ObjectMocks.MapBusinessMember object = ObjectMocks.instantiateMapBusinessMember();
        object.addValidableMockMap("invalid", ObjectMocks.instantiateInvalid());
        BReport report = buildObjectValidator(ObjectMocks.MapBusinessMember.class).validate(object);
        assertFalse(report.isValid());
    }

    @Test
    void testBusinessMapValidation() {
        Map<Object, ObjectMocks.DefaultValidableMock> objects = new LinkedHashMap<>();
        objects.put("first", ObjectMocks.instantiateValid());
        objects.put("second", ObjectMocks.instantiateValid());
        objects.put("third", ObjectMocks.instantiateValid());
        List<BReport> results= buildObjectValidator(ObjectMocks.DefaultValidableMock.class).validate(objects);
        assertFalse(results.isEmpty());
        for (int index = 0; index < results.size(); index++) {
            assertTrue(results.get(index).isValid());
            String key = String.valueOf(objects.keySet().stream().toList().get(index));
            assertEquals("validable-mock[" + key + "]", results.get(index).getObjectName(),
                    "Results' name of BusinessObjects in a collection should be incremented like an " +
                            "array.");
        }
    }

    @SuppressWarnings("unchecked")
    protected <R> BValidator<R> buildObjectValidator(Class<R> clazz) {
        if (clazz.equals(ObjectMocks.MapBusinessMember.class)) {
            return (BValidator<R>) getMapBusinessMemberBValidatorBuilder().build();
        } else {
            return manualSimpleTest.buildObjectValidator(clazz);
        }
    }

    private static BValidatorBuilder<ObjectMocks.MapBusinessMember> getMapBusinessMemberBValidatorBuilder() {
        return new BValidatorBuilder<>(ObjectMocks.MapBusinessMember.class)
                .addMember("map", ObjectMocks.MapBusinessMember::getValidableMockMap,
                        ManualSimpleTest.getDefaultValidableMockBValidatorBuilder());
    }
}
