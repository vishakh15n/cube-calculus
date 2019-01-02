package edu.pdx.vishakh.cubecalculus.algorithms;

public class StatsCollector {
    private long nMerges;
    private boolean cyclic;
    private int primaries;
    private int secondaries;
    private int tertiaries;
    private String type = "";

    public StatsCollector() {
        nMerges = 0;
    }

    public long numberOfMerges() {
        return nMerges;
    }

    public void increamentMerges() {
        ++nMerges;
    }

    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    public boolean hasPrimary() {
        return primaries > 0;
    }
    public boolean hasSecondary() {
        return secondaries > 0;
    }
    public boolean hasTertiary() {
        return tertiaries > 0;
    }

    public int getPrimaries() {
        return primaries;
    }

    public int getSecondaries() {
        return secondaries;
    }

    public int getTertiaries() {
        return tertiaries;
    }

    public void incrementPrimaries(int nPrimes) {
        primaries += nPrimes;
    }

    public void incrementSecondaries(int nPrimes) {
        secondaries += nPrimes;
    }

    public void incrementTertiaries(int nPrimes) {
        tertiaries += nPrimes;
    }

    public void setFunctionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
