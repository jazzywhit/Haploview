package edu.mit.wi.plink;


public class CCAssociationResult extends AssociationResult {

    private double freqA;
    private double freqU;

    public CCAssociationResult(Marker m, char a, char b, double af, double uf, double o, double c, double p){
        super(m,a,b,o,c,p);
        freqA = af;
        freqU = uf;
    }

    public double getFrequencyAffected(){
        return freqA;
    }

    public double getFrequencyUnaffected(){
        return freqU;
    }

}