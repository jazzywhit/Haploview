package edu.mit.wi.haploview;

class SNP{

    private String name;
    private long position;
    private double MAF;
    private byte minor, major;

    SNP(String n, long p, double m, byte a1, byte a2){
        name = n;
        position = p;
        MAF = m;
        major = a1;
        minor = a2;
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

    public byte getMinor(){
        return minor;
    }

    public byte getMajor(){
        return major;
    }
}
