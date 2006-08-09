package edu.mit.wi.plink;


public class Marker {

    private String chromosome;
    private String markerID;
    private long morganDistance;
    private long position;

    public Marker(String chrom, String marker, long md, long pos){
        chromosome = chrom;
        markerID = marker;
        morganDistance = md;
        position = pos;
    }

    public String getChromosome(){
        return chromosome;
    }

    public String getMarkerID(){
        return markerID;
    }

    public long getMorganDistance(){
        return morganDistance;
    }

    public long getPosition(){
        return position;
    }

}