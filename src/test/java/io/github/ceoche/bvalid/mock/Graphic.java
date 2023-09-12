package io.github.ceoche.bvalid.mock;

import java.util.ArrayList;
import java.util.List;

public class Graphic {

    private final List<Shape> shapeList;

    private Shape[] shapeArray;

    private Square squareOrRectangle;

    private Shape circle;

    private Graphic innerGraphic;

    private String name;

    public Graphic() {
        this.shapeList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Graphic setName(String name) {
        this.name = name;
        return this;
    }

    public List<Shape> getShapeList() {
        return shapeList;
    }

    public Graphic addShapeToList(Shape shape) {
        shapeList.add(shape);
        return this;
    }

    public Shape[] getShapeArray() {
        return shapeArray;
    }

    public Graphic setShapeArray(Shape[] shapeArray) {
        this.shapeArray = shapeArray;
        return this;
    }

    public Graphic getInnerGraphic() {
        return innerGraphic;
    }

    public Graphic setInnerGraphic(Graphic innerGraphic) {
        this.innerGraphic = innerGraphic;
        return this;
    }

    public Square getSquareOrRectangle() {
        return squareOrRectangle;
    }

    public Graphic setSquareOrRectangle(Square squareOrRectangle) {
        this.squareOrRectangle = squareOrRectangle;
        return this;
    }

    public Shape getCircle() {
        return circle;
    }

    public Graphic setCircle(Shape circle) {
        this.circle = circle;
        return this;
    }


    public boolean isNameValid() {
        return name != null && !name.isEmpty();
    }

    public boolean isShapeListValid() {
        return !shapeList.isEmpty();
    }

    public boolean isShapeArrayValid() {
        return shapeArray == null || shapeArray.length > 0;
    }

    public boolean isInnerGraphicValid() {
        return innerGraphic != null;
    }

    public boolean isSquareOrRectangleValid() {
        return squareOrRectangle != null;
    }
}
