package io.github.ceoche.bvalid;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Build a {@link BValidator} from a {@link BusinessObject} annotated class
 * using the {@link BValidatorManualBuilder}.
 *
 * @param <T>
 * @author a.achkari
 */
public class BValidatorAnnotationBuilder<T> implements BValidatorBuilder<T> {

    private final List<BusinessRuleObject<T>> rules;

    private final List<BusinessMemberBuilder<T,?>> members;

    private String businessObjectName = "";

    public BValidatorAnnotationBuilder(Class<T> clazz) {
        BusinessObject businessObject = clazz.getAnnotation(BusinessObject.class);
        if (businessObject != null) {
            businessObjectName = businessObject.name();
            if(businessObjectName.isEmpty()) {
                businessObjectName = clazz.getSimpleName();
            }
        }
        Class<T> assertedClass = (Class<T>) assertBusinessObjectClass(clazz);
        this.rules = getRules(assertedClass);
        this.members = getMembers(assertedClass);
    }


    public BValidatorAnnotationBuilder<T> setBusinessObjectName(String businessObjectName){
        this.businessObjectName = businessObjectName;
        return this;
    }

    @Override
    public BValidator<T> build(){
        return new BValidatorManualBuilder<T>()
                .addAllMembers(members)
                .addAllRules(rules)
                .setBusinessObjectName(businessObjectName)
                .build();
    }

    @Override
    public boolean isEmpty() {
        return rules.isEmpty() && members.isEmpty();
    }

    private List<BusinessRuleObject<T>> getRules(Class<T> clazz) {
        List<BusinessRuleObject<T>> rulesResult = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if(method.isAnnotationPresent(BusinessRule.class)){
                BusinessRule businessRule = method.getAnnotation(BusinessRule.class);
                String id = businessRule.id();
                if(id.isEmpty() || id.isBlank()){
                    id = getRuleIdFromMethodName(method.getName());
                }
                rulesResult.add(new BusinessRuleObject<>(id, getPredicate(method), businessRule.description()));
            }
        }
        return rulesResult;
    }

    private List<BusinessMemberBuilder<T,?>> getMembers(Class<T> clazz) {
        List<BusinessMemberBuilder<T,?>> memberBuilderList = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if(method.isAnnotationPresent(BusinessMember.class)){
                BusinessMember businessMember = method.getAnnotation(BusinessMember.class);
                memberBuilderList.add(new BusinessMemberBuilder<>(businessMember.name(), getFunction(method), getValidatorBuilderSupplier(method)));
            }
        }
        return memberBuilderList;
    }

    private Predicate<T> getPredicate(Method method) throws InvocationException {
        return object -> {
            try {
                return (boolean) method.invoke(object);
            } catch (Exception e) {
                throw new InvocationException(e.getCause());
            }
        };
    }

    private Function<T, ?> getFunction(Method method) throws InvocationException {
        return object -> {
            try {
                return method.invoke(object);
            } catch (Exception e) {
                throw new InvocationException(e);
            }
        };
    }

    private BValidatorBuilder<?> getValidatorBuilderSupplier(Method method) throws InvocationException {
        Class<?> clazz = getRealType(method);
        return new BValidatorAnnotationBuilder<>(clazz);
    }

    private Class<?> getRealType(Method method) {
        Class<?> clazz = method.getReturnType();
        if (clazz.isArray()) {
            return clazz.getComponentType();
        } else if (Collection.class.isAssignableFrom(clazz)) {
            return getGenericTypeParameter(method);
        } else {
            return clazz;
        }
    }

    private String getRuleIdFromMethodName(String methodName){
        return methodName.replace("is", "");
    }


    private Class<?> getGenericTypeParameter(Method method) {
        String genericType = method.getGenericReturnType().getTypeName();
        if(genericType.contains("<") && genericType.contains(">")){
            String className = genericType.substring(genericType.indexOf("<") + 1, genericType.indexOf(">"));
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Cannot find generic type for method " + method.getName());
    }



    private Class<?> assertBusinessObjectClass(Class<?> clazz) {
        if (!Object.class.equals(clazz) && (isBusinessObject(clazz) || hasASuperClassBusinessObject(clazz.getSuperclass()))) {
            return clazz;
        } else {
            throw new IllegalBusinessObjectException("The object's class " + clazz.getCanonicalName()
                    + " is not annotated with @BusinessObject.");
        }
    }

    private boolean isBusinessObject(Class<?> clazz) {
        return clazz.isAnnotationPresent(BusinessObject.class);
    }

    private boolean hasASuperClassBusinessObject(Class<?> superClass) {
        if (superClass == null || isOnTopClassHierarchy(superClass)) {
            return false;
        } else {
            if (isBusinessObject(superClass)) {
                return true;
            } else {
                return hasASuperClassBusinessObject(superClass.getSuperclass());
            }
        }
    }

    private boolean isOnTopClassHierarchy(Class<?> superClass) {
        return superClass.equals(Object.class);
    }


}
