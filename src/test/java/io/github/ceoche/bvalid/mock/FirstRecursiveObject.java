package io.github.ceoche.bvalid.mock;

public class FirstRecursiveObject {

    private String attr1;

    private FirstRecursiveObject firstRecursiveObject;

    private SecondRecursiveObject secondRecursiveObject;

    private Email email;


    public String getAttr1() {
        return attr1;
    }

    public FirstRecursiveObject setAttr1(String attr1) {
        this.attr1 = attr1;
        return this;
    }

    public FirstRecursiveObject getFirstRecursiveObject() {
        return firstRecursiveObject;
    }

    public FirstRecursiveObject setFirstRecursiveObject(FirstRecursiveObject firstRecursiveObject) {
        this.firstRecursiveObject = firstRecursiveObject;
        return this;
    }

    public Email getEmail() {
        return email;
    }

    public FirstRecursiveObject setEmail(Email email) {
        this.email = email;
        return this;
    }

    public SecondRecursiveObject getSecondRecursiveObject() {
        return secondRecursiveObject;
    }

    public FirstRecursiveObject setSecondRecursiveObject(SecondRecursiveObject secondRecursiveObject) {
        this.secondRecursiveObject = secondRecursiveObject;
        return this;
    }

    public boolean isAttr1Valid() {
        return attr1 != null && !attr1.isEmpty();
    }

    public boolean isEmailValid() {
        return email != null ;
    }

    public boolean isSecondRecursiveObjectValid() {
        return secondRecursiveObject != null;
    }

    public boolean isFirstRecursiveObjectValid() {
        return firstRecursiveObject == null || firstRecursiveObject.isAttr1Valid();
    }

}
