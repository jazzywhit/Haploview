package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.Haplotype;
import edu.mit.wi.haploview.Options;
import edu.mit.wi.haploview.Constants;
import edu.mit.wi.haploview.Chromosome;
import edu.mit.wi.pedfile.*;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Vector;
import java.util.Iterator;
import java.util.Arrays;


public class AssociationResult implements Constants{
    NumberFormat nf = NumberFormat.getInstance(Locale.US);

    //alleles contains Haplotype objects for the alleles of the current site
    private Vector alleles = new Vector();

    //filteredAlleles contains the alleles which pass the frequency cutoff
    private Vector filteredAlleles = new Vector();

    //chiSquares contains Doubles storing the chiSquare values for the alleles
    private Vector chiSquares;

    //pValues contains Double storing the p-values for the alleles
    private Vector pValues;

    //frequencyCutoff is the minimum frequency for an allele to have statistics calculated
    private double frequencyCutoff;

    //name is the name
    private String name;

    public AssociationResult(Haplotype[] locusHaplos, int freqCutoff, String n) {
        for (int i = 0; i < locusHaplos.length; i++){
            alleles.add(locusHaplos[i]);
        }
        setFrequencyCutoff(((double)freqCutoff)/100);
        name = n;
    }

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
        return ((Haplotype) alleles.get(i)).toString();
    }

    /**
     *
     * @return number of alleles for this block or marker
     */
    public int getAlleleCount(){
        return filteredAlleles.size();
    }

    public double getChiSquare(int i) {
        double formattedCS = Math.rint(((Double)chiSquares.get(i)).doubleValue()*1000.0)/1000.0;
        return formattedCS;
    }

    public String getHapCountString(int i){
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);

        Haplotype h = (Haplotype) alleles.get(i);
        StringBuffer countSB = new StringBuffer();
        if(Options.getAssocTest() == ASSOC_TRIO) {
            countSB.append(nf.format(h.getTransCount())).append(" : ").append(nf.format(h.getUntransCount()));
        } else if(Options.getAssocTest() == ASSOC_CC) {
            double caseSum = 0, controlSum = 0;
            for (int j = 0; j < this.getAlleleCount(); j++){
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

    public String getHapFreqString(int i ){
        if (Options.getAssocTest() == ASSOC_TRIO){
            return "";
        }

        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);

        StringBuffer countSB = new StringBuffer();
        Haplotype h = (Haplotype) alleles.get(i);
        double caseSum = 0, controlSum = 0;
        for (int j = 0; j < this.getAlleleCount(); j++){
            caseSum += ((Haplotype)alleles.get(j)).getCaseCount();
            controlSum += ((Haplotype)alleles.get(j)).getControlCount();
        }
        countSB.append(nf.format(h.getCaseCount()/caseSum)).append(", ");
        countSB.append(nf.format(h.getControlCount()/controlSum));

        return countSB.toString();
    }

    public String getSNPCountString(){
        Haplotype h1 = (Haplotype) alleles.get(0);
        Haplotype h2 = (Haplotype) alleles.get(1);

        if (Options.getAssocTest() == ASSOC_TRIO){
            if (h1.getTransCount() > h2.getTransCount()){
                return (int)h1.getTransCount() + ":" + (int)h2.getTransCount();
            }else{
                return (int)h2.getTransCount() + ":" + (int)h1.getTransCount();
            }
        }else{
            if (h1.getCaseCount() > h2.getCaseCount()){
                if (h1.getControlCount() > h2.getControlCount()){
                    return (int)h1.getCaseCount() + ":" + (int)h2.getCaseCount() +
                            ", " + (int)h1.getControlCount() + ":" + (int)h2.getControlCount();
                }else{
                    return (int)h1.getCaseCount() + ":" + (int)h2.getCaseCount() +
                            "," + (int)h2.getControlCount() + ":" + (int)h1.getControlCount();
                }
            }else{
                if (h1.getControlCount() > h2.getControlCount()){
                    return (int)h2.getCaseCount() + ":" + (int)h1.getCaseCount() +
                            ", " + (int)h1.getControlCount() + ":" + (int)h2.getControlCount();
                }else{
                    return (int)h2.getCaseCount() + ":" + (int)h1.getCaseCount() +
                            "," + (int)h2.getControlCount() + ":" + (int)h1.getControlCount();
                }
            }
        }
    }

    public String getSNPFreqString(){
        Haplotype h1 = (Haplotype) alleles.get(0);
        Haplotype h2 = (Haplotype) alleles.get(1);

        if(Options.getAssocTest() == ASSOC_TRIO){
            return "";
        }

        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        if (h1.getCaseCount() > h2.getCaseCount()){
            if (h1.getControlCount() > h2.getControlCount()){
                return nf.format(h1.getCaseCount()/ (h1.getCaseCount() + h2.getCaseCount())) +
                        ", " + nf.format(h1.getControlCount() / ( h1.getControlCount() + h2.getControlCount()));
            }else{
                return nf.format(h1.getCaseCount()/ (h1.getCaseCount() + h2.getCaseCount())) +
                        ", " + nf.format(h2.getControlCount() / ( h2.getControlCount() + h1.getControlCount()));
            }
        }else{
            if (h1.getControlCount() > h2.getControlCount()){
                return nf.format(h2.getCaseCount()/ (h2.getCaseCount() + h1.getCaseCount())) +
                        ", " + nf.format(h1.getControlCount() / ( h1.getControlCount() + h2.getControlCount()));
            }else{
                return nf.format(h2.getCaseCount()/ (h2.getCaseCount() + h1.getCaseCount())) +
                        ", " + nf.format(h2.getControlCount() / ( h2.getControlCount() + h1.getControlCount()));
            }
        }
    }

    public String getOverTransmittedAllele() {
        Haplotype h1 = (Haplotype) alleles.get(0);
        Haplotype h2 = (Haplotype) alleles.get(1);

        String retStr;

        if (Options.getAssocTest() == ASSOC_TRIO){
            if (h1.getTransCount() > h2.getTransCount()){
                retStr = getAlleleName(0);
            }else if (h1.getTransCount() == h2.getTransCount()){
                retStr = "-";
            }else{
                retStr = getAlleleName(1);
            }
        }else{
            if (h1.getCaseCount() > h2.getCaseCount()){
                retStr = getAlleleName(0);
            }else if (h1.getCaseCount() == h2.getCaseCount()){
                retStr = "-";
            }else{
                retStr = getAlleleName(1);
            }

            if (h1.getControlCount() > h2.getControlCount()){
                retStr += (", " + getAlleleName(0));
            }else if (h1.getControlCount() == h2.getControlCount()){
                retStr += ", -";
            }else{
                retStr += (", " + getAlleleName(1));
            }
        }
        return retStr;
    }

    public void setFrequencyCutoff(double f) {
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

    private void calculateChiSquares() {
        chiSquares = new Vector();
        pValues = new Vector();
        if (Options.getAssocTest() == ASSOC_TRIO) {
            Iterator faitr = filteredAlleles.iterator();
            while(faitr.hasNext()) {
                Haplotype curHap = (Haplotype) faitr.next();
                double chiSq = Math.pow(curHap.getTransCount() - curHap.getUntransCount(),2) / (curHap.getTransCount() + curHap.getUntransCount());
                chiSquares.add(new Double(chiSq));
            }
        } else if(Options.getAssocTest() == ASSOC_CC) {
            double caseSum =0;
            double controlSum = 0;
            Iterator faitr = filteredAlleles.iterator();
            while(faitr.hasNext()) {
                Haplotype curHap = (Haplotype) faitr.next();
                caseSum += curHap.getCaseCount();
                controlSum += curHap.getControlCount();
            }

            double chiSq = 0;
            double tempCaseSum, tempControlSum;
            double totalSum = caseSum + controlSum;
            faitr = filteredAlleles.iterator();
            while(faitr.hasNext()) {
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
        double pval = ((Double)pValues.get(i)).doubleValue();
        DecimalFormat df;
        //java truly sucks for simply restricting the number of sigfigs but still
        //using scientific notation when appropriate
        if (pval < 0.0001){
            df = new DecimalFormat("0.0000E0", new DecimalFormatSymbols(Locale.US));
        }else{
            df = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
        }
        String formattedNumber =  df.format(pval, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
        return formattedNumber;
    }

    public static Vector getAssociationResults(Haplotype[][] haplos){
        Vector results = new Vector();
        for (int i = 0; i < haplos.length; i++){
            String blockname = "Block " + (i+1);
            results.add(new AssociationResult(haplos[i], Options.getHaplotypeDisplayThreshold(),blockname));
        }

        return results;
    }

    public static Vector getCCAssociationResults(PedFile pf, Vector affectedStatus){
        Vector results = new Vector();
        int numMarkers = Chromosome.getUnfilteredSize();

        Vector indList = pf.getUnrelatedIndividuals();

        if(affectedStatus == null || affectedStatus.size() != indList.size()) {
            affectedStatus = new Vector();
            for(int i=0;i<indList.size();i++) {
                Individual tempInd = ((Individual)indList.elementAt(i));
                affectedStatus.add(new Integer(tempInd.getAffectedStatus()));
            }
        }


        boolean[] useable = new boolean[indList.size()];
        Arrays.fill(useable, false);

        //this loop determines who is eligible to be used for the case/control association test
        for(int i=0;i<useable.length;i++) {

            Individual tempInd = ((Individual)indList.elementAt(i));
            Family tempFam = pf.getFamily(tempInd.getFamilyID());

            //need to check to make sure we don't include both parents and kids of trios
            //so, we only set useable[i] to true if Individual at index i is not the child of a trio in the indList
            if (!(tempFam.containsMember(tempInd.getMomID()) &&
                    tempFam.containsMember(tempInd.getDadID()))){
                useable[i] = true;
            } else{
                try{
                    if (!(indList.contains(tempFam.getMember(tempInd.getMomID())) ||
                            indList.contains(tempFam.getMember(tempInd.getDadID())))){
                        useable[i] = true;
                    }
                }catch (PedFileException pfe){
                }
            }
        }

        for (int i = 0; i < numMarkers; i++){
            byte allele1 = 0, allele2 = 0;
            int[][] counts = new int[2][2];
            Individual currentInd;
            for (int j = 0; j < indList.size(); j++){
                //need to check below to make sure we don't include parents and kids of trios
                currentInd = (Individual)indList.elementAt(j);
                int cc = ((Integer)affectedStatus.get(j)).intValue();
                byte[] a = currentInd.getMarker(i);
                if(useable[j]) {
                    if (cc == 0) continue;
                    if (cc == 2) cc = 0;
                    byte a1 = a[0];
                    byte a2 = a[1];

                    if (a1 >= 5 && a2 >= 5){
                        counts[cc][0]++;
                        counts[cc][1]++;
                        if (allele1 == 0){
                            allele1 = (byte)(a1 - 4);
                            allele2 = (byte)(a2 - 4);
                        }
                    }else{
                        //seed the alleles as soon as they're found
                        if (allele1 == 0){
                            allele1 = a1;
                            if (a1 != a2){
                                allele2 = a2;
                            }
                        }else if (allele2 == 0){
                            if (a1 != allele1){
                                allele2 = a1;
                            }else if (a2 != allele1){
                                allele2 = a2;
                            }
                        }

                        if (a1 != 0){
                            if (a1 == allele1){
                                counts[cc][0] ++;
                            }else{
                                counts[cc][1] ++;
                            }
                        }
                        if (a2 != 0){
                            if (a2 == allele1){
                                counts[cc][0]++;
                            }else{
                                counts[cc][1]++;
                            }
                        }
                    }
                }
            }
            int[] g1 = {allele1};
            int[] g2 = {allele2};
            int[] m  = {i};
            double d = counts[0][0] + counts[0][1] + counts [1][0] + counts[1][1];
            Haplotype thisSNP1 = new Haplotype(g1, ((double)(counts[0][0] + counts[1][0]))/d, m);
            thisSNP1.setCaseCount(counts[0][0]);
            thisSNP1.setControlCount(counts[1][0]);
            Haplotype thisSNP2 = new Haplotype(g2, ((double)(counts[0][1] + counts[1][1]))/d, m);
            thisSNP2.setCaseCount(counts[0][1]);
            thisSNP2.setControlCount(counts[1][1]);

            Haplotype[] daBlock = {thisSNP1, thisSNP2};
            results.add(new AssociationResult(daBlock, 0, Chromosome.getMarker(i).getName()));
        }

        return results;
    }

    public static Vector getTDTAssociationResults(PedFile pf, boolean[] permuteInd) throws PedFileException{
        Vector results = new Vector();

        Vector indList = pf.getAllIndividuals();

        if(permuteInd == null || permuteInd.length != indList.size()) {
            permuteInd = new boolean[indList.size()];
            Arrays.fill(permuteInd, false);
        }

        int numMarkers = Chromosome.getUnfilteredSize();
        for (int i = 0; i < numMarkers; i++){
            Individual currentInd;
            Family currentFam;
            TallyTrio tt = new TallyTrio();
            for (int j = 0; j < indList.size(); j++){
                currentInd = (Individual)indList.elementAt(j);
                currentFam = pf.getFamily(currentInd.getFamilyID());
                if (currentFam.containsMember(currentInd.getMomID()) &&
                        currentFam.containsMember(currentInd.getDadID()) &&
                        currentInd.getAffectedStatus() == 2){
                    //if he has both parents, and is affected, we can get a transmission
                    Individual mom = currentFam.getMember(currentInd.getMomID());
                    Individual dad = currentFam.getMember(currentInd.getDadID());
                    byte[] thisMarker = currentInd.getMarker(i);
                    byte kid1 = thisMarker[0];
                    byte kid2 = thisMarker[1];
                    thisMarker = dad.getMarker(i);
                    byte dad1 = thisMarker[0];
                    byte dad2 = thisMarker[1];
                    thisMarker = mom.getMarker(i);
                    byte mom1 = thisMarker[0];
                    byte mom2 = thisMarker[1];
                    byte momT=0, momU=0, dadT=0, dadU=0;
                    if (kid1 == 0 || kid2 == 0 || dad1 == 0 || dad2 == 0 || mom1 == 0 || mom2 == 0) {
                        continue;
                    } else if (kid1 == kid2) {
                        //kid homozygous
                        if (dad1 == kid1) {
                            dadT = dad1;
                            dadU = dad2;
                        } else {
                            dadT = dad2;
                            dadU = dad1;
                        }

                        if (mom1 == kid1) {
                            momT = mom1;
                            momU = mom2;
                        } else {
                            momT = mom2;
                            momU = mom1;
                        }
                    } else {
                        if (dad1 == dad2 && mom1 != mom2) {
                            //dad hom mom het
                            dadT = dad1;
                            dadU = dad2;
                            if (kid1 == dad1) {
                                momT = kid2;
                                momU = kid1;
                            } else {
                                momT = kid1;
                                momU = kid2;
                            }
                        } else if (mom1 == mom2 && dad1 != dad2) {
                            //dad het mom hom
                            momT = mom1;
                            momU = mom2;
                            if (kid1 == mom1) {
                                dadT = kid2;
                                dadU = kid1;
                            } else {
                                dadT = kid1;
                                dadU = kid2;
                            }
                        } else if (dad1 == dad2 && mom1 == mom2) {
                            //mom & dad hom
                            dadT = dad1;
                            dadU = dad1;
                            momT = mom1;
                            momU = mom1;
                        } else {
                            //everybody het
                            dadT = (byte)(4+dad1);
                            dadU = (byte)(4+dad2);
                            momT = (byte)(4+mom1);
                            momU = (byte)(4+mom2);
                        }
                    }
                    if(permuteInd[j]) {
                        tt.tallyTrioInd(dadU, dadT);
                        tt.tallyTrioInd(momU, momT);
                    } else {
                        tt.tallyTrioInd(dadT, dadU);
                        tt.tallyTrioInd(momT, momU);
                    }
                }
            }
            int[] g1 = {tt.allele1};
            int[] g2 = {tt.allele2};
            int[] m  = {i};
            double d = tt.counts[0][0] + tt.counts[0][1] + tt.counts [1][0] + tt.counts[1][1];
            Haplotype thisSNP1 = new Haplotype(g1, (tt.counts[0][0] + tt.counts[1][0])/d, m);
            thisSNP1.setTransCount(tt.counts[0][0]);
            thisSNP1.setUntransCount(tt.counts[1][0]);
            Haplotype thisSNP2 = new Haplotype(g2, (tt.counts[0][1] + tt.counts[1][1])/d, m);
            thisSNP2.setTransCount(tt.counts[0][1]);
            thisSNP2.setUntransCount(tt.counts[1][1]);

            Haplotype[] daBlock = {thisSNP1, thisSNP2};
            results.add(new AssociationResult(daBlock, 0, Chromosome.getMarker(i).getName()));
        }
        return results;
    }

    public double getFreq(int j) {
        return ((Haplotype)alleles.get(j)).getPercentage();
    }

    private static class TallyTrio {
        int allele1=0, allele2=0;
        int[][] counts = new int[2][2];
        boolean tallyHet = true;

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
    }
}
