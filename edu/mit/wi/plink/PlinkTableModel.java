package edu.mit.wi.plink;


import javax.swing.table.AbstractTableModel;
import java.util.Vector;
import java.util.Comparator;
import java.util.Collections;

public class PlinkTableModel extends AbstractTableModel{

    private Vector columnNames;
    private Vector data;
    private Vector filtered;
    private Vector unknownColumns;

    private int NUM_COLUMN = 0;
    private int CHROM_COLUMN = 1;
    private int MARKER_COLUMN = 2;
    private int POSITION_COLUMN = 3;
    private int CHI_COLUMN = -1;
    private int PVAL_COLUMN = -1;

    public PlinkTableModel(Vector c, Vector d){
        columnNames=c;
        data=d;
        unknownColumns = new Vector();

        for (int j = 4; j < columnNames.size(); j++){
           String column = (String)columnNames.get(j);
            if ((column.equalsIgnoreCase("TDT_CHISQ")) || (column.equalsIgnoreCase("CHISQ"))){
                if (CHI_COLUMN == -1){
                    CHI_COLUMN = j;
                }
            }else if ((column.equalsIgnoreCase("TDT_P")) || (column.equalsIgnoreCase("P"))){
                if (PVAL_COLUMN == -1){
                    PVAL_COLUMN = j;
                }
            }else{
                unknownColumns.add(column);
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

    public Vector getUnknownColumns(){
        return unknownColumns;
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
            value = marker.getMarkerID();
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
            }catch (ArrayIndexOutOfBoundsException a){
                value = null;
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
        if (PVAL_COLUMN != -1){
            resetFilters();
            Collections.sort(data,new PvalComparator());

            Vector newFiltered = new Vector();

            for (int i =0; i < num; i ++){
                newFiltered.add(new Integer(i));
            }

            filtered = newFiltered;
        }
    }

    public void filterGeneric(String column, String s, String value){
        resetFilters();
        double rowVal;
        double val = 0;
        String stringVal = null;
        int col = -1;

        try{
            val = Double.parseDouble(value);
        }catch (NumberFormatException nfe){
            stringVal = value;
        }

        for (int j = 0; j < columnNames.size(); j++){
            if(column.equalsIgnoreCase((String)columnNames.get(j))){
                col = j;
                break;
            }
        }

        int rows = getRowCount();
        Vector newFiltered = new Vector();
        for (int i = 0; i < rows; i++){
            if (getValueAt(i,col) != null){
                if (stringVal != null){
                    if (((String)getValueAt(i,col)).equalsIgnoreCase(stringVal)){
                        newFiltered.add(new Integer(i));
                    }

                }else{
                    try{
                        rowVal = ((Double)getValueAt(i,col)).doubleValue();
                    }catch (ClassCastException cce){
                        rowVal = Double.NaN;
                    }
                    if (s.equals(">=")){
                        if (rowVal >= val){
                            newFiltered.add(new Integer(i));
                        }
                    }else if (s.equals("<=")){
                        if (rowVal <= val){
                            newFiltered.add(new Integer(i));
                        }
                    }else{
                        if (rowVal == val){
                            newFiltered.add(new Integer(i));
                        }
                    }
                }
            }
        }
        filtered = newFiltered;
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

        Collections.sort(data,new IndexComparator());

        for (int i = 0; i < data.size(); i++){
            filtered.add(new Integer(i));
        }
    }

    class PvalComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Double d1 = Double.valueOf((String)((AssociationResult)o1).getValues().get(PVAL_COLUMN-4));
            Double d2 = Double.valueOf((String)((AssociationResult)o2).getValues().get(PVAL_COLUMN-4));
            return d1.compareTo(d2);
        }
    }

    class IndexComparator implements Comparator {
        public int compare (Object o1, Object o2) {
            Integer i1 = new Integer(((AssociationResult)o1).getIndex());
            Integer i2 = new Integer(((AssociationResult)o2).getIndex());
            return i1.compareTo(i2);
        }
    }
}