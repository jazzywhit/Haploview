/*
* $Id: PedFile.java,v 3.50 2008/12/11 17:40:23 jcbarret Exp $
* WHITEHEAD INSTITUTE
* SOFTWARE COPYRIGHT NOTICE AGREEMENT
* This software and its documentation are copyright 2002 by the
* Whitehead Institute for Biomedical Research.  All rights are reserved.
*
* This software is supplied without any warranty or guaranteed support
* whatsoever.  The Whitehead Institute can not be responsible for its
* use, misuse, or functionality.
*/
package edu.mit.wi.pedfile;


import edu.mit.wi.haploview.Chromosome;
import edu.mit.wi.haploview.Options;
import edu.mit.wi.haploview.SNP;
import edu.mit.wi.haploview.Constants;
import edu.mit.wi.pedparser.PedParser;
import edu.mit.wi.pedparser.PedigreeException;

import java.util.*;
import java.util.zip.GZIPInputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.io.*;

import org._3pq.jgrapht.graph.SimpleGraph;

/**
 * Handles input and storage of Pedigree files
 *
 * this class is not thread safe (untested).
 * modified from original Pedfile and checkdata classes by Hui Gong
 * @author Julian Maller
 */
public class PedFile {
    private Hashtable families;

    private Vector axedPeople = new Vector();

    //stores the individuals found by parse() in allIndividuals. this is useful for outputting Pedigree information to a file of another type.
    private Vector allIndividuals;

    //stores the individuals chosen by pedparser
    private Vector unrelatedIndividuals;

    private Vector results = null;
    private String[][] hminfo;
    //bogusParents is true if someone in the file referenced a parent not in the file
    private boolean bogusParents = false;
    private Vector haploidHets;
    private boolean mendels = false;

    private static Hashtable hapMapTranslate;
    private int[] markerRatings;
    private int[] dups;
    private HashSet whitelist;


    public PedFile(){

        //hardcoded hapmap info
        this.families = new Hashtable();

        hapMapTranslate = new Hashtable(1700,1);
        try {
            InputStream is = getClass().getResourceAsStream("hapmap-info.txt");
            BufferedReader hapmapinfo = new BufferedReader(new InputStreamReader(is));
            String line;
            StringTokenizer st;
            while( (line = hapmapinfo.readLine()) != null ) {
                st = new StringTokenizer(line);
                st.nextToken();
                hapMapTranslate.put(st.nextToken(),line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * gets the allIndividuals Vector
     */
    public Vector getAllIndividuals() {
        return allIndividuals;
    }

    public Vector getUnusedIndividuals(){
        HashSet used = new HashSet(getUnrelatedIndividuals());
        HashSet all = new HashSet(getAllIndividuals());
        all.removeAll(used);
        return new Vector(all);
    }

    public Vector getUnrelatedIndividuals() {
        return unrelatedIndividuals;
    }

    /**
     *
     * @return enumeration containing a list of familyID's in the families hashtable
     */
    public Enumeration getFamList(){
        return this.families.keys();
    }
    /**
     *
     * @param familyID id of desired family
     * @return Family identified by familyID in families hashtable
     */
    public Family getFamily(String familyID){
        return (Family)this.families.get(familyID);
    }

    /**
     *
     * @return the number of Family objects in the families hashtable
     */
    public int getNumFamilies(){
        return this.families.size();
    }

    /**
     * this method iterates through each family in Hashtable families and adds up
     * the number of individuals in total across all families
     * @return the total number of individuals in all the family objects in the families hashtable
     */
    public int getNumIndividuals(){
        Enumeration famEnum = this.families.elements();
        int total =0;
        while (famEnum.hasMoreElements()) {
            Family fam = (Family) famEnum.nextElement();
            total += fam.getNumMembers();
        }
        return total;
    }

    /**
     * finds the first individual in the first family and returns the number of markers for that individual
     * @return the number of markers
     */
    public int getNumMarkers(){
        Enumeration famList = this.families.elements();
        int numMarkers = 0;
        while (famList.hasMoreElements()) {
            Family fam = (Family) famList.nextElement();
            Enumeration indList = fam.getMemberList();
            Individual ind = null;
            while(indList.hasMoreElements()){
                try{
                    ind = fam.getMember((String)indList.nextElement());
                }catch(PedFileException pfe){
                }
                numMarkers = ind.getNumMarkers();
                if(numMarkers > 0){
                    return numMarkers;
                }
            }
        }
        return 0;
    }

    /**
     * takes in a pedigree file in the form of a vector of strings and parses it.
     * data is stored in families in the member hashtable families
     */
    public void parseLinkage(Vector pedigrees) throws PedFileException {
        int colNum = -1;
        boolean withOptionalColumn = false;
        int numMarkers = 0;
        boolean genoError = false;
        int numLines = pedigrees.size();
        if (numLines == 0){
            throw new PedFileException("Data format error: empty file");
        }
        Individual ind;
        this.allIndividuals = new Vector();

        for(int k=0; k<numLines; k++){
            StringTokenizer tokenizer = new StringTokenizer((String)pedigrees.get(k), "\n\t\" \"");
            int numTokens = tokenizer.countTokens();

            //reading the first line
            if(colNum < 1){
                //only check column number count for the first nonblank line
                colNum = numTokens;
                if(colNum%2==1) {
                    withOptionalColumn = true;
                    numMarkers= (numTokens - 7)/2;
                }else {
                    numMarkers = (numTokens -6)/2;
                }
            }
            if(colNum != numTokens) {
                //this line has a different number of columns
                //should send some sort of error message
                throw new PedFileException("Column number mismatch in pedfile. line " + (k+1));
            }

            try{
                ind = new Individual(numMarkers, false);
            }catch(NegativeArraySizeException neg) {
                throw new PedFileException("File formatting error.");
            }
            if(numTokens < 6) {
                throw new PedFileException("Incorrect number of fields on line " + (k+1));
            }

            if(tokenizer.hasMoreTokens()){

                ind.setFamilyID(new String(tokenizer.nextToken().trim()));
                ind.setIndividualID(new String(tokenizer.nextToken().trim()));
                ind.setDadID(new String(tokenizer.nextToken().trim()));
                ind.setMomID(new String(tokenizer.nextToken().trim()));
                try {
                    ind.setGender(Integer.parseInt(tokenizer.nextToken().trim()));
                    ind.setAffectedStatus(Integer.parseInt(tokenizer.nextToken().trim()));
                    if(withOptionalColumn) {
                        ind.setLiability(Integer.parseInt(tokenizer.nextToken().trim()));
                    }
                }catch(NumberFormatException nfe) {
                    throw new PedFileException("Pedfile error: invalid gender or affected status on line " + (k+1));
                }

                byte genotype1;
                byte genotype2;
                if (!tokenizer.hasMoreTokens()){
                    throw new PedFileException("Pedfile error: no marker genotypes specified.");
                }
                while(tokenizer.hasMoreTokens()){
                    try {
                        String alleleA = tokenizer.nextToken();
                        String alleleB = tokenizer.nextToken();
                        int[] checker1, checker2;
                        checker1 = checkGenotype(alleleA);
                        checker2 = checkGenotype(alleleB);
                        if (checker1[1] != checker2[1]){
                            genoError = !genoError;
                        }

                        if (genoError){
                            throw new PedFileException("File input error on line " + (k+1) + ", marker " + (ind.getNumMarkers()+1)  +
                                    ".\nFor any marker, an individual's genotype must be only letters or only numbers.");
                        }

                        if(checker1[0] < 0 || checker1[0] > 4 || checker2[0] < 0 || checker2[0] > 4) {
                            throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k+1)
                                    + ".\n all genotypes must be 0-4 or A/C/G/T.");
                        }
                        genotype1 = (byte)checker1[0];
                        genotype2 = (byte)checker2[0];
                        ind.addMarker(genotype1,genotype2);
                    }catch(NumberFormatException nfe) {
                        throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k+1) );
                    }
                }

                //check if the family exists already in the Hashtable
                Family fam = (Family)this.families.get(ind.getFamilyID());
                if(fam == null){
                    //it doesnt exist, so create a new Family object
                    fam = new Family(ind.getFamilyID());
                }

                if (fam.getMembers().containsKey(ind.getIndividualID())){
                    throw new PedFileException("Individual "+ind.getIndividualID()+" in family "+ ind.getFamilyID()+" appears more than once.");
                }

                fam.addMember(ind);
                this.families.put(ind.getFamilyID(),fam);
                this.allIndividuals.add(ind);

            }
        }

        //now we check if anyone has a reference to a parent who isnt in the file, and if so, we remove the reference
        for(int i=0;i<allIndividuals.size();i++) {
            Individual currentInd = (Individual) allIndividuals.get(i);
            Hashtable curFam = ((Family)(families.get(currentInd.getFamilyID())) ).getMembers();
            if( !currentInd.getDadID().equals("0") && ! (curFam.containsKey(currentInd.getDadID()))) {
                currentInd.setDadID("0");
                bogusParents = true;
            }
            if(!currentInd.getMomID().equals("0") && ! (curFam.containsKey(currentInd.getMomID()))) {
                currentInd.setMomID("0");
                bogusParents = true;
            }
        }


    }

