package edu.mit.wi.haploview;

class SNP{

    private String name;
    private long position;
    private String origin;

    SNP(String n, long p, String o){
	name = n;
	position = p;
	origin = o;
    }

    SNP(String n, long p){
	name = n;
	position = p;
	origin = "unknown";
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
    
}
