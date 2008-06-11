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

/**
 * Connects with the Genecruiser server via HTTP request for obtaining an XML file with relevant data. Also parses said XML data.
 * @author Jesse Whitworth
 */

public class GeneCruiser {

    private
//    String origNamespace = "http://service.genecruiser.org/xsd";
//    String dataNamespace = "service.genecruiser.org";
    String host = "genecruiser.broad.mit.edu/genecruiser3_services";
    String firstResult = "0";
    String email = "haploview@broad.mit.edu";
    Logger logger = Logger.getRootLogger();
    int procCount = 0;
    int depthCount = 0;
    String geneId = "", displayName, IdType;
    String majorTree = "";
    String currId = "", currIdType = "", currIdDisplayName = "";


    public
//    gcGene currentGene = null;
    Vector<gcSNP> SNPs = new Vector<gcSNP>();
    double Start = 0, End = 0;


    /**
     * Gets the GeneCruiser data and returns it as an array of ints.
     * @param searchType Type of query, validated with if statements
     * @param searchID String literal of the user's query
     * @throws edu.mit.wi.haploview.HaploViewException If there are problems with data collection
     */

    public GeneCruiser(int searchType, String searchID) throws HaploViewException{

        BasicConfigurator.configure();
        logger.setLevel(Level.OFF);
        String address;

        //Make sure that the user has put in at least some request.
        if (searchID.length() > 0){

            if (searchType == 0){//For ENSMBL Searches        ENSG00000114784
                address = "http://" + host + "/rest/variation/byGenomicId?idType=ensembl_gene_stable_id&id=" + searchID + "&firstResult=" + firstResult + "&email=" + email;
                majorTree = "VariationQueryResult";

            }else if (searchType == 1){  //For HUGO Searches      1100
                address = "http://" + host + "/rest/variation/byGenomicId?idType=HUGO&id=" + searchID + "&firstResult=" + firstResult + "&email=" + email;
                majorTree = "VariationQueryResult";

            }else if (searchType ==2){//For SNP Searches    rs5004340
                address = "http://" + host + "/rest/variation/byName?name=" + searchID + "&firstResult=" + firstResult + "&email=" + email;
                majorTree = "Source";

            }else{
                throw new HaploViewException("Search Type Required for Genecruiser");
            }
        }else{
            throw new HaploViewException("Please Enter a Search Query");
        }
        //DEBUGGING FOR MULTIPLE GENES
//        address = "http://genecruiser.broad.mit.edu/genecruiser3_services/rest/variation/byLocation?locations=6:1234-5678+7:1234-5678&firstResult=0&email=haploview@broad.mit.edu";
//        majorTree = "Variation";
        collectData(address);

        //ENABLE FOR DEBUGGING
//        for (int i = 0; i < SNPs.size(); i++){
//
//            SNPs.get(i).print();
//        }
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

//            displayData();

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
                iter = ((OMElement)currentNode).getChildren();
                currentNode = (OMNode)iter.next();

            }else{
                break;
            }
        }

        //Check that the main Level is viable
        if(currentNode.getType()==1){
            while(iter.hasNext()){
                CaptureNode((OMNode)iter.next());
            }
        }
    }

    /**
     * Locates a SNP in the data
     * @param variationName
     * @return
     */
    public int findSNP(String variationName){

        for(int i = 0; i < SNPs.size(); i++){

            if (((gcSNP)SNPs.get(i)).getVariationName().equalsIgnoreCase(variationName.trim()))
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
        return SNPs.size();
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

    /**
     * Goes to a major level and finds nodes to capture and consume
     * @param inputNode The parent node containing children of interest
     * @throws HaploViewException
     */
    private void CaptureNode(OMNode inputNode)throws HaploViewException{

        OMNode childrenNode;

        if (((OMElement)inputNode).getLocalName().equalsIgnoreCase(majorTree)){
            if(majorTree.equals("Variation")){
                //Main Level contains only SNPs
                Iterator iter = (inputNode.getParent()).getChildren();

                while (iter.hasNext()){
                    CaptureVariation((OMNode)iter.next());
                }
            }else if(majorTree.equals("Source")){
                //Main Level contains a single SNP
                CaptureVariation((OMNode)inputNode.getParent());

            }else{
                //Main Level contains Gene information
                Iterator branch = ((OMElement)inputNode).getChildren();

                while (branch.hasNext()){
                    childrenNode = (OMNode)branch.next();

                    if (((OMElement)childrenNode).getLocalName().trim().equalsIgnoreCase("QueryParameter")){
                        captureGene(childrenNode);

                    }
                    if(((OMElement)childrenNode).getLocalName().trim().equalsIgnoreCase("Variation")){
                        CaptureVariation(childrenNode);

                    }
                }
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
    private void captureGene(OMNode inputNode) throws HaploViewException{
        Iterator childList = ((OMElement)inputNode).getChildren();

        while (childList.hasNext()){
            OMNode currentNode = (OMNode)childList.next();

            //Validate that the node is of a useable type, if not 1 then the tree is not fully broken down
            if (((OMElement)currentNode).getLocalName().equals("Id")){
                currId = ((OMElement)currentNode).getText();

            }else if (((OMElement)currentNode).getLocalName().equals("IdType")){
                currIdType = ((OMElement)currentNode).getText();

            }else if (((OMElement)currentNode).getLocalName().equals("IdDisplayName")){
                currIdDisplayName = ((OMElement)currentNode).getText();

            }
        }
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

                if ((SNPs.get(i)).getId().equalsIgnoreCase(Id.trim()))
                {
                    return i;
                }
            }
            return -1;
        }catch (NullPointerException npe){

            throw new HaploViewException("Error Reading Genecruiser Data; findGene");

        }
    }

    /**
     * @return Start of the Gene, on one chromosome
     * @throws HaploViewException
     */
    public double getStart() throws HaploViewException{

        if(SNPs.size() > 0){
            for(int i = 0; i < SNPs.size(); i++){
                if(Start > SNPs.get(i).getStart() || Start == 0){
                    Start = SNPs.get(i).getStart();
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
                if(End < SNPs.get(i).getEnd() || End == 0){
                    End = SNPs.get(i).getEnd();
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
            String chromStr = SNPs.get(0).getChromosome();

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

//    public void calibrateData(){
//
        //TODO PUT SOMETHING IN HERE TO CALIBRATE SO WHEN THERE ARE MULTIPLE GENES IT WILL KNOW, AND CAN ACT ACCORDINGLY.
//    }
}