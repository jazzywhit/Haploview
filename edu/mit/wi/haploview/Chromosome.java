package edu.mit.wi.haploview;


public class Chromosome{

    private String ped;
    private String individual;
    //genotypes[] used to be private but the accessor was wasting a lot of time when it would
    //get called literally millions of times. so we allow other classes to touch this array
    //in the interest of speed
    byte[] genotypes;
    private String origin;
    boolean[] kidMissing;
    private boolean affected = false;

    public static String dataChrom = null;
    static int[] realIndex;
    static int[] filterIndex;
    static Object[] markers;
    static int trueSize;

    Chromosome(String p, String i, byte[] g, boolean a, String o){
        ped = p;
        individual = i;
        genotypes = g;
        affected = a;
        origin = o;
        trueSize = genotypes.length;
    }

    Chromosome(String p, String i, byte[] g, boolean a){
        ped = p;
        individual = i;
        genotypes = g;
        affected = a;
        origin = "unknown";
        trueSize = genotypes.length;
    }

    Chromosome(String p, String i, byte[] g, boolean a, boolean[] isKidMissing){
        ped = p;
        individual = i;
        genotypes = g;
        affected = a;
        origin = "unknown";
        trueSize = genotypes.length;
        kidMissing = isKidMissing;
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

    public boolean getAffected(){
        return affected;
    }

    public void setGenotype(byte gen,int pos){
        this.genotypes[pos] = gen;
    }
}


