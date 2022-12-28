package io.github.ceoche.bvalid;

import java.util.function.Supplier;

public interface ValidatorBuilderSupplier<T> extends Supplier<BValidatorBuilder<T>> {

    BValidatorBuilder<T> get();

}
