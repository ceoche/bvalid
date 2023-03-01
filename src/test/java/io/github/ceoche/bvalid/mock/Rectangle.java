package io.github.ceoche.bvalid.mock;

public class Rectangle extends Square {

    private int height;


    public int getHeight() {
        return height;
    }

    public Rectangle setHeight(int height) {
        this.height = height;
        return this;
    }

    @Override
    public Rectangle setName(String name) {
        super.setName(name);
        return this;
    }

    public boolean isHeightValid() {
        return height > 0;
    }

}
