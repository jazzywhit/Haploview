package edu.mit.wi.haploview;


class Chromosome{

    private String ped;
    private String individual;
    private byte[] genotypes;
    private String origin;

    static int[] realIndex;
    static Object[] markers;

    Chromosome(String p, String i, byte[] g, String o){
        ped = p;
        individual = i;
        genotypes = g;
        origin = o;
    }

    Chromosome(String p, String i, byte[] g){
        ped = p;
        individual = i;
        genotypes = g;
        origin = "unknown";
    }

    public byte elementAt(int i){
        return genotypes[realIndex[i]];
    }

    public byte unfilteredElementAt(int i){
        return genotypes[i];
    }

    public static int size(){
        return realIndex.length;
    }

    public static SNP getMarker(int i){
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


