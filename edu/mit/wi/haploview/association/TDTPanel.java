package edu.mit.wi.haploview.association;

import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.PedFileException;
import edu.mit.wi.haploview.Constants;
import edu.mit.wi.haploview.Options;
import edu.mit.wi.haploview.Chromosome;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TDTPanel extends JPanel implements Constants, ActionListener {

    public Vector result;
    JTable table;
    Vector tableColumnNames = new Vector();
    //countsorfreqs stores the users current choice for displaying counts or frequencies.
    //values are SHOW_SINGLE_COUNTS or SHOW_SINGLE_FREQS
    //default is counts
    private int countsOrFreqs;

    public TDTPanel(PedFile pf) throws PedFileException{
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        if (Options.getAssocTest() == ASSOC_TRIO){
            result = MarkerAssociationResult.getTDTAssociationResults(pf, null);
        }else{
            result = MarkerAssociationResult.getCCAssociationResults(pf,null);
        }

        tableColumnNames.add("#");
        tableColumnNames.add("Name");
        if (Options.getAssocTest() == ASSOC_TRIO){
            tableColumnNames.add("Overtransmitted");
            tableColumnNames.add("T:U");
        }else{
            tableColumnNames.add("Major Alleles");
            tableColumnNames.add("Case, Control Ratios");
        }
        tableColumnNames.add("Chi Square");
        tableColumnNames.add("p value");

        refreshTable();
    }

    public void refreshNames() {
        for (int i = 0; i < table.getRowCount(); i++){
            table.setValueAt(Chromosome.getUnfilteredMarker(i).getName(),i,1);
        }
    }

	public JTable getTable(){
		return table;
	}

    public void refreshTable(){
        this.removeAll();
        Vector tableData = new Vector();

        int numRes = Chromosome.getSize();
        for (int i = 0; i < numRes; i++){
            Vector tempVect = new Vector();
            MarkerAssociationResult currentResult = (MarkerAssociationResult)result.get(Chromosome.realIndex[i]);
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

        TDTTableModel tm = new TDTTableModel(tableColumnNames, tableData);
        table = new JTable(tm);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        if (Options.getAssocTest() != ASSOC_TRIO){
            table.getColumnModel().getColumn(3).setPreferredWidth(160);
        }
        table.getColumnModel().getColumn(2).setPreferredWidth(100);


        JScrollPane tableScroller = new JScrollPane(table);
        tableScroller.setMaximumSize(tableScroller.getPreferredSize());
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

    class TDTTableModel extends AbstractTableModel {
		Vector columnNames; Vector data;

		public TDTTableModel(Vector c, Vector d){
			columnNames=c;
			data=d;
		}

        public String getColumnName(int i){
            return (String)columnNames.elementAt(i);
        }

        public Class getColumnClass(int c){
            //things look nicer if we use the String renderer to left align all the cols.
            return String.class;
		}

		public int getColumnCount(){
			return columnNames.size();
		}

		public int getRowCount(){
			return data.size();
		}

		public Object getValueAt(int row, int column){
			return ((Vector)data.elementAt(row)).elementAt(column);
		}

	}

}




