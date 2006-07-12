package edu.mit.wi.plink;


public class TDTAssociationResult extends AssociationResult {
    private int transmitted;
    private int untransmitted;

    public TDTAssociationResult(Marker m, char a, char b, int t, int u, double o, double c, double p){
        super(m,a,b,o,c,p);
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