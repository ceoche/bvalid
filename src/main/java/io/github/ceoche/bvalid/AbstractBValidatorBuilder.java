package io.github.ceoche.bvalid;

import java.util.*;

abstract public class AbstractBValidatorBuilder<T> implements BValidatorBuilder<T> {

    protected Class<T> type;

    abstract public Set<BusinessRuleObject<T>> getRules();

    abstract public Set<BusinessMemberBuilder<T, ?>> getMembers();

    abstract public String getBusinessObjectName();

    public boolean isEmpty(){
        return getRules().isEmpty() && getMembers().isEmpty();
    }

    public AbstractBValidatorBuilder(Class<T> type) {
        this.type = type;
    }

    protected final BValidator<T> build(Map<AbstractBValidatorBuilder<?>,BValidator<?>> visitedBuilders){
        if(type == null){
            throw new IllegalStateException("Type is not set");
        }
        Set<BusinessMemberObject<T,?>> businessMemberObjects = new LinkedHashSet<>();
        BValidator<T> validator = new BValidator<>(getRules(), businessMemberObjects, getBusinessObjectName());
        visitedBuilders.put(this, validator);
        for (BusinessMemberBuilder<T,?> businessMemberBuilder : getMembers()) {
            if(!allBuildersAreEmpty(businessMemberBuilder.getValidatorBuilders())){
                AbstractBValidatorBuilder<?>[] subValidatorBuilders = Arrays.stream(businessMemberBuilder.getValidatorBuilders()).map(bValidatorBuilder -> (AbstractBValidatorBuilder<?>) bValidatorBuilder).toArray(AbstractBValidatorBuilder[]::new);
                BusinessMemberObject<T,?> businessMemberObject = new BusinessMemberObject<>(businessMemberBuilder.getName(), businessMemberBuilder.getGetter(), new HashMap<>());
                for(AbstractBValidatorBuilder<?> subValidatorBuilder : subValidatorBuilders){
                    if(!visitedBuilders.containsKey(subValidatorBuilder)){
                        businessMemberObject.addValidator(subValidatorBuilder.type,subValidatorBuilder.build(visitedBuilders));
                    }
                    else{
                        businessMemberObject.addValidator(subValidatorBuilder.type,visitedBuilders.get(subValidatorBuilder));
                    }
                }
                businessMemberObjects.add(businessMemberObject);

            }
            else{
                throw new IllegalStateException("All sub validators are empty");
            }
        }
        assertBuilderNotEmpty();
        return validator;
    }


    private void assertBuilderNotEmpty(){
        if(isEmpty()){
            throw new IllegalBusinessObjectException("Rules or members must be provided for a business object: " + getBusinessObjectName());
        }
    }

    private boolean allBuildersAreEmpty(BValidatorBuilder<?>[] bValidatorBuilders){
        for (BValidatorBuilder<?> bValidatorBuilder : bValidatorBuilders) {
            if(!bValidatorBuilder.isEmpty()){
                return false;
            }
        }
        return true;
    }



}
