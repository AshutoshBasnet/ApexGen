package com.evolution.spatial;

import com.evolution.model.PointItem;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * High-performance generic 2D QuadTree spatial partitioning data structure.
 *
 * Reduces spatial query and collision detection overhead from O(N^2) brute force
 * to O(N log N) build time and O(log N) query time.
 *
 * @param <T> Item type implementing PointItem
 */
public class QuadTree<T extends PointItem> {
    private final Rectangle2D.Double boundary;
    private final int capacity;
    private final int depth;
    private static final int MAX_DEPTH = 10;

    private final List<T> items;
    private boolean divided;

    private QuadTree<T> northWest;
    private QuadTree<T> northEast;
    private QuadTree<T> southWest;
    private QuadTree<T> southEast;

    /**
     * Constructs a QuadTree node with specified boundary and capacity.
     *
     * @param boundary Spatial 2D rectangular bounding box
     * @param capacity Maximum items stored before quadrant subdivision
     */
    public QuadTree(Rectangle2D.Double boundary, int capacity) {
        this(boundary, capacity, 0);
    }

    private QuadTree(Rectangle2D.Double boundary, int capacity, int depth) {
        this.boundary = boundary;
        this.capacity = Math.max(1, capacity);
        this.depth = depth;
        this.items = new ArrayList<>(this.capacity);
        this.divided = false;
    }

    /**
     * Inserts an item into the QuadTree.
     *
     * @param item Spatial item to insert
     * @return true if successfully inserted within bounds, false otherwise
     */
    public boolean insert(T item) {
        if (item == null) {
            return false;
        }

        // Boundary containment check
        if (!containsPoint(boundary, item.getX(), item.getY())) {
            return false;
        }

        // Store item in this leaf node if below capacity or at max recursion depth
        if (!divided && (items.size() < capacity || depth >= MAX_DEPTH)) {
            items.add(item);
            return true;
        }

        // Subdivide if not already divided
        if (!divided) {
            subdivide();
        }

        // Try inserting into quadrants
        if (northWest.insert(item)) return true;
        if (northEast.insert(item)) return true;
        if (southWest.insert(item)) return true;
        if (southEast.insert(item)) return true;

        // Fallback safety (e.g. edge boundary precision)
        items.add(item);
        return true;
    }

    /**
     * Subdivides this node into four child quadrants (NW, NE, SW, SE).
     */
    private void subdivide() {
        double x = boundary.x;
        double y = boundary.y;
        double halfW = boundary.width / 2.0;
        double halfH = boundary.height / 2.0;
        int nextDepth = depth + 1;

        northWest = new QuadTree<>(new Rectangle2D.Double(x, y, halfW, halfH), capacity, nextDepth);
        northEast = new QuadTree<>(new Rectangle2D.Double(x + halfW, y, halfW, halfH), capacity, nextDepth);
        southWest = new QuadTree<>(new Rectangle2D.Double(x, y + halfH, halfW, halfH), capacity, nextDepth);
        southEast = new QuadTree<>(new Rectangle2D.Double(x + halfW, y + halfH, halfW, halfH), capacity, nextDepth);

        divided = true;

        // Re-distribute existing items to children
        List<T> currentItems = new ArrayList<>(this.items);
        this.items.clear();
        for (T existingItem : currentItems) {
            boolean placed = northWest.insert(existingItem) ||
                             northEast.insert(existingItem) ||
                             southWest.insert(existingItem) ||
                             southEast.insert(existingItem);
            if (!placed) {
                this.items.add(existingItem);
            }
        }
    }

    /**
     * Performs a rectangular bounding-box range query.
     *
     * @param range Bounding box query region
     * @param found Accumulator list of found items
     */
    public void query(Rectangle2D.Double range, List<T> found) {
        if (range == null || found == null) {
            return;
        }

        // Fast prune: do not traverse if range does not intersect this node's boundary
        if (!boundary.intersects(range.x, range.y, range.width, range.height)) {
            return;
        }

        // Check local items
        for (T item : items) {
            if (containsPoint(range, item.getX(), item.getY())) {
                found.add(item);
            }
        }

        // Recursively query children if divided
        if (divided) {
            northWest.query(range, found);
            northEast.query(range, found);
            southWest.query(range, found);
            southEast.query(range, found);
        }
    }

    /**
     * Performs an exact circular neighborhood query (e.g. for vision sensory radius).
     *
     * @param cx     Center X of query circle
     * @param cy     Center Y of query circle
     * @param radius Query circle radius
     * @param found  Accumulator list for items within the circle
     */
    public void queryCircle(double cx, double cy, double radius, List<T> found) {
        if (found == null || radius <= 0) {
            return;
        }

        // Circle vs AABB intersection test
        double closestX = Math.max(boundary.x, Math.min(cx, boundary.x + boundary.width));
        double closestY = Math.max(boundary.y, Math.min(cy, boundary.y + boundary.height));
        double dx = cx - closestX;
        double dy = cy - closestY;

        if ((dx * dx + dy * dy) > (radius * radius)) {
            return; // Circle does not intersect this quadrant
        }

        // Check items in this node
        double radiusSq = radius * radius;
        for (T item : items) {
            double itemDx = item.getX() - cx;
            double itemDy = item.getY() - cy;
            if ((itemDx * itemDx + itemDy * itemDy) <= radiusSq) {
                found.add(item);
            }
        }

        // Recurse into children
        if (divided) {
            northWest.queryCircle(cx, cy, radius, found);
            northEast.queryCircle(cx, cy, radius, found);
            southWest.queryCircle(cx, cy, radius, found);
            southEast.queryCircle(cx, cy, radius, found);
        }
    }

    /**
     * Clears all items and collapses child partitions.
     */
    public void clear() {
        items.clear();
        if (divided) {
            northWest.clear();
            northEast.clear();
            southWest.clear();
            southEast.clear();
            northWest = null;
            northEast = null;
            southWest = null;
            southEast = null;
            divided = false;
        }
    }

    /**
     * Counts the total number of items stored in this subtree.
     */
    public int size() {
        int count = items.size();
        if (divided) {
            count += northWest.size();
            count += northEast.size();
            count += southWest.size();
            count += southEast.size();
        }
        return count;
    }

    /**
     * Helper to test if a point is within a rectangle (inclusive min, exclusive max).
     */
    private static boolean containsPoint(Rectangle2D.Double rect, double px, double py) {
        return px >= rect.x && px <= (rect.x + rect.width) &&
               py >= rect.y && py <= (rect.y + rect.height);
    }

    /**
     * Debug visualization helper to render QuadTree cell partition lines.
     *
     * @param g Graphics2D rendering context
     */
    public void renderGrid(Graphics2D g) {
        g.setColor(new Color(60, 120, 200, 45));
        g.drawRect((int) boundary.x, (int) boundary.y, (int) boundary.width, (int) boundary.height);

        if (divided) {
            northWest.renderGrid(g);
            northEast.renderGrid(g);
            southWest.renderGrid(g);
            southEast.renderGrid(g);
        }
    }

    public Rectangle2D.Double getBoundary() {
        return boundary;
    }

    public boolean isDivided() {
        return divided;
    }
}
