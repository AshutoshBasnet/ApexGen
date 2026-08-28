package com.evolution.ui;

import com.evolution.engine.EvolutionEngine;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Main Application Frame and Entry Point for the Natural Selection & Evolution Simulator.
 *
 * Runs a 60 FPS Swing Game Loop driving the EvolutionEngine,
 * SimulationPanel renderer, and AnalyticsPanel dashboard.
 */
public class MainGUI extends JFrame {
    private static final int ARENA_WIDTH = 930;
    private static final int ARENA_HEIGHT = 770;
    private static final int TARGET_FPS = 60;
    private static final int TIMER_DELAY_MS = 1000 / TARGET_FPS;

    private final EvolutionEngine engine;
    private final SimulationPanel simulationPanel;
    private final AnalyticsPanel analyticsPanel;
    private final Timer gameLoopTimer;

    private int simulationSpeed = 1;

    public MainGUI() {
        setTitle("ApexGen: Natural Selection & Genetic Drift Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Instantiate Core Engine
        engine = new EvolutionEngine(ARENA_WIDTH, ARENA_HEIGHT);
        engine.setRunning(true);

        // 2. Instantiate Viewport & Dashboard
        simulationPanel = new SimulationPanel(engine);
        simulationPanel.setPreferredSize(new Dimension(ARENA_WIDTH, ARENA_HEIGHT));

        analyticsPanel = new AnalyticsPanel(engine, simulationPanel, this);

        // 3. Assemble Layout
        add(simulationPanel, BorderLayout.CENTER);
        add(analyticsPanel, BorderLayout.EAST);

        // 4. Menu Bar for DSA Explanations and Settings
        createMenuBar();

        // 5. Simulation & Rendering Timer (~60 FPS Game Loop)
        gameLoopTimer = new Timer(TIMER_DELAY_MS, e -> onSimulationLoopTick());
        gameLoopTimer.start();

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu dsaMenu = new JMenu("DSA Architecture");
        JMenuItem quadTreeInfo = new JMenuItem("1. QuadTree Spatial Partitioning");
        quadTreeInfo.addActionListener(e -> showQuadTreeDialog());

        JMenuItem elitismInfo = new JMenuItem("2. Max-Heap Elitism (PriorityQueue)");
        elitismInfo.addActionListener(e -> showElitismDialog());

        JMenuItem rouletteInfo = new JMenuItem("3. Prefix-Sum Roulette Wheel (Binary Search)");
        rouletteInfo.addActionListener(e -> showRouletteDialog());

        dsaMenu.add(quadTreeInfo);
        dsaMenu.add(elitismInfo);
        dsaMenu.add(rouletteInfo);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About Simulator");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(dsaMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Executes the high-frequency animation and physics tick loop.
     */
    private void onSimulationLoopTick() {
        if (engine.isRunning()) {
            for (int i = 0; i < simulationSpeed; i++) {
                engine.update();
            }
        }
        simulationPanel.repaint();
        analyticsPanel.updateHUD();
    }

    public void setSimulationSpeed(int speed) {
        this.simulationSpeed = Math.max(1, speed);
    }

    private void showQuadTreeDialog() {
        JOptionPane.showMessageDialog(this,
            "<html><body style='width: 380px; font-family: sans-serif;'>"
            + "<h3 style='color: #2563EB;'>QuadTree 2D Spatial Partitioning</h3>"
            + "<p><b>Problem:</b> In a naive simulation with N creatures and M food items, checking which food is visible to each creature requires <b>O(N &times; M)</b> distance calculations every tick.</p>"
            + "<p><b>DSA Solution:</b> The custom <code>QuadTree&lt;T&gt;</code> recursively partitions the 2D arena into NW, NE, SW, and SE quadrants. When querying for food within a creature's sensory radius, non-intersecting quadrants are pruned immediately.</p>"
            + "<p><b>Complexity:</b> Construction in <b>O(M log M)</b> and Range Queries in <b>O(log M)</b>.</p>"
            + "</body></html>",
            "DSA 1: QuadTree Spatial Partitioning",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showElitismDialog() {
        JOptionPane.showMessageDialog(this,
            "<html><body style='width: 380px; font-family: sans-serif;'>"
            + "<h3 style='color: #10B981;'>Max-Heap Elitism (PriorityQueue)</h3>"
            + "<p><b>Goal:</b> Ensure that top performing genetic lineages are never lost due to random mutation or crossover degradation.</p>"
            + "<p><b>DSA Solution:</b> At the end of each generation, the population is inserted into a <code>PriorityQueue&lt;Creature&gt;</code> configured with a Max-Heap comparator based on individual fitness.</p>"
            + "<p><b>Complexity:</b> Extracting the top K elite individuals operates in <b>O(N log K)</b> time, passing exact copies of their genomes to the next generation.</p>"
            + "</body></html>",
            "DSA 2: Max-Heap Elitism",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showRouletteDialog() {
        JOptionPane.showMessageDialog(this,
            "<html><body style='width: 380px; font-family: sans-serif;'>"
            + "<h3 style='color: #8B5CF6;'>Roulette Wheel Parent Selection (Prefix Sum + Binary Search)</h3>"
            + "<p><b>Goal:</b> Select parents with probability strictly proportional to their fitness scores.</p>"
            + "<p><b>DSA Solution:</b></p>"
            + "<ol>"
            + "<li>Compute a 1D <b>Prefix Sum array</b> of fitness values: <code>P[i] = P[i-1] + fitness[i]</code> in <b>O(N)</b>.</li>"
            + "<li>Sample a uniform random number <code>r &isin; [0, TotalFitness)</code>.</li>"
            + "<li>Perform <code>Arrays.binarySearch(P, r)</code> to locate the parent index in <b>O(log N)</b> time instead of an O(N) linear scan.</li>"
            + "</ol>"
            + "</body></html>",
            "DSA 3: Prefix-Sum Binary Search Selection",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "<html><body style='width: 340px; font-family: sans-serif;'>"
            + "<h2>ApexGen: Natural Selection Simulator</h2>"
            + "<p>Built in 100% pure standard Java (Swing/AWT) demonstrating core Data Structures and Algorithms.</p>"
            + "<p><b>Features:</b></p>"
            + "<ul>"
            + "<li>Basal Metabolic Rate (BMR) energy costs</li>"
            + "<li>QuadTree spatial acceleration</li>"
            + "<li>Max-Heap elitism & Prefix-sum roulette selection</li>"
            + "<li>Real-time evolutionary drift line chart</li>"
            + "</ul>"
            + "</body></html>",
            "About ApexGen",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Application Entry Point.
     */
    public static void main(String[] args) {
        // Set modern system look and feel if available
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainGUI app = new MainGUI();
            app.setVisible(true);
        });
    }
}