    public void parseHapMap(Vector lines, Vector hapsData) throws PedFileException {
        int colNum = -1;
        int numLines = lines.size();
        if (numLines < 2){
            throw new PedFileException("Hapmap data format error: empty file");
        }
        if (hapsData != null){
            String indName;
            for (int i=0; i < hapsData.size(); i++){
                StringTokenizer hd = new StringTokenizer((String)hapsData.get(i));
                if (hd.countTokens() < 6){
                    throw new PedFileException("Hapmap data format error: pedigree data on line " + (i+1) + ".");
                }
                if (hd.countTokens() > 7){
                    throw new PedFileException("Hapmap data format error: pedigree data on line " + (i+1) + ".");
                }
                hd.nextToken();
                indName = hd.nextToken();
                hapMapTranslate.put(indName, (String)hapsData.get(i));
            }
        }
        Individual ind;

        this.allIndividuals = new Vector();

        //enumerate indivs
        StringTokenizer st = new StringTokenizer((String)lines.get(0), "\n\t\" \"");
        int numMetaColumns = 0;
        boolean doneMeta = false;
        boolean genoErrorB = false;
        while(!doneMeta && st.hasMoreTokens()){
            String thisfield = st.nextToken();
            numMetaColumns++;
            //first indiv ID will be a string beginning with "NA"
            if (thisfield.startsWith("NA")){
                doneMeta = true;
            }
        }
        numMetaColumns--;

        st = new StringTokenizer((String)lines.get(0), "\n\t\" \"");
        for (int i = 0; i < numMetaColumns; i++){
            st.nextToken();
        }
        Vector namesIncludingDups = new Vector();
        StringTokenizer dt;
        while (st.hasMoreTokens()){
            //todo: sort out how this used to work. now it's counting the header line so we subtract 1
            ind = new Individual(numLines-1, false);

            String name = st.nextToken();
            namesIncludingDups.add(name);
            if (name.endsWith("dup")){
                //skip dups (i.e. don't add 'em to ind array)
                continue;
            }
            String details = (String)hapMapTranslate.get(name);
            if (details == null){
                throw new PedFileException("Hapmap data format error: " + name);
            }
            dt = new StringTokenizer(details, "\n\t\" \"");
            ind.setFamilyID(dt.nextToken().trim());
            ind.setIndividualID(dt.nextToken().trim());
            ind.setDadID(dt.nextToken().trim());
            ind.setMomID(dt.nextToken().trim());
            try {
                ind.setGender(Integer.parseInt(dt.nextToken().trim()));
                ind.setAffectedStatus(Integer.parseInt(dt.nextToken().trim()));
            }catch(NumberFormatException nfe) {
                throw new PedFileException("File error: invalid gender or affected status for indiv " + name);
            }

            //check if the family exists already in the Hashtable
            Family fam = (Family)this.families.get(ind.getFamilyID());
            if(fam == null){
                //it doesnt exist, so create a new Family object
                fam = new Family(ind.getFamilyID());
            }
            fam.addMember(ind);
            this.families.put(ind.getFamilyID(),fam);
            this.allIndividuals.add(ind);
        }

        //start at k=1 to skip header which we just processed above.
        hminfo = new String[numLines-1][];
        for(int k=1;k<numLines;k++){
            StringTokenizer tokenizer = new StringTokenizer((String)lines.get(k));
            //reading the first line
            if(colNum < 0){
                //only check column number count for the first line
                colNum = tokenizer.countTokens();
            }
            if(colNum != tokenizer.countTokens()) {
                //this line has a different number of columns
                //should send some sort of error message
                //TODO: add something which stores number of markers for all lines and checks that they're consistent
                throw new PedFileException("Line number mismatch in input file. line " + (k+1));
            }

            if(tokenizer.hasMoreTokens()){
                hminfo[k-1] = new String[2];
                for (int skip = 0; skip < numMetaColumns; skip++){
                    //meta-data crap
                    String s;
                    try{
                        s = tokenizer.nextToken().trim();
                    }catch(NoSuchElementException nse){
                        throw new PedFileException("Data format error on line " + (k+1) + ": " + (String)lines.get(k));
                    }

                    //get marker name, chrom and pos
                    if (skip == 0){
                        hminfo[k-1][0] = s;
                    }
                    if (skip == 2){
                        String dc = Chromosome.getDataChrom();
                        if (dc != null && !dc.equals("none")){
                            if (!dc.equalsIgnoreCase(s)){
                                throw new PedFileException("Hapmap file format error on line " + (k+1) +
                                        ":\n The file appears to contain multiple chromosomes:" +
                                        "\n" + dc + ", " + s);
                            }
                        }else{
                            Chromosome.setDataChrom(s);
                        }
                    }
                    if (skip == 3){
                        hminfo[k-1][1] = s;
                    }
                    if (skip == 5){
                        Chromosome.setDataBuild(s);
                    }
                }
                int index = 0;
                int indexIncludingDups = -1;
                while(tokenizer.hasMoreTokens()){
                    String alleles = tokenizer.nextToken();

                    indexIncludingDups++;
                    //we've skipped the dups in the ind array, so we skip their genotypes
                    if (((String)namesIncludingDups.elementAt(indexIncludingDups)).endsWith("dup")){
                        continue;
                    }

                    ind = (Individual)allIndividuals.elementAt(index);
                    int[] checker1, checker2;
                    try{
                        checker1 = checkGenotype(alleles.substring(0,1));
                        checker2 = checkGenotype(alleles.substring(1,2));
                    }catch(NumberFormatException nfe){
                        throw new PedFileException("Invalid genotype on individual " + ind.getIndividualID() + ".");
                    }
                    if (checker1[1] != checker2[1]){
                        genoErrorB = !genoErrorB;
                    }
                    byte allele1 = (byte)checker1[0];
                    byte allele2 = (byte)checker2[0];
                    ind.addMarker(allele1, allele2);
                    if (genoErrorB){
                        throw new PedFileException("File input error: individual " + ind.getIndividualID() + ", marker "
                                + this.hminfo[ind.getNumMarkers()-1][0] + ".\nFor any marker, an individual's genotype must be only letters or only numbers.");
                    }
                    index++;
                }
            }
        }
    }

