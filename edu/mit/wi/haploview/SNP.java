package edu.mit.wi.haploview;

class SNP{

    private String name;
    private long position;
    private double MAF;

    SNP(String n, long p, double m){
        name = n;
        position = p;
        MAF = m;
    }

    public String getName(){
        return name;
    }

    public long getPosition(){
        return position;
    }

    public double getMAF(){
        return MAF;
    }
}
