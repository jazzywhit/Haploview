package edu.mit.wi.tagger;

import java.util.HashSet;

public interface AlleleCorrelator {

    public LocusCorrelation getCorrelation(VariantSequence v1, VariantSequence v2);

    public void phaseAndCache(HashSet snpList);

}
