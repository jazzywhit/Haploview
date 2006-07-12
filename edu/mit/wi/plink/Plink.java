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

    public void parseTDT(String wga, String map) throws PlinkException {
        markers = new Vector();
        results = new Vector();
        columns = new Vector();
        columns.add("Result");
        columns.add("Chrom");
        columns.add("Marker");
        columns.add("Position");
        columns.add("A1:A2");
        columns.add("TDT_T:U");
        columns.add("TDT_OR");
        columns.add("TDT_CHISQ");
        columns.add("TDT_P");

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
            String wgaLine;

            while((wgaLine = wgaReader.readLine())!=null){
               if (wgaLine.length() == 0){
                    //skip blank lines
                    continue;
               }
                if (wgaLine.startsWith(" CHR")){
                    //skip header
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(wgaLine,"\t :");
                String chrom = tokenizer.nextToken();
                String marker = tokenizer.nextToken();
                Marker assocMarker = (Marker)markerHash.get(marker);
                if (assocMarker == null){
                    throw new PlinkException("Marker " + marker + " does not appear in the map file.");
                }
                if (!(assocMarker.getChromosome().equalsIgnoreCase(chrom))){
                    throw new PlinkException("Incompatible chromsomes.");
                }
                char allele1 = tokenizer.nextToken().charAt(0);
                char allele2 = tokenizer.nextToken().charAt(0);
                int t = Integer.parseInt(tokenizer.nextToken());
                int u = Integer.parseInt(tokenizer.nextToken());
                String oddsString = tokenizer.nextToken();
                double odds;
                if (oddsString.equalsIgnoreCase("NA")){
                    odds = Double.NaN;
                }else{
                    odds = Double.parseDouble(oddsString);
                }
                double chisq = Double.parseDouble(tokenizer.nextToken());
                double pval = Double.parseDouble(tokenizer.nextToken());

                TDTAssociationResult result = new TDTAssociationResult(assocMarker,allele1,allele2,
                        t,u,odds,chisq,pval);
                results.add(result);
            }
        }catch(IOException ioe){
            throw new PlinkException("File error.");
        }catch(NumberFormatException nfe){
            throw new PlinkException("File formatting error.");
        }
        hv.setPlinkData(results,columns);
    }

    public void parseCC(String wga, String map) throws PlinkException {
        markers = new Vector();
        results = new Vector();
        columns = new Vector();
        columns.add("Result");
        columns.add("Chrom");
        columns.add("Marker");
        columns.add("Position");
        columns.add("A1");
        columns.add("F_A");
        columns.add("F_U");
        columns.add("A2");
        columns.add("CHISQ");
        columns.add("P");
        columns.add("OR");

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
            String wgaLine;

            while((wgaLine = wgaReader.readLine())!=null){
                if (wgaLine.length() == 0){
                    //skip blank lines
                    continue;
                }
                if (wgaLine.startsWith(" CHR")){
                    //skip header
                    continue;
                }

                StringTokenizer tokenizer = new StringTokenizer(wgaLine,"\t ");
                String chrom = tokenizer.nextToken();
                String marker = tokenizer.nextToken();
                Marker assocMarker = (Marker)markerHash.get(marker);
                if (assocMarker == null){
                    throw new PlinkException("Marker " + marker + " does not appear in the map file.");
                }
                //TODO: mess with this nonsense.
                if(chrom.equals("23")){
                    chrom = "X";
                }
                if (!(assocMarker.getChromosome().equalsIgnoreCase(chrom))){
                    throw new PlinkException("Incompatible chromsomes.");
                }
                char allele1 = tokenizer.nextToken().charAt(0);
                double freqA = Double.parseDouble(tokenizer.nextToken());
                double freqU = Double.parseDouble(tokenizer.nextToken());
                char allele2 = tokenizer.nextToken().charAt(0);
                double chisq = Double.parseDouble(tokenizer.nextToken());
                double pval = Double.parseDouble(tokenizer.nextToken());
                String oddsString = tokenizer.nextToken();
                double odds;
                if (oddsString.equalsIgnoreCase("NA")){
                    odds = Double.NaN;
                }else{
                    odds = Double.parseDouble(oddsString);
                }

                CCAssociationResult result = new CCAssociationResult(assocMarker,allele1,allele2,
                        freqA,freqU,odds,chisq,pval);
                results.add(result);
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