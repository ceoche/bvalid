package io.github.ceoche.bvalid;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

class BusinessMemberObject<T,R> {

    private final String name;

    private final Function<T, ?> getter;

    private final Map<Class<? extends R>,BValidator<? extends R>> validators;

    BusinessMemberObject(String name, Function<T, ?> getter, Map<Class<? extends R>,BValidator<? extends R>> validators) {
        this.name = name;
        this.getter = getter;
        this.validators = validators;
    }

    String getName() {
        return name;
    }

    Object getMemberValue(T object) {
        return getter.apply(object);
    }

    void addValidator(Class<? extends R> clazz, BValidator<? extends R> validator) {
        this.validators.put(clazz, validator);

    }

    public Map<Class<? extends R>, BValidator<? extends R>> getValidators() {
        return validators;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessMemberObject)) return false;
        BusinessMemberObject<?, ?> that = (BusinessMemberObject<?, ?>) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
