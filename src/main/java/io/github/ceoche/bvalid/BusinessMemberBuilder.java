package io.github.ceoche.bvalid;

import java.util.function.Function;

/**
 * Utility class to hold builders of a member at runtime.
 * Class not intended to be used outside the library.
 * @param <T> the type of the business object to validate
 * @param <R> the type of the member to validate
 */
class BusinessMemberBuilder <T,R>{

    private final String name;

    private final Function<T, ?> getter;

    private final BValidatorBuilder<? extends R>[] validatorBuilder;


    @SafeVarargs
    BusinessMemberBuilder(String name, Function<T, ?> getter, BValidatorBuilder<? extends R> ...bValidatorBuilder) {
        this.name = name;
        this.getter = getter;
        this.validatorBuilder = bValidatorBuilder;
    }

    public String getName() {
        return name;
    }

    public Function<T, ?> getGetter() {
        return getter;
    }

    public BValidatorBuilder<? extends R>[] getValidatorBuilders() {
        return validatorBuilder;
    }


}
