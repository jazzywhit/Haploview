package edu.mit.wi.tagger;

import java.util.Vector;
import java.util.Comparator;
import java.util.HashSet;

public class SNP extends VariantSequence {
    private String name;
    private long location;
    private double MAF;
    private HashSet LDList;

    public SNP(String n, long l,double maf) {
        name = n;
        location =l;
        MAF = maf;
        LDList = new HashSet();
    }

    public SNP(String n, long l) {
        this(n,l,-1);
    }

    public SNP(String n) {
        this(n,-1);
    }

    public boolean equals(Object o) {
        if (o instanceof SNP){
            SNP s = (SNP)o;
            if(name.equals(s.name) && location == s.location) {
                return true;
            }else {
                return false;
            }
        }
        return false;
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

    public void addToLDList(SNP s){
        LDList.add(s);
    }

    public HashSet getLDList() {
        return LDList;
    }
 }
