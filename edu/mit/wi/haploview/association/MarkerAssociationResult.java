package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.Haplotype;
import edu.mit.wi.haploview.Options;
import edu.mit.wi.haploview.Chromosome;
import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.Individual;
import edu.mit.wi.pedfile.Family;
import edu.mit.wi.pedfile.PedFileException;

import java.util.Vector;
import java.util.Arrays;

public class MarkerAssociationResult extends AssociationResult{

    public MarkerAssociationResult(Haplotype[] locusHaplos, String n) {
        nf.setGroupingUsed(false);

        for (int i = 0; i < locusHaplos.length; i++){
            alleles.add(locusHaplos[i]);
        }
        setFrequencyCutoff(0);
        name = n;
    }

    public String getCountString(){
        return getCountString(0);
    }

    public String getFreqString(){
        return getFreqString(0);
    }

    public String getDisplayName(int i) {
        return this.getName();
    }

    public String getCountString(int i){
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

    public String getFreqString(int i){
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
            results.add(new MarkerAssociationResult(daBlock, Chromosome.getMarker(i).getName()));
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
            results.add(new MarkerAssociationResult(daBlock, Chromosome.getUnfilteredMarker(i).getName()));
        }
        return results;
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
}
