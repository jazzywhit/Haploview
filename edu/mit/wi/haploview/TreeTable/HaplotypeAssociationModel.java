package edu.mit.wi.haploview.TreeTable;

import edu.mit.wi.haploview.Constants;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Mark Daly
 * Date: Sep 3, 2004
 * Time: 11:07:30 AM
 * To change this template use File | Settings | File Templates.
 */

public class HaplotypeAssociationModel extends AbstractTreeTableModel
                             implements TreeTableModel, Constants {
    // Names of the columns.
    Vector colNames;
    //this int is 0 to show ratios, 1 to show counts
    private int countsOrRatios;


    public HaplotypeAssociationModel(Vector colNames, HaplotypeAssociationNode han) {
        super(han);
        this.colNames = colNames;
    }

    public Class getColumnClass(int c){
        if (c == 0){
            return TreeTableModel.class;
        }else{
            return String.class;
        }
    }

    public int getColumnCount() {
        return colNames.size();
    }

    public String getColumnName(int column) {
        return (String)colNames.elementAt(column);
    }

    public Object getValueAt(Object node,
                             int column) {
        HaplotypeAssociationNode n = (HaplotypeAssociationNode) node;
        switch (column){
            case 0:
                return n.getName();
            case 1:
                return n.getFreq();
            case 2:
                if(this.countsOrRatios == SHOW_HAP_COUNTS) {
                    return n.getCounts();
                } else if(this.countsOrRatios == SHOW_HAP_RATIOS) {
                    return n.getCCFreqs();
                }
            case 3:
                return n.getChiSq();
            case 4:
                return n.getPVal();
        }
        return null;
    }

    public int getChildCount(Object parent) {
        return ((HaplotypeAssociationNode) parent).children.size();
    }

    public Object getChild(Object parent,
                           int index) {
        return ((HaplotypeAssociationNode) parent).children.elementAt(index);
    }

    public int getCountsOrRatios() {
        return countsOrRatios;
    }

    public void setCountsOrRatios(int countsOrRatios) {
        this.countsOrRatios = countsOrRatios;
    }
}


