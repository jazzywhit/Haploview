package edu.mit.wi.haploview;


class Chromosome{

    private String ped;
    private String individual;
    private byte[] genotypes;
    private String origin;


    private boolean transmitted = false;

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

    Chromosome(String p, String i, byte[] g, boolean isTransmitted){
        ped = p;
        individual = i;
        genotypes = g;
        origin = "unknown";
        trueSize = genotypes.length;
        transmitted = isTransmitted;
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

    public boolean isTransmitted() {
        return transmitted;
    }

    public void setTransmitted(boolean transmitted) {
        this.transmitted = transmitted;
    }


}


