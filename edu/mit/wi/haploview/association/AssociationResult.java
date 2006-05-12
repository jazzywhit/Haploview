package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.Haplotype;
import edu.mit.wi.haploview.Options;
import edu.mit.wi.haploview.Constants;
import edu.mit.wi.haploview.Util;
import edu.mit.wi.pedfile.*;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;
import java.util.Iterator;


public abstract class AssociationResult implements Constants{
    static NumberFormat nf = NumberFormat.getInstance(Locale.US);

    //alleles contains Haplotype objects for the alleles of the current site
    protected Vector alleles = new Vector();

    //filteredAlleles contains the alleles which pass the frequency cutoff
    protected Vector filteredAlleles = new Vector();

    //chiSquares contains Doubles storing the chiSquare values for the alleles
    protected Vector chiSquares;

    //pValues contains Double storing the p-values for the alleles
    protected Vector pValues;

    //frequencyCutoff is the minimum frequency for an allele to have statistics calculated
    protected double frequencyCutoff;

    //name is the name
    protected String name;

    /**
     *
     * @return name of block or marker
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param i allele number
     * @return name of i-th allele
     */
    public String getAlleleName(int i) {
        return ((Haplotype) filteredAlleles.get(i)).toString();
    }

    public String getNumericAlleleName(int i){
        return ((Haplotype) filteredAlleles.get(i)).toNumericString();
    }

    public abstract String getDisplayName(int i);

    /**
     *
     * @return number of alleles for this block or marker
     */
    public int getAlleleCount(){
        return filteredAlleles.size();
    }

    public double getChiSquare(int i) {
        if (chiSquares == null || i >= chiSquares.size()){
            return 0;
        }
        double formattedCS = Util.roundDouble(((Double)chiSquares.get(i)).doubleValue(),3);
        return formattedCS;
    }

    public abstract String getCountString(int i);

    public abstract String getFreqString(int i);

    public void filterByFrequency(double f) {
        filteredAlleles.removeAllElements();
        if(f >= 0 && f<=1) {
            frequencyCutoff = f;
            for(int i=0;i<alleles.size();i++) {
                Haplotype curHap = (Haplotype) alleles.get(i);
                if(curHap.getPercentage() >= frequencyCutoff) {
                    filteredAlleles.add(curHap);
                }
            }
        }
        calculateChiSquares();
    }

    protected void calculateChiSquares() {
        chiSquares = new Vector();
        pValues = new Vector();
        if (Options.getAssocTest() == ASSOC_TRIO) {
            Iterator faitr = filteredAlleles.iterator();
            while(faitr.hasNext()) {
                if(Options.getTdtType() == TDT_STD) {
                    Haplotype curHap = (Haplotype) faitr.next();
                    double chiSq = Math.pow(curHap.getTransCount() - curHap.getUntransCount(),2) / (curHap.getTransCount() + curHap.getUntransCount());
                    chiSquares.add(new Double(chiSq));
                }else if (Options.getTdtType() == TDT_PAREN) {
                    Haplotype curHap = (Haplotype) faitr.next();
                    double[] counts = curHap.getDiscordantAlleleCounts();
                    //statistic is [T+d+h+2g - (U+b+f+2c)]^2  /  [T+U+d+h+b+f+4(c+g)] distributed as a chi-square
                    double numr =  Math.pow(curHap.getTransCount() + counts[3] + counts[7] + 2*counts[6]
                            - (curHap.getUntransCount() + counts[1] + counts[5] + 2*counts[2]),2);
                    double denom = (curHap.getTransCount() + curHap.getUntransCount() + counts[3] + counts[7] + counts[1] + counts[5] + 4*(counts[2] + counts[6])); 

                    double chiSq = numr / denom;
                    chiSquares.add(new Double(chiSq));
                }
            }
        } else if(Options.getAssocTest() == ASSOC_CC) {
            double caseSum =0;
            double controlSum = 0;
            Iterator aitr = alleles.iterator();
            while(aitr.hasNext()) {
                Haplotype curHap = (Haplotype) aitr.next();
                caseSum += curHap.getCaseCount();
                controlSum += curHap.getControlCount();
            }

            double chiSq;
            double tempCaseSum, tempControlSum;
            double totalSum = caseSum + controlSum;
            Iterator faitr = filteredAlleles.iterator();
            while(faitr.hasNext()) {
                chiSq = 0;
                Haplotype curHap = (Haplotype) faitr.next();
                tempCaseSum = caseSum - curHap.getCaseCount();
                tempControlSum = controlSum - curHap.getControlCount();

                double nij = (caseSum * (curHap.getCaseCount() + curHap.getControlCount())) / totalSum;
                chiSq += Math.pow(curHap.getCaseCount() - nij,2) / nij;

                nij = (caseSum * (tempCaseSum + tempControlSum)) / totalSum;
                chiSq += Math.pow(tempCaseSum - nij,2) / nij;

                nij = (controlSum * (curHap.getCaseCount() + curHap.getControlCount())) / totalSum;
                chiSq += Math.pow(curHap.getControlCount() - nij,2) / nij;

                nij = (controlSum * (tempCaseSum + tempControlSum)) / totalSum;
                chiSq += Math.pow(tempControlSum - nij,2) / nij;

                chiSquares.add(new Double(chiSq));
            }
        }

        for(int i=0;i<chiSquares.size();i++) {
            pValues.add(new Double(MathUtil.gammq(.5,.5*(((Double)chiSquares.get(i)).doubleValue()))));
        }
    }

