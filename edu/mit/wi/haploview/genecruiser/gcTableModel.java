package edu.mit.wi.haploview.genecruiser;

import edu.mit.wi.haploview.Constants;
import javax.swing.table.AbstractTableModel;
import java.util.Vector;

///////////////////////////////////////////
// Created with IntelliJ IDEA.           //
// User: Jesse Whitworth                 //
// Date: Jun 11, 2008                    //
// Time: 10:34:39 PM                     //
///////////////////////////////////////////

public class gcTableModel extends AbstractTableModel implements Constants {

    private String[] columnNames;
    private Object[][] data;

    public gcTableModel(String[] columnNames, Object[][] data){

        if(columnNames != null && data != null){
            this.columnNames = columnNames;
            this.data = data;
        }
    }

    public gcTableModel(Vector columnNames, Vector data){

        columnNames.trimToSize();
        this.columnNames = strVec2Array(columnNames);
        data.trimToSize();
        Object[][] data_array = new Object[data.size()][columnNames.size()];
        String[] curr_row;
        for(int i = 0; i < data.size(); i++){

            curr_row = strVec2Array((Vector)data.get(i));
            for (int j = 0; j < columnNames.size(); j++){

                data_array[i][j] = curr_row[j];
                    
            }
        }
        this.data = data_array;
    }

    public String[] strVec2Array(Vector vector){

        String[] curr_array = new String[vector.size()];
        for (int i = 0; i < vector.size(); i++){
            curr_array[i] = (String)vector.get(i);
        }
       return curr_array;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 2) {
            return false;
        } else {
            return true;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        if (DEBUG) {
            System.out.println("Setting value at " + row + "," + col
                    + " to " + value
                    + " (an instance of "
                    + value.getClass() + ")");
        }

        data[row][col] = value;
        fireTableCellUpdated(row, col);

        if (DEBUG) {
            System.out.println("New value of data:");
            printDebugData();
        }
    }

    private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + data[i][j]);
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
}