package edu.mit.wi.haploview;

public class SNP{

    private String name;
    private long position;
    private double MAF;
    private String extra;
    private byte minor, major;
    private int dup;

    SNP(String n, long p, double m, byte a1, byte a2){
        name = n;
        position = p;
        MAF = m;
        major = a1;
        minor = a2;
    }

    SNP(String n, long p, double m, byte a1, byte a2, String e){
        name = n;
        position = p;
        MAF = m;
        major = a1;
        minor = a2;
        extra = e;
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

    public String getExtra(){
        return extra;
    }

    public byte getMinor(){
        return minor;
    }

    public byte getMajor(){
        return major;
    }

    public void setDup(int i) {
        //0 for non dup
        //1 for "used" dup (has higher geno% than twin)
        //2 for "unused" dup
        dup = i;
    }

    public int getDupStatus(){
        return dup;
    }
}
