package edu.mit.wi.haploview.association;

import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.PedFileException;
import edu.mit.wi.pedfile.Individual;
import edu.mit.wi.haploview.*;

import java.util.Vector;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;


public class PermutationTestSet implements Constants{
    private int permutationCount;

    private HaploData theData;
    private PedFile pedFile;
    private Vector observedResults;

    private double[] permBestChiSq;
    private Haplotype[][] haplotypes;


    public boolean stopProcessing;

    //this variable is updated as permutations are performed. Once
    //this variable reaches the value of permutationCount, permutation tests have all completed.
    private int permutationsPerformed;
    private double permBestOverallChiSq;

    /**
     *
     * @param permCount the number of permutations to do
     * @param hd  the Haplodata object
     * @param obsResults all the marker and haplotype association results observed
     */
    public PermutationTestSet(int permCount, HaploData hd , Vector obsResults) {
        if(permCount > 0) {
            permutationCount = permCount;
        } else {
            permCount = 0;
        }

        theData = hd;
        pedFile = theData.getPedFile();
        observedResults = obsResults;
    }

    public void doPermutations() {
        stopProcessing = false;
        Vector curResults = null;

        haplotypes = (Haplotype[][]) theData.getRawHaplotypes().clone();

        Vector theEMs = theData.getSavedEMs();

        //we need to make fake Haplotype objects so that we can use the getcounts() and getChiSq() methods of
        //AssociationResult. kludgetastic!
        Haplotype[][] fakeHaplos;

        if(haplotypes != null) {
            fakeHaplos = new Haplotype[haplotypes.length][];
            for(int j=0;j<haplotypes.length;j++) {

                fakeHaplos[j] =  new Haplotype[haplotypes[j].length];
                for(int k=0;k<haplotypes[j].length;k++) {
                    fakeHaplos[j][k] = new Haplotype(new int[0],haplotypes[j][k].getPercentage(),null);
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
        boolean[] permuteInd = new boolean[pedFile.getAllIndividuals().size()];

        permutationsPerformed = 0;
        permBestChiSq = new double[permutationCount];
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
                    for(int j =0;j<permuteInd.length;j++) {
                        if(Math.random() < .5) {
                            permuteInd[j] = true;
                        } else {
                            permuteInd[j] = false;
                        }
                    }

                    curResults = MarkerAssociationResult.getTDTAssociationResults(pedFile, permuteInd);
                } catch(PedFileException pfe) {
                }
            }else if (Options.getAssocTest() == ASSOC_CC){
                Vector indList = pedFile.getUnrelatedIndividuals();
                affectedStatus = new Vector();
                for(int j=0;j<indList.size();j++) {
                    affectedStatus.add(new Integer(((Individual)indList.elementAt(j)).getAffectedStatus()));
                }
                Collections.shuffle(affectedStatus);
                curResults = MarkerAssociationResult.getCCAssociationResults(pedFile,affectedStatus);
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
                curResults.addAll(HaplotypeAssociationResult.getAssociationResults(fakeHaplos));
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

            permBestChiSq[i] = tempBestChiSquare;

            if(permBestChiSq[i] > permBestOverallChiSq){
                permBestOverallChiSq = permBestChiSq[i];
            }
            permutationsPerformed++;
        }

        Arrays.sort(permBestChiSq);
    }

    public double getBestChiSquare() {
        return permBestOverallChiSq;
    }

    public double getPermPValue(double obsChiSq) {
        int exceedObserved =0;

        int i = permBestChiSq.length - permutationsPerformed;

        while(i < permBestChiSq.length && permBestChiSq[i] < obsChiSq) {
            exceedObserved++;
            i++;
        }

        return 1-((double)exceedObserved / (double)permutationsPerformed);
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

    public AssociationResult getBestObsChiSq(){
        double curBest = 0;
        AssociationResult best = null;
        for(int i=0;i<observedResults.size();i++) {
            AssociationResult tmpRes = (AssociationResult) observedResults.get(i);
            for (int j = 0; j < tmpRes.getAlleleCount(); j++){
                if (tmpRes.getChiSquare(j) > curBest){
                    best = tmpRes;
                    curBest = tmpRes.getChiSquare(j);
                }
            }
        }

        return best;
    }

    public Vector getResults() {
        Vector results = new Vector();
        for(int i=0;i<observedResults.size();i++) {
            AssociationResult tmpRes = (AssociationResult) observedResults.get(i);
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
        return results;
    }

    class SigResComparator implements Comparator{
        public int compare(Object o1, Object o2) {
            Double d1 = Double.valueOf(((String)((Vector)o1).elementAt(2)));
            Double d2 = Double.valueOf(((String)((Vector)o2).elementAt(2)));
            return d1.compareTo(d2);
        }
    }
}
