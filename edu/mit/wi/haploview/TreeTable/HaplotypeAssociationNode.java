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

    public String getRatios() {
        //if the array is null this is a block-title node, not an actual hap
        if (counts == null){
            return ("");
        }
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);

        for(int i= 0;i<counts.length;i++) {
            for(int j= 0;j<counts[i].length;j++) {
                counts[i][j] = (new Double(nf.format(counts[i][j]))).doubleValue();
            }
        }

        if (counts.length == 1){
            //TDT
            if (this.counts[0][0] > this.counts[0][1]){
                return this.counts[0][0] + " : " + this.counts[0][1];
            }else{
                return this.counts[0][1] + " : " + this.counts[0][0];
            }
        }else{
            //case-control
            if (this.counts[0][0] > this.counts[0][1]){
                if (this.counts[1][0] > this.counts[1][1]){
                    return this.counts[0][0] + " : " + this.counts[0][1] +
                            ", " + this.counts[1][0] + " : " + this.counts[1][1];
                }else{
                    return this.counts[0][0] + " : " + this.counts[0][1] +
                            "," + this.counts[1][1] + " : " + this.counts[1][0];
                }
            }else{
                if (this.counts[1][0] > this.counts[1][1]){
                    return this.counts[0][1] + " : " + this.counts[0][0] +
                            ", " + this.counts[1][0] + " : " + this.counts[1][1];
                }else{
                    return this.counts[0][1] + " : " + this.counts[0][0] +
                            "," + this.counts[1][1] + " : " + this.counts[1][0];
                }
            }
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
