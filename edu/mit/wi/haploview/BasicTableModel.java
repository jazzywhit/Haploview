package edu.mit.wi.haploview;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

public class BasicTableModel extends AbstractTableModel{
    static final long serialVersionUID = -5805808329328199L;

    Vector columnNames; Vector data;

    public BasicTableModel(Vector c, Vector d){
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

    public void setValueAt(Object o, int row, int column){
        ((Vector)data.elementAt(row)).set(column,o);
        fireTableCellUpdated(row, column);
    }

}
