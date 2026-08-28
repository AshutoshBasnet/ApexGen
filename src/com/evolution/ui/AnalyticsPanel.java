package com.evolution.ui;

import com.evolution.engine.EvolutionEngine;
import com.evolution.engine.EvolutionStats;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Analytics and Control Dashboard for ApexGen.
 * Displays real-time simulation metrics, genetic trait meters,
 * an evolutionary drift line graph, and interactive controls.
 */
public class AnalyticsPanel extends JPanel {
    private final EvolutionEngine engine;
    private final SimulationPanel simulationPanel;
    private final MainGUI mainGUI;

    // HUD Labels
    private final JLabel genLabel;
    private final JLabel tickLabel;
    private final JLabel aliveLabel;
    private final JLabel foodLabel;
    private final JProgressBar tickProgressBar;

    // Trait Value Labels & Meters
    private final TraitMeter speedMeter;
    private final TraitMeter sizeMeter;
    private final TraitMeter strengthMeter;

    // Real-Time Drift Graph
    private final EvolutionChartPanel chartPanel;

    // Controls
    private final ModernButton playPauseBtn;
    private final ModernButton stepBtn;
    private final ModernButton restartBtn;
    private final JSlider speedSlider;
    private final JLabel speedValueLabel;

    // Checkboxes
    private final JCheckBox quadTreeCheck;
    private final JCheckBox energyBarCheck;

    // Modern Palette
    private static final Color BG_DARK = new Color(20, 25, 36);
    private static final Color CARD_BG = new Color(28, 36, 52);
    private static final Color TEXT_WHITE = new Color(245, 247, 250);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_GREEN = new Color(34, 197, 94);
    private static final Color ACCENT_RED = new Color(239, 68, 68);

