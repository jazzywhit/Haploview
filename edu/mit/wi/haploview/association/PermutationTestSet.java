package edu.mit.wi.haploview.association;

import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.PedFileException;
import edu.mit.wi.pedfile.Individual;
import edu.mit.wi.haploview.*;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;


public class PermutationTestSet implements Constants{
    private int permutationCount;
    private int bestExceededCount;
    private double bestObsChiSq;
    private String bestObsName;

    private Vector theEMs;
    private PedFile pedFile;
    private AssociationTestSet testSet;

    private double[] permBestChiSq;


    public boolean stopProcessing;

    //this variable is updated as permutations are performed. Once
    //this variable reaches the value of permutationCount, permutation tests have all completed.
    private int permutationsPerformed;
    private double permBestOverallChiSq;

    public PermutationTestSet(int permCount, Vector ems, PedFile pf, AssociationTestSet ats){
        if(permCount > 0) {
            permutationCount = permCount;
        } else {
            permCount = 0;
        }

        theEMs = ems;
        pedFile = pf;
        testSet = ats;

        double curBest = 0;
        String curName = "";
        for(int i=0;i<ats.getFilteredResults().size();i++) {
            AssociationResult tmpRes = (AssociationResult) ats.getFilteredResults().get(i);
            for (int j = 0; j < tmpRes.getAlleleCount(); j++){
                if (tmpRes.getChiSquare(j) > curBest){
                    curName = tmpRes.getDisplayName(j);
                    curBest = tmpRes.getChiSquare(j);
                }
            }
        }
        bestObsChiSq = curBest;
        bestObsName = curName;
    }

    public void doPermutations() {
        stopProcessing = false;
        Vector curResults = null;

        Haplotype[][] haplotypes = new Haplotype[testSet.getHaplotypeAssociationResults().size()][];
        Iterator hitr = testSet.getHaplotypeAssociationResults().iterator();
        int count = 0;
        while (hitr.hasNext()){
            haplotypes[count] = ((HaplotypeAssociationResult)hitr.next()).getHaps();
            count++;
        }

        Vector snpSet = new Vector();
        Iterator sitr = testSet.getFilteredResults().iterator();
        while (sitr.hasNext()){
            Object o = sitr.next();
            if (o instanceof MarkerAssociationResult){
                SNP snp = ((MarkerAssociationResult)o).getSnp();
                snpSet.add(snp);
            }
        }

        //we need to make fake Haplotype objects so that we can use the getcounts() and getChiSq() methods of
        //AssociationResult. kludgetastic!
        Haplotype[][] fakeHaplos;

        if(haplotypes != null) {
            fakeHaplos = new Haplotype[haplotypes.length][];
            for(int j=0;j<haplotypes.length;j++) {

                fakeHaplos[j] =  new Haplotype[haplotypes[j].length];
                for(int k=0;k<haplotypes[j].length;k++) {
                    fakeHaplos[j][k] = new Haplotype(haplotypes[j][k].getGeno(),haplotypes[j][k].getPercentage(),
                            haplotypes[j][k].getMarkers());
                }
            }
        } else {
            fakeHaplos = new Haplotype[0][0];
        }

        //need to use the same affected status for marker and haplotype association tests in case control,
        //so affectedStatus stores the shuffled affected status
        Vector affectedStatus = null;
        //need to use the same coin toss for marker and haplotype association tests in trio tdt,
        //so permuteInd stores whether each individual is permuted
        Vector permuteInd = null;

        permutationsPerformed = 0;
        bestExceededCount = 0;
        double[] chiSqs = new double[permutationCount];
        permBestOverallChiSq = 0;
        //start the permuting!
        for(int i=0;i<permutationCount; i++) {
            //this variable gets set by the thread we're running if it wants us to stop
            if(stopProcessing) {
                break;
            }

            //begin single marker association test
            if (Options.getAssocTest() == ASSOC_TRIO){
                try {
                    permuteInd = new Vector();
                    for(int j =0;j<pedFile.getAllIndividuals().size();j++) {
                        if(Math.random() < .5) {
                            permuteInd.add(new Boolean(true));
                        } else {
                            permuteInd.add(new Boolean(false));
                        }
                    }

                    curResults = new AssociationTestSet(pedFile, permuteInd, snpSet).getMarkerAssociationResults();
                } catch(PedFileException pfe) {
                }
            }else if (Options.getAssocTest() == ASSOC_CC){
                Vector indList = pedFile.getUnrelatedIndividuals();
                affectedStatus = new Vector();
                for(int j=0;j<indList.size();j++) {
                    affectedStatus.add(new Integer(((Individual)indList.elementAt(j)).getAffectedStatus()));
                }
                Collections.shuffle(affectedStatus);
                try{
                    curResults = new AssociationTestSet(pedFile,affectedStatus, snpSet).getMarkerAssociationResults();
                }catch (PedFileException pfe){
                }
            }

            //end of marker association test

            //begin haplotype association test

            if(Options.getAssocTest() == ASSOC_TRIO) {
                for(int j=0;j<fakeHaplos.length;j++) {
                    EM curEM = (EM) theEMs.get(j);
                    curEM.doAssociationTests(null,permuteInd);
                    for(int k=0;k<fakeHaplos[j].length;k++) {
                        fakeHaplos[j][k].setTransCount(curEM.getTransCount(k));
                        fakeHaplos[j][k].setUntransCount(curEM.getUntransCount(k));
                    }
                }
            } else if(Options.getAssocTest() == ASSOC_CC) {
                for(int j=0;j<fakeHaplos.length;j++) {
                    EM curEM = (EM) theEMs.get(j);
                    curEM.doAssociationTests(affectedStatus,null);
                    for(int k=0;k<fakeHaplos[j].length;k++) {
                        fakeHaplos[j][k].setCaseCount(curEM.getCaseCount(k));
                        fakeHaplos[j][k].setControlCount(curEM.getControlCount(k));
                    }
                }
            }
            if(Options.getAssocTest() == ASSOC_TRIO || Options.getAssocTest() == ASSOC_CC) {
                AssociationTestSet ats = null;
                if (testSet.getFilterAlleles() == null){
                    ats = new AssociationTestSet(fakeHaplos, null);
                }else{
                    try{
                        ats = new AssociationTestSet(fakeHaplos,null,testSet.getFilterAlleles());
                    }catch (HaploViewException hve){
                    }
                }
                curResults.addAll(ats.getResults());
            }

            //end of haplotype association test

            //find the best chi square from all the tests
            double tempBestChiSquare = 0;
            for(int j=0; j<curResults.size();j++) {
                AssociationResult tempResult = (AssociationResult) curResults.elementAt(j);
                for (int k = 0; k < tempResult.getAlleleCount(); k++){
                    if(tempResult.getChiSquare(k) > tempBestChiSquare) {
                        tempBestChiSquare = tempResult.getChiSquare(k);
                    }
                }
            }

            chiSqs[i] = tempBestChiSquare;

            if (chiSqs[i] >= bestObsChiSq){
                bestExceededCount++;
            }

            if(chiSqs[i] > permBestOverallChiSq){
                permBestOverallChiSq = chiSqs[i];
            }
            permutationsPerformed++;
        }

        permBestChiSq = new double[permutationsPerformed];
        for (int i = 0; i < permutationsPerformed; i++){
            permBestChiSq[i] = chiSqs[i];
        }
        Arrays.sort(permBestChiSq);
    }

