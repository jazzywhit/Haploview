package edu.mit.wi.haploview.TreeTable;

import edu.mit.wi.haploview.association.AssociationResult;

import java.util.Vector;
import java.util.Locale;
import java.text.NumberFormat;


public class HaplotypeAssociationNode {
    String name, pval;
    Vector children = new Vector();
    double freq, chisq;
    NumberFormat nf = NumberFormat.getInstance(Locale.US);
    String freqStr = "", countStr = "";

    public HaplotypeAssociationNode(String name) {
        this.name = name;
        this.freq = -1;
        this.chisq = -1;
        this.pval = "";
    }

    public HaplotypeAssociationNode(AssociationResult ar, int index) {
        this.name = ar.getAlleleName(index);
        this.chisq = ar.getChiSquare(index);
        this.pval = ar.getPValue(index);
        this.freq = ar.getFreq(index);
        this.freqStr = ar.getHapFreqString(index);
        this.countStr = ar.getHapCountString(index);
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
        if (freq < 0){
            return ("");
        }else{
            nf.setMinimumFractionDigits(3);
            nf.setMaximumFractionDigits(3);
            return nf.format(freq);
        }
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
