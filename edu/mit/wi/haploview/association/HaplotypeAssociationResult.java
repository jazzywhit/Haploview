package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.Haplotype;
import edu.mit.wi.haploview.Options;
import edu.mit.wi.haploview.HaploViewException;

public class HaplotypeAssociationResult extends AssociationResult{

    Haplotype[] haps;

    public HaplotypeAssociationResult(Haplotype[] locusHaplos, double freqCutoff, String n) {
        nf.setGroupingUsed(false);

        for (int i = 0; i < locusHaplos.length; i++){
            alleles.add(locusHaplos[i]);
        }
        filterByFrequency(freqCutoff);
        name = n;

        haps = locusHaplos;
    }

    public HaplotypeAssociationResult(Haplotype[] locusHaplos, String allele, String name) throws HaploViewException{
        nf.setGroupingUsed(false);
        for (int i = 0; i < locusHaplos.length; i++){
            alleles.add(locusHaplos[i]);
        }
        filterByAllele(allele);
        this.name = name;

        haps = locusHaplos;
    }

    public String getDisplayName(int i) {
        return this.getName() + ": " + this.getAlleleName(i);
    }

    public String getCountString(int i){
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);

        Haplotype h = (Haplotype) filteredAlleles.get(i);
        StringBuffer countSB = new StringBuffer();
        if(Options.getAssocTest() == ASSOC_TRIO) {
            countSB.append(nf.format(h.getTransCount())).append(" : ").append(nf.format(h.getUntransCount()));
        } else if(Options.getAssocTest() == ASSOC_CC) {
            double caseSum = 0, controlSum = 0;
            for (int j = 0; j < alleles.size(); j++){
                if (h != alleles.get(j)){
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
        Haplotype h = (Haplotype) filteredAlleles.get(i);
        double caseSum = 0, controlSum = 0;
        for (int j = 0; j < alleles.size(); j++){
            caseSum += ((Haplotype)alleles.get(j)).getCaseCount();
            controlSum += ((Haplotype)alleles.get(j)).getControlCount();
        }
        countSB.append(nf.format(h.getCaseCount()/caseSum)).append(", ");
        countSB.append(nf.format(h.getControlCount()/controlSum));

        return countSB.toString();
    }

    public void filterByAllele(String allele) throws HaploViewException{
        if (allele == null){
            filterByFrequency(Options.getHaplotypeDisplayThreshold());
        }else{
            filteredAlleles.removeAllElements();
            for(int i=0;i<alleles.size();i++) {
                Haplotype curHap = (Haplotype) alleles.get(i);
                if(curHap.toNumericString().equals(allele)){
                    filteredAlleles.add(curHap);
                }
            }
            if (filteredAlleles.size() == 0){
                throw new HaploViewException(allele + ": no such allele for test:\n" +
                        getName());
            }
            calculateChiSquares();
        }
    }

    public Haplotype[] getHaps() {
        return haps;
    }
}
