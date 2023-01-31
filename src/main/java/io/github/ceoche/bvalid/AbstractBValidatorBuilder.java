package io.github.ceoche.bvalid;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

abstract public class AbstractBValidatorBuilder<T> implements BValidatorBuilder<T> {

    abstract public Set<BusinessRuleObject<T>> getRules();

    abstract public Set<BusinessMemberBuilder<T, ?>> getMembers();

    abstract public String getBusinessObjectName();

    public boolean isEmpty(){
        return getRules().isEmpty() && getMembers().isEmpty();
    }

    protected final BValidator<T> build(Map<AbstractBValidatorBuilder<?>,BValidator<?>> visitedBuilders){
        Set<BusinessMemberObject<T,?>> businessMemberObjects = new LinkedHashSet<>();
        BValidator<T> validator = new BValidator<>(getRules(), businessMemberObjects, getBusinessObjectName());
        visitedBuilders.put(this, validator);
        for (BusinessMemberBuilder<T,?> businessMemberBuilder : getMembers()) {
            if(!businessMemberBuilder.getValidatorBuilder().isEmpty()){
                AbstractBValidatorBuilder<?> subValidatorBuilder = (AbstractBValidatorBuilder<?>) businessMemberBuilder.getValidatorBuilder();
                if(!visitedBuilders.containsKey(subValidatorBuilder)){
                    businessMemberObjects.add(new BusinessMemberObject<>(businessMemberBuilder.getName(),
                            businessMemberBuilder.getGetter(),subValidatorBuilder.build(visitedBuilders)));
                }
                else{
                    businessMemberObjects.add(new BusinessMemberObject<>(
                            businessMemberBuilder.getName(),
                            businessMemberBuilder.getGetter(),
                            visitedBuilders.get(subValidatorBuilder)
                    ));
                }
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
}
