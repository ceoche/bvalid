package io.github.ceoche.bvalid;

import java.util.function.Function;


/**
 * Represents a supplier of an actual value that is associated with a specific name.
 *
 * @param <T> the type of the input object from which the actual value is derived
 *
 * @param name the name associated with the supplier
 * @param actualValue a function that computes the actual value from an object of type T
 */
public record ActualValueSupplier<T> (String name, Function<T, ?> actualValue) {}
