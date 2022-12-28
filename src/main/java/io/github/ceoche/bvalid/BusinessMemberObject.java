package io.github.ceoche.bvalid;

import java.util.Objects;
import java.util.function.Function;

public class BusinessMemberObject<T,R> {

    private final String name;

    private final Function<T, ?> getter;

    private final BValidator<R> validator;

    public BusinessMemberObject(String name, Function<T, ?> getter, BValidator<R> validator) {
        this.name = name;
        this.getter = getter;
        this.validator = validator;
    }

    public String getName() {
        return name;
    }

    public Object getMemberValue(T object) {
        return getter.apply(object);
    }

    public  BValidator<R> getValidator() {
        return validator;
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
