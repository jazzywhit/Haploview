package edu.mit.wi.haploview;

import java.io.*;
import java.util.*;
/**
 * <p>Title: PedFileParser.java </p>
 * <p>Description: Uses it to parse a pedigree file and store the data in object PedFile.
 * The format of pedigree file is:
 * column 1: familyID
 * column 2: individual ID
 * column 3: dad ID
 * column 4: mom ID
 * column 5: gender ( 1 is male, 2 is female )
 * column 6: affected status (1 is unaffected, 2 is affected )
 * column 7: (optional) libility
 * column 8 and 9: marker 1 (seperated by space)
 * column 2n and 2n+1: marker n (seperated by space)
 * Columns are seperated by tab except between one set of marker.
 * Family ID plus individual ID is unique and "0" stands for data missing.
 * </p>
 * @author Hui Gong
 * @version $Revision 1.2 $
 */

public class PedFileParser {

    public PedFileParser() {
    }

    /**
     * Parses input file to object PedFile
     * @param in pedigree input file
     */
    public static PedFile parse(File in) throws Exception{
        PedFile pedFile = new PedFile();

        BufferedReader reader = new BufferedReader(new FileReader(in));
        String line=null;
        int colNum = -1;
        boolean withOptionalColumn = false;
        while((line=reader.readLine())!=null){
            StringTokenizer tokenizer = new StringTokenizer(line, "\n\t\" \"");
            //reading the first line
            if(colNum < 0){
                //only check column number count for the first line
                colNum = tokenizer.countTokens();
                if(colNum%2==1) withOptionalColumn = true;
            }
            PedFileEntry entry = new PedFileEntry();
            if(tokenizer.hasMoreTokens()){
                entry.setFamilyID(tokenizer.nextToken().trim());
                entry.setIndivID(tokenizer.nextToken().trim());
                entry.setDadID(tokenizer.nextToken().trim());
                entry.setMomID(tokenizer.nextToken().trim());
                entry.setGender(Integer.parseInt(tokenizer.nextToken().trim()));
                entry.setAffectedStatus(Integer.parseInt(tokenizer.nextToken().trim()));
                if(withOptionalColumn) entry.setLibility(tokenizer.nextToken().trim());
		boolean isTyped = false;
                while(tokenizer.hasMoreTokens()){
                    //alleles seperated by space
                    PedMarker marker = new PedMarker();
                    String allele1 = tokenizer.nextToken().trim();
                    String allele2 = tokenizer.nextToken().trim();
		    if (!(allele1.equals("0") && allele2.equals("0"))) isTyped = true;
                    marker.setAllele1(Integer.parseInt(allele1));
                    marker.setAllele2(Integer.parseInt(allele2));
                    entry.addMarker(marker);
                }
		//note whether this is a real indiv (true) or a "dummy" (false)
		if (isTyped) entry.setIsTyped(true);
            }
	    pedFile.addContent(entry);
        }
        return pedFile;
    }


    /**
     * Parse an input PedFile with header
     * @param in pedigree file with header
     */
    public static PedFile parserHeaderFile(File in) throws Exception{
        PedFile pedFile = new PedFile();

        BufferedReader reader = new BufferedReader(new FileReader(in));
        String line=null;
        int colNum = -1, markerNum=0;
        boolean withOptionalColumn = false;
        String header="";
        //first line for Markerfile is header
        if((line=reader.readLine())!=null) header = line;
        while((line=reader.readLine())!=null){
            StringTokenizer tokenizer = new StringTokenizer(line, "\n\t\" \"");
            //reading the first line
            if(colNum < 0){
                //only check column number count for the first line
                colNum = tokenizer.countTokens();
                if(colNum%2==1) withOptionalColumn = true;
            }
            PedFileEntry entry = new PedFileEntry();
            if(tokenizer.hasMoreTokens()){
                entry.setFamilyID(tokenizer.nextToken().trim());
                entry.setIndivID(tokenizer.nextToken().trim());
                entry.setDadID(tokenizer.nextToken().trim());
                entry.setMomID(tokenizer.nextToken().trim());
                entry.setGender(Integer.parseInt(tokenizer.nextToken().trim()));
                entry.setAffectedStatus(Integer.parseInt(tokenizer.nextToken().trim()));
                if(withOptionalColumn) entry.setLibility(tokenizer.nextToken().trim());

                while(tokenizer.hasMoreTokens()){
                    //alleles seperated by space
                    PedMarker marker = new PedMarker();
                    String allele1 = tokenizer.nextToken().trim();
                    String allele2 = tokenizer.nextToken().trim();
                    marker.setAllele1(Integer.parseInt(allele1));
                    marker.setAllele2(Integer.parseInt(allele2));
                    entry.addMarker(marker);
                }
            }
            pedFile.addContent(entry);
        }
        StringTokenizer tokenizer = new StringTokenizer(header);
        int numBeforeMarker = withOptionalColumn?7:6;

        int i=0;
        Vector names = new Vector();
        while(tokenizer.hasMoreTokens()){
            i++;
            if(i<=numBeforeMarker){
                tokenizer.nextToken();
                continue;
            }
            else{
                names.add(tokenizer.nextToken());
            }
        }
        //System.out.println("name: "+names.size());
        pedFile.setMarkerNames(names);
        return pedFile;
    }


}