    public void parseHapMapPhase(String[] info) throws IOException, PedFileException{
        if (info[3].equals("")){
            Chromosome.setDataChrom("none");
        }else{
            Chromosome.setDataChrom("chr" + info[3]);
        }
        Chromosome.setDataBuild("ncbi_b35");
        Vector sampleData = new Vector();
        Vector legendData = new Vector();
        Vector legendMarkers = new Vector();
        Vector legendPositions = new Vector();
        Individual ind = null;
        byte[] byteDataT = new byte[0];
        byte[] byteDataU = new byte[0];
        this.allIndividuals = new Vector();

        InputStream phaseStream, sampleStream, legendStream;
        String phaseName, sampleName, legendName;

        try {
            URL sampleURL = new URL(info[1]);
            sampleName = sampleURL.getFile();
            sampleStream = sampleURL.openStream();
        }catch (MalformedURLException mfe){
            File sampleFile = new File(info[1]);
            if (sampleFile.length() < 1){
                throw new PedFileException("Sample file is empty or non-existent: " + sampleFile.getName());
            }
            sampleName = sampleFile.getName();
            sampleStream = new FileInputStream(sampleFile);
        }catch (IOException ioe){
            throw new PedFileException("Could not connect to " + info[1]);
        }

        //read in the individual ids data.
        try{
            BufferedReader sampleBuffReader;
            if (Options.getGzip()){
                GZIPInputStream sampleInputStream = new GZIPInputStream(sampleStream);
                sampleBuffReader = new BufferedReader(new InputStreamReader(sampleInputStream));
            }else{
                sampleBuffReader = new BufferedReader(new InputStreamReader(sampleStream));
            }
            String sampleLine;
            while((sampleLine = sampleBuffReader.readLine())!=null){
                StringTokenizer sampleTokenizer = new StringTokenizer(sampleLine);
                sampleData.add(sampleTokenizer.nextToken());
            }
        }catch(NoSuchElementException nse){
            throw new PedFileException("File format error in " + sampleName);
        }

         try {
             URL legendURL = new URL(info[2]);
             legendName = legendURL.getFile();
             legendStream = legendURL.openStream();
         }catch (MalformedURLException mfe){
             File legendFile = new File(info[2]);
             if (legendFile.length() < 1){
                 throw new PedFileException("Legend file is empty or non-existent: " + legendFile.getName());
             }
             legendName = legendFile.getName();
             legendStream = new FileInputStream(legendFile);
         }catch (IOException ioe){
             throw new PedFileException("Could not connect to " + info[2]);
         }

        //read in the legend data
        try{
            BufferedReader legendBuffReader;
            if (Options.getGzip()){
                GZIPInputStream legendInputStream = new GZIPInputStream(legendStream);
                legendBuffReader = new BufferedReader(new InputStreamReader(legendInputStream));
            }else{
                legendBuffReader = new BufferedReader(new InputStreamReader(legendStream));
            }
            String legendLine;
            String zero, one;
            while((legendLine = legendBuffReader.readLine())!=null){
                StringTokenizer legendSt = new StringTokenizer(legendLine);
                String markerid = legendSt.nextToken();
                if (markerid.equalsIgnoreCase("rs") || markerid.equalsIgnoreCase("marker")){ //skip header
                    continue;
                }
                legendMarkers.add(markerid);
                legendPositions.add(legendSt.nextToken());
                byte[] legendBytes = new byte[2];
                zero = legendSt.nextToken();
                one = legendSt.nextToken();

                if (zero.equalsIgnoreCase("A")){
                    legendBytes[0] = 1;
                }else if (zero.equalsIgnoreCase("C")){
                    legendBytes[0] = 2;
                }else if (zero.equalsIgnoreCase("G")){
                    legendBytes[0] = 3;
                }else if (zero.equalsIgnoreCase("T")){
                    legendBytes[0] = 4;
                }else{
                    throw new PedFileException("Invalid allele: " + zero);
                }

                if (one.equalsIgnoreCase("A")){
                    legendBytes[1] = 1;
                }else if (one.equalsIgnoreCase("C")){
                    legendBytes[1] = 2;
                }else if (one.equalsIgnoreCase("G")){
                    legendBytes[1] = 3;
                }else if (one.equalsIgnoreCase("T")){
                    legendBytes[1] = 4;
                }else{
                    throw new PedFileException("Invalid allele: " + one);
                }

                legendData.add(legendBytes);
            }

            hminfo = new String[legendPositions.size()][2];

            for (int i = 0; i < legendPositions.size(); i++){
                //marker name.
                hminfo[i][0] = (String)legendMarkers.get(i);
                //marker position.
                hminfo[i][1] = (String)legendPositions.get(i);
            }
        }catch(NoSuchElementException nse){
            throw new PedFileException("File format error in " + legendName);
        }

        try {
            URL phaseURL = new URL(info[0]);
            phaseName = phaseURL.getFile();
            phaseStream = phaseURL.openStream();
        }catch (MalformedURLException mfe){
            File phaseFile = new File(info[0]);
            if (phaseFile.length() < 1){
                throw new PedFileException("Genotypes file is empty or non-existent: " + phaseFile.getName());
            }
            phaseName = phaseFile.getName();
            phaseStream = new FileInputStream(phaseFile);
        }catch (IOException ioe){
            throw new PedFileException("Could not connect to " + info[0]);
        }

        //read in the phased data.
        try{
            BufferedReader phasedBuffReader;
            if (Options.getGzip()){
                GZIPInputStream phasedInputStream = new GZIPInputStream(phaseStream);
                phasedBuffReader = new BufferedReader(new InputStreamReader(phasedInputStream));
            }else{
                phasedBuffReader = new BufferedReader(new InputStreamReader(phaseStream));
            }
            String phasedLine;
            int columns = 0;
            String token;
            boolean even = false;
            int iterator = 0;
            while((phasedLine = phasedBuffReader.readLine()) != null){
                StringTokenizer phasedSt = new StringTokenizer(phasedLine);
                columns = phasedSt.countTokens();
                if(even){
                    iterator++;
                }else{   //Only set up a new individual every 2 lines.
                    ind = new Individual(columns, true);
                    try{
                        ind.setIndividualID((String)sampleData.get(iterator));
                    }catch (ArrayIndexOutOfBoundsException e){
                        throw new PedFileException("File error: Sample file is missing individual IDs");
                    }
                    if (columns != legendData.size()){
                        throw new PedFileException("File error: invalid number of markers on Individual " + ind.getIndividualID());
                    }
                    String details = (String)hapMapTranslate.get(ind.getIndividualID());
                    //exception in case of wierd compression combos in input files
                    if (details == null){
                        throw new PedFileException("File format error in " + sampleName);
                    }
                    StringTokenizer dt = new StringTokenizer(details, "\n\t\" \"");
                    ind.setFamilyID(dt.nextToken().trim());
                    //skip individualID since we already have it.
                    dt.nextToken();
                    ind.setDadID(dt.nextToken());
                    ind.setMomID(dt.nextToken());
                    try {
                        ind.setGender(Integer.parseInt(dt.nextToken().trim()));
                        ind.setAffectedStatus(Integer.parseInt(dt.nextToken().trim()));
                    }catch(NumberFormatException nfe) {
                        throw new PedFileException("File error: invalid gender or affected status for indiv " + ind.getIndividualID());
                    }

                    //check if the family exists already in the Hashtable
                    Family fam = (Family)this.families.get(ind.getFamilyID());
                    if(fam == null){
                        //it doesnt exist, so create a new Family object
                        fam = new Family(ind.getFamilyID());
                    }
                    fam.addMember(ind);
                    this.families.put(ind.getFamilyID(),fam);
                    this.allIndividuals.add(ind);
                }

                int index = 0;
                if (!even){
                    byteDataT = new byte[columns];
                }else{
                    byteDataU = new byte[columns];
                }
                while(phasedSt.hasMoreTokens()){
                    token = phasedSt.nextToken();
                    if (!even){
                        if (token.equalsIgnoreCase("0")){
                            byteDataT[index] = ((byte[])legendData.get(index))[0];
                        }else if (token.equalsIgnoreCase("1")){
                            byteDataT[index] = ((byte[])legendData.get(index))[1];
                        }else {
                            throw new PedFileException("File format error in " + phaseName);
                        }
                    }else{
                        if (token.equalsIgnoreCase("0")){
                            byteDataU[index] = ((byte[])legendData.get(index))[0];
                        }else if (token.equalsIgnoreCase("1")){
                            byteDataU[index] = ((byte[])legendData.get(index))[1];
                        }else if (Chromosome.getDataChrom().equalsIgnoreCase("chrx") && ind.getGender() == Individual.MALE && token.equalsIgnoreCase("-")){
                            //X male
                        }else {
                            throw new PedFileException("File format error in " + phaseName);
                        }
                    }
                    index++;
                }
                if (even){
                    if (ind.getGender() == Individual.MALE && Chromosome.getDataChrom().equalsIgnoreCase("chrx")){
                        for(int i=0; i < columns; i++){
                            ind.addMarker(byteDataT[i], byteDataT[i]);
                        }
                    }else{
                        for(int i=0; i < columns; i++){
                            ind.addMarker(byteDataT[i], byteDataU[i]);
                        }
                    }
                }
                even = !even;
            }
        }catch(NoSuchElementException nse){
            throw new PedFileException("File format error in " + phaseName);
        }
    }

