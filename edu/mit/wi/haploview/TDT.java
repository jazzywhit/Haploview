package edu.mit.wi.haploview;

import java.util.Vector;


public class TDT {
    Vector results;

    public TDT() {
        results = new Vector();
    }

    public Vector calcTDT(Vector chromosomes) {
        int numChroms;
        Chromosome chromT, chromU,chromTemp;
        String ped,ind;
        Vector temp;

        numChroms = chromosomes.size();
        temp = (Vector)chromosomes.clone();
        chromosomes = temp;

        int numMarkers = Chromosome.getSize();

        for(int k=0;k<numMarkers;k++){
            this.results.add(new TDTResult(Chromosome.getMarker(k)));
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
                byte allele1T = chrom1T.getGenotype(j);
                byte allele1U = chrom1U.getGenotype(j);
                byte allele2T = chrom2T.getGenotype(j);
                byte allele2U = chrom2U.getGenotype(j);

                TDTResult curRes = (TDTResult)results.get(j);

                //System.out.println("marker "+ j + ":\t "  + allele1T + "\t" + allele1U + "\t" + allele2T + "\t" + allele2U);
                curRes.tallyInd(allele1T,allele1U);
                curRes.tallyInd(allele2T,allele2U);


            }

        }
        for(int i=0;i<this.results.size();i++){
            TDTResult tempRes = (TDTResult)this.results.get(i);
            int[][] counts = tempRes.counts;
            System.out.println( counts[0][0] + "\t" + counts[1][1] + "\t" + counts[0][1] + "\t" + counts[1][0]);
        }
        return this.results;
    }
}
