package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.Haplotype;
import edu.mit.wi.haploview.Options;

import java.util.Vector;
import java.util.Iterator;

public class HaplotypeAssociationResult extends AssociationResult{

    public HaplotypeAssociationResult(Haplotype[] locusHaplos, int freqCutoff, String n) {
        nf.setGroupingUsed(false);

        for (int i = 0; i < locusHaplos.length; i++){
            alleles.add(locusHaplos[i]);
        }
        setFrequencyCutoff(((double)freqCutoff)/100);
        name = n;
    }

    public String getDisplayName(int i) {
        return this.getName() + ": " + this.getAlleleName(i);
    }

    public String getCountString(int i){
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);

        Haplotype h = (Haplotype) alleles.get(i);
        StringBuffer countSB = new StringBuffer();
        if(Options.getAssocTest() == ASSOC_TRIO) {
            countSB.append(nf.format(h.getTransCount())).append(" : ").append(nf.format(h.getUntransCount()));
        } else if(Options.getAssocTest() == ASSOC_CC) {
            double caseSum = 0, controlSum = 0;
            for (int j = 0; j < alleles.size(); j++){
                if (i!=j){
                    caseSum += ((Haplotype)alleles.get(j)).getCaseCount();
                    controlSum += ((Haplotype)alleles.get(j)).getControlCount();
                }
            }
            countSB.append(nf.format(h.getCaseCount())).append(" : ").append(nf.format(caseSum)).append(", ");
            countSB.append(nf.format(h.getControlCount())).append(" : ").append(nf.format(controlSum));
        }

        return countSB.toString();
    }

    public String getFreqString(int i ){
        if (Options.getAssocTest() == ASSOC_TRIO){
            return "";
        }

        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);

        StringBuffer countSB = new StringBuffer();
        Haplotype h = (Haplotype) alleles.get(i);
        double caseSum = 0, controlSum = 0;
        for (int j = 0; j < alleles.size(); j++){
            caseSum += ((Haplotype)alleles.get(j)).getCaseCount();
            controlSum += ((Haplotype)alleles.get(j)).getControlCount();
        }
        countSB.append(nf.format(h.getCaseCount()/caseSum)).append(", ");
        countSB.append(nf.format(h.getControlCount()/controlSum));

        return countSB.toString();
    }

    public static Vector getAssociationResults(Haplotype[][] haplos){
        Vector results = new Vector();
        for (int i = 0; i < haplos.length; i++){
            String blockname = "Block " + (i+1);
            results.add(new HaplotypeAssociationResult(haplos[i], Options.getHaplotypeDisplayThreshold(),blockname));
        }

        return results;
    }

}