    public void parsePhasedDownload(String[] info) throws IOException, PedFileException{
        String targetChrom = "chr" + info[4];
        Chromosome.setDataChrom(targetChrom);
        Vector legendMarkers = new Vector();
        Vector legendPositions = new Vector();
        Vector hmpVector = new Vector();
        Individual ind = null;
        byte[] byteDataT = new byte[0];
        byte[] byteDataU = new byte[0];
        this.allIndividuals = new Vector();
        String panelChoice;
        if (info[1].equals("CHB+JPT")){
            panelChoice = "JC";
        }else{
            panelChoice = info[1];
        }
        boolean pseudoChecked = false;
        long startPos;
        if (info[2].equals("0")){
            startPos = 1;
        }else{
            startPos = (Integer.parseInt(info[2]))*1000;
        }
        long stopPos = (Integer.parseInt(info[3]))*1000;
        String phaseChoice;
        if (info[5].startsWith("16")){
            Chromosome.setDataBuild("ncbi_b34");
            phaseChoice = "I";
        }else if (info[5].equals("21")){
            Chromosome.setDataBuild("ncbi_b35");
            phaseChoice = "II";
        }else{
            Chromosome.setDataBuild("ncbi_b36");
            phaseChoice = "III";
        }
        String output = info[6];
        boolean infoDone = false;
        boolean hminfoDone = false;
        String urlHmp = "http://www.hapmap.org/cgi-perl/phased?chr=" + targetChrom + "&pop=" + panelChoice +
                "&start=" + startPos + "&stop=" + stopPos + "&ds=p" + phaseChoice + "&out=" + output + "&filter=cons+"
                + panelChoice.toLowerCase();

        try{
            URL hmpUrl = new URL(urlHmp);
            HttpURLConnection hmpCon = (HttpURLConnection)hmpUrl.openConnection();
            hmpCon.setRequestProperty("User-agent", Constants.USER_AGENT);
            hmpCon.setRequestProperty("Accept-Encoding","gzip");
            hmpCon.connect();

            int response = hmpCon.getResponseCode();

            if ((response != HttpURLConnection.HTTP_ACCEPTED) && (response != HttpURLConnection.HTTP_OK)) {
                throw new IOException("Could not connect to HapMap database.");
            }else {
                GZIPInputStream g = new GZIPInputStream(hmpCon.getInputStream());
                BufferedReader hmpBuffReader = new BufferedReader(new InputStreamReader(g));
                String hmpLine;
                char token;
                int columns;
                while((hmpLine = hmpBuffReader.readLine())!=null){
                    if (hmpLine.startsWith("---")){
                        //continue;
                    }else if (hmpLine.startsWith("pop:")){
                        //continue;
                    }else if (hmpLine.startsWith("build:")){
                        StringTokenizer buildSt = new StringTokenizer(hmpLine);
                        buildSt.nextToken();
                        Chromosome.setDataBuild(new String(buildSt.nextToken()));
                    }else if (hmpLine.startsWith("hapmap_release:")){
                        //continue;
                    }else if (hmpLine.startsWith("filters:")){
                        //continue;
                    }else if (hmpLine.startsWith("start:")){
                        //continue;
                    }else if (hmpLine.startsWith("stop:")){
                        //continue;
                    }else if (hmpLine.startsWith("snps:")){
                        //continue;
                    }else if (hmpLine.startsWith("phased_haplotypes:")){
                        infoDone = true;
                    }else if (hmpLine.startsWith("No")){
                        throw new PedFileException(hmpLine);
                    }else if (hmpLine.startsWith("Too many")){
                        throw new PedFileException(hmpLine);
                    }else if (!infoDone){
                        StringTokenizer posSt = new StringTokenizer(hmpLine," \t:-");
                        //posSt.nextToken(); //skip the -
                        legendMarkers.add(posSt.nextToken());
                        legendPositions.add(posSt.nextToken());
                    }else if (infoDone){
                        if (!hminfoDone){
                            hminfo = new String[legendPositions.size()][2];
                            for (int i = 0; i < legendPositions.size(); i++){
                                //marker name.
                                hminfo[i][0] = (String)legendMarkers.get(i);
                                //marker position.
                                hminfo[i][1] = (String)legendPositions.get(i);
                            }
                            hminfoDone = true;
                        }
                        hmpVector.add(hmpLine);
                    }
                }

                for (int i = 0; i < hmpVector.size(); i++){
                    StringTokenizer dataSt = new StringTokenizer((String)hmpVector.get(i));
                    dataSt.nextToken(); //skip the -
                    String newid = dataSt.nextToken();  //individual ID with _c1/_c2
                    String data = dataSt.nextToken(); //alleles
                    columns = data.length();
                    StringTokenizer filter = new StringTokenizer(newid,"_:");
                    String id = filter.nextToken();
                    String strand = filter.nextToken();
                    if (strand.equals("c1")){   //Only set up a new individual on c1.
                        ind = new Individual(columns, true);
                        ind.setIndividualID(new String(id));
                        if (columns != legendMarkers.size()){
                            throw new PedFileException("File error: invalid number of markers on Individual " + ind.getIndividualID());
                        }
                        String details = (String)hapMapTranslate.get(ind.getIndividualID());
                        StringTokenizer dt = new StringTokenizer(details, "\n\t\" \"");
                        ind.setFamilyID(dt.nextToken().trim());
                        //skip individualID since we already have it.
                        dt.nextToken();
                        ind.setDadID(dt.nextToken());
                        ind.setMomID(dt.nextToken());
                        try {
                            ind.setGender(Integer.parseInt(dt.nextToken().trim()));
                            ind.setAffectedStatus(Integer.parseInt(dt.nextToken().trim()));
                        }catch(NumberFormatException nfe) {
                            throw new PedFileException("File error: invalid gender or affected status for indiv " + ind.getIndividualID());
                        }
                        if (!pseudoChecked){
                            if (ind.getGender() == Individual.MALE){
                                pseudoChecked = true;
                                if (Chromosome.getDataChrom().equalsIgnoreCase("chrx")){
                                    StringTokenizer checkSt = new StringTokenizer((String)hmpVector.get(i+1),":- \t");
                                    String checkNewid = checkSt.nextToken();
                                    checkSt.nextToken(); //alleles
                                    StringTokenizer checkFilter = new StringTokenizer(checkNewid,"_");
                                    checkFilter.nextToken();
                                    String checkStrand = checkFilter.nextToken();
                                    if (checkStrand.equals("c2")){
                                        Chromosome.setDataChrom("chrp");
                                    }
                                }
                            }
                        }

                        //check if the family exists already in the Hashtable
                        Family fam = (Family)this.families.get(ind.getFamilyID());
                        if(fam == null){
                            //it doesnt exist, so create a new Family object
                            fam = new Family(ind.getFamilyID());
                        }
                        fam.addMember(ind);
                        this.families.put(ind.getFamilyID(),fam);
                        this.allIndividuals.add(ind);
                    }

                    int index = 0;
                    if (strand.equals("c1")){
                        byteDataT = new byte[columns];
                    }else{
                        byteDataU = new byte[columns];
                    }
                    for(int k = 0; k < columns; k++){
                        token = data.charAt(k);
                        if (strand.equals("c1")){
                            if (token == 'A'){
                                byteDataT[index] = 1;
                            }else if (token == 'C'){
                                byteDataT[index] = 2;
                            }else if (token == 'G'){
                                byteDataT[index] = 3;
                            }else if (token == 'T'){
                                byteDataT[index] = 4;
                            }else {
                                throw new PedFileException("Invalid Allele: " + token);
                            }
                        }else{
                            if (token == 'A'){
                                byteDataU[index] = 1;
                            }else if (token == 'C'){
                                byteDataU[index] = 2;
                            }else if (token == 'G'){
                                byteDataU[index] = 3;
                            }else if (token == 'T'){
                                byteDataU[index] = 4;
                            }else if (token == '-'){
                                /*if (!(Chromosome.getDataChrom().equalsIgnoreCase("chrx"))){
                                                       throw new PedFileException("Missing allele on non X-chromosome data");
                                                   }else{
                                                       byteDataU[index] = byteDataT[index];
                                                   }*/
                                throw new PedFileException("Haploview does not currently support regions encompassing both\n"
                                        + "pseudoautosomal and non-pseudoautosomal markers.");
                            }else {
                                throw new PedFileException("File format error.");
                            }
                        }
                        index++;
                    }
                    if (strand.equals("c2")){
                        for(int j=0; j < columns; j++){
                            ind.addMarker(byteDataT[j], byteDataU[j]);
                        }
                    }else if (strand.equals("c1") && (ind.getGender() == Individual.MALE) &&
                            (Chromosome.getDataChrom().equalsIgnoreCase("chrx"))){
                        for(int j=0; j < columns; j++){
                            ind.addMarker(byteDataT[j], byteDataT[j]);
                        }
                    }
                }
            }
            hmpCon.disconnect();
        }catch(IOException io){
            throw new IOException("Could not connect to HapMap database.");
        }
    }

/*    public void parseFastPhase(String[] info) throws IOException, PedFileException{
        if (info[3].equals("")){
            Chromosome.setDataChrom("none");
        }else{
            Chromosome.setDataChrom("chr" + info[3]);
        }
        Chromosome.setDataBuild("ncbi_b35");
        BufferedReader reader;
        InputStream inStream;

        try {
            URL inURL = new URL(info[0]);
            inStream = inURL.openStream();
        }catch (MalformedURLException mfe){
            File inFile = new File(info[0]);
            if (inFile.length() < 1){
                throw new PedFileException("Genotype file is empty or non-existent: " + inFile.getName());
            }
            inStream = new FileInputStream(inFile);
        }catch (IOException ioe){
            throw new PedFileException("Could not connect to " + info[0]);
        }

        if (Options.getGzip()){
            GZIPInputStream sampleInputStream = new GZIPInputStream(inStream);
            reader = new BufferedReader(new InputStreamReader(sampleInputStream));
        }else{
            reader = new BufferedReader(new InputStreamReader(inStream));
        }
        this.allIndividuals = new Vector();
        //TODO: put fastPHASE parsing code here
*//*        byte[] byteDataT = new byte[0];
        byte[] byteDataU = new byte[0];
        char token;
        int numMarkers;
        int lineNumber = 0;
        String line;
        Individual ind = null;
        while((line = reader.readLine())!=null){
            StringTokenizer st = new StringTokenizer(line);
            if (st.countTokens() != 5){
                throw new PedFileException("Invalid file formatting on line " + lineNumber+1);
            }
            String markers = new String(st.nextToken());
            st.nextToken(); //marker numbering
            int gender = Integer.parseInt(st.nextToken());
            String id = st.nextToken();
            char strand = st.nextToken().charAt(0); // T or U
            numMarkers = markers.length();
            if (strand == 'T'){
                ind = new Individual(numMarkers, true);
                ind.setGender(gender);
                ind.setIndividualID(id);
                ind.setFamilyID("Bender");
                ind.setDadID("0");
                ind.setMomID("0");
                byteDataT = new byte[numMarkers];

                //check if the family exists already in the Hashtable
                Family fam = (Family)this.families.get(ind.getFamilyID());
                if(fam == null){
                    //it doesnt exist, so create a new Family object
                    fam = new Family(ind.getFamilyID());
                }
                fam.addMember(ind);
                this.families.put(ind.getFamilyID(),fam);
                this.allIndividuals.add(ind);
            }else{
                byteDataU = new byte[numMarkers];
            }

            int index = 0;
            for (int i = 0; i < numMarkers; i++){
                token = markers.charAt(i);
                if (strand == 'T'){
                    if (token == '1'){
                        byteDataT[index] = 1;
                    }else if (token == '2'){
                        byteDataT[index] = 2;
                    }else if (token == '3'){
                        byteDataT[index] = 3;
                    }else if (token == '4'){
                        byteDataT[index] = 4;
                    }else {
                        throw new PedFileException("Invalid Allele: " + token);
                    }
                }else{
                    if (token == '1'){
                        byteDataU[index] = 1;
                    }else if (token == '2'){
                        byteDataU[index] = 2;
                    }else if (token == '3'){
                        byteDataU[index] = 3;
                    }else if (token == '4'){
                        byteDataU[index] = 4;
                    }else {
                        throw new PedFileException("Invalid Allele: " + token);
                    }
                }
                index++;
            }

            if (strand == 'U'){
                for(int j=0; j < numMarkers; j++){
                    ind.addMarker(byteDataT[j], byteDataU[j]);
                }
            }
            lineNumber++;
        }*//*
    }*/

