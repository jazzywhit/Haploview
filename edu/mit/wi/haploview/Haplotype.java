package edu.mit.wi.haploview;


public class Haplotype{
    private static final String[] alleleCodes = {"X","A","C","G","T"};

    private int[] genotypes;
    private int[] markers;
    private int listorder;
    private boolean[] tags;
    private double percentage;
    private double[] crossovers;
    private double transCount;
    private double untransCount;
    private double caseCount;
    private double controlCount;

    public Haplotype(int[] g, double p, int[] m){
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

    public void setTags(boolean[] t){
        tags = t;
    }

    public double getCrossover(int index){
        return crossovers[index];
    }

    public double[] getCrossovers(){
        return crossovers;
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

    public double getTransCount() {
        return transCount;
    }

    public void setTransCount(double transCount) {
        this.transCount = transCount;
    }

    public double getUntransCount() {
        return untransCount;
    }

    public void setUntransCount(double untransCount) {
        this.untransCount = untransCount;
    }

    public double getCaseCount() {
        return caseCount;
    }

    public void setCaseCount(double caseCount) {
        this.caseCount = caseCount;
    }

    public double getControlCount() {
        return controlCount;
    }

    public void setControlCount(double controlCount) {
        this.controlCount = controlCount;
    }

    public String toString(){
        StringBuffer curHap = new StringBuffer(genotypes.length);
        for(int k=0;k<genotypes.length;k++) {
            curHap.append(alleleCodes[genotypes[k]]);
        }

        return curHap.toString();
    }
}
