package edu.mit.wi.plink;


import javax.swing.table.AbstractTableModel;
import java.util.Vector;

public class PlinkTableModel extends AbstractTableModel{

    private Vector columnNames;
    private Vector data;
    private Vector filtered;

    private int NUM_COLUMN = 0;
    private int CHROM_COLUMN = 1;
    private int MARKER_COLUMN = 2;
    private int POSITION_COLUMN = 3;
    private int CHI_COLUMN = -1;
    private int PVAL_COLUMN = -1;

    public PlinkTableModel(Vector c, Vector d){
        columnNames=c;
        data=d;

        for (int j = 0; j < columnNames.size(); j++){
           String column = (String)columnNames.get(j);
           if ((column.equals("TDT_CHISQ")) || (column.equals("CHISQ"))){
               CHI_COLUMN = j;
           }else if ((column.equals("TDT_P")) || (column.equals("P"))){
               PVAL_COLUMN = j;
           }
        }

        filtered = new Vector();

        for (int i = 0; i < data.size(); i++){
            filtered.add(new Integer(i));
        }
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
        //return data.size();
        return filtered.size();
    }

    public Object getValueAt(int row, int column){
        int realIndex = ((Integer)filtered.get(row)).intValue();
        //AssociationResult result = (AssociationResult)data.get(row);
        AssociationResult result = (AssociationResult)data.get(realIndex);
        Marker marker = result.getMarker();
        Object value = null;
        if (column == NUM_COLUMN){
            value = new Integer(row+1);
        }else if (column == CHROM_COLUMN){
            value = marker.getChromosome();
        }else if (column == MARKER_COLUMN){
            value = marker.getMarker();
        }else if (column == POSITION_COLUMN){
            value = new Long(marker.getPosition());
        }else{
            try{
                value = new Double((String)result.getValues().get(column-4));
            }catch (NumberFormatException nfe){
                value = result.getValues().get(column-4);
                if (((String)value).equals("NA")){
                    value = new Double(Double.NaN);
                }
            }
        }
        return (value);
    }

    public void setValueAt(Object o, int row, int column){
        //This method shouldn't get called...
        //((Vector)data.elementAt(row)).set(column,o);
        fireTableCellUpdated(row, column);
    }

    public void filterTop(int num){

    }

    public void filterAll(String chr, long start, long end, double chisq, double pval){
        resetFilters();
        int rows = getRowCount();
        Vector newFiltered = new Vector();
        boolean chromPass = false;
        boolean chiPass = false;
        boolean pvalPass = false;
        for (int i = 0; i < rows; i++){
            if (!(chr.equals(""))){
                if (((String)getValueAt(i,1)).equalsIgnoreCase(chr)){
                    if ((((Long)getValueAt(i,3)).longValue() >= start) || (start == -1)){
                        if ((((Long)getValueAt(i,3)).longValue() <= end) || (end == -1)){
                            chromPass = true;
                        }
                    }
                }
            }else{
                chromPass = true;
            }

            if (CHI_COLUMN != -1){
                if (chisq > 0){
                    double chi = ((Double)getValueAt(i,CHI_COLUMN)).doubleValue();
                    if (chi >= chisq){
                        chiPass = true;
                    }
                }else{
                    chiPass = true;
                }
            }else{
                chiPass = true;
            }

            if (PVAL_COLUMN != -1){
                if (pval != -1){
                    double p = ((Double)getValueAt(i,PVAL_COLUMN)).doubleValue();
                    if (p <= pval){
                        pvalPass = true;
                    }
                }else{
                    pvalPass = true;
                }
            }else{
                pvalPass = true;
            }

            if (chromPass && chiPass && pvalPass){
                newFiltered.add(new Integer(i));
            }

            chromPass = false;
            chiPass = false;
            pvalPass = false;
        }
        filtered = newFiltered;
    }

    public void resetFilters(){
        filtered = new Vector();

        for (int i = 0; i < data.size(); i++){
            filtered.add(new Integer(i));
        }
    }

}