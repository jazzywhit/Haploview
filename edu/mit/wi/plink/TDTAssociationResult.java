package edu.mit.wi.plink;

//This class should no longer be used but we're keeping it around in CVS for now

public class TDTAssociationResult {
    private int transmitted;
    private int untransmitted;

    public TDTAssociationResult(Marker m, char a, char b, int t, int u, double o, double c, double p){
        //super(m,a,b,o,c,p);
        transmitted = t;
        untransmitted = u;
    }

    public int getTransmitted(){
        return transmitted;
    }

    public int getUntransmitted(){
        return untransmitted;
    }
}