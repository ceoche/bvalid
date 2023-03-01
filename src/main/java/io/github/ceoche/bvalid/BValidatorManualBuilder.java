package io.github.ceoche.bvalid;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class BValidatorManualBuilder<T> extends AbstractBValidatorBuilder<T> {

    private final Set<BusinessRuleObject<T>> rules = new LinkedHashSet<>();

    private final Set<BusinessMemberBuilder<T,?>> members = new LinkedHashSet<>();

    private final Set<Class<?>> implementations = new HashSet<>();

    private String businessObjectName = "";

    public BValidatorManualBuilder(Class<T> type){
        super(type);
    }

    @Override
    public Set<BusinessRuleObject<T>> getRules() {
        return rules;
    }

    @Override
    public Set<BusinessMemberBuilder<T, ?>> getMembers() {
        return members;
    }

    @Override
    public String getBusinessObjectName() {
        return businessObjectName;
    }

    public BValidatorManualBuilder<T> addRule(String id, Predicate<T> rule, String description){
        if(id == null || rule == null){
            throw new IllegalArgumentException("Id and rule must not be null");
        }
        rules.add(new BusinessRuleObject<>(id, rule, description));
        return this;
    }

    @SafeVarargs
    public final <R> BValidatorManualBuilder<T> addMember(String name, Function<T, ?> getter, BValidatorBuilder<? extends R>... bValidatorBuilders){
        if(name == null || getter == null || isThereNullBuilder(bValidatorBuilders)){
            throw new IllegalArgumentException("Name, getter and bValidatorBuilders must not be null");
        }
        members.add(new BusinessMemberBuilder<>(name, getter, bValidatorBuilders));
        return this;
    }

    public BValidatorManualBuilder<T> addAllMembers(Set<BusinessMemberBuilder<T, ?>> members){
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
