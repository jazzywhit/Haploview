package edu.mit.wi.haploview;

class Haplotype{

    private int[] genotypes;
    private int[] markers;
    private int listorder;
    private boolean[] tags;
    private double percentage;
    private double[] crossovers;

    Haplotype(int[] g, double p, int[] m){
        genotypes=g;
        percentage = p;
        markers = m;
        tags = new boolean[genotypes.length];
        listorder = 0;
    }

    public int[] getGeno(){
        return genotypes;
    }

    public double getPercentage(){
        return percentage;
    }

    public void addCrossovers(double[] c){
        crossovers = c;
    }

    public void addTag(int t){
        tags[t] = true;
    }

    public void clearTags(){
        for (int t = 0; t < tags.length; t++){
            tags[t] = false;
        }
    }

    public double getCrossover(int index){
        return crossovers[index];
    }

    public int[] getMarkers(){
        return markers;
    }

    public boolean[] getTags(){
        return tags;
    }

    public int getListOrder(){
        return listorder;
    }

    public void setListOrder(int slo){
        listorder = slo;
    }
}
