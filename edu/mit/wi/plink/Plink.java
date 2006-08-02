package edu.mit.wi.plink;

import edu.mit.wi.haploview.Constants;
import edu.mit.wi.haploview.HaploView;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Hashtable;


public class Plink implements Constants {

    private Vector markers = null;
    private Vector results = null;
    private Vector columns = null;
    private HaploView hv;

    public Plink(HaploView h){
    hv = h;
    }

    public void parseWGA(String wga, String map) throws PlinkException {
        markers = new Vector();
        results = new Vector();
        columns = new Vector();
        columns.add("Result");
        columns.add("Chrom");
        columns.add("Marker");
        columns.add("Position");

        final File wgaFile = new File(wga);
        final File mapFile = new File(map);
        Hashtable markerHash = new Hashtable(1,1);

        try{
            if (wgaFile.length() < 1){
                throw new PlinkException("plink file is empty or nonexistent.");
            }
            if (mapFile.length() < 1){
                throw new PlinkException("Map file is empty or nonexistent.");
            }

            BufferedReader mapReader = new BufferedReader(new FileReader(mapFile));
            String mapLine;
            String unknownChrom = "0";

            while((mapLine = mapReader.readLine())!=null) {
                if (mapLine.length() == 0){
                    //skip blank lines
                    continue;
                }

                StringTokenizer st = new StringTokenizer(mapLine,"\t ");

                String chrom = st.nextToken();
                String chr;
                if (chrom.equals("0")){
                    chr = unknownChrom;
                }else if (chrom.equalsIgnoreCase("x")){
                    chr = CHROM_NAMES[22];
                }
                else{
                    chr = CHROM_NAMES[Integer.parseInt(chrom)-1];
                }
                String marker = new String(st.nextToken());
                long mDistance = Long.parseLong(st.nextToken());
                long position = Long.parseLong(st.nextToken());

                Marker mark = new Marker(chr, marker, mDistance, position);
                markers.add(mark);
                markerHash.put(mark.getMarker(), mark);
            }

            BufferedReader wgaReader = new BufferedReader(new FileReader(wgaFile));
            boolean snpColumn = false;
            int numColumns = 0;
            int markerColumn = -1;
            int chromColumn = -1;
            String headerLine = wgaReader.readLine();
            StringTokenizer headerSt = new StringTokenizer(headerLine);
            while (headerSt.hasMoreTokens()){
                String column = new String(headerSt.nextToken());
                if (column.equals("SNP")){
                    snpColumn = true;
                    markerColumn = numColumns;
                    numColumns++;
                }else if (column.equals("CHR")){
                    chromColumn = numColumns;
                    numColumns++;
                }else{
                    columns.add(column);
                    numColumns++;
                }
            }

            if (!snpColumn){
                throw new PlinkException("Results file must contain a SNP column.");
            }

            String wgaLine;
            int lineNumber = 0;

            while((wgaLine = wgaReader.readLine())!=null){
               if (wgaLine.length() == 0){
                    //skip blank lines
                    continue;
               }
                int tokenNumber = 0;
                //StringTokenizer tokenizer = new StringTokenizer(wgaLine,"\t :");
                StringTokenizer tokenizer = new StringTokenizer(wgaLine);
                String marker = null;
                String chrom = null;
                Vector values = new Vector();
                while(tokenizer.hasMoreTokens()){
                    if (tokenNumber == markerColumn){
                        marker = new String(tokenizer.nextToken());
                    }else if(tokenNumber == chromColumn){
                        chrom = new String(tokenizer.nextToken());
                        //TODO: mess with this nonsense.
                        if(chrom.equals("23")){
                            chrom = "X";
                        }
                    }
                    else{
                        values.add(new String(tokenizer.nextToken()));
                    }
                    tokenNumber++;
                }

                if (tokenNumber != numColumns){
                    throw new PlinkException("Inconsistent column number on line " + (lineNumber+1));
                }

                Marker assocMarker = (Marker)markerHash.get(marker);

                if (assocMarker == null){
                    throw new PlinkException("Marker " + marker + " does not appear in the map file.");
                }else if (!(assocMarker.getChromosome().equalsIgnoreCase(chrom))){
                    throw new PlinkException("Incompatible chromsomes.");
                }

                AssociationResult result = new AssociationResult(assocMarker,values);
                results.add(result);
                lineNumber++;
            }
        }catch(IOException ioe){
            throw new PlinkException("File error.");
        }catch(NumberFormatException nfe){
            throw new PlinkException("File formatting error.");
        }
        hv.setPlinkData(results,columns);
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