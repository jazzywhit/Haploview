/////////////////////////////////////////////
// Created with IntelliJ IDEA.             //
// User: Jesse Whitworth                   //
// Date: Apr 11, 2008                      //
// Time: 1:30:17 PM                        //
/////////////////////////////////////////////
package edu.mit.wi.haploview.genecruiser;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import java.io.*;
import java.util.Iterator;
import java.util.Vector;
import java.net.URL;
import javax.xml.stream.*;

import edu.mit.wi.haploview.HaploViewException;
import edu.mit.wi.haploview.Constants;

/**
 * Connects with the Genecruiser server via HTTP request for obtaining an XML file with relevant data. Also parses said XML data.
 * @author Jesse Whitworth
 */
public class GeneCruiser implements Constants {

    private
//    String origNamespace = "http://service.genecruiser.org/xsd";
//    String dataNamespace = "service.genecruiser.org";
    String host = "genecruiser.broad.mit.edu/genecruiser3_services";
    String firstResult = "0";
    String email = "haploview@broad.mit.edu";
    Logger logger = Logger.getRootLogger();
    int procCount = 0;
    int depthCount = 0;
    int searchType = -1;
    String geneId = "", displayName, IdType;
    String majorTree = "";
    String currId = "", currIdType = "", currIdDisplayName = "";
    Vector SNPs = new Vector();
    Vector Genes = new Vector();
    double Start = 0, End = 0;


    /**
     * Gets the GeneCruiser data and returns it as an array of ints.
     * @param searchType Type of query, validated with if statements
     * @param searchID String literal of the user's query
     * @throws edu.mit.wi.haploview.HaploViewException If there are problems with data collection
     */

    public GeneCruiser(int searchType, String searchID) throws HaploViewException{

        this.searchType = searchType;
        BasicConfigurator.configure();
        logger.setLevel(Level.OFF);
        String address;

        //Make sure that the user has put in at least some request.
        if (searchID.length() > 0){

            if (searchType == 0){//For ENSMBL Searches        ENSG00000114784
                address = "http://" + host + "/rest/variation/byGenomicId?idType=ensembl_gene_stable_id&id=" + searchID + "&firstResult=" + firstResult + "&email=" + email;
                majorTree = "VariationQueryResults";

            }else if (searchType == 1){  //For HUGO Searches      1100
                address = "http://" + host + "/rest/variation/byGenomicId?idType=HUGO&id=" + searchID + "&firstResult=" + firstResult + "&email=" + email;
                majorTree = "VariationQueryResults";

            }else if (searchType == 2){//For SNP Searches    rs5004340
                address = "http://" + host + "/rest/variation/byName?name=" + searchID + "&firstResult=" + firstResult + "&email=" + email;
                majorTree = "Variations";

            }else if (searchType == 3){//For Region Searches    6:1234-5678+7:1234-5678

                address = "http://" + host + "/rest/variation/byLocation?locations=" + searchID + "&firstResult=" + firstResult + "&email=" + email;
                majorTree = "Variations";

            }else if (searchType == 4){//For searching flanking regions

                //http://genecruiser.broad.mit.edu/genecruiser3_services/rest/gene/byVariationIdFivePrimeThreePrime?id=rs12949853&fivePrimeSize=1000&threePrimeSize=100000&firstResult=0&email=haploview@broad.mit.edu
                // searchID = rs12949853&fivePrimeSize=1000&threePrimeSize=100000    .... this is the format to follow
                address = "http://" + host + "/rest/gene/byVariationIdFivePrimeThreePrime?id=" + searchID + "&firstResult=" + firstResult + "&email=" + email;
                majorTree = "GeneByVariationIdResults";

            }else{
                throw new HaploViewException("Search Type Required for Genecruiser");
            }
        }else{
            throw new HaploViewException("Please Enter a Search Query");
        }

        collectData(address);
    }


