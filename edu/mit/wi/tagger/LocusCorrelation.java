package edu.mit.wi.tagger;

import java.util.Hashtable;

public class LocusCorrelation {
    Allele a;
    double rsq;

    public LocusCorrelation(Allele a, double rsq) {
        this.a = a;
        this.rsq = rsq;
    }

    public Allele getAllele(){
        return a;
    }

    public double getRsq(){
        return rsq;
    }
}
