package io.github.ceoche.bvalid.mock;

public class SecondRecursiveObject {

    private  String attr2;

    private FirstRecursiveObject firstRecursiveObject;

    public SecondRecursiveObject() {

    }

    public String getAttr2() {
        return attr2;
    }

    public SecondRecursiveObject setAttr2(String attr2) {
        this.attr2 = attr2;
        return this;
    }

    public FirstRecursiveObject getFirstRecursiveObject() {
        return firstRecursiveObject;
    }

    public SecondRecursiveObject setFirstRecursiveObject(FirstRecursiveObject firstRecursiveObject) {
        this.firstRecursiveObject = firstRecursiveObject;
        return this;
    }

    public boolean isAttr2Valid() {
        return attr2 != null && !attr2.isEmpty();
    }

    public boolean isFirstRecursiveObjectValid() {
        return firstRecursiveObject != null;
    }
}
