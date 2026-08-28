package com.evolution.ui;

import com.evolution.engine.EvolutionEngine;
import com.evolution.model.Creature;
import com.evolution.model.Food;
import com.evolution.model.Genome;
import com.evolution.spatial.QuadTree;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * High-performance double-buffered visual viewport for the evolution arena.
 * Renders creatures, food particles, and QuadTree spatial partitions.
 */
public class SimulationPanel extends JPanel {
    private final EvolutionEngine engine;

    // Visual Toggles
    private boolean showQuadTree = false;
    private boolean showEnergyBars = true;
    private boolean showTrajectories = true;

    // Selection / Inspector
    private Creature selectedCreature = null;

    // Dark aesthetic color palette
    private static final Color BG_COLOR = new Color(14, 18, 26);
    private static final Color GRID_COLOR = new Color(28, 36, 52);
    private static final Color ARENA_BORDER_COLOR = new Color(45, 60, 85);
    private static final Color FOOD_COLOR = new Color(64, 230, 120);
    private static final Color FOOD_GLOW = new Color(64, 230, 120, 50);

    public SimulationPanel(EvolutionEngine engine) {
        this.engine = engine;
        setBackground(BG_COLOR);
        setDoubleBuffered(true);

        // Click listener to inspect individual creatures
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    private void handleMouseClick(int mx, int my) {
        List<Creature> creatures = engine.getCreatures();
        Creature clicked = null;
        double minDistSq = Double.MAX_VALUE;

        for (Creature c : creatures) {
            double dx = c.getX() - mx;
            double dy = c.getY() - my;
            double distSq = dx * dx + dy * dy;
            double radius = c.getPhysicalRadius() + 6.0;

            if (distSq <= radius * radius && distSq < minDistSq) {
                minDistSq = distSq;
                clicked = c;
            }
        }

        selectedCreature = clicked;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Enable Anti-Aliasing for crisp geometric rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        // 1. Draw subtle background grid
        drawBackgroundGrid(g2, w, h);

        // 2. Draw QuadTree spatial partition grid (if enabled)
        if (showQuadTree) {
            QuadTree<Food> foodTree = engine.getFoodQuadTree();
            if (foodTree != null) {
                foodTree.renderGrid(g2);
            }
        }

        // 3. Render Food Particles
        List<Food> foods = engine.getFoods();
        for (Food food : foods) {
            if (!food.isEaten()) {
                drawFood(g2, food);
            }
        }

        // 4. Render Creatures
        List<Creature> creatures = engine.getCreatures();
        for (Creature creature : creatures) {
            if (creature.isAlive()) {
                drawCreature(g2, creature);
            }
        }

        // 5. Draw Selected Creature Highlight & Tooltip
        if (selectedCreature != null && selectedCreature.isAlive()) {
            drawSelectionHighlight(g2, selectedCreature);
        }

        // 6. Draw arena border
        g2.setColor(ARENA_BORDER_COLOR);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRect(1, 1, w - 2, h - 2);
    }

    private void drawBackgroundGrid(Graphics2D g2, int w, int h) {
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(1.0f));
        int gridSize = 50;
        for (int x = gridSize; x < w; x += gridSize) {
            g2.drawLine(x, 0, x, h);
        }
        for (int y = gridSize; y < h; y += gridSize) {
            g2.drawLine(0, y, w, y);
        }
    }

    private void drawFood(Graphics2D g2, Food food) {
        int fx = (int) food.getX();
        int fy = (int) food.getY();
        int r = (int) food.getRadius();

        // Outer glow
        g2.setColor(FOOD_GLOW);
        g2.fillOval(fx - r - 3, fy - r - 3, (r + 3) * 2, (r + 3) * 2);

        // Core
        g2.setColor(FOOD_COLOR);
        g2.fillOval(fx - r, fy - r, r * 2, r * 2);
    }

