package edu.mit.wi.haploview;

class SNP{

    private String name;
    private long position;
    private double MAF;
    private String extra;
    private String gene_accession;
    private String gene_symbol;
    private byte minor, major;

    SNP(String n, long p, double m, byte a1, byte a2){
        name = n;
        position = p;
        MAF = m;
        major = a1;
        minor = a2;
    }

    SNP(String n, long p, double m, byte a1, byte a2, String a, String s, String e){
        name = n;
        position = p;
        MAF = m;
        major = a1;
        minor = a2;
        gene_accession = a;
        gene_symbol = s;
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

    public String getGeneAccession(){
	return gene_accession;
    }

    public String getGeneSymbol(){
	return gene_symbol;
    }

    public byte getMinor(){
        return minor;
    }

    public byte getMajor(){
        return major;
    }
}
