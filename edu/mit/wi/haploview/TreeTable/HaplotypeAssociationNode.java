package edu.mit.wi.haploview.TreeTable;

import edu.mit.wi.haploview.association.HaplotypeAssociationResult;

import java.util.Vector;
import java.util.Locale;
import java.text.NumberFormat;


public class HaplotypeAssociationNode {
    String name, pval;
    Vector children = new Vector();
    double chisq;
    NumberFormat nf = NumberFormat.getInstance(Locale.US);
    String freqStr = "", countStr = "", freq = "";

    public HaplotypeAssociationNode(String name) {
        this.name = name;
        this.freq = "";
        this.chisq = -1;
        this.pval = "";
    }

    public HaplotypeAssociationNode(HaplotypeAssociationResult ar, int index) {
        this.name = ar.getAlleleName(index);
        this.chisq = ar.getChiSquare(index);
        this.pval = ar.getPValue(index);
        this.freq = ar.getFreq(index);
        this.freqStr = ar.getFreqString(index);
        this.countStr = ar.getCountString(index);
    }

    public void add(HaplotypeAssociationNode child){
        children.add(child);
    }

    public String toString(){
        return name;
    }

    public String getName() {
        return name;
    }

    public String getFreq() {
        return freq;
    }

    public String getCCFreqs() {
        return freqStr;
    }

    public String getCounts() {
        return countStr;
    }

    public String getChiSq() {
        if (chisq < 0){
            return ("");
        }else{
            return (new Double(chisq)).toString();
        }
    }

    public String getPVal(){
        return pval;
    }
}