    public void parseHapsFile(Vector individs) throws PedFileException{
        //This method is used to parse haps files which now go through similar processing to ped files.
        String currentLine;
        byte[] genos = new byte[0];
        String ped, indiv;
        int numLines = individs.size();
        if (numLines == 0){
            throw new PedFileException("Data format error: empty file");
        }

        Individual ind = null;
        this.allIndividuals = new Vector();
        int lineCount = 0;
        int numTokens = 0;
        Vector chromA = new Vector();
        Vector chromB = new Vector();
        boolean hapsEven = false;
        boolean hapsError = false;

        for (int i=0; i<numLines; i++){
            lineCount++;
            currentLine = (individs.get(i)).toString();
            if (currentLine.length() == 0){
                continue;
            }
            StringTokenizer st = new StringTokenizer(currentLine);
            //first two tokens are expected to be ped, indiv
            if (st.countTokens() >2){
                ped = st.nextToken();
                indiv = st.nextToken();
            }else{
                throw new PedFileException("Genotype file error:\nLine " + lineCount +
                        " appears to have fewer than 3 columns.");
            }
            if(hapsEven){
                ind = new Individual(st.countTokens(), false);
                ind.setFamilyID(ped);
                ind.setIndividualID(indiv);
                ind.setDadID("");
                ind.setMomID("");
                ind.setGender(0);
                ind.setAffectedStatus(0);
            }

            //all other tokens are loaded into a vector (they should all be genotypes)
            genos = new byte[st.countTokens()];

            int q = 0;

            if (numTokens == 0){
                numTokens = st.countTokens();
            }
            if (numTokens != st.countTokens()){
                throw new PedFileException("Genotype file error:\nLine " + lineCount +
                        " appears to have an incorrect number of entries");
            }
            //Allowed for A/C/G/T input in Haps files.
            while (st.hasMoreTokens()){
                String thisGenotype = (String)st.nextElement();
                if (!hapsEven){
                    chromA.add(thisGenotype);
                }
                else {
                    chromB.add(thisGenotype);
                }
                if (thisGenotype.equalsIgnoreCase("h")) {
                    genos[q] = 9;
                }else if (thisGenotype.equalsIgnoreCase("A")){
                    genos[q] = 1;
                }else if (thisGenotype.equalsIgnoreCase("C")){
                    genos[q] = 2;
                }else if (thisGenotype.equalsIgnoreCase("G")){
                    genos[q] = 3;
                }else if (thisGenotype.equalsIgnoreCase("T")){
                    genos[q] = 4;
                }
                else{
                    try{
                        genos[q] = Byte.parseByte(thisGenotype);
                    }catch (NumberFormatException nfe){
                        throw new PedFileException("Genotype file input error:\ngenotype value \""
                                + thisGenotype + "\" on line " + lineCount + " not allowed.");
                    }
                }
                //Allele values other then 0-4 or 9 generate exceptions.
                if ((genos[q] < 0 || genos[q] > 4) && (genos[q] != 9)){
                    throw new PedFileException("Genotype file input error:\ngenotype value \"" + genos[q] +
                            "\" on line " + lineCount + " not allowed.");
                }
                q++;
            }

            if (hapsEven) {
                for (int m=0; m<chromA.size(); m++){
                    if (((String)chromA.get(m)).equalsIgnoreCase("h")){
                        chromA.set(m, "9");
                    }else if (((String)chromA.get(m)).equalsIgnoreCase("A")){
                        chromA.set(m, "1");
                        hapsError = !hapsError;
                    }else if (((String)chromA.get(m)).equalsIgnoreCase("C")){
                        chromA.set(m, "2");
                        hapsError = !hapsError;
                    }else if (((String)chromA.get(m)).equalsIgnoreCase("G")){
                        chromA.set(m, "3");
                        hapsError = !hapsError;
                    }else if (((String)chromA.get(m)).equalsIgnoreCase("T")){
                        chromA.set(m, "4");
                        hapsError = !hapsError;
                    }
                    if (((String)chromB.get(m)).equalsIgnoreCase("h")){
                        chromB.set(m, "9");
                    }else if (((String)chromB.get(m)).equalsIgnoreCase("A")){
                        chromB.set(m, "1");
                        hapsError = !hapsError;
                    }else if (((String)chromB.get(m)).equalsIgnoreCase("C")){
                        chromB.set(m, "2");
                        hapsError = !hapsError;
                    }else if (((String)chromB.get(m)).equalsIgnoreCase("G")){
                        chromB.set(m, "3");
                        hapsError = !hapsError;
                    }else if (((String)chromB.get(m)).equalsIgnoreCase("T")){
                        chromB.set(m, "4");
                        hapsError = !hapsError;
                    }
                    if (hapsError){
                        throw new PedFileException("File input error: Individual " + ind.getFamilyID() + " strand " + ind.getIndividualID()  + ", marker " + (m+1)  +
                                ".\nFor any marker, an individual's genotype must be only letters or only numbers.");
                    }
                    byte allele1 = Byte.parseByte(chromA.get(m).toString());
                    byte allele2 = Byte.parseByte(chromB.get(m).toString());
                    ind.addMarker(allele1, allele2);
                }
                //check if the family exists already in the Hashtable
                Family fam = (Family)this.families.get(ind.getFamilyID());
                if(fam == null){
                    //it doesnt exist, so create a new Family object
                    fam = new Family(ind.getFamilyID());
                }

                if (fam.getMembers().containsKey(ind.getIndividualID())){
                    throw new PedFileException("Individual "+ind.getIndividualID()+" in family "+ ind.getFamilyID()+" appears more than once.");
                }

                fam.addMember(ind);
                this.families.put(ind.getFamilyID(),fam);
                this.allIndividuals.add(ind);
                chromA = new Vector();
                chromB = new Vector();
            }
            hapsEven = !hapsEven;
        }
        if (hapsEven){
            //we're missing a line here
            throw new PedFileException("Genotype file appears to have an odd number of lines.\n"+
                    "Each individual is required to have two chromosomes");
        }


    }

