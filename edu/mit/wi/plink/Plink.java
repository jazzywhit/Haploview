package edu.mit.wi.plink;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Hashtable;



public class Plink {

    private Vector markers = null;
    private Vector results = null;
    private Vector columns = null;
    private Vector ignoredMarkers;

    public void parseWGA(String wga, String map, boolean embed) throws PlinkException {
        markers = new Vector();
        results = new Vector();
        columns = new Vector();
        columns.add("CHROM");
        columns.add("MARKER");
        columns.add("POSITION");

        final File wgaFile = new File(wga);
        final File mapFile = new File(map);
        Hashtable markerHash = new Hashtable(1,1);
        ignoredMarkers = new Vector();

        try{
            if (wgaFile.length() < 1){
                throw new PlinkException("plink file is empty or nonexistent.");
            }

            if (!embed){
                if (mapFile.length() < 1){
                    throw new PlinkException("Map file is empty or nonexistent.");
                }

                BufferedReader mapReader = new BufferedReader(new FileReader(mapFile));
                String mapLine;

                while((mapLine = mapReader.readLine())!=null) {
                    if (mapLine.length() == 0){
                        //skip blank lines
                        continue;
                    }

                    StringTokenizer st = new StringTokenizer(mapLine,"\t ");

                    if (st.countTokens() < 4){
                        throw new PlinkException("Map file is not correctly formatted.");
                    }

                    String chrom = st.nextToken();
                    if (chrom.equals("23")){
                        chrom = "X";
                    }else if (chrom.equals("24")){
                        chrom = "Y";
                    }else if (chrom.equals("25")){
                        chrom = "XY";
                    }
                    String marker = new String(st.nextToken());
                    //useless morgan distance
                    st.nextToken();
                    String pos = st.nextToken();
                    if (pos.startsWith("-")){
                        continue;
                    }
                    long position = Long.parseLong(pos);

                    Marker mark = new Marker(chrom, marker, position);
                    markers.add(mark);
                    markerHash.put(mark.getMarkerID(), mark);
                }
            }

            BufferedReader wgaReader = new BufferedReader(new FileReader(wgaFile));
            int numColumns = 0;
            int markerColumn = -1;
            int chromColumn = -1;
            int positionColumn = -1;
            String headerLine = wgaReader.readLine();
            StringTokenizer headerSt = new StringTokenizer(headerLine);
            while (headerSt.hasMoreTokens()){
                String column = headerSt.nextToken();
                if (column.equalsIgnoreCase("SNP")){
                    markerColumn = numColumns;
                    numColumns++;
                }else if (column.equalsIgnoreCase("CHR")){
                    chromColumn = numColumns;
                    numColumns++;
                }else if (column.equalsIgnoreCase("POS")||column.equalsIgnoreCase("POSITION")){
                    positionColumn = numColumns;
                    numColumns++;
                }else{
                    columns.add(column);
                    numColumns++;
                }
            }

            if (markerColumn == -1){
                throw new PlinkException("Results file must contain a SNP column.");
            }

            if (embed){
                if (chromColumn == -1 || positionColumn == -1){
                    throw new PlinkException("Results files with embedded map files must contain CHR and POS columns.");
                }
            }

            String wgaLine;
            int lineNumber = 0;
            Hashtable markerDups = new Hashtable(1,1);

            while((wgaLine = wgaReader.readLine())!=null){
               if (wgaLine.length() == 0){
                    //skip blank lines
                    continue;
               }
                int tokenNumber = 0;
                StringTokenizer tokenizer = new StringTokenizer(wgaLine);
                String marker = null;
                String chromosome = null;
                long position = 0;
                Vector values = new Vector();
                while(tokenizer.hasMoreTokens()){
                    if (tokenNumber == markerColumn){
                        marker = new String(tokenizer.nextToken());
                        if (markerDups.containsKey(marker)){
                            throw new PlinkException("Marker: " + marker +
                            " appears more than once.");
                        }else{
                            markerDups.put(marker,"");
                        }
                    }else if (tokenNumber == chromColumn){
                        chromosome = tokenizer.nextToken();
                        if(chromosome.equals("23")){
                            chromosome = "X";
                        }else if(chromosome.equals("24")){
                            chromosome = "Y";
                        }else if(chromosome.equals("25")){
                            chromosome = "XY";
                        }
                    }else if (tokenNumber == positionColumn){
                        position = Long.parseLong(tokenizer.nextToken());
                    }else{
                        values.add(tokenizer.nextToken());
                    }
                    tokenNumber++;
                }

                if (tokenNumber != numColumns){
                    throw new PlinkException("Inconsistent column number on line " + (lineNumber+1));
                }

                Marker assocMarker;
                if (!embed){
                    assocMarker = (Marker)markerHash.get(marker);

                    if (assocMarker == null){
                        ignoredMarkers.add(marker);
                        lineNumber++;
                        continue;
                    }else if (!(assocMarker.getChromosome().equalsIgnoreCase(chromosome)) && chromosome != null){
                        throw new PlinkException("Incompatible chromosomes for marker " + marker +
                                "\non line " + lineNumber);
                    }
                }else{
                    assocMarker = new Marker(chromosome,marker,position);
                }

                AssociationResult result = new AssociationResult(lineNumber,assocMarker,values);
                results.add(result);
                lineNumber++;
            }
        }catch(IOException ioe){
            throw new PlinkException("File error.");
        }catch(NumberFormatException nfe){
            throw new PlinkException("File formatting error.");
        }
    }

