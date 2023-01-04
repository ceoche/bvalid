package io.github.ceoche.bvalid;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class BValidatorManualBuilder<T> implements BValidatorBuilder<T> {

    private final Set<BusinessRuleObject<T>> rules = new HashSet<>();

    private final Set<BusinessMemberBuilder<T,?>> members = new HashSet<>();


    private String businessObjectName = "";

    public BValidatorManualBuilder<T> addRule(String id, Predicate<T> rule, String description){
        if(id == null || rule == null){
            throw new IllegalArgumentException("Id and rule must not be null");
        }
        rules.add(new BusinessRuleObject<>(id, rule, description));
        return this;
    }

    public <R> BValidatorManualBuilder<T> addMember(String name, Function<T, ?> getter, BValidatorBuilder<R> bValidatorBuilder){
        if(name == null || getter == null || bValidatorBuilder == null){
            throw new IllegalArgumentException("Name, getter and bValidatorBuilder must not be null");
        }
        members.add(new BusinessMemberBuilder<>(name, getter, bValidatorBuilder));
        return this;
    }

    public BValidatorManualBuilder<T> addAllMembers(Collection<BusinessMemberBuilder<T,?>> members){
        this.members.addAll(members);
        return this;
    }

    public BValidatorManualBuilder<T> addAllRules(Collection<BusinessRuleObject<T>> rules){
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
        Set<BusinessMemberObject<T,?>> businessMemberObjects = new HashSet<>();
        for (BusinessMemberBuilder<T,?> businessMemberBuilder : members) {
            if(!businessMemberBuilder.getValidatorBuilder().isEmpty()){
                businessMemberObjects.add(new BusinessMemberObject<>(businessMemberBuilder.getName(),
                        businessMemberBuilder.getGetter(), businessMemberBuilder.getValidatorBuilder().build()));
            }
        }
        return new BValidator<>(rules, businessMemberObjects, businessObjectName);
    }

   @Override
   public boolean isEmpty() {
        return rules.isEmpty() && members.isEmpty();
    }
}
