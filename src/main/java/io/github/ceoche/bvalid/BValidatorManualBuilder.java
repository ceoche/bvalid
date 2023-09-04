package io.github.ceoche.bvalid;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Build a {@link BValidator} programmatically using the {@link BValidatorManualBuilder}.
 * @param <T> the type of the business object to validate
 */
public class BValidatorManualBuilder<T> extends AbstractBValidatorBuilder<T> {

    private final Set<BusinessRuleObject<T>> rules = new LinkedHashSet<>();

    private final Set<BusinessMemberBuilder<T,?>> members = new LinkedHashSet<>();


    private String businessObjectName = "";

    public BValidatorManualBuilder(Class<T> type){
        super(type);
    }

    public BValidatorManualBuilder(BValidatorManualBuilder<? super T> builder, Class<T> type){
        super(type);
        builder.rules.forEach(rule -> rules.add((BusinessRuleObject<T>) rule));
        builder.members.forEach(member -> members.add((BusinessMemberBuilder<T, ?>) member));
    }


    @Override
    public Set<BusinessRuleObject<T>> getRules() {
        return rules;
    }

    @Override
    Set<BusinessMemberBuilder<T, ?>> getMembers() {
        return members;
    }

    @Override
    public String getBusinessObjectName() {
        return businessObjectName;
    }

    /**
     * Add a rule in form of Java Predicate {@link Predicate<T>} to the validator.
     * @param id the specification id of the rule
     * @param rule the rule to add
     * @param description the description of the rule
     * @return the builder
     * @throws IllegalArgumentException if the rule is null
     */
    public BValidatorManualBuilder<T> addRule(String id, Predicate<T> rule, String description){
        if(rule == null){
            throw new IllegalArgumentException("Id and rule must not be null");
        }
        rules.add(new BusinessRuleObject<>(id, rule, description));
        return this;
    }

    /**
     * Add a rule in form of Java Predicate {@link Predicate<T>} to the validator.
     * @param rule the rule to add
     * @param description the description of the rule
     * @return the builder
     * @throws IllegalArgumentException if the rule is null
     */
    public BValidatorManualBuilder<T> addRule(Predicate<T> rule, String description){
        addRule("", rule, description);
        return this;
    }


    /**
     * Add a member to the validator.
     * @param name the name of the field
     * @param getter the getter of the field in form of a Java Function {@link Function}
     * @param bValidatorBuilders All the validators builders of the possible subtypes of the field
     * @return the builder
     * @param <R> the type of the field
     * @throws IllegalArgumentException if the name, getter or bValidatorBuilders are null
     */
    @SafeVarargs
    public final <R> BValidatorManualBuilder<T> addMember(String name, Function<T, ?> getter, BValidatorBuilder<? extends R>... bValidatorBuilders){
        if(name == null || getter == null || isThereNullBuilder(bValidatorBuilders)){
            throw new IllegalArgumentException("Name, getter and bValidatorBuilders must not be null");
        }
        members.add(new BusinessMemberBuilder<>(name, getter, bValidatorBuilders));
        return this;
    }


    BValidatorManualBuilder<T> addAllMembers(Set<BusinessMemberBuilder<T, ?>> members){
        this.members.addAll(members);
        return this;
    }

    public BValidatorManualBuilder<T> addAllRules(Set<BusinessRuleObject<T>> rules){
        this.rules.addAll(rules);
        return this;
    }

    public BValidatorManualBuilder<T> setBusinessObjectName(String businessObjectName){
        this.businessObjectName = businessObjectName;
        return this;
    }

    public int getRulesCount(){
        return rules.size();
    }

    public int getMembersCount(){
        return members.size();
    }

    @Override
    public BValidator<T> build(){
        Map<AbstractBValidatorBuilder<?>,BValidator<?>> visitedBuilders = new HashMap<>();
        return build(visitedBuilders);
    }


    private boolean isThereNullBuilder(BValidatorBuilder<?>... bValidatorBuilders){
        for (BValidatorBuilder<?> bValidatorBuilder : bValidatorBuilders) {
            if(bValidatorBuilder == null){
                return true;
            }
        }
        return false;
    }

}
