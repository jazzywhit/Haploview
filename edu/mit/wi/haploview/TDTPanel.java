package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.PedFileException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Vector;

public class TDTPanel extends JPanel implements Constants {

    Vector result;
    JTable table;
    Vector tableColumnNames = new Vector();

    public TDTPanel(PedFile pf) throws PedFileException{
        if (Options.getAssocTest() == ASSOC_TRIO){
            result = TDT.calcTrioTDT(pf);
        }else{
            result = TDT.calcCCTDT(pf);
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
        tableColumnNames.add("Chi Squared");
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
            TDTResult currentResult = (TDTResult)result.get(Chromosome.realIndex[i]);
            tempVect.add(new Integer(Chromosome.realIndex[i]+1));
            tempVect.add(currentResult.getName());
            tempVect.add(currentResult.getOverTransmittedAllele(Options.getAssocTest()));
            tempVect.add(currentResult.getTURatio(Options.getAssocTest()));
            tempVect.add(new Double(currentResult.getChiSq(Options.getAssocTest())));
            tempVect.add(currentResult.getPValue());

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
        add(tableScroller);

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
			return getValueAt(0, c).getClass();
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




