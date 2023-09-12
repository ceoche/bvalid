package io.github.ceoche.bvalid;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A business rule object is a predicate with an id and a description.
 * It is used to validate a business object with the manual builder.
 * It's the equivalent of a {@link BusinessMember} in annotation based validation.
 * @param <T> the type of the business object to validate
 */
public class BusinessRuleObject<T> {

    private final String id;

    private final String description;

    private final Predicate<T> rule;


    public BusinessRuleObject(String id, Predicate<T> rule, String description) {
        this.id = id;
        this.description = description;
        this.rule = rule;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Boolean apply(T object) {
        return rule.test(object);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BusinessRuleObject)) return false;
        BusinessRuleObject<?> that = (BusinessRuleObject<?>) o;

        return description.equals(that.description) &&
                rule.equals(that.rule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, rule);
    }
}
