package edu.mit.wi.haploview;


class Chromosome{

    private String ped;
    private String individual;
    private byte[] genotypes;
    private String origin;

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
        return genotypes[i];
    }
    public int size(){
        return genotypes.length;
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


