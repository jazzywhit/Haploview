package edu.mit.wi.haploview;

import java.util.Vector;

public class EMReturn {
    private int[][] haplotypes;
    private double[] frequencies;
    private Vector obsT, obsU;

    public EMReturn(int[][] haplotypes, double[] frequencies, Vector obsT, Vector obsU) {
        this.haplotypes = haplotypes;
        this.frequencies = frequencies;
        this.obsT = obsT;
        this.obsU = obsU;
    }

    public EMReturn(int[][] haplotypes, double[] frequencies) {
        this.haplotypes = haplotypes;
        this.frequencies = frequencies;
    }

    public int[][] getHaplotypes() {
        return haplotypes;
    }

    public void setHaplotypes(int[][] haplotypes) {
        this.haplotypes = haplotypes;
    }

    public double[] getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(double[] frequencies) {
        this.frequencies = frequencies;
    }

    public int numHaplos(){
        return haplotypes.length;
    }

    public Vector getObsT() {
        return obsT;
    }

    public void setObsT(Vector obsT) {
        this.obsT = obsT;
    }

    public Vector getObsU() {
        return obsU;
    }

    public void setObsU(Vector obsU) {
        this.obsU = obsU;
    }    
}
