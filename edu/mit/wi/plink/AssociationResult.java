package edu.mit.wi.plink;

import java.util.Vector;

public class AssociationResult {

    private Marker thisMarker;
    private Vector data;

    public AssociationResult(Marker m, Vector values){
        thisMarker = m;
        data = values;
    }

    public Marker getMarker(){
        return thisMarker;
    }

    public Vector getValues(){
        return data;
    }


}
