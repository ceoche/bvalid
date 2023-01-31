package io.github.ceoche.bvalid;

import java.util.function.Function;

public class BusinessMemberBuilder <T,R>{

    private final String name;

    private final Function<T, ?> getter;

    private final BValidatorBuilder<R> validatorBuilder;


    public BusinessMemberBuilder(String name, Function<T, ?> getter, BValidatorBuilder<R> bValidatorBuilder) {
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

    public BValidatorBuilder<R> getValidatorBuilder() {
        return validatorBuilder;
    }


}
