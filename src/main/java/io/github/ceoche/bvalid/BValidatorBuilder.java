package io.github.ceoche.bvalid;

public interface BValidatorBuilder <T> {

    /**
     * Build the {@link BValidator} from the builder.
     * @return the {@link BValidator} built
     * @throws IllegalStateException if the type of the business object is not set
     * @throws IllegalBusinessObjectException if the builder is empty (i.e. no rules or members)
     */
    BValidator<T> build();

    /**
     * Check if the builder is empty (i.e. no rules or members).
     * @return true if the builder is empty, false otherwise
     */
    boolean isEmpty();

}
