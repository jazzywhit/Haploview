package edu.mit.wi.haploview.tagger;

import edu.mit.wi.tagger.AlleleCorrelator;
import edu.mit.wi.tagger.VariantSequence;
import edu.mit.wi.haploview.DPrimeTable;
import edu.mit.wi.haploview.PairwiseLinkage;

import java.util.Hashtable;

public class HaploviewAlleleCorrelator implements AlleleCorrelator{
    private Hashtable indicesByVarSeq;
    private DPrimeTable dpTable;

    public HaploviewAlleleCorrelator(Hashtable indices,DPrimeTable dpTable) {
        indicesByVarSeq = indices;
        this.dpTable = dpTable;
    }

    public double getCorrelation(VariantSequence v1, VariantSequence v2) {
        if(v1 == v2) {
            return 1;
        }

        int v1Index = ((Integer)indicesByVarSeq.get(v1)).intValue();
        int v2Index = ((Integer)indicesByVarSeq.get(v2)).intValue();
        if (v1Index > v2Index){
            return ((PairwiseLinkage)dpTable.getLDStats(v2Index,
                    v1Index)).getRSquared();
        }else{
            return ((PairwiseLinkage)dpTable.getLDStats(v1Index,
                    v2Index)).getRSquared();            
        }
    }

}