    public double getBestPermChiSquare() {
        return permBestOverallChiSq;
    }

    public double getPermPValue(double obsChiSq) {
        int observedExceeds =0;

        for (int i = 0; i < permutationsPerformed; i++){
            if (permBestChiSq[i] < obsChiSq){
                observedExceeds++;
            }else{
                break;
            }
        }

        return 1-((double)observedExceeds / (double)permutationsPerformed);
    }

    public int getPermutationsPerformed() {
        return permutationsPerformed;
    }

    public void setPermutationCount(int c) {
        if(c >= 0) {
            permutationCount = c;
        } else {
            permutationCount = 0;
        }
    }

    public int getPermutationCount() {
        return permutationCount;
    }

    public double getBestObsChiSq(){
        return bestObsChiSq;
    }

    public String getBestObsName(){
        return bestObsName;
    }

    public Vector getResults() {
        Vector results = new Vector();
        //dont loop through if we haven't done any permutations yet
        if(permutationsPerformed > 0) {
            for(int i=0;i<testSet.getFilteredResults().size();i++) {
                AssociationResult tmpRes = (AssociationResult) testSet.getFilteredResults().get(i);
                for (int j = 0; j < tmpRes.getAlleleCount(); j++){
                    Vector fieldValues = new Vector();
                    fieldValues.add(tmpRes.getDisplayName(j));
                    fieldValues.add(String.valueOf(tmpRes.getChiSquare(j)));
                    fieldValues.add(Util.formatPValue(getPermPValue(tmpRes.getChiSquare(j))));

                    results.add(fieldValues);
                    if (tmpRes instanceof MarkerAssociationResult){
                        break;
                    }
                }
            }

            Collections.sort(results,new SigResComparator());
        }
        return results;
    }

    public int getBestExceededCount() {
        return bestExceededCount;
    }

    public double[] getPermBestChiSq() {
        return permBestChiSq;
    }

    public void writeResultsToFile(File outFile) throws IOException {
        Vector results = getResults();

        BufferedWriter out = new BufferedWriter(new FileWriter(outFile));

        out.write("#"+getPermutationsPerformed()+" permutations performed.");
        out.newLine();

        if (getPermutationsPerformed() > 0){
            out.write("Name\tChi Square\tPermutation p-value");
            out.newLine();
            for(int i=0;i<results.size();i++) {
                Vector tempRes = (Vector) results.get(i);
                StringBuffer line = new StringBuffer();
                for(int j=0;j<tempRes.size()-1;j++) {
                    line.append(tempRes.get(j));
                    line.append("\t");
                }
                line.append(tempRes.get(tempRes.size()-1));
                out.write(line.toString());
                out.newLine();
            }
        }
        out.close();
    }

    class SigResComparator implements Comparator{
        public int compare(Object o1, Object o2) {
            Double d1 = Double.valueOf(((String)((Vector)o1).elementAt(2)));
            Double d2 = Double.valueOf(((String)((Vector)o2).elementAt(2)));
            return d1.compareTo(d2);
        }
    }
}
