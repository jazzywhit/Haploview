package edu.mit.wi.haploview;

import edu.mit.wi.haploview.genecruiser.GeneCruiser;
import edu.mit.wi.haploview.genecruiser.gcTableModel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.*;


public class RegionDialog extends JDialog implements ActionListener, Constants {
    static final long serialVersionUID = 8970225298794816733L;

    HaploView hv;
    JComboBox panelChooser, releaseChooser, hmpVersionChooser, chromChooser;
    JCheckBox gBrowse, annotate, searchSnps, searchGenes;
    NumberTextField rangeInput, startPos, endPos, gcrangeInput;
    String marker, chrom;
    long markerPosition,currStart,currEnd;
    int chromNum, openChrom;
    PlinkResultsPanel prp;
    GeneCruiser gncr;
    JPanel geneCruiserPanel;
    JTabbedPane resultsTab;
    Vector activeTables = new Vector();
    String gcRequest;

    public RegionDialog (HaploView hv, String chrom, String marker, PlinkResultsPanel prp, long markerPosition, String title) throws HaploViewException {
        super(hv,title);

        this.hv = hv;
        this.markerPosition = markerPosition;
        this.marker = marker;
        this.prp = prp;
        this.chrom = chrom;

        try{
            chromNum = Integer.parseInt(chrom);

        }catch(NumberFormatException nfe){
            if (chrom.equalsIgnoreCase("x")){
                chromNum = 22;
            }else if(chrom.equalsIgnoreCase("y")){
                chromNum = 23;
            }else{
                throw new HaploViewException("Error with Chromosome");
            }
        }
        openChrom = chromNum;

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
        GridBagConstraints contents_grid = new GridBagConstraints();
        contents_grid.anchor = GridBagConstraints.CENTER;

        ////////////////////////
        contents_grid.gridy = 0;
        contents_grid.gridx = 0;
        ////////////////////////
        JPanel chooserPanel = new JPanel();
        GridBagConstraints chooserPanel_grid = new GridBagConstraints();

        //CHOOSER PANEL
        chooserPanel.add(new JLabel("Hapmap Version"), chooserPanel_grid);
        hmpVersionChooser = new JComboBox(VERSION_HAPMAP);
        hmpVersionChooser.setSelectedIndex(0);
        chooserPanel.add(hmpVersionChooser, chooserPanel_grid);
        chooserPanel_grid.gridx++;
        chooserPanel.add(new JLabel("Release:"), chooserPanel_grid);
        releaseChooser = new JComboBox(RELEASE_NAMES);
        releaseChooser.setSelectedIndex(1);
        chooserPanel.add(releaseChooser, chooserPanel_grid);
        chooserPanel_grid.gridx++;
        chooserPanel.add(new JLabel("Panel:"), chooserPanel_grid);
        panelChooser = new JComboBox(PANEL_NAMES);
        panelChooser.setSelectedIndex(0);
        chooserPanel.add(panelChooser, chooserPanel_grid);
        chooserPanel.add(new JLabel("Chr:"), chooserPanel_grid);
        chromChooser = new JComboBox(CHROM_NAMES);
        chromChooser.setSelectedIndex(chromNum);
        chooserPanel.add(chromChooser, chooserPanel_grid);
        contents.add(chooserPanel, contents_grid);

        ////////////////////////
        contents_grid.gridy++;
        contents_grid.gridx = 0;
        ////////////////////////
        JPanel TopButtonPanel = new JPanel();
        GridBagConstraints TopButtonPanel_grid = new GridBagConstraints();

        TopButtonPanel.add(new JLabel("Start"), chooserPanel_grid);
        startPos = new NumberTextField(Long.toString(this.markerPosition / 1000), 6, false, false);
        currStart = markerPosition;
        TopButtonPanel.add(startPos, chooserPanel_grid);
        TopButtonPanel_grid.gridx++;
        endPos = new NumberTextField(Long.toString(this.markerPosition / 1000), 6, false, false);
        currEnd = markerPosition;
        TopButtonPanel.add(new JLabel("End"), chooserPanel_grid);
        TopButtonPanel.add(endPos);
        TopButtonPanel_grid.gridx++;
        TopButtonPanel.add(new JLabel("+/-"), chooserPanel_grid);
        TopButtonPanel_grid.gridx++;
        rangeInput = new NumberTextField("100",6,false,false);
        TopButtonPanel.add(rangeInput, chooserPanel_grid);
        TopButtonPanel.add(new JLabel("kb"), chooserPanel_grid);
        TopButtonPanel_grid.gridx++;
        annotate = new JCheckBox("Annotate LD");
        annotate.setSelected(true);
        TopButtonPanel.add(annotate, chooserPanel_grid);
        TopButtonPanel_grid.gridx++;
        gBrowse = new JCheckBox("HapMap Info Track");
        gBrowse.setSelected(true);
        TopButtonPanel.add(gBrowse, chooserPanel_grid);
        contents.add(TopButtonPanel, contents_grid);

        ////////////////////////
        contents_grid.gridy++;
        contents_grid.gridx = 0;
        ////////////////////////
        JPanel BottomButtonPanel = new JPanel();
        GridBagConstraints BottonButtonPanel_grid = new GridBagConstraints();

        JButton goButton = new JButton("Go to Region");
        goButton.addActionListener(this);
        this.getRootPane().setDefaultButton(goButton);
        BottomButtonPanel.add(goButton, BottonButtonPanel_grid);
        BottonButtonPanel_grid.gridx++;
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        BottomButtonPanel.add(cancelButton, BottonButtonPanel_grid);
        BottonButtonPanel_grid.gridx++;
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        BottomButtonPanel.add(resetButton, BottonButtonPanel_grid);
        contents.add(BottomButtonPanel, contents_grid);

        ////////////////////////
        contents_grid.gridy++;
        contents_grid.gridx = 0;
        ////////////////////////
        //RESULTS PANEL
        resultsTab = new JTabbedPane(JTabbedPane.TOP);
        resultsTab.setPreferredSize(new Dimension(400, 250));
        resultsTab.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray),
                "Search Results"));
        contents.add(resultsTab);

        ////////////////////////
        contents_grid.gridy++;
        contents_grid.gridx = 0;
        ////////////////////////
        //GENECRUISER SEARCH PANEL
        geneCruiserPanel = new JPanel();
        GridBagConstraints geneCruiserPanel_grid = new GridBagConstraints();

        searchSnps = new JCheckBox ("Search for SNPs");
        geneCruiserPanel.add(searchSnps,geneCruiserPanel_grid);
        geneCruiserPanel_grid.gridx++;

        searchSnps.setSelected(true);

        searchGenes = new JCheckBox ("Search for Genes");
        geneCruiserPanel.add(searchGenes,geneCruiserPanel_grid);
        geneCruiserPanel_grid.gridx++;

        searchGenes.setSelected(true);

        geneCruiserPanel.add(new JLabel("+/-"), chooserPanel_grid);
        geneCruiserPanel_grid.gridx++;
        gcrangeInput = new NumberTextField("100",6,false,false);
        geneCruiserPanel.add(gcrangeInput, geneCruiserPanel_grid);
        geneCruiserPanel.add(new JLabel("kb"), geneCruiserPanel_grid);
        geneCruiserPanel_grid.gridx++;
        JButton geneCruiseButton = new JButton("Find Neighbors");
        geneCruiseButton.setActionCommand("GeneCruise");
        geneCruiseButton.addActionListener(this);
        geneCruiserPanel.add(geneCruiseButton, geneCruiserPanel_grid);
        geneCruiserPanel_grid.gridx++;
        JButton setActive = new JButton("Set Active Location");
        setActive.setActionCommand("setActive");
        setActive.addActionListener(this);
        geneCruiserPanel.add(setActive, geneCruiserPanel_grid);
        contents.add(geneCruiserPanel, contents_grid);

        ////////////////////////
        contents_grid.gridy++;
        contents_grid.gridx = 0;
        ////////////////////////
        contents.add(new JLabel("*Phased HapMap downloads require an active internet connection"),geneCruiserPanel_grid);

        //FINALIZE
        setContentPane(contents);
        this.getRootPane().setDefaultButton(goButton);
        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);

    }

    public JTable makeGeneTable(GeneCruiser gncr) throws HaploViewException{

        Vector columnNames = new Vector();
        columnNames.addElement("GeneId");
        columnNames.addElement("Source");
        columnNames.addElement("Start");
        columnNames.addElement("End");
        columnNames.addElement("Description");

        Vector rowData;
        Vector rows = new Vector();
        int width = 0;
        FontMetrics metrics = getFontMetrics(this.getFont());

        if(gncr.size() > 0){
            for (int i = 0;i < gncr.size(); i++){
                rowData = new Vector();
                rowData.addElement(gncr.getGene(i).GeneId);
                rowData.addElement(gncr.getGene(i).Source);
                rowData.addElement(String.valueOf((int)gncr.getGene(i).Start));
                rowData.addElement(String.valueOf((int)gncr.getGene(i).End));
                rowData.addElement(gncr.getGene(i).Description);
                if(metrics.stringWidth(gncr.getGene(i).Description) > width)
                    width = metrics.stringWidth(gncr.getGene(i).Description);
                rows.addElement(rowData);
            }
        }else{
            throw new HaploViewException("No Gene Data was found in that region");
        }

        TableSorter sorter = new TableSorter(new gcTableModel(columnNames, rows));
        JTable geneTable = new JTable(sorter);
        sorter.setTableHeader(geneTable.getTableHeader());
        geneTable.setPreferredScrollableViewportSize(new Dimension(500, 70));

        //Set up tool tips for column headers.
        geneTable.getTableHeader().setToolTipText(
                "Click to specify sorting; Control-Click to specify secondary sorting");

        geneTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setPreferredColumnWidth(4, width, geneTable);
        return geneTable;
    }

    public JTable makeSnpTable(GeneCruiser gncr) throws HaploViewException{

        Vector columnNames = new Vector();
        columnNames.addElement("SNP");
        columnNames.addElement("Position");
        columnNames.addElement("Alleles");
        columnNames.addElement("Strand");                
        columnNames.addElement("Consequence");

        Vector rowData;
        Vector rows = new Vector();

        if(gncr.size() > 0){
            for (int i = 0;i < gncr.size(); i++){
                rowData = new Vector();
                rowData.addElement(gncr.getSNP(i).getVariationName());
                rowData.addElement(String.valueOf((int)gncr.getSNP(i).getStart()));
                rowData.addElement(gncr.getSNP(i).getAllele());
                rowData.addElement(gncr.getSNP(i).getStrand());
                rowData.addElement(gncr.getSNP(i).getConsequenceType());
                rows.addElement(rowData);
            }
        }else{
            throw new HaploViewException("No SNP Data was found in that region");
        }

        TableSorter sorter = new TableSorter(new gcTableModel(columnNames, rows));
        JTable snpTable = new JTable(sorter);
        sorter.setTableHeader(snpTable.getTableHeader());
        snpTable.setPreferredScrollableViewportSize(new Dimension(500, 70));

        //Set up tool tips for column headers.
        snpTable.getTableHeader().setToolTipText(
                "Click to specify sorting; Control-Click to specify secondary sorting");

        return snpTable;
    }
    
    public void setPreferredColumnWidth(int i, int size, JTable table)
    {
        if (i < table.getColumnModel().getColumnCount()){
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(size);
        }
    }

    private String chromInt2Str(int chromNum){
        if(chromNum == 22){
            return "X";
        }else if(chromNum == 23){
            return "Y";
        }else{
            return Integer.toString(chromNum);
        }
    }

    private void deleteTab(int index){

        resultsTab.remove(index);
        activeTables.remove(index);

    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("Cancel")) {
            this.dispose();
        }
        if(command.equals("GeneCruise")){
            try{

                long start_pos = Long.parseLong(startPos.getText()) * 1000;
                long range = Long.parseLong(gcrangeInput.getText()) * 1000;
                long end_pos = Long.parseLong(endPos.getText()) * 1000;

                if(searchSnps.isSelected()){
                    if ((start_pos - range) > 0){
                        gcRequest = chrom + ":" + String.valueOf(start_pos - range)
                                + "-" + String.valueOf(end_pos + range);
                    }else{
                        gcRequest = chrom + ":0-" + String.valueOf(end_pos + range);
                    }

                    String tabName = gcRequest;

                    gncr = new GeneCruiser(3,gcRequest);
                    JTable table = makeSnpTable(gncr);
                    resultsTab.add(tabName, new JScrollPane(table));
                    activeTables.add(resultsTab.getTabCount()-1, table);
                }

                if(searchGenes.isSelected()){

                    if ((start_pos - range) > 0){
                        gcRequest = chrom + ":" + String.valueOf(start_pos - range)
                                + "-" + String.valueOf(end_pos + range);
                    }else{
                        gcRequest = chrom + ":0-" + String.valueOf(end_pos + range);
                    }

                    String tabName = gcRequest;
                    gncr = new GeneCruiser(3,gcRequest);
                    String best_snp = "";
                    long average = (start_pos + end_pos)/2;
                    long curr_dist;
                    long best_snp_loc = 0;
                    long least_dist = average;
                    for (int i = 0; i < gncr.size(); i++){

                        curr_dist = ((long)(gncr.getSNP(i).getStart())) - average;

                        if (Math.abs(curr_dist) < least_dist){

                            least_dist  = Math.abs(curr_dist);
                            best_snp = gncr.getSNP(i).getVariationName();
                            best_snp_loc = (long)gncr.getSNP(i).getStart();
                        }
                    }

                    gcRequest = best_snp + "&fivePrimeSize=" + (Long.parseLong(gcrangeInput.getText())*1000 + Math.abs((start_pos - best_snp_loc))) + "&threePrimeSize=" + (Long.parseLong(gcrangeInput.getText())*(long)1000 + Math.abs((end_pos - best_snp_loc)));

                    gncr = new GeneCruiser(4,gcRequest);
                    JTable table = makeGeneTable(gncr);
                    JScrollPane pane = new JScrollPane(table);
                    tabName = "Genes - " + tabName;
                    resultsTab.add(tabName, pane);
                    activeTables.add(resultsTab.getTabCount()-1, table);
                }
                this.repaint();
            }catch(HaploViewException hve){
                JOptionPane.showMessageDialog(this,
                        hve.getMessage(),
                        "Connection Problem",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        if(command.equals("Reset")){

            startPos.setText(Long.toString(markerPosition / 1000));
            endPos.setText(Long.toString(markerPosition / 1000));
            chromChooser.setSelectedIndex(openChrom);

            while(resultsTab.getTabCount() > 0){
                deleteTab(0);
            }

        }
        if(command.equals("setActive")){

            if(activeTables.size() >= resultsTab.getSelectedIndex()){

                JTable tempTable = (JTable)activeTables.get(resultsTab.getSelectedIndex());
                String curr_tab_name = resultsTab.getTitleAt(resultsTab.getSelectedIndex());

                if(curr_tab_name.startsWith("Gene")){

                    startPos.setText(String.valueOf(Long.parseLong((String)tempTable.getValueAt(tempTable.getSelectedRow(),2))/1000));
                    endPos.setText(String.valueOf(Long.parseLong((String)tempTable.getValueAt(tempTable.getSelectedRow(),3))/1000));
                    
                }else{
                    startPos.setText(String.valueOf(Long.parseLong((String)tempTable.getValueAt(tempTable.getSelectedRow(),1))/1000));
                    endPos.setText(String.valueOf(Long.parseLong((String)tempTable.getValueAt(tempTable.getSelectedRow(),1))/1000));
                }
            }
        }
        if (command.equals("Go to Region")){

            if(hmpVersionChooser.getSelectedIndex() == 1){
                 JOptionPane.showMessageDialog(this,
                        "HapMap3 is not ready yet.",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(rangeInput.getText().equals("")){
                JOptionPane.showMessageDialog(this,
                        "Please enter a range",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            String panel = (String) panelChooser.getSelectedItem();
            int range = Integer.parseInt(rangeInput.getText());
            long start = (Long.parseLong(startPos.getText()))-range;
            if (start < 0){
                start = 0;
            }
            long end = (Long.parseLong(endPos.getText())) + range;
            String gotoStart = Long.toString(start);
            String gotoEnd = Long.toString(end);
            String phase = (String)releaseChooser.getSelectedItem();
            prp.setChosenMarker(marker);

            if (gBrowse.isSelected()){
                Options.setShowGBrowse(true);
            }

            String[] returnStrings;
            returnStrings = new String[]{"Chr" + chromInt2Str(chromNum) + ":" + panel + ":" + gotoStart + ".." +
                    gotoEnd, panel, gotoStart, gotoEnd, chromInt2Str(chromNum), phase, "txt"};
            this.dispose();
            hv.readGenotypes(returnStrings, HMPDL_FILE);
            Vector chipSNPs = new Vector(prp.getSNPs());
            if (Chromosome.getUnfilteredSize() > 0){
                if (annotate.isSelected()){
                    for (int i = 0; i < Chromosome.getSize(); i++){
                        if (chipSNPs.contains(Chromosome.getMarker(i).getName())){
                            Vector extras = new Vector();
                            for (int j = 1; j < prp.getOriginalColumns().size(); j++){
                                extras.add(prp.getOriginalColumns().get(j) + ": " + String.valueOf(prp.getValueAt(chipSNPs.indexOf(Chromosome.getMarker(i).getName()),j+2)));
                            }
                            Chromosome.getMarker(i).setExtra(extras);
                        }
                    }
                }else{
                    for (int i = 0; i < Chromosome.getSize(); i++){
                        if (chipSNPs.contains(Chromosome.getMarker(i).getName())){
                            Vector plink = new Vector();
                            plink.add("PLINK");
                            Chromosome.getMarker(i).setExtra(plink);
                        }
                    }
                }
            }
        }
    }
}

