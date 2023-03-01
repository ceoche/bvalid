package io.github.ceoche.bvalid;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class BusinessMemberObject<T,R> {

    private final String name;

    private final Function<T, ?> getter;

    private final Map<Class<?>,BValidator<? extends R>> validators;

    public BusinessMemberObject(String name, Function<T, ?> getter, Map<Class<?>,BValidator<? extends R>> validators) {
        this.name = name;
        this.getter = getter;
        this.validators = validators;
    }

    public String getName() {
        return name;
    }

    public Object getMemberValue(T object) {
        return getter.apply(object);
    }

    public void addValidator(Class<?> clazz, BValidator<?> validator) {
        this.validators.put(clazz, (BValidator<? extends R>) validator);

    }

    public Map<Class<?>, BValidator<? extends R>> getValidators() {
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
