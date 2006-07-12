package edu.mit.wi.haploview;

import java.util.Vector;


public class Chromosome{

    private String ped;
    private String individual;

    private int affected;
    //kidAffected stores the status of the child of this chromosome if it is from a trio
    private Integer kidAffected;
    //genotypes[] used to be private but the accessor was wasting a lot of time when it would
    //get called literally millions of times. so we allow other classes to touch this array
    //in the interest of speed
    byte[] genotypes;
    private String origin;

    private static String dataChrom = "none";
    private static String dataBuild = "none";
    public static int[] realIndex;
    public static int[] filterIndex;
    static Vector markers;
    static int trueSize;
    private boolean haploid = false;
    private boolean phased = false;

    Chromosome(String p, String i, byte[] g, String o, int a, boolean isPhased) throws HaploViewException{
        ped = p;
        individual = i;
        genotypes = g;
        if(a < 0 || a >2) {
            throw new HaploViewException("invalid affected status");
        }
        affected = a;
        origin = o;
        trueSize = genotypes.length;
        phased = isPhased;
    }

    Chromosome(String p, String i, byte[] g, int a, int kidA, boolean isPhased) throws HaploViewException{
        ped = p;
        individual = i;
        genotypes = g;
        if(a < 0 || a >2) {
            throw new HaploViewException("invalid affected status");
        }
        affected = a;
        kidAffected = new Integer(kidA);
        origin = "unknown";
        trueSize = genotypes.length;
        phased = isPhased;
    }

    public int getAffected() {
        return affected;
    }

    public static void doFilter(boolean[] markerResults) {
        //set up the indexing to take into account skipped markers. Need
        //to loop through twice because first time we just count number of
        //unskipped markers
        int count = 0;
        for (int i = 0; i < markerResults.length; i++){
            if (markerResults[i]){
                count++;
            }
        }
        Chromosome.filterIndex = new int[markerResults.length];
        Chromosome.realIndex = new int[count];
        int k = 0;
        for (int i =0; i < markerResults.length; i++){
            if (markerResults[i]){
                realIndex[k] = i;
                filterIndex[i] = k;
                k++;
            }else{
                filterIndex[i] = -1;
            }
        }
    }

    public static void doFilter(int size){
        realIndex = new int[size];
        filterIndex = new int[size];
        for (int i = 0; i < size; i++){
            realIndex[i] = i;
            filterIndex[i] = i;
        }
    }

    //all of these accessors below (getGenotype, getSize and getMarker) are for the filtered
    //array of markers because after the data are loaded one usually wants to only be looking
    //at the markers that haven't been filtered out of subsequent analyses. the getUnfiltered
    //versions of same are mostly used in the early processing steps

    public byte getGenotype(int i){
        //gets genotype from filtered position i
        return genotypes[realIndex[i]];
    }

    public byte getUnfilteredGenotype(int i){
        //gets genotype from unfiltered position i
        return genotypes[i];
    }

    public static int getSize(){
        //get number of filtered markers
        return realIndex.length;
    }

    public static int getUnfilteredSize(){
        //get total number of markers (i.e. without filtering)
        return trueSize;
    }

    public static SNP getUnfilteredMarker(int i){
        //get SNP at unfiltered position i
        return (SNP)markers.get(i);
    }

    public static SNP getMarker(int i){
        //get SNP at filtered position i
        return (SNP)markers.get(realIndex[i]);
    }

    public static Vector getAllMarkers(){
        return markers;
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

    public void setGenotype(byte gen,int pos){
        this.genotypes[pos] = gen;
    }

    public static void setDataChrom(String chrom) {
        if (chrom != null){
            dataChrom = chrom.toLowerCase();
        }else{
            dataChrom = null;
        }
    }

    public static String getDataChrom(){
        return dataChrom;
    }

    public static void setDataBuild(String build){
        dataBuild = build;
    }

    public static String getDataBuild(){
        return dataBuild;
    }

    public Integer getKidAffected() {
        return kidAffected;
    }

    public void setKidAffected(int kidAffected) {
        this.kidAffected = new Integer(kidAffected);
    }


    public boolean isHaploid() {
        return haploid;
    }

    public void setHaploid(boolean haploid) {
        this.haploid = haploid;
    }
}


