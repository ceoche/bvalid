package io.github.ceoche.bvalid;

import java.util.function.Function;

public record ActualValueSupplier<T> (String name, Function<T, ?> actualValue) {}
