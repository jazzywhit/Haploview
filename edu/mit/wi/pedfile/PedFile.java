/*
* $Id: PedFile.java,v 1.21 2004/08/27 15:27:04 jcbarret Exp $
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

import java.util.*;
//import edu.mit.wi.haploview.Chromosome;

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
    private Vector axedFamilies = new Vector();

    /*
    * stores the familyIDs and individualIDs of all individuals found by parse()
    * in order. this is useful for outputting Pedigree information to a file of another type.
    * the information is stored as an array of two strings.
    * also used in parsing hapmap data
    */
    private Vector order;
    private Vector results = null;
    private String[][] hminfo;

    private static Hashtable hapMapTranslate;

    public PedFile(){

        //hardcoded hapmap info
        this.families = new Hashtable();

        hapMapTranslate = new Hashtable(90,1);
        hapMapTranslate.put("NA10846", "1334 NA10846 NA12144 NA12145 1 1" );
        hapMapTranslate.put("NA12144", "1334 NA12144 0 0 1 1");
        hapMapTranslate.put("NA12145", "1334 NA12145 0 0 2 1");
        hapMapTranslate.put("NA10847", "1334 NA10847 NA12146 NA12239 2 1" );
        hapMapTranslate.put("NA12146", "1334 NA12146 0 0 1 1");
        hapMapTranslate.put("NA12239", "1334 NA12239 0 0 2 1");
        hapMapTranslate.put("NA07029", "1340 NA07029 NA06994 NA07000 1 1" );
        hapMapTranslate.put("NA06994", "1340 NA06994 0 0 1 1");
        hapMapTranslate.put("NA07000", "1340 NA07000 0 0 2 1");
        hapMapTranslate.put("NA07019", "1340 NA07019 NA07022 NA07056 2 1" );
        hapMapTranslate.put("NA07022", "1340 NA07022 0 0 1 1");
        hapMapTranslate.put("NA07056", "1340 NA07056 0 0 2 1");
        hapMapTranslate.put("NA07048", "1341 NA07048 NA07034 NA07055 1 1" );
        hapMapTranslate.put("NA07034", "1341 NA07034 0 0 1 1");
        hapMapTranslate.put("NA07055", "1341 NA07055 0 0 2 1");
        hapMapTranslate.put("NA06991", "1341 NA06991 NA06993 NA06985 2 1" );
        hapMapTranslate.put("NA06993", "1341 NA06993 0 0 1 1");
        hapMapTranslate.put("NA06993.dup", "dup NA06993.dup 0 0 1 1");
        hapMapTranslate.put("NA06985", "1341 NA06985 0 0 2 1");
        hapMapTranslate.put("NA10851", "1344 NA10851 NA12056 NA12057 1 1" );
        hapMapTranslate.put("NA12056", "1344 NA12056 0 0 1 1");
        hapMapTranslate.put("NA12057", "1344 NA12057 0 0 2 1");
        hapMapTranslate.put("NA07348", "1345 NA07348 NA07357 NA07345 2 1" );
        hapMapTranslate.put("NA07357", "1345 NA07357 0 0 1 1");
        hapMapTranslate.put("NA07345", "1345 NA07345 0 0 2 1");
        hapMapTranslate.put("NA10857", "1346 NA10857 NA12043 NA12044 1 1" );
        hapMapTranslate.put("NA12043", "1346 NA12043 0 0 1 1");
        hapMapTranslate.put("NA12044", "1346 NA12044 0 0 2 1");
        hapMapTranslate.put("NA10859", "1347 NA10859 NA11881 NA11882 2 1" );
        hapMapTranslate.put("NA11881", "1347 NA11881 0 0 1 1");
        hapMapTranslate.put("NA11882", "1347 NA11882 0 0 2 1");
        hapMapTranslate.put("NA10854", "1349 NA10854 NA11839 NA11840 2 1" );
        hapMapTranslate.put("NA11839", "1349 NA11839 0 0 1 1");
        hapMapTranslate.put("NA11840", "1349 NA11840 0 0 2 1");
        hapMapTranslate.put("NA10856", "1350 NA10856 NA11829 NA11830 1 1" );
        hapMapTranslate.put("NA11829", "1350 NA11829 0 0 1 1");
        hapMapTranslate.put("NA11830", "1350 NA11830 0 0 2 1");
        hapMapTranslate.put("NA10855", "1350 NA10855 NA11831 NA11832 2 1" );
        hapMapTranslate.put("NA11831", "1350 NA11831 0 0 1 1");
        hapMapTranslate.put("NA11832", "1350 NA11832 0 0 2 1");
        hapMapTranslate.put("NA12707", "1358 NA12707 NA12716 NA12717 1 1" );
        hapMapTranslate.put("NA12716", "1358 NA12716 0 0 1 1");
        hapMapTranslate.put("NA12717", "1358 NA12717 0 0 2 1");
        hapMapTranslate.put("NA10860", "1362 NA10860 NA11992 NA11993 1 1" );
        hapMapTranslate.put("NA11992", "1362 NA11992 0 0 1 1");
        hapMapTranslate.put("NA11993", "1362 NA11993 0 0 2 1");
        hapMapTranslate.put("NA11993.dup", "dup NA11993.dup 0 0 2 1");
        hapMapTranslate.put("NA10861", "1362 NA10861 NA11994 NA11995 2 1" );
        hapMapTranslate.put("NA11994", "1362 NA11994 0 0 1 1");
        hapMapTranslate.put("NA11995", "1362 NA11995 0 0 2 1");
        hapMapTranslate.put("NA10863", "1375 NA10863 NA12264 NA12234 2 1" );
        hapMapTranslate.put("NA12264", "1375 NA12264 0 0 1 1");
        hapMapTranslate.put("NA12234", "1375 NA12234 0 0 2 1");
        hapMapTranslate.put("NA10830", "1408 NA10830 NA12154 NA12236 1 1" );
        hapMapTranslate.put("NA12154", "1408 NA12154 0 0 1 1");
        hapMapTranslate.put("NA12236", "1408 NA12236 0 0 2 1");
        hapMapTranslate.put("NA10831", "1408 NA10831 NA12155 NA12156 2 1" );
        hapMapTranslate.put("NA12155", "1408 NA12155 0 0 1 1");
        hapMapTranslate.put("NA12156", "1408 NA12156 0 0 2 1");
        hapMapTranslate.put("NA12156.dup", "dup NA12156.dup 0 0 2 1");
        hapMapTranslate.put("NA10835", "1416 NA10835 NA12248 NA12249 1 1" );
        hapMapTranslate.put("NA12248", "1416 NA12248 0 0 1 1");
        hapMapTranslate.put("NA12248.dup", "dup NA1248.dup 0 0 1 1");
        hapMapTranslate.put("NA12249", "1416 NA12249 0 0 2 1");
        hapMapTranslate.put("NA10838", "1420 NA10838 NA12003 NA12004 1 1" );
        hapMapTranslate.put("NA12003", "1420 NA12003 0 0 1 1");
        hapMapTranslate.put("NA12003.dup", "dup NA12003.dup 0 0 1 1");
        hapMapTranslate.put("NA12004", "1420 NA12004 0 0 2 1");
        hapMapTranslate.put("NA10839", "1420 NA10839 NA12005 NA12006 2 1" );
        hapMapTranslate.put("NA12005", "1420 NA12005 0 0 1 1");
        hapMapTranslate.put("NA12006", "1420 NA12006 0 0 2 1");
        hapMapTranslate.put("NA12740", "1444 NA12740 NA12750 NA12751 2 1" );
        hapMapTranslate.put("NA12750", "1444 NA12750 0 0 1 1");
        hapMapTranslate.put("NA12751", "1444 NA12751 0 0 2 1");
        hapMapTranslate.put("NA12752", "1447 NA12752 NA12760 NA12761 1 1" );
        hapMapTranslate.put("NA12760", "1447 NA12760 0 0 1 1");
        hapMapTranslate.put("NA12761", "1447 NA12761 0 0 2 1");
        hapMapTranslate.put("NA12753", "1447 NA12753 NA12762 NA12763 2 1" );
        hapMapTranslate.put("NA12762", "1447 NA12762 0 0 1 1");
        hapMapTranslate.put("NA12763", "1447 NA12763 0 0 2 1");
        hapMapTranslate.put("NA12801", "1454 NA12801 NA12812 NA12813 1 1" );
        hapMapTranslate.put("NA12812", "1454 NA12812 0 0 1 1");
        hapMapTranslate.put("NA12813", "1454 NA12813 0 0 2 1");
        hapMapTranslate.put("NA12802", "1454 NA12802 NA12814 NA12815 2 1" );
        hapMapTranslate.put("NA12814", "1454 NA12814 0 0 1 1");
        hapMapTranslate.put("NA12815", "1454 NA12815 0 0 2 1");
        hapMapTranslate.put("NA12864", "1459 NA12864 NA12872 NA12873 1 1" );
        hapMapTranslate.put("NA12872", "1459 NA12872 0 0 1 1");
        hapMapTranslate.put("NA12873", "1459 NA12873 0 0 2 1");
        hapMapTranslate.put("NA12865", "1459 NA12865 NA12874 NA12875 2 1" );
        hapMapTranslate.put("NA12874", "1459 NA12874 0 0 1 1");
        hapMapTranslate.put("NA12875", "1459 NA12875 0 0 2 1");
        hapMapTranslate.put("NA12878", "1463 NA12878 NA12891 NA12892 2 1" );
        hapMapTranslate.put("NA12891", "1463 NA12891 0 0 1 1");
        hapMapTranslate.put("NA12892", "1463 NA12892 0 0 2 1");
    }

    /**
     * gets the order Vector
     * @return
     */
    public Vector getOrder() {
        return order;
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
     * @param pedigrees a Vector of strings containing one pedigree line per string
     */
    public void parseLinkage(Vector pedigrees) throws PedFileException {
        int colNum = -1;
        boolean withOptionalColumn = false;
        int numLines = pedigrees.size();
        Individual ind;

        this.order = new Vector();

        for(int k=0; k<numLines; k++){
            StringTokenizer tokenizer = new StringTokenizer((String)pedigrees.get(k), "\n\t\" \"");
            //reading the first line
            if(colNum < 1){
                //only check column number count for the first nonblank line
                colNum = tokenizer.countTokens();
                if(colNum%2==1) {
                    withOptionalColumn = true;
                }
            }
            if(colNum != tokenizer.countTokens()) {
                //this line has a different number of columns
                //should send some sort of error message
                //TODO: add something which stores number of markers for all lines and checks that they're consistent
                throw new PedFileException("line number mismatch in pedfile. line " + (k+1));
            }

            ind = new Individual(tokenizer.countTokens());

            if(tokenizer.hasMoreTokens()){

                ind.setFamilyID(tokenizer.nextToken().trim());
                ind.setIndividualID(tokenizer.nextToken().trim());
                ind.setDadID(tokenizer.nextToken().trim());
                ind.setMomID(tokenizer.nextToken().trim());
                try {
                    //TODO: affected/liability should not be forced into Integers!
                    ind.setGender(Integer.parseInt(tokenizer.nextToken().trim()));
                    ind.setAffectedStatus(Integer.parseInt(tokenizer.nextToken().trim()));
                    if(withOptionalColumn) {
                        ind.setLiability(Integer.parseInt(tokenizer.nextToken().trim()));
                    }
                }catch(NumberFormatException nfe) {
                    throw new PedFileException("Pedfile error: invalid gender or affected status on line " + (k+1));
                }

                boolean isTyped = false;
                while(tokenizer.hasMoreTokens()){
                    try {
                        int allele1 = Integer.parseInt(tokenizer.nextToken().trim());
                        int allele2 = Integer.parseInt(tokenizer.nextToken().trim());
                        if ( !( (allele1==0) && (allele2 == 0) ) ) isTyped = true;
                        if(allele1 <0 || allele1 > 4 || allele2 <0 || allele2 >4) {
                            throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k+1)
                                    + ".\n all genotypes must be 0-4.");
                        }
                        byte[] markers = new byte[2];
                        markers[0] = (byte)allele1;
                        markers[1]= (byte)allele2;
                        ind.addMarker(markers);
                    }catch(NumberFormatException nfe) {
                        throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k+1) );
                    }
                }

                //note whether this is a real indiv (true) or a "dummy" (false)
                ind.setIsTyped(isTyped);

                //check if the family exists already in the Hashtable
                Family fam = (Family)this.families.get(ind.getFamilyID());
                if(fam == null){
                    //it doesnt exist, so create a new Family object
                    fam = new Family(ind.getFamilyID());
                }
                fam.addMember(ind);
                this.families.put(ind.getFamilyID(),fam);

                String[] indFamID = new String[2];
                indFamID[0] = ind.getFamilyID();
                indFamID[1] = ind.getIndividualID();
                this.order.add(ind);

            }
        }
    }

    public void parseHapMap(Vector rawLines) throws PedFileException {
        int colNum = -1;
        int numLines = rawLines.size();
        Individual ind;

        this.order = new Vector();

        //sort first
        Vector lines = new Vector();
        Hashtable sortHelp = new Hashtable(numLines-1,1.0f);
        long[] pos = new long[numLines-1];
        lines.add(rawLines.get(0));
        for (int k = 1; k < numLines; k++){
            StringTokenizer st = new StringTokenizer((String) rawLines.get(k));
            //strip off 1st 3 cols
            st.nextToken();st.nextToken();st.nextToken();
            pos[k-1] = new Long(st.nextToken()).longValue();
            sortHelp.put(new Long(pos[k-1]),rawLines.get(k));
        }
        Arrays.sort(pos);
        for (int i = 0; i < pos.length; i++){
            lines.add(sortHelp.get(new Long(pos[i])));
        }

        //enumerate indivs
        StringTokenizer st = new StringTokenizer((String)lines.get(0), "\n\t\" \"");
        int numMetaColumns = 0;
        boolean doneMeta = false;
        while(!doneMeta && st.hasMoreTokens()){
            String thisfield = st.nextToken();
            numMetaColumns++;
            //so currently the first person ID always starts with NA (Coriell ID) but
            //todo: will this be true with AA samples etc?
            if (thisfield.startsWith("NA")){
                doneMeta = true;
            }
        }
        numMetaColumns--;

        st = new StringTokenizer((String)lines.get(0), "\n\t\" \"");
        for (int i = 0; i < numMetaColumns; i++){
            st.nextToken();
        }

        StringTokenizer dt;
        while (st.hasMoreTokens()){
            ind = new Individual(numLines);
            String name = st.nextToken();
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
            ind.setIsTyped(true);

            //check if the family exists already in the Hashtable
            Family fam = (Family)this.families.get(ind.getFamilyID());
            if(fam == null){
                //it doesnt exist, so create a new Family object
                fam = new Family(ind.getFamilyID());
            }
            fam.addMember(ind);
            this.families.put(ind.getFamilyID(),fam);

            String[] indFamID = new String[2];
            indFamID[0] = ind.getFamilyID();
            indFamID[1] = ind.getIndividualID();
            this.order.add(ind);
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
                    String s = tokenizer.nextToken().trim();

                    //get marker name, chrom and pos
                    if (skip == 0){
                        hminfo[k-1][0] = s;
                    }
                    if (skip == 2){
                        if (Chromosome.dataChrom != null){
                            if (!Chromosome.dataChrom.equals(s)){
                                throw new PedFileException("Hapmap file format error on line " + (k+1)+
                                        ":\n There appear to be multiple chromosomes in the file.");
                            }
                        }else{
                            Chromosome.dataChrom = s;
                        }
                    }
                    if (skip == 3){
                        hminfo[k-1][1] = s;
                    }
                }
                int index = 0;
                while(tokenizer.hasMoreTokens()){
                    ind = (Individual)order.elementAt(index);
                    String alleles = tokenizer.nextToken();
                    int allele1=0, allele2=0;
                    if (alleles.substring(0,1).equals("A")){
                        allele1 = 1;
                    }else if (alleles.substring(0,1).equals("C")){
                        allele1 = 2;
                    }else if (alleles.substring(0,1).equals("G")){
                        allele1 = 3;
                    }else if (alleles.substring(0,1).equals("T")){
                        allele1 = 4;
                    }
                    if (alleles.substring(1,2).equals("A")){
                        allele2 = 1;
                    }else if (alleles.substring(1,2).equals("C")){
                        allele2 = 2;
                    }else if (alleles.substring(1,2).equals("G")){
                        allele2 = 3;
                    }else if (alleles.substring(1,2).equals("T")){
                        allele2 = 4;
                    }
                    byte[] markers = new byte[2];
                    markers[0] = (byte)allele1;
                    markers[1]= (byte)allele2;
                    ind.addMarker(markers);
                    index++;
                }
            }
        }
    }

    public Vector check() throws PedFileException{
        //before we perform the check we want to prune out individuals with too much missing data
        //or trios which contain individuals with too much missing data
        Vector indList = getOrder();
        Individual currentInd;
        Family currentFamily;
        //the killMe array has a boolean for each person which specifies which people should be removed.
        //the parents or children of such a person are also removed.
        boolean[] killMe = new boolean[indList.size()];
        Arrays.fill(killMe, false);

        for(int x=0; x < indList.size(); x++){
            currentInd = (Individual)indList.elementAt(x);
            currentFamily = getFamily(currentInd.getFamilyID());
            if(currentInd.getIsTyped()){
                //this person is a singleton
                if(currentFamily.getNumMembers() == 1){
                    double numMissing = 0;
                    int numMarkers = currentInd.getNumMarkers();
                    for (int i = 0; i < numMarkers; i++){
                        byte[] thisMarker = currentInd.getMarker(i);
                        if (thisMarker[0] == 0 || thisMarker[1] == 0){
                            numMissing++;
                        }
                    }
                    if (numMissing/numMarkers > Options.getMissingThreshold()){
                        killMe[x] = true;
                    }
                }else if (currentInd.hasBothParents()){
                    //this person has parents
                    Individual mom = currentFamily.getMember(currentInd.getMomID());
                    double momMissing = 0;
                    double dadMissing = 0;
                    Individual dad = currentFamily.getMember(currentInd.getDadID());
                    if (!(mom.hasBothParents() || dad.hasBothParents())){
                        //if my parents have parents, skip me because i don't add any information
                        //i.e. i'm a 3rd+ generation member of this family
                        int numMarkers = currentInd.getNumMarkers();
                        for (int i = 0; i < numMarkers; i++){
                            byte[] dadMarker = dad.getMarker(i);
                            byte[] momMarker = mom.getMarker(i);
                            if (momMarker[0] == 0 || momMarker[1] == 0){
                                momMissing++;
                            }
                            if (dadMarker[0] == 0 || dadMarker[1] == 0){
                                dadMissing++;
                            }
                        }
                        if (momMissing/numMarkers > Options.getMissingThreshold() ||
                                dadMissing/numMarkers > Options.getMissingThreshold()){
                            killMe[x] = true;
                        }
                    }
                }
            }
        }

        for (int x = 0; x < killMe.length; x++){
            //now loop through the list of people to be killed
            //making sure to get their parents and kids if they exist
            if (killMe[x]){
                currentInd = (Individual)indList.elementAt(x);
                currentFamily = getFamily(currentInd.getFamilyID());
                order.removeElement(currentInd);
                axedPeople.add(currentInd.getIndividualID());
                currentFamily.removeMember(currentInd.getIndividualID());
                Enumeration peopleinFam = currentFamily.getMemberList();
                while (peopleinFam.hasMoreElements()){
                    //this looks at the whole family and if any of the members are
                    //parents or kids of the current ind, we kill them too
                    String p = (String)peopleinFam.nextElement();
                    Individual thisPerson = currentFamily.getMember(p);
                    if (thisPerson.getMomID().equals(currentInd.getIndividualID()) ||
                            thisPerson.getDadID().equals(currentInd.getIndividualID()) ||
                            currentInd.getMomID().equals(thisPerson.getIndividualID()) ||
                            currentInd.getDadID().equals(thisPerson.getIndividualID())){
                        order.removeElement(thisPerson);
                        axedPeople.add(currentInd.getIndividualID());
                        currentFamily.removeMember(thisPerson.getIndividualID());
                    }
                }
                if (currentFamily.getNumMembers() == 0){
                    //if everyone in a family is gone, we remove it from the list
                    families.remove(currentInd.getFamilyID());
                    axedFamilies.add(currentInd.getFamilyID());
                }
            }
        }

        CheckData cd = new CheckData(this);
        Vector results = cd.check();
        /*int size = results.size();
        for (int i = 0; i < size; i++) {
        MarkerResult markerResult = (MarkerResult) results.elementAt(i);
        System.out.println(markerResult.toString());
        }*/
        this.results = results;
        return results;
    }

    public String[][] getHMInfo() {
        return hminfo;
    }

    public Vector getResults() {
        return results;
    }
}