    public int[] checkGenotype(String allele) throws PedFileException{
        //This method cleans up the genotype checking process for hap map and ped files & allows for both numerical and alphabetical input.
        int[] genotype = new int[2];

        if (allele.equalsIgnoreCase("N")){
            genotype[0] = 0;
        }else if (allele.equalsIgnoreCase("A")){
            genotype[0] = 1;
        }else if (allele.equalsIgnoreCase("C")){
            genotype[0] = 2;
        }else if (allele.equalsIgnoreCase("G")){
            genotype[0] = 3;
        }else if (allele.equalsIgnoreCase("T")){
            genotype[0] = 4;
        }else{
            genotype[0] = Integer.parseInt(allele.trim());
            genotype[1] = 1;
        }

        return genotype;
    }

    public Vector check() throws PedFileException{
        //before we perform the check we want to prune out individuals with too much missing data
        //or trios which contain individuals with too much missing data

        Iterator fitr = families.values().iterator();
        Vector useable = new Vector();
        while (fitr.hasNext()){
            Family curFam = (Family) fitr.next();
            Enumeration indIDEnum = curFam.getMemberList();
            Vector victor = new Vector();
            while (indIDEnum.hasMoreElements()){
                victor.add(curFam.getMember((String) indIDEnum.nextElement()));
            }

            PedParser pp = new PedParser();
            try {
                SimpleGraph sg = pp.buildGraph(victor, Options.getMissingThreshold());
                Vector indStrings = pp.parsePed(sg);
                if (indStrings != null){
                    Iterator sitr = indStrings.iterator();
                    while (sitr.hasNext()){
                        useable.add(curFam.getMember((String)sitr.next()));
                    }
                }
            }catch (PedigreeException pe){
                String pem = pe.getMessage();
                if (pem.indexOf("one parent") != -1){
                    indIDEnum = curFam.getMemberList();
                    while (indIDEnum.hasMoreElements()){
                        curFam.getMember((String) indIDEnum.nextElement()).setReasonImAxed(pem);
                    }
                }else{
                    throw new PedFileException(pem + "\nin family " + curFam.getFamilyName());
                }
            }
        }

        unrelatedIndividuals = useable;

        Vector indList = (Vector)allIndividuals.clone();
        Individual currentInd;
        Family currentFamily;

        //deal with individuals who are missing too much data
        for(int x=0; x < indList.size(); x++){
            currentInd = (Individual)indList.elementAt(x);
            currentFamily = getFamily(currentInd.getFamilyID());

            if (currentInd.getGenoPC() < 1 - Options.getMissingThreshold()){
                allIndividuals.removeElement(currentInd);
                axedPeople.add(currentInd);
                currentInd.setReasonImAxed("% Genotypes: " + new Double(currentInd.getGenoPC()*100).intValue());
                currentFamily.removeMember(currentInd.getIndividualID());
                if (currentFamily.getNumMembers() == 0){
                    //if everyone in a family is gone, we remove it from the list
                    families.remove(currentInd.getFamilyID());
                }
            }else if (!useable.contains(currentInd)){
                axedPeople.add(currentInd);
                if (currentInd.getReasonImAxed() == null){
                    currentInd.setReasonImAxed("Not a member of maximum unrelated subset.");
                }
            }
        }
        if (useable.size() == 0){
            //todo: this should be more specific about the problems.
            throw new PedFileException("File contains zero valid individuals.");
        }

        setMendelsExist(false);
        CheckData cd = new CheckData(this);
        Vector results = cd.check();
        this.results = results;
        return results;
    }