    public Vector getIgnoredMarkers() {
        return ignoredMarkers;
    }

    public Vector parseMoreResults(String wga) throws PlinkException {
        File moreResultsFile = new File(wga);
        Vector newColumns = new Vector();
        ignoredMarkers = new Vector();
        Vector duplicateColumns = new Vector();
        boolean addColumns = false;


        try{
            if (moreResultsFile.length() < 1){
                throw new PlinkException("plink file is empty or nonexistent.");
            }

            BufferedReader moreResultsReader = new BufferedReader(new FileReader(moreResultsFile));
            int numColumns = 0;
            int markerColumn = -1;
            int chromColumn = -1;
            int posColumn = -1;
            String headerLine = moreResultsReader.readLine();
            StringTokenizer headerSt = new StringTokenizer(headerLine);

            while (headerSt.hasMoreTokens()){
                String column = new String(headerSt.nextToken());

                if (column.equalsIgnoreCase("SNP")){
                    if (markerColumn != -1){
                        throw new PlinkException("Results file contains more then one SNP column.");
                    }
                    markerColumn = numColumns;
                    numColumns++;
                }else if (column.equalsIgnoreCase("CHR")){
                    chromColumn = numColumns;
                    numColumns++;
                }else if (column.equalsIgnoreCase("POS") || column.equalsIgnoreCase("POSITION")){
                    posColumn = numColumns;
                    numColumns++;
                }else{
                    if(columns.contains(column)){
                        String dupColumn = column + "*";
                        duplicateColumns.add(dupColumn);
                        newColumns.add(dupColumn);
                    }else{
                        newColumns.add(column);
                    }
                    numColumns++;
                }
            }

            if (markerColumn == -1){
                throw new PlinkException("Results file must contain a SNP column.");
            }

            String wgaLine;
            int lineNumber = 0;

            Hashtable resultsHash = new Hashtable(1,1);
            for (int i = 0; i < results.size(); i++){
                String markerID = ((AssociationResult)results.get(i)).getMarker().getMarkerID();
                resultsHash.put(markerID,new Integer(i));
            }

            while((wgaLine = moreResultsReader.readLine())!=null){
                if (wgaLine.length() == 0){
                    //skip blank lines
                    continue;
                }
                int tokenNumber = 0;
                StringTokenizer tokenizer = new StringTokenizer(wgaLine);
                String marker = null;
                Vector values = new Vector();
                while(tokenizer.hasMoreTokens()){
                    if (tokenNumber == markerColumn){
                        marker = new String(tokenizer.nextToken());
                    }else if(tokenNumber == chromColumn || tokenNumber == posColumn){
                        //we don't give a toss for the chromosome or position...
                        tokenizer.nextToken();
                    }else{
                        String value = tokenizer.nextToken();
                        values.add(value);
                    }
                    tokenNumber++;
                }

                if (tokenNumber != numColumns){
                    throw new PlinkException("Inconsistent column number on line " + (lineNumber+1));
                }

                AssociationResult currentResult;

                if (resultsHash.containsKey(marker)){
                    addColumns = true;
                    currentResult = (AssociationResult)results.get(((Integer)resultsHash.get(marker)).intValue());
                    currentResult.addValues(values);
                }else{
                    ignoredMarkers.add(marker);
                }

                lineNumber++;
            }
        }catch(IOException ioe){
            throw new PlinkException("File error.");
        }

        if (addColumns){
            for (int i = 0; i < newColumns.size(); i++){
                columns.add(newColumns.get(i));
            }
        }
        return duplicateColumns;
    }

    public Vector getMarkers(){
        return markers;
    }

    public Vector getResults(){
        return results;
    }

    public Vector getColumnNames(){
        return columns;
    }

}