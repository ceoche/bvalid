package io.github.ceoche.bvalid.mock;

public class Square extends Shape {

    private int side;



    public int getSide() {
        return side;
    }

    public Square setSide(int side) {
        this.side = side;
        return this;
    }

    @Override
    public Square setName(String name) {
        super.setName(name);
        return this;
    }

    public boolean isSideValid() {
        return side > 0;
    }
}
