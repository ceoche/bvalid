package io.github.ceoche.bvalid.mock;

import java.util.List;

public class CollectionRecursiveObject {

    private String collectionAttr;

    private CollectionRecursiveObject collectionRecursiveObject;

    private FirstRecursiveObject[] firstRecursiveObjects;

    private List<SecondRecursiveObject> secondRecursiveObjects;

    public String getCollectionAttr() {
        return collectionAttr;
    }

    public CollectionRecursiveObject setCollectionAttr(String collectionAttr) {
        this.collectionAttr = collectionAttr;
        return this;
    }

    public CollectionRecursiveObject getCollectionRecursiveObject() {
        return collectionRecursiveObject;
    }

    public CollectionRecursiveObject setCollectionRecursiveObject(CollectionRecursiveObject collectionRecursiveObject) {
        this.collectionRecursiveObject = collectionRecursiveObject;
        return this;
    }

    public FirstRecursiveObject[] getFirstRecursiveObjects() {
        return firstRecursiveObjects;
    }

    public CollectionRecursiveObject setFirstRecursiveObjects(FirstRecursiveObject[] firstRecursiveObjects) {
        this.firstRecursiveObjects = firstRecursiveObjects;
        return this;
    }

    public List<SecondRecursiveObject> getSecondRecursiveObjects() {
        return secondRecursiveObjects;
    }

    public CollectionRecursiveObject setSecondRecursiveObjects(List<SecondRecursiveObject> secondRecursiveObjects) {
        this.secondRecursiveObjects = secondRecursiveObjects;
        return this;
    }

    public boolean isCollectionAttrValid() {
        return collectionAttr != null && !collectionAttr.isEmpty();
    }

    public boolean isCollectionRecursiveObjectValid() {
        return collectionRecursiveObject == null || collectionRecursiveObject.isCollectionAttrValid();
    }

    public boolean isFirstRecursiveObjectsValid() {
        return firstRecursiveObjects != null && firstRecursiveObjects.length > 0;
    }

    public boolean isSecondRecursiveObjectsValid() {
        return secondRecursiveObjects != null && secondRecursiveObjects.size() > 0;
    }
}