    private void drawCreature(Graphics2D g2, Creature c) {
        double x = c.getX();
        double y = c.getY();
        double r = c.getPhysicalRadius();
        Genome genome = c.getGenome();

        // 1. Trajectory Heading Line
        if (showTrajectories) {
            g2.setColor(new Color(255, 255, 255, 70));
            double headingDist = r + 8.0;
            g2.drawLine((int) x, (int) y, (int) (x + c.getVx() * 4.0), (int) (y + c.getVy() * 4.0));
        }

        // 2. Dynamic Biological Trait Coloration:
        // Strength -> Red channel
        // Speed    -> Blue channel
        // Size     -> Green channel mix
        int red = (int) (50 + (genome.getStrength() / 100.0) * 205);
        int green = (int) (40 + (genome.getSize() / 100.0) * 160);
        int blue = (int) (60 + (genome.getSpeed() / 100.0) * 195);
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        Color creatureBodyColor = new Color(red, green, blue);

        // Body with gradient depth
        Point2D center = new Point2D.Double(x - r * 0.3, y - r * 0.3);
        float gradientRadius = (float) (r * 1.4);
        RadialGradientPaint gradient = new RadialGradientPaint(
            center,
            Math.max(1.0f, gradientRadius),
            new float[]{0.0f, 1.0f},
            new Color[]{creatureBodyColor.brighter(), creatureBodyColor.darker()}
        );
        g2.setPaint(gradient);
        g2.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));

        // Border outline
        g2.setColor(new Color(240, 245, 255, 200));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));

        // 4. Directional Eye / Pupil
        double angle = Math.atan2(c.getVy(), c.getVx());
        double eyeOffset = r * 0.55;
        double eyeX = x + Math.cos(angle) * eyeOffset;
        double eyeY = y + Math.sin(angle) * eyeOffset;
        double eyeR = Math.max(2.0, r * 0.3);

        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(eyeX - eyeR, eyeY - eyeR, eyeR * 2, eyeR * 2));

        double pupilR = eyeR * 0.5;
        double pupilX = eyeX + Math.cos(angle) * (eyeR * 0.4);
        double pupilY = eyeY + Math.sin(angle) * (eyeR * 0.4);
        g2.setColor(new Color(20, 20, 25));
        g2.fill(new Ellipse2D.Double(pupilX - pupilR, pupilY - pupilR, pupilR * 2, pupilR * 2));

        // 5. Energy Mini-Bar
        if (showEnergyBars) {
            double barW = Math.max(16.0, r * 2.2);
            double barH = 3.0;
            double barX = x - barW / 2.0;
            double barY = y - r - 6.0;

            // Bar background
            g2.setColor(new Color(20, 20, 25, 180));
            g2.fillRect((int) barX, (int) barY, (int) barW, (int) barH);

            // Bar fill
            double energyPct = c.getEnergyPercentage();
            Color barColor = energyPct > 0.5 ? new Color(50, 220, 90) :
                             energyPct > 0.25 ? new Color(235, 190, 40) :
                             new Color(235, 60, 50);
            g2.setColor(barColor);
            g2.fillRect((int) barX, (int) barY, (int) (barW * energyPct), (int) barH);
        }
    }

    private void drawSelectionHighlight(Graphics2D g2, Creature c) {
        double x = c.getX();
        double y = c.getY();
        double r = c.getPhysicalRadius();

        // Pulsing golden ring
        g2.setColor(new Color(255, 215, 0, 220));
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(new Ellipse2D.Double(x - r - 4, y - r - 4, (r + 4) * 2, (r + 4) * 2));

        // Tooltip Info card near creature
        int tipX = (int) (x + r + 10);
        int tipY = (int) (y - 35);
        if (tipX + 160 > getWidth()) tipX = (int) (x - r - 170);
        if (tipY < 10) tipY = 10;

        g2.setColor(new Color(18, 24, 38, 225));
        g2.fillRoundRect(tipX, tipY, 155, 80, 8, 8);
        g2.setColor(new Color(255, 215, 0, 180));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(tipX, tipY, 155, 80, 8, 8);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString(String.format("Fitness: %.1f", c.getFitness()), tipX + 8, tipY + 14);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString(String.format("Food: %d | Age: %d", c.getFoodEaten(), c.getSurvivalTicks()), tipX + 8, tipY + 28);
        g2.drawString(String.format("Energy: %.1f / %.1f", c.getEnergy(), c.getMaxEnergy()), tipX + 8, tipY + 42);
        Genome g = c.getGenome();
        g2.drawString(String.format("Spd: %.0f | Siz: %.0f | Str: %.0f", g.getSpeed(), g.getSize(), g.getStrength()), tipX + 8, tipY + 56);
    }

    // Toggle setters
    public void setShowQuadTree(boolean showQuadTree) {
        this.showQuadTree = showQuadTree;
        repaint();
    }

    public void setShowEnergyBars(boolean showEnergyBars) {
        this.showEnergyBars = showEnergyBars;
        repaint();
    }

    public void setShowTrajectories(boolean showTrajectories) {
        this.showTrajectories = showTrajectories;
        repaint();
    }

    public boolean isShowQuadTree() {
        return showQuadTree;
    }

    public boolean isShowEnergyBars() {
        return showEnergyBars;
    }

    public boolean isShowTrajectories() {
        return showTrajectories;
    }
}
