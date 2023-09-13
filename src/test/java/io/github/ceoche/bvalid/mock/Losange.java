package io.github.ceoche.bvalid.mock;

public class Losange extends Rectangle{


    private int diagonal;


    @Override
    public Losange setName(String name) {
        super.setName(name);
        return this;
    }

    public Losange setDiagonal(int diagonal) {
        this.diagonal = diagonal;
        return this;
    }
}
