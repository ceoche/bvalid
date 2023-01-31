package io.github.ceoche.bvalid;

import java.util.Set;

public interface BValidatorBuilder <T> {

    BValidator<T> build();

    boolean isEmpty();

    Set<BusinessMemberBuilder<T,?>> getMembers();
}
