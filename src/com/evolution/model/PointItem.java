package com.evolution.model;

/**
 * Interface representing any 2D point item that can be indexed within a QuadTree.
 */
public interface PointItem {
    /**
     * @return The X-coordinate in 2D simulation space.
     */
    double getX();

    /**
     * @return The Y-coordinate in 2D simulation space.
     */
    double getY();
}
