package io.github.ceoche.bvalid;

import java.util.Objects;
import java.util.function.Predicate;

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
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