    /**
     * Grabs data from the genecruiser service, when you provide the HTTP request
     * @param address Valid HTTP request for Genecruiser Query
     * @throws HaploViewException
     */
    //TODO Make this ThreadSafe to avoid lockups if the server is down.
    private void collectData(String address)throws HaploViewException{

        try {

            //Connection address
            URL inURL = new URL(address);

            //Load the XML file into the parser
            XMLStreamReader parser =
                    XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(inURL.openStream()));
            StAXOMBuilder builder = new StAXOMBuilder(parser);

            //Create the XML Document
            OMElement docElement =  builder.getDocumentElement();

            //Check the document to make sure it is of the correct type

            if (docElement.getType() == 1){
                Iterator iter = docElement.getChildren();
                NamespaceNav(iter);
            }

            if(DEBUG){
                System.out.println("\n\n --------------------------------------");
                System.out.println("Printing SNPs\n");
                System.out.println("--------------------------------------\n\n");
                for(int i = 0; i < SNPs.size(); i++){
                    ((gcSNP)SNPs.get(i)).print();
                }
                System.out.println("\n\n --------------------------------------");
                System.out.println("Printing Genes\n");
                System.out.println("--------------------------------------\n\n");
                for(int i = 0; i < Genes.size(); i++){
                    ((gcGene)Genes.get(i)).print();
                }
            }

        }catch(IOException ioe){
            throw new HaploViewException("Error Connecting to GeneCruiser");
        }catch(XMLStreamException xmls){
            throw new HaploViewException("Error Reading Genecruiser Data; collectData");
        }
    }

    /**
     * Navigates through the XML document, searching for the main level. The main level is the first level of the XML that
     * has relatives.
     * @param iter A series of nodes that need to be navigated and validated
     * @throws HaploViewException If there are problems with data collection
     */
    private void NamespaceNav(Iterator iter) throws HaploViewException{

        OMNode currentNode = (OMNode)iter.next();

        while (!iter.hasNext()){
            if (HasChildren(currentNode)){
                //Looking for main Level
                if(((OMElement)currentNode).getLocalName().trim().equals(majorTree)){
                    break;
                }
                iter = ((OMElement)currentNode).getChildren();
                currentNode = (OMNode)iter.next();
            }else{
                break;
            }
        }

        //Check that the main Level is viable
        if(currentNode.getType()==1){
            CaptureNode(currentNode);
        }
    }

    /**
     * Goes to a major level and finds nodes to capture and consume
     * @param inputNode The parent node containing children of interest
     * @throws HaploViewException
     */
    private void CaptureNode(OMNode inputNode)throws HaploViewException{

        Iterator iter = ((OMElement)inputNode).getChildren();
        while(iter.hasNext()){

            if (searchType == 0){

                captureGene((OMElement)iter.next());

            }else if (searchType == 1){

                captureGene((OMElement)iter.next());

            }else if (searchType == 2){

                CaptureVariation((OMElement)iter.next());

            }else if (searchType == 3){
                CaptureVariation((OMElement)iter.next());

            }else if (searchType == 4){

                CaptureFlanking((OMElement)iter.next());

            }
        }
    }

    /**
     * Checks to make sure that the node has children, if it does not, then it is not pursued
     * @param currentNode Node that is to be checked for children
     * @return
     */
    private boolean HasChildren(OMNode currentNode){

        Iterator iter = ((OMElement)currentNode).getChildren();
        return (iter.hasNext());

    }

    /**
     * Capture the main data about each SNP
     * @param inputNode A Node that has children containing SNP data
     * @throws HaploViewException
     */
    private void CaptureVariation(OMNode inputNode) throws HaploViewException{

        String VariationName = "", Source= "", Allele= "", ConsequenceType= "", Chromosome= "",
        Start= "", End= "", Strand= "";
        Iterator childList = ((OMElement)inputNode).getChildren();

        try{
        while (childList.hasNext()) {
            OMNode currentNode = (OMNode)childList.next();
            OMElement temp = (OMElement)currentNode;

            //Validate that the node is of a useable type, if not 1 then the tree is not fully broken down
            if (((OMElement)currentNode).getLocalName().equals("VariationName")){
                VariationName = ((OMElement)currentNode).getText();

            }else if (((OMElement)currentNode).getLocalName().equals("Source")){
                Source = ((OMElement)currentNode).getText();

            }else if (((OMElement)currentNode).getLocalName().equals("Allele")){
                Allele = ((OMElement)currentNode).getText();

            }else if (((OMElement)currentNode).getLocalName().equals("ConsequenceType")){
                ConsequenceType = ((OMElement)currentNode).getText();

            }else if (((OMElement)currentNode).getLocalName().equals("Chromosome")){
                Chromosome = ((OMElement)currentNode).getText();

            }else if (((OMElement)currentNode).getLocalName().equals("Start")){
                Start = ((OMElement)currentNode).getText();

            }else if (((OMElement)currentNode).getLocalName().equals("End")){
                End = ((OMElement)currentNode).getText();

            }else if (((OMElement)currentNode).getLocalName().equals("Strand")){
                Strand = ((OMElement)currentNode).getText();
            }
        }
        }catch(ClassCastException cce){
            throw new HaploViewException("Error Reading Genecruiser Data; CaptureVariation");

        }
       SNPs.add(new gcSNP(currId, currIdType, currIdDisplayName, VariationName, Source, Allele, ConsequenceType, Chromosome,
                Start, End, Strand));
    }

    /**
     * Capture the Gene information
     * @param inputNode A node that has children containing the SNPs in a gene
     * @throws HaploViewException
     */
    private void captureGene(OMElement inputNode) throws HaploViewException{

        System.out.println(inputNode.getLocalName());
        Iterator childList = inputNode.getChildren();

        while (childList.hasNext()){

            OMElement currentNode = (OMElement)childList.next();
            if (currentNode.getLocalName().equals("QueryParameter")){
                Iterator grandchildList = currentNode.getChildren();
                while (grandchildList.hasNext()){
                    OMElement secondNode = (OMElement)grandchildList.next();

                    //Validate that the node is of a useable type, if not 1 then the tree is not fully broken down
                    if (secondNode.getLocalName().equals("Id")){
                        currId = secondNode.getText();

                    }else if (secondNode.getLocalName().equals("IdType")){
                        currIdType = secondNode.getText();

                    }else if (secondNode.getLocalName().equals("IdDisplayName")){
                        currIdDisplayName = secondNode.getText();
                    }
                }
            }else if(currentNode.getLocalName().equals("Variation")){
                CaptureVariation(currentNode);
            }
        }
    }
    private void CaptureFlanking(OMNode inputNode) throws HaploViewException{

        Iterator childList = ((OMElement)inputNode).getChildren();
        String GeneId = "", Description= "", Source= "", BioType= "", Chromosome= "", Strand= "", StableID= "", Start= "0", End= "0";

        while (childList.hasNext()){
            OMNode currentNode = (OMNode)childList.next();
            if (((OMElement)currentNode).getLocalName().equals("Gene")){
                Iterator grandchildList = ((OMElement)currentNode).getChildren();
                while (grandchildList.hasNext()){
                    OMElement secondNode = (OMElement)grandchildList.next();

                    //Validate that the node is of a useable type, if not 1 then the tree is not fully broken down
                    if (secondNode.getLocalName().equals("GeneId")){
                        GeneId = secondNode.getText();
                    }else if (secondNode.getLocalName().equals("Description")){
                        Description = secondNode.getText();
                    }else if (secondNode.getLocalName().equals("Source")){
                        Source = secondNode.getText();
                    }else if (secondNode.getLocalName().equals("Biotype")){
                        BioType = secondNode.getText();
                    }else if (secondNode.getLocalName().equals("Chromosome")){
                        Chromosome = secondNode.getText();
                    }else if (secondNode.getLocalName().equals("Start")){
                        Start = secondNode.getText();
                    }else if (secondNode.getLocalName().equals("End")){
                        End = secondNode.getText();
                    }else if (secondNode.getLocalName().equals("Strand")){
                        Strand = secondNode.getText();
                    }else if (secondNode.getLocalName().equals("StableId")){
                        StableID = secondNode.getText();
                    }
                }
            }
        }
        Genes.add(new gcGene(GeneId, Description, Source, BioType, Chromosome, Start, End, Strand, StableID)); /*, Vector<String> GenomicIds*/
    }
    /**
     * Locate the first instance of a Gene
     * @param Id Requested geneID
     * @return index of a found Gene
     * @throws HaploViewException
     */
    public int findGene(String Id)throws HaploViewException{

        try{
            for(int i = 0; i < SNPs.size(); i++){

                if (((gcSNP)(SNPs.get(i))).getId().equalsIgnoreCase(Id.trim()))
                {
                    return i;
                }
            }
            return -1;
        }catch (NullPointerException npe){
            throw new HaploViewException("Error Reading Genecruiser Data; findGene");
        }
    }



    /////////////////////////////////////////////////////////
    //                SNP FUNCTIONS                        //
    /////////////////////////////////////////////////////////

    public Vector getSNPs(){

        return SNPs;

    }
    public gcSNP getSNP(int i)throws HaploViewException{

        if (SNPs.size() >= i)
            return (gcSNP)SNPs.get(i);
        else
            throw new HaploViewException("There is no SNP at this location");
    }

    /**
     * Locates a SNP in the data
     * @param variationName
     * @return
     */
    public int findSNP(String variationName){

        for(int i = 0; i < SNPs.size(); i++){

            if (((gcSNP)(SNPs.get(i))).getVariationName().equalsIgnoreCase(variationName.trim()))
            {return i;}
        }
        return -1;
    }

    /**
     * Checks if a SNP is contained in the data
     * @param variationName
     * @return
     */
    public boolean contains(String variationName){
        if(findSNP(variationName)==-1){
            return false;
        }
        return true;
    }

    /**
     * Returns the size of collected SNPs
     * @return
     */
    public int size(){
        if (SNPs.size() > 0)
            return SNPs.size();
        else if(Genes.size() > 0)
            return Genes.size();
        else
            return 0;
    }

    /**
     * Deletes a SNP of your choosing
     * @param variationName
     * @return
     */
    public boolean deleteSNP(String variationName){
        int index = findSNP(variationName);
        if(index==-1){
            return false;
        }else{
            SNPs.remove(index);
            return true;
        }
    }

    public Vector getGenes(){

        return Genes;
    }

    public gcGene getGene(int i)throws HaploViewException{

        if (Genes.size() >= i)
            return (gcGene)Genes.get(i);
        else
            throw new HaploViewException("There is no Gene at this location");
    }

    public double getStart() throws HaploViewException{

        if(SNPs.size() > 0){
            for(int i = 0; i < SNPs.size(); i++){
                if(Start > ((gcSNP)SNPs.get(i)).getStart() || Start == 0){
                    Start = ((gcSNP)SNPs.get(i)).getStart();
                }
            }
        }else{
            throw new HaploViewException("Data Not Found on Genecruiser");
        }
        return Start;
    }

    public double getEnd()throws HaploViewException{

        if(SNPs.size() > 0){
            for(int i = 0; i < SNPs.size(); i++){
                if(End < ((gcSNP)SNPs.get(i)).getEnd() || End == 0){
                    End = ((gcSNP)SNPs.get(i)).getEnd();
                }
            }
        }else{
            throw new HaploViewException("Data Not Found on Genecruiser");
        }
        return End;
    }

    public int getChromosome() throws HaploViewException{

        int chromInt = -1;
        if (SNPs.size() > 0){
            String chromStr = ((gcSNP)SNPs.get(0)).getChromosome();

            try{
                chromInt = Integer.parseInt(chromStr.trim());

            }catch(NumberFormatException nfe){
                if(chromStr.equalsIgnoreCase("x")){
                    return 23;

                }else if(chromStr.equalsIgnoreCase("y")){
                    return 24;

                }else{
                    throw new HaploViewException("Error Reading Genecruiser Data; getChromosome");
                }
            }
        }else{
            throw new HaploViewException("Data Not Found on Genecruiser");
        }
        return chromInt;
    }
}