package edu.mit.wi.plink;

//This class should no longer be used but we're keeping it around in CVS for now

public class CCAssociationResult {

    private double freqA;
    private double freqU;

    public CCAssociationResult(Marker m, char a, char b, double af, double uf, double o, double c, double p){
        //super(m,a,b,o,c,p);
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