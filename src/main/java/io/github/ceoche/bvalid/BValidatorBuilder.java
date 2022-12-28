package io.github.ceoche.bvalid;

public interface BValidatorBuilder <T> {

    BValidator<T> build();

    boolean isEmpty();
}
