package edu.mit.wi.haploview;

import javax.swing.table.TableModel;

public class CheckDataTableSorter extends TableSorter {
    static final long serialVersionUID = -1547877018850397541L;

    CheckDataTableSorter(TableModel tm){
        super(tm);
    }

    public int getRating(int row){
        return ((CheckDataPanel.CheckDataTableModel)tableModel).getRating(modelIndex(row));
    }

    public int getDupStatus(int row){
        return ((CheckDataPanel.CheckDataTableModel)tableModel).getDupStatus(modelIndex(row));
    }
}
