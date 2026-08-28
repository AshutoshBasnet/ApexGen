package com.evolution.engine;

/**
 * Encapsulates generational metrics and average trait snapshots.
 * Used for live analytics and real-time evolutionary drift visualization.
 */
public class EvolutionStats {
    private final int generation;
    private final double avgSpeed;
    private final double avgSize;
    private final double avgStrength;
    private final double avgFitness;
    private final double maxFitness;
    private final int survivors;
    private final int totalPopulation;

    public EvolutionStats(int generation, double avgSpeed, double avgSize,
                          double avgStrength, double avgFitness, double maxFitness,
                          int survivors, int totalPopulation) {
        this.generation = generation;
        this.avgSpeed = avgSpeed;
        this.avgSize = avgSize;
        this.avgStrength = avgStrength;
        this.avgFitness = avgFitness;
        this.maxFitness = maxFitness;
        this.survivors = survivors;
        this.totalPopulation = totalPopulation;
    }

    public int getGeneration() {
        return generation;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getAvgSize() {
        return avgSize;
    }

    public double getAvgStrength() {
        return avgStrength;
    }

    public double getAvgFitness() {
        return avgFitness;
    }

    public double getMaxFitness() {
        return maxFitness;
    }

    public int getSurvivors() {
        return survivors;
    }

    public int getTotalPopulation() {
        return totalPopulation;
    }
}
