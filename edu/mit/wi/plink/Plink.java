package edu.mit.wi.plink;

import edu.mit.wi.haploview.Constants;
import edu.mit.wi.haploview.HaploView;
import edu.mit.wi.haploview.TableSorter;
import edu.mit.wi.haploview.BasicTableModel;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;


public class Plink implements Constants {

    private Vector markers = null;
    private Vector results = null;
    private Vector columns = null;
    private Vector ignoredMarkers;
    private HaploView hv;

    public Plink(HaploView h){
        hv = h;
    }

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
                if (column.equals("SNP")){
                    markerColumn = numColumns;
                    numColumns++;
                }else if (column.equals("CHR")){
                    chromColumn = numColumns;
                    numColumns++;
                }else if (column.equals("POS")){
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

        hv.setPlinkData(results,columns);
    }

    public Vector getIgnoredMarkers() {
        return ignoredMarkers;
    }

    public void parseMoreResults(String wga) throws PlinkException {
        File moreResultsFile = new File(wga);
        results = hv.getPlinkData();
        columns = hv.getPlinkColumns();
        Vector newColumns = new Vector();
        Vector ignoredMarkers = new Vector();
        boolean addColumns = false;


        try{
            if (moreResultsFile.length() < 1){
                throw new PlinkException("plink file is empty or nonexistent.");
            }

            BufferedReader moreResultsReader = new BufferedReader(new FileReader(moreResultsFile));
            int numColumns = 0;
            int markerColumn = -1;
            int chromColumn = -1;
            String headerLine = moreResultsReader.readLine();
            StringTokenizer headerSt = new StringTokenizer(headerLine);

            while (headerSt.hasMoreTokens()){
                String column = new String(headerSt.nextToken());

                if (column.equals("SNP")){
                    if (markerColumn != -1){
                        throw new PlinkException("Results file contains more then one SNP column.");
                    }
                    markerColumn = numColumns;
                    numColumns++;
                }else if (column.equals("CHR")){
                    chromColumn = numColumns;
                    numColumns++;
                }else{
                    if(columns.contains(column)){
                        String dupColumn = column + "*";
                        JOptionPane.showMessageDialog(hv,
                                column + " already appears in the dataset.\n" +
                                        "Duplicates are marked as " + dupColumn,
                                "Duplicate value",
                                JOptionPane.ERROR_MESSAGE);
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
                    }else if(tokenNumber == chromColumn){
                        //we don't give a toss for the chromosome...
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

        if (ignoredMarkers.size() != 0){
            IgnoredMarkersDialog imd = new IgnoredMarkersDialog(hv,"Ignored Markers",ignoredMarkers,true);
            imd.pack();
            imd.setVisible(true);
        }


        if (addColumns){
            for (int i = 0; i < newColumns.size(); i++){
                columns.add(newColumns.get(i));
            }
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

    class IgnoredMarkersDialog extends JDialog implements ActionListener {

        public IgnoredMarkersDialog (HaploView h, String title, Vector ignored, boolean extra){
            super(h,title);

            JPanel contents = new JPanel();
            contents.setPreferredSize(new Dimension(200,200));
            contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
            JTable table;

            Vector colNames = new Vector();
            colNames.add("#");
            colNames.add("SNP");

            Vector data = new Vector();

            for (int i = 0; i < ignored.size(); i++){
                Vector tmpVec = new Vector();
                tmpVec.add(new Integer(i+1));
                tmpVec.add((String)ignored.get(i));
                data.add(tmpVec);
            }

            TableSorter sorter = new TableSorter(new BasicTableModel(colNames, data));
            table = new JTable(sorter);
            sorter.setTableHeader(table.getTableHeader());
            table.getColumnModel().getColumn(0).setPreferredWidth(30);
            table.getColumnModel().getColumn(1).setPreferredWidth(75);

            JScrollPane tableScroller = new JScrollPane(table);
            tableScroller.setPreferredSize(new Dimension(75,300));

            JLabel label;
            if (extra){
                label = new JLabel("<HTML><b>The following markers do not appear in the " +
                        "loaded dataset and will therefore be ignored.</b>");
            }else{
                label = new JLabel("<HTML><b>The following markers do not appear in the " +
                        "loaded mapfile and will therefore be ignored.</b>");
            }
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            contents.add(label);
            tableScroller.setAlignmentX(Component.CENTER_ALIGNMENT);
            contents.add(tableScroller);
            JButton okButton = new JButton("Close");
            okButton.addActionListener(this);
            okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            contents.add(okButton);
            setContentPane(contents);

            this.setLocation(this.getParent().getX() + 100,
                    this.getParent().getY() + 100);
            this.setModal(true);
            //this.setResizable(false);
        }

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals("Close")) {
                this.dispose();
            }
        }
    }

}