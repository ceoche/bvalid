package io.github.ceoche.bvalid;

import java.util.function.Supplier;

public interface ValidatorSupplier<T> extends Supplier<BValidator<T>> {

    BValidator<T> get();

}
