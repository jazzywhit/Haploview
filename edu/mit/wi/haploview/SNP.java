package edu.mit.wi.haploview;

class SNP{

    private String name;
    private long position;
    private String origin;
    private double MAF;

    SNP(String n, long p, String o, double m){
	name = n;
	position = p;
	origin = o;
	MAF = m;
    }

    SNP(String n, long p, double m){
	name = n;
	position = p;
	origin = "unknown";
	MAF = m;
    }

    public String getName(){
	return name;
    }
    
    public long getPosition(){
	return position;
    }

    public String getOrigin(){
	return origin;
    }

    public double getMAF(){
	return MAF;
    }
}
