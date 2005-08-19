package edu.mit.wi.haploview.tagger;

import edu.mit.wi.tagger.*;
import edu.mit.wi.tagger.SNP;
import edu.mit.wi.haploview.*;

import java.util.Hashtable;
import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;

public class HaploviewAlleleCorrelator implements AlleleCorrelator{
    private Hashtable indicesByVarSeq;
    private DPrimeTable dpTable;
    private HaploData theData;
    private Haplotype[] phasedCache;
    private Hashtable phasedCacheIndicesByVarSeq;
    private Hashtable lcByComparison;

    public HaploviewAlleleCorrelator(Hashtable indices, HaploData hd) {
        indicesByVarSeq = indices;
        dpTable = hd.dpTable;
        theData = hd;
        lcByComparison = new Hashtable();
    }

    public LocusCorrelation getCorrelation(VariantSequence v1, VariantSequence v2) {
        if(v1 == v2) {
            return new LocusCorrelation(null,1);
        }

        if (v1 instanceof SNP && v2 instanceof SNP){
            //we are comparing two snps
            int v1Index = ((Integer)indicesByVarSeq.get(v1)).intValue();
            int v2Index = ((Integer)indicesByVarSeq.get(v2)).intValue();

            double rsq;
            PairwiseLinkage pl;
            if (v1Index > v2Index){
                 pl = dpTable.getLDStats(v2Index,v1Index);
            }else{
                pl = dpTable.getLDStats(v1Index,v2Index);
            }

            if(pl == null) {
                rsq = 0;
            } else {
                rsq =pl.getRSquared();
            }

            LocusCorrelation lc = new LocusCorrelation(null,rsq);
            return lc;
        }else{
            //we are comparing a snp vs. a block
            SNP theSNP;
            Block theBlock;
            if (v1 instanceof SNP){
                theSNP = (SNP) v1;
                theBlock = (Block) v2;
            }else{
                theSNP = (SNP) v2;
                theBlock = (Block) v1;
            }

            Comparison c = new Comparison(theSNP, theBlock);
            if (lcByComparison.containsKey(c)){
                return (LocusCorrelation) lcByComparison.get(c);
            }


            Allele curBestAllele = null;
            double curBestRsq = 0;
            int[][] genos = new int[phasedCache.length][theBlock.getMarkerCount()+1];
            for (int i = 0; i < phasedCache.length; i++){
                //create a temporary set of mini hap genotypes with theSNP as the first marker and theBlock's markers as the rest
                genos[i][0] = phasedCache[i].getGeno()[((Integer)phasedCacheIndicesByVarSeq.get(theSNP)).intValue()];
                for (int j = 1; j < theBlock.getMarkerCount()+1; j++){
                    genos[i][j] = phasedCache[i].getGeno()[((Integer)phasedCacheIndicesByVarSeq.get(theBlock.getSNP(j-1))).intValue()];
                }
            }
            for (int i = 0; i < genos.length; i++){
                double aa=0,ab=0,bb=0,ba=0;
                for (int j = 0; j < genos.length; j++){
                    if (genos[j][0] == genos[0][0]){
                        if(!sameHap(genos[i], genos[j])){
                            ab += phasedCache[j].getPercentage();
                        }else{
                            aa += phasedCache[j].getPercentage();
                        }
                    }else{
                        if(!sameHap(genos[i], genos[j])){
                            bb += phasedCache[j].getPercentage();
                        }else{
                            ba += phasedCache[j].getPercentage();
                        }
                    }
                }
                //p is snp's freq, q is hap's freq
                double p = aa+ab;
                double q = ba+aa;
                //round to 5 decimal places.
                double rsq = Util.roundDouble(Math.pow((aa*bb - ab*ba),2)/(p*(1-p)*q*(1-q)),3);
                if (rsq > curBestRsq){
                    StringBuffer sb = new StringBuffer();
                    for (int j = 1; j < genos[i].length; j++){
                        sb.append(genos[i][j]);
                    }
                    curBestAllele = new Allele(theBlock,sb.toString());
                    curBestRsq = rsq;
                }
            }
            LocusCorrelation lc = new LocusCorrelation(curBestAllele, curBestRsq);
            lcByComparison.put(new Comparison(theSNP, theBlock),lc);
            return (lc);
        }
    }

    private boolean sameHap(int[] a, int[] b) {
        if(a == null || b == null) {
            throw new NullPointerException("blah");
        }
        if(a.length != b.length) {
            return false;
        }
        for(int i=1;i<a.length;i++) {
            if(a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public void phaseAndCache(HashSet snpList){
        phasedCacheIndicesByVarSeq = new Hashtable();
        try{
            //create a pseudo block of the SNP to be correlated and the markers from the multi-test
            int[] blockArray = new int[snpList.size()];
            Iterator itr = snpList.iterator();
            for (int i = 0; i < blockArray.length; i++){
                SNP n = (SNP) itr.next();
                blockArray[i] = ((Integer)indicesByVarSeq.get(n)).intValue();
                phasedCacheIndicesByVarSeq.put(n,new Integer(i));
            }

            Vector victor = new Vector();
            victor.add(blockArray);
            phasedCache = theData.generateHaplotypes(victor, true)[0];
        }catch (HaploViewException hve){
            throw new RuntimeException("PC_LOADLETTER.\n" + hve.getMessage());
        }
    }

    class Comparison{
        SNP s;
        Block b;

        public Comparison(SNP s, Block b){
            this.s = s;
            this.b = b;
        }

        public boolean equals(Object o){
            Comparison c = (Comparison) o;
            return (s.equals(c.s) && b.equals(c.b));
        }

        public int hashCode(){
            return s.hashCode() + b.hashCode();
        }
    }
}
