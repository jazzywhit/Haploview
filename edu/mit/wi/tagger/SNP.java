package edu.mit.wi.tagger;

import java.util.Vector;
import java.util.Comparator;

public class SNP extends VariantSequence {
    String name;
    long location;
    //minor allele frequency
    double MAF;

    public SNP(String n, long l,double maf) {
        name = n;
        location =l;
        MAF = maf;
    }

    public SNP(String n, long l) {
        this(n,l,-1);
    }


    public SNP(String n) {
        this(n,-1);
    }

    public String getName() {
        return name;
    }

    public double getMAF() {
        return MAF;
    }

    public long getLocation() {
        return location;
    }
 }
