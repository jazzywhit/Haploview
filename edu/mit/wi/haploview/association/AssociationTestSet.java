package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.*;
import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.Individual;
import edu.mit.wi.pedfile.Family;
import edu.mit.wi.pedfile.PedFileException;

import java.util.*;
import java.io.*;

public class AssociationTestSet implements Constants{

    private Vector tests;
    private Vector results;
    private HashSet whitelist;

    public AssociationTestSet(){
        results = new Vector();
        whitelist = new HashSet();
    }

    public AssociationTestSet(PedFile pf, Vector permute, Vector snpsToBeTested) throws PedFileException{
        whitelist = new HashSet();

        if (Options.getAssocTest() == ASSOC_TRIO){
            buildTrioSet(pf, permute, snpsToBeTested);
        }else if (Options.getAssocTest() == ASSOC_CC){
            buildCCSet(pf, permute, snpsToBeTested);
        }
    }

    private void buildCCSet(PedFile pf, Vector affectedStatus, Vector snpsToBeTested){
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
            SNP currentMarker = Chromosome.getUnfilteredMarker(i);
            if (snpsToBeTested.contains(currentMarker)){
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

                Haplotype thisSNP1 = new Haplotype(g1, 0, m);
                thisSNP1.setCaseCount(counts[0][0]);
                thisSNP1.setControlCount(counts[1][0]);
                Haplotype thisSNP2 = new Haplotype(g2, 0, m);
                thisSNP2.setCaseCount(counts[0][1]);
                thisSNP2.setControlCount(counts[1][1]);


                Haplotype[] daBlock = {thisSNP1, thisSNP2};
                results.add(new MarkerAssociationResult(daBlock, currentMarker.getName(), currentMarker));
            }
        }

