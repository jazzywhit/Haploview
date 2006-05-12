package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.*;

import javax.swing.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

public class TDTPanel extends HaploviewTab
        implements Constants, ActionListener {

    private AssociationTestSet assocSet;
    private JTable table;
    private Vector tableColumnNames = new Vector();
    //countsorfreqs stores the users current choice for displaying counts or frequencies.
    //values are SHOW_SINGLE_COUNTS or SHOW_SINGLE_FREQS
    //default is counts
    private int countsOrFreqs;

    public TDTPanel(AssociationTestSet ats){
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        assocSet = ats;

        tableColumnNames.add("#");
        tableColumnNames.add("Name");
        if (Options.getAssocTest() == ASSOC_TRIO){
            tableColumnNames.add("Overtransmitted");
            if(Options.getTdtType() == TDT_STD) {
                tableColumnNames.add("T:U");
            }else if(Options.getTdtType() == TDT_PAREN) {
                tableColumnNames.add("T:U,PA:PU");
            }
        }else{
            tableColumnNames.add("Assoc Allele");
            tableColumnNames.add("Case, Control Ratios");
        }
        tableColumnNames.add("Chi Square");
        tableColumnNames.add("p value");

        refreshTable();
    }

    public void refreshNames() {
        for (int i = 0; i < table.getRowCount(); i++){
            table.setValueAt(Chromosome.getMarker(i).getDisplayName(),i,1);
        }
    }

	public JTable getTable(){
		return table;
	}

    public void refreshTable(){
        this.removeAll();
        Vector tableData = new Vector();
        Iterator itr = assocSet.getMarkerAssociationResults().iterator();
        Hashtable markerResultHash = new Hashtable();
        while (itr.hasNext()){
            MarkerAssociationResult m = (MarkerAssociationResult) itr.next();
            markerResultHash.put(m.getSnp(), m);
        }

        for (int i = 0; i < Chromosome.getSize(); i++){
            Vector tempVect = new Vector();
            SNP currentMarker = Chromosome.getMarker(i);
            MarkerAssociationResult currentResult = (MarkerAssociationResult)markerResultHash.get(currentMarker);
            tempVect.add(new Integer(Chromosome.realIndex[i]+1));
            tempVect.add(currentResult.getName());
            tempVect.add(currentResult.getOverTransmittedAllele());
            if(this.countsOrFreqs == SHOW_SINGLE_FREQS) {
                tempVect.add(currentResult.getFreqString());
            } else if (this.countsOrFreqs == SHOW_SINGLE_COUNTS) {
                tempVect.add(currentResult.getCountString());
            }

            tempVect.add(new Double(currentResult.getChiSquare(0)));
            tempVect.add(currentResult.getPValue(0));

            tableData.add(tempVect.clone());
        }

        TableSorter sorter = new TableSorter(new BasicTableModel(tableColumnNames, tableData));
        table = new JTable(sorter);
        sorter.setTableHeader(table.getTableHeader());

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        if (Options.getAssocTest() != ASSOC_TRIO){
            table.getColumnModel().getColumn(3).setPreferredWidth(160);
        }
        table.getColumnModel().getColumn(2).setPreferredWidth(100);


        JScrollPane tableScroller = new JScrollPane(table);
        //tableScroller.setMaximumSize(tableScroller.getPreferredSize());
        tableScroller.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        add(tableScroller);

         if(Options.getAssocTest() == ASSOC_CC) {
            JRadioButton countsButton = new JRadioButton("Show CC counts");
            JRadioButton ratiosButton = new JRadioButton("Show CC frequencies");

            ButtonGroup bg = new ButtonGroup();

            bg.add(countsButton);
            bg.add(ratiosButton);
            countsButton.addActionListener(this);
            ratiosButton.addActionListener(this);
            JPanel buttPan = new JPanel();
            buttPan.add(countsButton);
            buttPan.add(ratiosButton);
            add(buttPan);
            if(countsOrFreqs == SHOW_SINGLE_FREQS) {
                ratiosButton.setSelected(true);
            }else{
                countsButton.setSelected(true);
            }
        }


    }

    public AssociationTestSet getTestSet() {
        return assocSet;
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("Show CC counts")) {
            this.countsOrFreqs = SHOW_SINGLE_COUNTS;
            this.refreshTable();
        }
        else if (command.equals("Show CC frequencies")) {
            this.countsOrFreqs = SHOW_SINGLE_FREQS;
            this.refreshTable();
        }
    }
}




