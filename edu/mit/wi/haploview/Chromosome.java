package edu.mit.wi.haploview;


class Chromosome{

    private String ped;
    private String individual;
    private byte[] genotypes;
    private String origin;

    static int[] realIndex;
    static Object[] markers;
    static int trueSize;

    Chromosome(String p, String i, byte[] g, String o){
        ped = p;
        individual = i;
        genotypes = g;
        origin = o;
        trueSize = genotypes.length;
    }

    Chromosome(String p, String i, byte[] g){
        ped = p;
        individual = i;
        genotypes = g;
        origin = "unknown";
        trueSize = genotypes.length;
    }

    public byte getFilteredGenotype(int i){
        return genotypes[realIndex[i]];
    }

    public byte getGenotype(int i){
        return genotypes[i];
    }

    public static int getFilteredSize(){
        return realIndex.length;
    }

    public static int getSize(){
        return trueSize;
    }

    public static SNP getMarker(int i){
        return (SNP)markers[i];
    }

    public static SNP getFilteredMarker(int i){
        return (SNP)markers[realIndex[i]];
    }

    public String getPed(){
        return ped;
    }

    public String getIndividual(){
        return individual;
    }

    public String getOrigin(){
        return origin;
    }

}