    public String[][] getHMInfo() {
        return hminfo;
    }

    public Vector getResults() {
        return results;
    }

    public void setResults(Vector res){
        results = res;
    }

    public Vector getAxedPeople() {
        return axedPeople;
    }

    public boolean isBogusParents() {
        return bogusParents;
    }

    public Vector getTableData(){
        Vector tableData = new Vector();
        int numResults = results.size();
        markerRatings = new int[numResults];
        dups = new int[numResults];
        for (int i = 0; i < numResults; i++){
            Vector tempVect = new Vector();
            MarkerResult currentResult = (MarkerResult)results.get(i);
            tempVect.add(new Integer(i+1));
            if (Chromosome.getUnfilteredMarker(0).getName() != null){
                tempVect.add(Chromosome.getUnfilteredMarker(i).getDisplayName());
                tempVect.add(new Long(Chromosome.getUnfilteredMarker(i).getPosition()));
            }
            tempVect.add(new Double(currentResult.getObsHet()));
            tempVect.add(new Double(currentResult.getPredHet()));
            tempVect.add(new Double(currentResult.getHWpvalue()));
            tempVect.add(new Double(currentResult.getGenoPercent()));
            tempVect.add(new Integer(currentResult.getFamTrioNum()));
            tempVect.add(new Integer(currentResult.getMendErrNum()));
            tempVect.add(new Double(currentResult.getMAF()));
            tempVect.add(currentResult.getMajorAllele() + ":" + currentResult.getMinorAllele());

            int dupStatus = Chromosome.getUnfilteredMarker(i).getDupStatus();
            if ((currentResult.getRating() > 0 && dupStatus != 2) ||
                    isWhiteListed(Chromosome.getUnfilteredMarker(i))){
                tempVect.add(new Boolean(true));
            }else{
                tempVect.add(new Boolean(false));
            }

            //these values are never displayed, just kept for bookkeeping
            markerRatings[i] = currentResult.getRating();
            dups[i] = dupStatus;

            tableData.add(tempVect.clone());
        }

        return tableData;
    }

