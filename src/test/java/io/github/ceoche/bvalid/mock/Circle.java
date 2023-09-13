package io.github.ceoche.bvalid.mock;

public class Circle extends Shape {

    private  int radius;
    private  int ox;
    private  int oy;


    public int getRadius() {
        return radius;
    }

    public int getOx() {
        return ox;
    }

    public int getOy() {
        return oy;
    }

    @Override
    public Circle setName(String name) {
        super.setName(name);
        return this;
    }

    public Circle setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public Circle setOx(int ox) {
        this.ox = ox;
        return this;
    }

    public Circle setOy(int oy) {
        this.oy = oy;
        return this;
    }

    public boolean isRadiusValid() {
        return radius > 0;
    }

    public boolean isOxValid() {
        return ox > 0;
    }



}