        this.results = results;
    }

    private void buildTrioSet(PedFile pf, Vector permuteInd, Vector snpsToBeTested) throws PedFileException{
        Vector results = new Vector();

        Vector indList = pf.getAllIndividuals();

        if(permuteInd == null || permuteInd.size() != indList.size()) {
            permuteInd = new Vector();
            for (int i = 0; i < indList.size(); i++){
                permuteInd.add(new Boolean(false));
            }
        }

        int numMarkers = Chromosome.getUnfilteredSize();
        for (int i = 0; i < numMarkers; i++){
            SNP currentMarker = Chromosome.getUnfilteredMarker(i);
            if (snpsToBeTested.contains(currentMarker)){
                Individual currentInd;
                Family currentFam;
                AssociationResult.TallyTrio tt = new AssociationResult.TallyTrio();
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
                        if(((Boolean)permuteInd.get(j)).booleanValue()) {
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

                Haplotype thisSNP1 = new Haplotype(g1, 0, m);
                thisSNP1.setTransCount(tt.counts[0][0]);
                thisSNP1.setUntransCount(tt.counts[1][0]);
                Haplotype thisSNP2 = new Haplotype(g2, 0, m);
                thisSNP2.setTransCount(tt.counts[0][1]);
                thisSNP2.setUntransCount(tt.counts[1][1]);

                Haplotype[] daBlock = {thisSNP1, thisSNP2};
                results.add(new MarkerAssociationResult(daBlock, currentMarker.getName(), currentMarker));
            }
        }
        this.results = results;
    }

    public AssociationTestSet(Haplotype[][] haplos, Vector names){
        whitelist = new HashSet();
        Vector results = new Vector();
        if (haplos != null){
            for (int i = 0; i < haplos.length; i++){
                String blockname;
                if (names == null){
                    blockname = "Block " + (i+1);
                }else{
                    blockname = (String) names.get(i);
                }
                results.add(new HaplotypeAssociationResult(haplos[i], Options.getHaplotypeDisplayThreshold(),blockname));
            }
        }
        this.results = results;
    }

    public AssociationTestSet(String fileName) throws IOException, HaploViewException{
        tests = new Vector();
        whitelist = new HashSet();
        File testListFile = new File(fileName);
        BufferedReader in = new BufferedReader(new FileReader(testListFile));

        String currentLine;
        int lineCount = 0;

        //we need to be able to identify marker index by name
        Hashtable indicesByName = new Hashtable();
        Iterator mitr = Chromosome.getAllMarkers().iterator();
        int count = 0;
        while (mitr.hasNext()){
            SNP n = (SNP) mitr.next();
            indicesByName.put(n.getName(),new Integer(count));
            count++;
        }

        while ((currentLine = in.readLine()) != null){
            lineCount++;
            //first determine if a tab specifies a specific allele for a multi-marker test
            StringTokenizer st = new StringTokenizer(currentLine, "\t");
            String markerNames;
            String alleles;
            if (st.countTokens() == 0){
                //skip blank lines
                continue;
            }else if (st.countTokens() == 1){
                //this is just a list of markers
                markerNames = st.nextToken();
                alleles = null;
            }else if (st.countTokens() == 2){
                //this has markers and alleles
                markerNames = st.nextToken();
                alleles = st.nextToken();
            }else{
                //this is *!&#ed up
                markerNames = null;
                alleles = null;
                throw new HaploViewException("Format error on line " + lineCount + " of tests file.");
            }

            StringTokenizer mst = new StringTokenizer(markerNames, ", ");
            AssociationTest t = new AssociationTest();
            while (mst.hasMoreTokens()) {
                String currentToken = mst.nextToken();
                if (!indicesByName.containsKey(currentToken)){
                    throw new HaploViewException("I don't know anything about marker " + currentToken +
                            " in custom tests file.");
                }

                Integer nt = (Integer) indicesByName.get(currentToken);
                t.addMarker(nt);
            }
            tests.add(t);

            if (alleles != null){
                //we have alleles
                StringBuffer asb = new StringBuffer();
                StringTokenizer ast = new StringTokenizer(alleles, ", ");
                if (ast.countTokens() != t.getNumMarkers()){
                    throw new HaploViewException("Allele and marker name mismatch on line " + lineCount + " of tests file.");
                }
                while (ast.hasMoreTokens()){
                    asb.append(ast.nextToken());
                }
                t.specifyAllele(asb.toString());
            }
        }

    }

    public void runFileTests(HaploData theData, Vector inputSNPResults) throws HaploViewException {
        Vector res = new Vector();
        if(tests == null || theData == null) {
            return;
        }

        Vector blocks = new Vector();
        Vector names = new Vector();
        for(int i=0;i<tests.size();i++) {
            //first go through and get all the multimarker tests to package up to hand to theData.generateHaplotypes()
            AssociationTest currentTest = (AssociationTest) tests.get(i);
            if(currentTest.getNumMarkers() > 1) {
                blocks.add(currentTest.getFilteredMarkerArray());
                names.add(currentTest.getName());
            }
        }
        Haplotype[][] blockHaps = theData.generateHaplotypes(blocks, true);
        Vector blockResults = new AssociationTestSet(blockHaps, names).getResults();
        Iterator britr = blockResults.iterator();

        for (int i = 0; i < tests.size(); i++){
            AssociationTest currentTest = (AssociationTest) tests.get(i);
            if(currentTest.getNumMarkers() > 1) {
                //grab the next block result from above
                //check to see if a specific allele was given
                HaplotypeAssociationResult har = (HaplotypeAssociationResult) britr.next();
                //todo: this is borken. needs count of all other alleles.
                if (currentTest.getAllele() != null){
                    boolean foundAllele = false;
                    for (int j = 0; j < har.getAlleleCount(); j++){
                        if (har.getNumericAlleleName(j).equals(currentTest.getAllele())){
                            Haplotype[] filtHaps = {har.getHaps()[j]};
                            res.add(new HaplotypeAssociationResult(filtHaps,0,har.getName()));
                            foundAllele = true;
                            break;
                        }
                    }
                    if (!foundAllele){
                        throw new HaploViewException(currentTest.getAllele() + ": no such allele for test:\n" +
                                har.getName());
                    }
                }else{
                    res.add(har);
                }
            }else if (currentTest.getNumMarkers() == 1){
                //grab appropriate single marker result.
                res.add(inputSNPResults.get(currentTest.getMarkerArray()[0]));
            }
        }

        results =  res;
    }

    public Vector getResults() {
        return results;
    }

    public Vector getFilteredResults(){
        //return the results but without any single snps which are filtered out.
        Vector filt = new Vector();

        Vector unFilteredMarkers = new Vector();
        for (int i = 0; i < Chromosome.getSize(); i++){
            unFilteredMarkers.add(Chromosome.getMarker(i));
        }

        Iterator itr = results.iterator();
        while (itr.hasNext()){
            Object o = itr.next();
            if (o instanceof HaplotypeAssociationResult){
                filt.add(o);
            }else{
                if (unFilteredMarkers.contains(((MarkerAssociationResult)o).getSnp())){
                    //only add it if it's not filtered.
                    filt.add(o);
                }
            }
        }

        return filt;
    }

    public Vector getMarkerAssociationResults(){
        Vector ret = new Vector();

        Iterator itr = results.iterator();
        while (itr.hasNext()){
            Object o = itr.next();
            if (o instanceof MarkerAssociationResult){
                ret.add(o);
            }
        }

        return ret;
    }

    public Vector getHaplotypeAssociationResults(){
        Vector ret = new Vector();

        Iterator itr = results.iterator();
        while (itr.hasNext()){
            Object o = itr.next();
            if (o instanceof HaplotypeAssociationResult){
                ret.add(o);
            }
        }

        return ret;
    }

    public HashSet getWhitelist() {
        return whitelist;
    }

    public void cat(AssociationTestSet ats){
        results.addAll(ats.getResults());
    }

    class AssociationTest {
        Vector markers;
        String allele;

        public AssociationTest() {
            markers = new Vector();
        }

        void addMarker(Integer m) {
            if(m != null) {
                markers.add(m);
            }
            whitelist.add(Chromosome.getUnfilteredMarker(m.intValue()));
        }

        int getNumMarkers() {
            return markers.size();
        }

        int[] getMarkerArray() {
            int[] tempArray = new int[markers.size()];

            for(int i =0; i<tempArray.length;i++) {
                tempArray[i] = ((Integer)markers.get(i)).intValue();
            }
            return tempArray;
        }

        int[] getFilteredMarkerArray(){
            //it is important that none of the markers in any of the cust blocks have been filtered or else this
            //method chokes.
            int[] tempArray = new int[markers.size()];
            for(int i = 0; i < tempArray.length; i++){
                tempArray[i] = Chromosome.filterIndex[((Integer)markers.get(i)).intValue()];
            }
            return tempArray;
        }

        String getName(){
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < markers.size(); i++){
                sb.append(((Integer)markers.get(i)).intValue()+1);
                sb.append(" ");
            }

            return sb.toString();
        }

        public void specifyAllele(String s) {
            allele = s;
        }

        public String getAllele() {
            return allele;
        }
    }

    public void saveResultsToText(File outputFile) throws IOException{
        if(results == null) {
            return;
        }

        if(Options.getAssocTest() != ASSOC_TRIO && Options.getAssocTest() != ASSOC_CC) {
            return;
        }

        FileWriter fw;
        fw = new FileWriter(outputFile);

        StringBuffer result = new StringBuffer();
        if(Options.getAssocTest() == ASSOC_TRIO) {
            result.append("Test\tAllele\tFreq.\tT:U\tChi Square\tP Value\n");
        } else if(Options.getAssocTest() == ASSOC_CC) {
            result.append("Test\tAllele\tFreq.\tCase, Control Ratios\tChi Square\tP Value\n");
        }

        for (int i = 0; i < results.size(); i++){
            AssociationResult ar = (AssociationResult) results.elementAt(i);
            for (int j = 0; j < ar.getAlleleCount(); j++){
                result.append(ar.getName()).append("\t");
                if (ar instanceof MarkerAssociationResult){
                    result.append(((MarkerAssociationResult)ar).getOverTransmittedAllele()).append("\t");
                    result.append("\t");
                }else{
                    result.append(ar.getAlleleName(j)).append("\t");
                    result.append(ar.getFreq(j)).append("\t");
                }
                result.append(ar.getCountString(j)).append("\t");
                result.append(ar.getChiSquare(j)).append("\t");
                result.append(ar.getPValue(j)).append("\n");

                if (ar instanceof MarkerAssociationResult){
                    //only show one line for SNPs instead of one line per allele
                    break;
                }
            }
        }

        fw.write(result.toString().toCharArray());
        fw.close();
    }

    public void saveHapsToText(File outputFile) throws IOException{
        if(results == null) {
            return;
        }

        if(Options.getAssocTest() != ASSOC_TRIO && Options.getAssocTest() != ASSOC_CC) {
            return;
        }

        FileWriter fw;
        fw = new FileWriter(outputFile);

        StringBuffer result = new StringBuffer();
        if(Options.getAssocTest() == ASSOC_TRIO) {
            result.append("Block\tHaplotype\tFreq.\tT:U\tChi Square\tP Value\n");
        } else if(Options.getAssocTest() == ASSOC_CC) {
            result.append("Block\tHaplotype\tFreq.\tCase, Control Ratios\tChi Square\tP Value\n");
        }

        for (int i = 0; i < results.size(); i++){
            if (results.elementAt(i) instanceof HaplotypeAssociationResult){
                HaplotypeAssociationResult ar = (HaplotypeAssociationResult) results.elementAt(i);
                result.append("Block " + (i+1)).append("\n");
                for (int j = 0; j < ar.getAlleleCount(); j++){
                    result.append(ar.getAlleleName(j)).append("\t");
                    result.append(ar.getFreq(j)).append("\t");
                    result.append(ar.getCountString(j)).append("\t");
                    result.append(ar.getChiSquare(j)).append("\t");
                    result.append(ar.getPValue(j)).append("\n");
                }
            }
        }

        fw.write(result.toString().toCharArray());
        fw.close();
    }

    public void saveSNPsToText(File outputFile) throws IOException{
        if(results == null) {
            return;
        }

        if(Options.getAssocTest() != ASSOC_TRIO && Options.getAssocTest() != ASSOC_CC) {
            return;
        }

        FileWriter fw;
        fw = new FileWriter(outputFile);

        StringBuffer result = new StringBuffer();
        if(Options.getAssocTest() == ASSOC_TRIO) {
            result.append("#\tName\tOvertransmitted\tT:U\tChi square\tP value\n");
        } else if(Options.getAssocTest() == ASSOC_CC) {
            result.append("#\tName\tMajor Alleles\tCase,Control Ratios\tChi square\tP value\n");
        }

        //only output assoc results for markers which werent filtered
        for(int i=0;i<Chromosome.getSize();i++) {
            if (results.get(Chromosome.realIndex[i]) instanceof MarkerAssociationResult){
                MarkerAssociationResult currentResult = (MarkerAssociationResult) results.get(Chromosome.realIndex[i]);
                result.append((Chromosome.realIndex[i] + 1)).append("\t");
                result.append(currentResult.getName()).append("\t");
                result.append(currentResult.getOverTransmittedAllele()).append("\t");
                result.append(currentResult.getCountString()).append("\t");
                result.append(currentResult.getChiSquare(0)).append("\t");
                result.append(currentResult.getPValue(0)).append("\n");
            }
        }

        fw.write(result.toString().toCharArray());
        fw.close();
    }
}
