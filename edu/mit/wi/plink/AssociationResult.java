package edu.mit.wi.plink;

import java.util.Vector;

public class AssociationResult {

    private Marker thisMarker;
    private Vector data;
    private int index;

    public AssociationResult(int i, Marker m, Vector values){
        thisMarker = m;
        data = values;
        index = i;
    }

    public int getIndex(){
        return index;
    }

    public Marker getMarker(){
        return thisMarker;
    }

    public Vector getValues(){
        return data;
    }

    public void addValues(Vector values){
        for (int i = 0; i < values.size(); i++){
            data.add(values.get(i));
        }
    }
}
