package edu.mit.wi.plink;

public abstract class AssociationResult {

    private Marker thisMarker;
    private char allele1;
    private char allele2;
    private double odds;
    private double chisq;
    private double pval;

    public AssociationResult(Marker m, char a, char b, double o, double c, double p){
        thisMarker = m;
        allele1 = a;
        allele2 = b;
        odds = o;
        chisq = c;
        pval = p;
    }

    public Marker getMarker(){
        return thisMarker;
    }

    public char getAllele1(){
        return allele1;
    }

    public char getAllele2(){
        return allele2;
    }

    public double getOdds(){
        return odds;
    }

    public double getChisq(){
        return chisq;
    }

    public double getPval(){
        return pval;
    }
}
