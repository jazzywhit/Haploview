package edu.mit.wi.haploview.TreeTable;

import java.util.Vector;
import java.util.Locale;
import java.text.NumberFormat;


public class HaplotypeAssociationNode {
    String name, pval;
    Vector children = new Vector();
    double[][] counts;
    double freq, chisq;
    NumberFormat nf = NumberFormat.getInstance(Locale.US);

    public HaplotypeAssociationNode(String name) {
         this(name,-1,null,-1,"");
    }

    public HaplotypeAssociationNode(String name, double freq, double[][] counts, double chisq, String pval) {
        this.name = name;
        //counts is a 2D array with the following format:
        //TDT -- counts[0][0] = trans, counts[0][1] = untrans
        //CC -- counts[0][0], counts[0][1] case ratio inputs, [1][0] & [1][1] control ratio inputs.
        this.counts = counts;
        this.chisq = chisq;
        this.pval = pval;
        this.freq = freq;
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
        //this will only be called if we're doing case control
        if(counts == null || counts.length == 1) {
            //counts.length==1 should never happen,since that means it's Trio
            return "";
        }
        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        //we divide by the sum of the two numbers since we really want the frequency of this haplotype
        //for cases/controls
        return nf.format(this.counts[0][0] / ( this.counts[0][0] + this.counts[0][1])) + ", "
                + nf.format(this.counts[1][0] / (this.counts[1][0] + this.counts[1][1])) ;

    }

    public String getCounts() {
        //if the array is null this is a block-title node, not an actual hap
        if (counts == null){
            return ("");
        }

        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);

        if (counts.length == 1){
            //TDT
            return nf.format(this.counts[0][0]) + " : " + nf.format(this.counts[0][1]);
        }else{
            //case-control
            return nf.format(this.counts[0][0]) + " : " + nf.format(this.counts[0][1]) +
                    ", " + nf.format(this.counts[1][0]) + " : " + nf.format(this.counts[1][1]);
        }
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
