package com.example.leahalpert.setsolver;

/**
 * Created by eli on 12/20/15.
 */
public class Card {

    Shape shape;
    Shading shading;
    Count count;
    Color color;
    int id;

    public Card(Shape shape, Shading shading, Count count, Color color, int id) {
        this.shape = shape;
        this.shading = shading;
        this.count = count;
        this.color = color;
        this.id = id;
    }
    public enum Shape {
        OVAL,
        DIAMOND,
        SQUIGGLE
    }
    public enum Shading {
        SOLID,
        STRIPED,
        OPEN
    }
    public enum Count {
        ONE,
        TWO,
        THREE
    }

    public enum Color {
        RED,
        GREEN,
        PURPLE
    }
}

