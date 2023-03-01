package io.github.ceoche.bvalid.mock;

public abstract class Shape {

    private String name;

    public String getName() {
        return name;
    }

    public Shape setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isNameValid() {
        return name != null && !name.isEmpty();
    }

    public void draw() {
        System.out.println("Drawing a shape");
    }
}