    public int[] getMarkerRatings(){
        return markerRatings;
    }

    public int[] getDups(){
        return dups;
    }

    public Vector getColumnNames() {
        Vector c = new Vector();
        c = new Vector();
        c.add("#");
        if (Chromosome.getUnfilteredMarker(0).getName() != null){
            c.add("Name");
            c.add("Position");
        }
        c.add("ObsHET");
        c.add("PredHET");
        c.add("HWpval");
        c.add("%Geno");
        c.add("FamTrio");
        c.add("MendErr");
        c.add("MAF");
        c.add("Alleles");
        c.add("Rating");
        return c;
    }

    public void saveCheckDataToText(File outfile) throws IOException {
        FileWriter checkWriter = null;
        if (outfile != null){
            checkWriter = new FileWriter(outfile);
        }else{
            throw new IOException("Error saving checkdata to file.");
        }

        Vector names = getColumnNames();
        int numCols = names.size();
        StringBuffer header = new StringBuffer();
        for (int i = 0; i < numCols; i++){
            header.append(names.get(i)).append("\t");
        }
        header.append("\n");
        checkWriter.write(header.toString());

        Vector tableData = getTableData();
        for (int i = 0; i < tableData.size(); i++){
            StringBuffer sb = new StringBuffer();
            Vector row = (Vector)tableData.get(i);
            //don't print the true/false vals in last column
            for (int j = 0; j < numCols-1; j++){
                sb.append(row.get(j)).append("\t");
            }
            //print BAD if last column is false
            if (((Boolean)row.get(numCols-1)).booleanValue()){
                sb.append("\n");
            }else{
                sb.append("BAD\n");
            }
            checkWriter.write(sb.toString());
        }

        checkWriter.close();
    }

    public void setWhiteList(HashSet whiteListedCustomMarkers) {
        whitelist = whiteListedCustomMarkers;
    }

    public boolean isWhiteListed(SNP snp){
        return whitelist.contains(snp);
    }

    public Vector getHaploidHets() {
        return haploidHets;
    }

    public void addHaploidHet(String haploid) {
        if(haploidHets != null){
            haploidHets.add(haploid);
        }else{
            haploidHets = new Vector();
            haploidHets.add(haploid);
        }
    }

    public boolean getMendelsExist(){
        return mendels;
    }

    public void setMendelsExist(boolean mendel){
        mendels = mendel;
    }
}


