package edu.mit.wi.haploview;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.Vector;

import edu.mit.wi.pedfile.MarkerResult;
import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.PedFileException;


public class CheckDataPanel extends JPanel implements TableModelListener{
	JTable table;
	PedFile pedfile;

    boolean changed;
    private static int STATUS_COL = 9;

    public CheckDataPanel(PedFile pf) throws IOException, PedFileException{
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        pedfile = pf;
        Vector result = pedfile.getResults();

        int numResults = result.size();

        Vector tableColumnNames = new Vector();
        tableColumnNames.add("#");
        tableColumnNames.add("Name");
        tableColumnNames.add("ObsHET");
        tableColumnNames.add("PredHET");
        tableColumnNames.add("HWpval");
        tableColumnNames.add("%Geno");
        tableColumnNames.add("FamTrio");
        tableColumnNames.add("MendErr");
        tableColumnNames.add("MAF");
        tableColumnNames.add("Rating");

        Vector tableData = new Vector();
        int[] markerRatings = new int[numResults];
        for (int i = 0; i < numResults; i++){
            Vector tempVect = new Vector();
            MarkerResult currentResult = (MarkerResult)result.get(i);
            tempVect.add(new Integer(i+1));
            tempVect.add(currentResult.getName());
            tempVect.add(new Double(currentResult.getObsHet()));
            tempVect.add(new Double(currentResult.getPredHet()));
            tempVect.add(new Double(currentResult.getHWpvalue()));
            tempVect.add(new Double(currentResult.getGenoPercent()));
            tempVect.add(new Integer(currentResult.getFamTrioNum()));
            tempVect.add(new Integer(currentResult.getMendErrNum()));
            tempVect.add(new Double(currentResult.getMAF()));

            if (currentResult.getRating() > 0){
                tempVect.add(new Boolean(true));
            }else{
                tempVect.add(new Boolean(false));
            }

            //this value is never displayed, just kept for bookkeeping
            markerRatings[i] = currentResult.getRating();

            tableData.add(tempVect.clone());
        }

        final CheckDataTableModel tableModel = new CheckDataTableModel(tableColumnNames, tableData, markerRatings);
        tableModel.addTableModelListener(this);
        table = new JTable(tableModel);
        final CheckDataCellRenderer renderer = new CheckDataCellRenderer();
        try{
            table.setDefaultRenderer(Class.forName("java.lang.Double"), renderer);
            table.setDefaultRenderer(Class.forName("java.lang.Integer"), renderer);
        }catch (Exception e){
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);

        JScrollPane tableScroller = new JScrollPane(table);
        add(tableScroller);
	}

	public PedFile getPedFile(){
		return pedfile;
	}

	public JTable getTable(){
		return table;
	}

    public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == STATUS_COL){
            changed = true;
        }
    }

    public void refreshNames() {
        for (int i = 0; i < table.getRowCount(); i++){
              table.setValueAt(Chromosome.getMarker(i).getName(),i,1);
        }
    }

    public void selectAll(){
        for (int i = 0; i < table.getRowCount(); i++){
            table.setValueAt(new Boolean(true), i, STATUS_COL);
        }
    }

    public void redoRatings(){
        try{
            Vector result = pedfile.check();
            int[] ratings = new int[table.getRowCount()];
            for (int i = 0; i < table.getRowCount(); i++){
                MarkerResult cur = (MarkerResult)result.get(i);
                int rating = cur.getRating();
                if (rating > 0){
                    table.setValueAt(new Boolean(true),i,STATUS_COL);
                }else{
                    table.setValueAt(new Boolean(false),i,STATUS_COL);
                }
                ratings[i] = rating;
            }
            ((CheckDataTableModel)table.getModel()).ratings = ratings;
            table.repaint();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    class CheckDataTableModel extends AbstractTableModel {
		Vector columnNames; Vector data; int[] ratings;

		public CheckDataTableModel(Vector c, Vector d, int[] r){
			columnNames=c;
			data=d;
            ratings = r;
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

		public Class getColumnClass(int c){
			return getValueAt(0, c).getClass();
		}

		public int getRating(int row){
			return ratings[row];
		}

		public String getColumnName(int n){
			return (String)columnNames.elementAt(n);
		}

		public boolean isCellEditable(int row, int col){
			if (getColumnName(col).equals("Rating")){
				return true;
			}else{
				return false;
			}
		}

		public void setValueAt(Object value, int row, int col){
			((Vector)data.elementAt(row)).set(col, value);
			fireTableCellUpdated(row, col);
		}
	}

	class CheckDataCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent
		        (JTable table, Object value, boolean isSelected,
		         boolean hasFocus, int row, int column)
		{
			Component cell = super.getTableCellRendererComponent
			        (table, value, isSelected, hasFocus, row, column);
			int myRating = ((CheckDataTableModel)table.getModel()).getRating(row);
			String thisColumnName = table.getColumnName(column);
            cell.setForeground(Color.black);
            //bitmasking to decode the status bits
            if (myRating < 0){
                myRating *= -1;
                if ((myRating & 1) != 0){
                    if(thisColumnName.equals("ObsHET")){
                        cell.setForeground(Color.red);
                    }
                }
                if ((myRating & 2) != 0){
                    if (thisColumnName.equals("%Geno")){
                        cell.setForeground(Color.red);
                    }
                }
                if ((myRating & 4) != 0){
                    if (thisColumnName.equals("HWpval")){
                        cell.setForeground(Color.red);
                    }
                }
                if ((myRating & 8) != 0){
                    if (thisColumnName.equals("MendErr")){
                        cell.setForeground(Color.red);
                    }
                }
                if ((myRating & 16) != 0){
                    if (thisColumnName.equals("MAF")){
                        cell.setForeground(Color.red);
                    }
                }
            }
			return cell;
		}
	}

}