    public AnalyticsPanel(EvolutionEngine engine, SimulationPanel simulationPanel, MainGUI mainGUI) {
        this.engine = engine;
        this.simulationPanel = simulationPanel;
        this.mainGUI = mainGUI;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(BG_DARK);
        setPreferredSize(new Dimension(380, 740));
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // 1. Header Section
        JPanel headerPanel = createCardPanel();
        headerPanel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("ApexGen Analytics HUD");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(TEXT_WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel);
        add(Box.createVerticalStrut(6));

        // 2. Generation & Lifecycle Metrics Card
        JPanel statsCard = createCardPanel();
        statsCard.setLayout(new GridLayout(2, 2, 6, 6));

        genLabel = createMetricLabel("Gen: 1", ACCENT_BLUE);
        tickLabel = createMetricLabel("Year: 0 / 300", TEXT_WHITE);
        aliveLabel = createMetricLabel("Alive: 75 / 75", ACCENT_GREEN);
        foodLabel = createMetricLabel("Food: 160", TEXT_WHITE);

        statsCard.add(genLabel);
        statsCard.add(aliveLabel);
        statsCard.add(tickLabel);
        statsCard.add(foodLabel);

        JPanel lifecycleWrapper = createCardPanel();
        lifecycleWrapper.setLayout(new BoxLayout(lifecycleWrapper, BoxLayout.Y_AXIS));
        lifecycleWrapper.add(statsCard);

        tickProgressBar = new JProgressBar(0, engine.getMaxTicksPerGeneration());
        tickProgressBar.setValue(0);
        tickProgressBar.setForeground(ACCENT_BLUE);
        tickProgressBar.setBackground(new Color(15, 20, 30));
        tickProgressBar.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        tickProgressBar.setPreferredSize(new Dimension(350, 8));
        lifecycleWrapper.add(tickProgressBar);

        add(lifecycleWrapper);
        add(Box.createVerticalStrut(6));

        // 3. Trait Averages Card (Live Genetic Drift)
        JPanel traitCard = createCardPanel();
        traitCard.setLayout(new BoxLayout(traitCard, BoxLayout.Y_AXIS));

        JLabel traitTitle = new JLabel("Population Trait Averages (BMR Trade-offs)");
        traitTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        traitTitle.setForeground(TEXT_MUTED);
        traitTitle.setBorder(BorderFactory.createEmptyBorder(2, 4, 6, 4));
        traitCard.add(traitTitle);

        speedMeter = new TraitMeter("Speed", ACCENT_BLUE);
        sizeMeter = new TraitMeter("Size", ACCENT_GREEN);
        strengthMeter = new TraitMeter("Strength", ACCENT_RED);

        traitCard.add(speedMeter);
        traitCard.add(Box.createVerticalStrut(4));
        traitCard.add(sizeMeter);
        traitCard.add(Box.createVerticalStrut(4));
        traitCard.add(strengthMeter);

        add(traitCard);
        add(Box.createVerticalStrut(6));

        // 4. Evolutionary Drift Line Graph
        chartPanel = new EvolutionChartPanel(engine);
        chartPanel.setPreferredSize(new Dimension(350, 160));
        add(chartPanel);
        add(Box.createVerticalStrut(6));

        // 5. Visual Overlays Card
        JPanel overlayCard = createCardPanel();
        overlayCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 2));

        quadTreeCheck = new JCheckBox("QuadTree Grid", simulationPanel.isShowQuadTree());
        styleCheckBox(quadTreeCheck);
        quadTreeCheck.addActionListener(e -> simulationPanel.setShowQuadTree(quadTreeCheck.isSelected()));

        energyBarCheck = new JCheckBox("Energy Bars", simulationPanel.isShowEnergyBars());
        styleCheckBox(energyBarCheck);
        energyBarCheck.addActionListener(e -> simulationPanel.setShowEnergyBars(energyBarCheck.isSelected()));

        overlayCard.add(quadTreeCheck);
        overlayCard.add(energyBarCheck);
        add(overlayCard);
        add(Box.createVerticalStrut(6));

        // 6. Interactive Controls Panel
        JPanel controlsCard = createCardPanel();
        controlsCard.setLayout(new BoxLayout(controlsCard, BoxLayout.Y_AXIS));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        buttonRow.setOpaque(false);

        playPauseBtn = new ModernButton("Pause", ACCENT_BLUE);
        playPauseBtn.addActionListener(e -> togglePlayPause());

        stepBtn = new ModernButton("Step 1x", new Color(75, 85, 105));
        stepBtn.addActionListener(e -> {
            engine.update();
            simulationPanel.repaint();
            updateHUD();
        });

        restartBtn = new ModernButton("Restart", new Color(185, 28, 28));
        restartBtn.addActionListener(e -> {
            engine.initializeSimulation();
            chartPanel.repaint();
            simulationPanel.repaint();
            updateHUD();
        });

        buttonRow.add(playPauseBtn);
        buttonRow.add(stepBtn);
        buttonRow.add(restartBtn);
        controlsCard.add(buttonRow);

        // Simulation Speed Slider
        JPanel speedRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        speedRow.setOpaque(false);
        JLabel speedLbl = new JLabel("Speed Multiplier:");
        speedLbl.setForeground(TEXT_MUTED);
        speedLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));

        speedSlider = new JSlider(1, 10, 1);
        speedSlider.setBackground(CARD_BG);
        speedSlider.setForeground(ACCENT_BLUE);
        speedSlider.setPreferredSize(new Dimension(140, 22));

        speedValueLabel = new JLabel("1x");
        speedValueLabel.setForeground(TEXT_WHITE);
        speedValueLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        speedSlider.addChangeListener(e -> {
            int speed = speedSlider.getValue();
            speedValueLabel.setText(speed + "x");
            mainGUI.setSimulationSpeed(speed);
        });

        speedRow.add(speedLbl);
        speedRow.add(speedSlider);
        speedRow.add(speedValueLabel);
        controlsCard.add(speedRow);

        add(controlsCard);
    }

    private void togglePlayPause() {
        boolean isRunning = !engine.isRunning();
        engine.setRunning(isRunning);
        playPauseBtn.setText(isRunning ? "Pause" : "Play");
        playPauseBtn.setBgColor(isRunning ? ACCENT_BLUE : ACCENT_GREEN);
    }

    public void updateHUD() {
        genLabel.setText(String.format("Gen: %d", engine.getCurrentGeneration()));
        tickLabel.setText(String.format("Year: %d / %d", engine.getCurrentTick(), engine.getMaxTicksPerGeneration()));
        aliveLabel.setText(String.format("Alive: %d / %d", engine.getAliveCount(), engine.getCreatures().size()));
        foodLabel.setText(String.format("Food: %d", engine.getFoodCount()));

        tickProgressBar.setMaximum(engine.getMaxTicksPerGeneration());
        tickProgressBar.setValue(engine.getCurrentTick());

        speedMeter.setValue(engine.getAvgSpeed());
        sizeMeter.setValue(engine.getAvgSize());
        strengthMeter.setValue(engine.getAvgStrength());

        chartPanel.repaint();
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(45, 60, 85), 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return panel;
    }

    private JLabel createMetricLabel(String text, Color accent) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(accent);
        label.setOpaque(true);
        label.setBackground(new Color(18, 24, 38));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setForeground(TEXT_WHITE);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cb.setFocusPainted(false);
    }

    /**
     * Modern custom-painted dark button that reliably works across all Look & Feels.
     */
    private static class ModernButton extends JButton {
        private Color bgColor;

        public ModernButton(String text, Color bg) {
            super(text);
            this.bgColor = bg;
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        }

        public void setBgColor(Color bg) {
            this.bgColor = bg;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color currentBg = bgColor;
            if (getModel().isPressed()) {
                currentBg = bgColor.darker();
            } else if (getModel().isRollover()) {
                currentBg = bgColor.brighter();
            }

            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();

            super.paintComponent(g);
        }
    }

    /**
     * Custom horizontal Trait Bar Meter component.
     */
    private static class TraitMeter extends JPanel {
        private final String traitName;
        private final Color traitColor;
        private double value;

        public TraitMeter(String traitName, Color traitColor) {
            this.traitName = traitName;
            this.traitColor = traitColor;
            this.value = 50.0;
            setOpaque(false);
            setPreferredSize(new Dimension(320, 20));
        }

        public void setValue(double value) {
            this.value = value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Label text
            g2.setColor(TEXT_WHITE);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String nameStr = String.format("%-8s", traitName);
            g2.drawString(nameStr, 2, h - 5);

            // Value text
            String valStr = String.format("%.1f", value);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(valStr, 70, h - 5);

            // Bar background
            int barX = 110;
            int barW = w - barX - 5;
            int barH = 8;
            int barY = (h - barH) / 2;

            g2.setColor(new Color(15, 20, 30));
            g2.fillRoundRect(barX, barY, barW, barH, 4, 4);

            // Bar fill
            double fillPct = Math.max(0.0, Math.min(1.0, (value - 5.0) / 95.0));
            int fillW = (int) (barW * fillPct);
            g2.setColor(traitColor);
            g2.fillRoundRect(barX, barY, Math.max(3, fillW), barH, 4, 4);
        }
    }

    /**
     * Custom Real-Time Multi-Series Line Chart for Evolutionary Drift tracking.
     */
    private static class EvolutionChartPanel extends JPanel {
        private final EvolutionEngine engine;

        public EvolutionChartPanel(EvolutionEngine engine) {
            this.engine = engine;
            setBackground(CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 60, 85), 1),
                BorderFactory.createEmptyBorder(5, 6, 5, 6)
            ));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Background
            g2.setColor(new Color(16, 22, 34));
            g2.fillRect(6, 6, w - 12, h - 12);

            // Title and Legend
            g2.setColor(TEXT_MUTED);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2.drawString("TRAIT DRIFT OVER GENERATIONS", 12, 18);

            // Legend dots
            drawLegendItem(g2, "Spd", ACCENT_BLUE, w - 130, 18);
            drawLegendItem(g2, "Siz", ACCENT_GREEN, w - 85, 18);
            drawLegendItem(g2, "Str", ACCENT_RED, w - 42, 18);

            List<EvolutionStats> history = engine.getStatsHistory();
            int plotX = 30;
            int plotY = 26;
            int plotW = w - 42;
            int plotH = h - 40;

            // Grid lines
            g2.setColor(new Color(30, 42, 60));
            g2.drawLine(plotX, plotY, plotX, plotY + plotH);
            g2.drawLine(plotX, plotY + plotH, plotX + plotW, plotY + plotH);
            g2.drawLine(plotX, plotY + plotH / 2, plotX + plotW, plotY + plotH / 2);

            // Y-axis labels
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g2.drawString("100", 8, plotY + 6);
            g2.drawString("50", 12, plotY + plotH / 2 + 4);
            g2.drawString("0", 16, plotY + plotH + 2);

            if (history == null || history.isEmpty()) {
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
                g2.drawString("Awaiting Generation 1 completion...", plotX + 25, plotY + plotH / 2);
                return;
            }

            int count = history.size();
            double xStep = (double) plotW / Math.max(1, count - 1);

            // Render 3 trait series
            renderSeries(g2, history, plotX, plotY, plotW, plotH, xStep, 0, ACCENT_BLUE);   // Speed
            renderSeries(g2, history, plotX, plotY, plotW, plotH, xStep, 1, ACCENT_GREEN);  // Size
            renderSeries(g2, history, plotX, plotY, plotW, plotH, xStep, 2, ACCENT_RED);    // Strength
        }

        private void drawLegendItem(Graphics2D g2, String name, Color c, int x, int y) {
            g2.setColor(c);
            g2.fillRect(x, y - 7, 7, 7);
            g2.setColor(TEXT_WHITE);
            g2.drawString(name, x + 9, y);
        }

        private void renderSeries(Graphics2D g2, List<EvolutionStats> history,
                                  int px, int py, int pw, int ph, double xStep, int traitIdx, Color color) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f));

            int prevX = -1, prevY = -1;
            for (int i = 0; i < history.size(); i++) {
                EvolutionStats s = history.get(i);
                double val = switch (traitIdx) {
                    case 0 -> s.getAvgSpeed();
                    case 1 -> s.getAvgSize();
                    default -> s.getAvgStrength();
                };

                int cx = px + (int) (i * xStep);
                int cy = py + ph - (int) ((val / 100.0) * ph);

                if (prevX != -1) {
                    g2.drawLine(prevX, prevY, cx, cy);
                }

                // Draw point node
                g2.fillOval(cx - 2, cy - 2, 5, 5);

                prevX = cx;
                prevY = cy;
            }
        }
    }
}