    public String getPValue(int i) {
        if (pValues == null || i >= pValues.size()){
            return "";
        }
        return Util.formatPValue(((Double)pValues.get(i)).doubleValue());
    }

    public String getFreq(int i) {
        double freq = ((Haplotype)alleles.get(i)).getPercentage();

        if (freq < 0){
            return ("");
        }else{
            nf.setMinimumFractionDigits(3);
            nf.setMaximumFractionDigits(3);
            return nf.format(freq);
        }
    }

    protected static class TallyTrio {
        int allele1=0, allele2=0;
        int[][] counts = new int[2][2];
        boolean tallyHet = true;

        //for parenTDT
        // values are {a b c d e f g h i} from matrix of values
        int[] discordantAlleleCounts = new int[9];
        int discordantTallied=0;

        public void tallyTrioInd(byte alleleT, byte alleleU){
            if(alleleT >= 5 && alleleU >= 5) {
                if(tallyHet){
                    counts[0][0]++;
                    counts[1][1]++;
                    counts[0][1]++;
                    counts[1][0]++;
                    this.tallyHet = false;
                }
                else {
                    this.tallyHet = true;
                }
            }
            else if( (alleleT != alleleU) && (alleleT!=0) && (alleleU!=0) ) {
                if(allele1==0 && allele2==0 ) {
                    allele1 = alleleT;
                    allele2 = alleleU;
                }

                if(alleleT == allele1) {
                    counts[0][0]++;
                }
                else if(alleleT==allele2) {
                    counts[0][1]++;
                }
                if(alleleU == allele1){
                    counts[1][0]++;
                }
                else if(alleleU == allele2) {
                    counts[1][1]++;
                }

            }
        }

        public void tallyDiscordantParents(byte affected1, byte affected2, byte unaffected1, byte unaffected2) {
            if(affected1 >= 5 && affected2 >= 5 && unaffected1 >= 5 && unaffected2 >=5) {
                return;
            }
            if(affected1 == affected2 && unaffected1 == unaffected2 && affected1 == unaffected1){
                return;
            }
            if(affected1 == 0 || affected2 == 0 || unaffected1 == 0 || unaffected2 == 0) {
                return;
            }
            discordantTallied++;


            if(affected1 != affected2 ) {
                if(unaffected1 == allele1 && unaffected2 == allele1) {
                    discordantAlleleCounts[1]++;
                }else if (unaffected1 == allele2 && unaffected2 == allele2) {
                    discordantAlleleCounts[7]++;
                }
            }else if(affected1 == allele1 && affected2 == allele1) {
                if(unaffected1 != unaffected2 ) {
                    discordantAlleleCounts[3]++;
                }else if(unaffected1 == allele2 && unaffected2 == allele2) {
                    discordantAlleleCounts[6]++;
                }
            }else if(affected1 == allele2 && affected2 == allele2) {
                if(unaffected1 != unaffected2) {
                    discordantAlleleCounts[5]++;
                }else if(unaffected1 == allele1 && unaffected2 == allele1) {
                    discordantAlleleCounts[2]++;
                }
            }
        }

        public int[] getDiscordantCountsAllele2() {
            int[] counts = new int[9];
            for(int i=0;i<9;i++) {
                counts[i] = discordantAlleleCounts[8-i];
            }
            return counts;
        }
    }
}
