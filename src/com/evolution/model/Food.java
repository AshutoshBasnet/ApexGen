package com.evolution.model;

/**
 * Represents a food particle in the simulation arena.
 * Implements PointItem for QuadTree spatial indexing.
 */
public class Food implements PointItem {
    private final double x;
    private final double y;
    private final double energyValue;
    private final double radius;
    private boolean eaten;

    /**
     * Constructs a Food item.
     *
     * @param x           X position in the arena
     * @param y           Y position in the arena
     * @param energyValue Energy replenished to a creature upon consumption
     */
    public Food(double x, double y, double energyValue) {
        this.x = x;
        this.y = y;
        this.energyValue = energyValue;
        this.radius = 3.5;
        this.eaten = false;
    }

    /**
     * Constructs a Food item with default 40.0 energy value.
     */
    public Food(double x, double y) {
        this(x, y, 40.0);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    public double getEnergyValue() {
        return energyValue;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isEaten() {
        return eaten;
    }

    public void setEaten(boolean eaten) {
        this.eaten = eaten;
    }
}
