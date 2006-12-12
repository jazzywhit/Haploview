package edu.mit.wi.plink;


public class Marker {

    private String chromosome;
    private String markerID;
    private long position;

    public Marker(String chrom, String marker, long pos){
        chromosome = chrom;
        markerID = marker;
        position = pos;
    }

    public String getChromosome(){
        return chromosome;
    }

    public String getMarkerID(){
        return markerID;
    }

    public long getPosition(){
        return position;
    }

}