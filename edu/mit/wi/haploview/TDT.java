package edu.mit.wi.haploview;

import java.util.Vector;


public class TDT {
    public static Vector calcCCTDT(Vector chromosomes){
        Vector results = new Vector();
        int numMarkers = Chromosome.getFilteredSize();
        for (int i = 0; i < numMarkers; i++){
            TDTResult thisResult = new TDTResult(Chromosome.getMarker(i));
            for (int j = 0; j < chromosomes.size()-1; j++){
                Chromosome theChrom = (Chromosome)chromosomes.get(j);
                j++;
                Chromosome nextChrom = (Chromosome)chromosomes.get(j);
                if (theChrom.getAffected()){
                    thisResult.tallyCCInd(theChrom.getFilteredGenotype(i), nextChrom.getFilteredGenotype(i), 0);
                }else{
                    thisResult.tallyCCInd(theChrom.getFilteredGenotype(i), nextChrom.getFilteredGenotype(i), 1);
                }
            }
            results.add(thisResult);
        }

        return results;
    }

    public static Vector calcTrioTDT(Vector chromosomes) {
        Vector results = new Vector();
        int numMarkers = Chromosome.getFilteredSize();

        for(int k=0;k<numMarkers;k++){
            results.add(new TDTResult(Chromosome.getMarker(k)));
        }

        for(int i=0;i<chromosomes.size()-3;i++){
            Chromosome chrom1T = (Chromosome)chromosomes.get(i);
            i++;
            Chromosome chrom1U = (Chromosome)chromosomes.get(i);
            i++;
            Chromosome chrom2T = (Chromosome)chromosomes.get(i);
            i++;
            Chromosome chrom2U = (Chromosome)chromosomes.get(i);


            //System.out.println("ind1T: " + chrom1T.getPed() + "\t" + chrom1T.getIndividual() );
            //System.out.println("ind1U: " + chrom1U.getPed() + "\t" + chrom1U.getIndividual() );
            //System.out.println("ind2T: " + chrom2T.getPed() + "\t" + chrom2T.getIndividual() );
            //System.out.println("ind2U: " + chrom2U.getPed() + "\t" + chrom2U.getIndividual() );

            for(int j=0;j<numMarkers;j++){
                if(!chrom1T.kidMissing[j] && !chrom2T.kidMissing[j]) {
                    byte allele1T = chrom1T.getFilteredGenotype(j);
                    byte allele1U = chrom1U.getFilteredGenotype(j);
                    byte allele2T = chrom2T.getFilteredGenotype(j);
                    byte allele2U = chrom2U.getFilteredGenotype(j);

                    if( !(allele1T == 0 || allele1U == 0 || allele2T == 0 || allele2U == 0) ){
                        TDTResult curRes = (TDTResult)results.get(j);
                        curRes.tallyTrioInd(allele1T,allele1U);
                        curRes.tallyTrioInd(allele2T,allele2U);
                    }
                }
            }

        }
        return results;
    }
}
