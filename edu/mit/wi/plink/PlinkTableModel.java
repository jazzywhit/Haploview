package edu.mit.wi.plink;


import edu.mit.wi.haploview.Options;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;
import java.util.Comparator;
import java.util.Collections;

public class PlinkTableModel extends AbstractTableModel{

    private Vector columnNames;
    private Vector data;
    private Vector filtered;
    private Vector unknownColumns;
    private Vector snps;

    private int CHROM_COLUMN = 0;
    private int MARKER_COLUMN = 1;
    private int POSITION_COLUMN = 2;
    private int HAPLOTYPE_COLUMN = -1;
    private int A1_COLUMN = -1;
    private int A2_COLUMN = -1;
    private int FID_COLUMN = -1;
    private int IID_COLUMN = -1;

    public PlinkTableModel(Vector c, Vector d){
        columnNames=c;
        data=d;
        unknownColumns = new Vector();
        unknownColumns.add("");

        if (Options.getSNPBased()){
            snps = new Vector();
            for (int i = 0; i < d.size(); i++){
                snps.add(((AssociationResult)d.get(i)).getMarker().getMarkerID());
            }
            for (int j = 3; j < columnNames.size(); j++){
                String column = (String)columnNames.get(j);

                if (column.equalsIgnoreCase("HAPLOTYPE")){ //TODO: change to starts with?
                    HAPLOTYPE_COLUMN = j;
                }else if (column.equalsIgnoreCase("A1")){
                    A1_COLUMN = j;
                }else if (column.equalsIgnoreCase("A2")){
                    A2_COLUMN = j;
                }
                unknownColumns.add(column);
            }
        }else{ //not snp based
            CHROM_COLUMN = -1;
            MARKER_COLUMN = -1;
            POSITION_COLUMN = -1;
            for (int i = 0; i < columnNames.size(); i++){
                String column = (String)columnNames.get(i);
                if (column.equalsIgnoreCase("FID")){
                    FID_COLUMN = i;
                }else if (column.equalsIgnoreCase("IID")){
                    IID_COLUMN = i;
                }else{
                    unknownColumns.add(column);
                }
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
        Object value;
        if (Options.getSNPBased()){
            Marker marker = result.getMarker();
            if (column == CHROM_COLUMN){
                value = marker.getChromosome();
            }else if (column == MARKER_COLUMN){
                value = marker.getMarkerID();
            }else if (column == POSITION_COLUMN){
                value = new Long(marker.getPosition());
            }else if (column == HAPLOTYPE_COLUMN || column == A1_COLUMN || column == A2_COLUMN){
                if (result.getValues().size() <= column-3){
                    value = null;
                }else{
                    if(result.getValues().get(column-3) != null){
                        if (result.getValues().get(column-3) instanceof String){
                            value = result.getValues().get(column-3);
                        }else{
                            value = String.valueOf(((Double)(result.getValues().get(column-3))).intValue());
                        }
                    }else{
                        value = null;
                    }
                }
            }else{
                try{
                    if (result.getValues().size() <= column-3){
                        value = null;
                    }else{
                        if(result.getValues().get(column-3) != null){
                            if (result.getValues().get(column-3) instanceof String){
                                value = result.getValues().get(column-3);
                            }else{
                                value = result.getValues().get(column-3);
                            }
                        }else{
                            value = null;
                        }
                    }
                }catch (NumberFormatException nfe){
                    value = result.getValues().get(column-3);
                    if ((value).equals("NA")){
                        value = new Double(Double.NaN);
                    }
                }
            }
        }else{
            if (column == IID_COLUMN || column == FID_COLUMN){
                if (result.getValues().get(column) instanceof Double){
                    value = (result.getValues().get(column)).toString();
                }else{
                    value = result.getValues().get(column);
                }
            }else{
                value = result.getValues().get(column);
            }
        }

        return value;
    }

    public void setValueAt(Object o, int row, int column){
        //This method shouldn't get called...
        //((Vector)data.elementAt(row)).set(column,o);
        fireTableCellUpdated(row, column);
    }

    public void filterAll(String chr, int start, int end, String column1, String s1, String value1, String column2, String s2, String value2){
        resetFilters();
        int rows = getRowCount();
        Vector newFiltered = new Vector();
        boolean chromPass = false;
        boolean genericPass1 = false;
        boolean genericPass2 = false;
        long realStart = start*1000;
        long realEnd = end*1000;
        int col1 = -1;
        int col2 = -1;

        for (int i = 0; i < rows; i++){
            if (!(chr.equals(""))){
                if (((String)getValueAt(i,CHROM_COLUMN)).equalsIgnoreCase(chr)){
                    if ((((Long)getValueAt(i,POSITION_COLUMN)).longValue() >= realStart) || (start == -1)){
                        if ((((Long)getValueAt(i,POSITION_COLUMN)).longValue() <= realEnd) || (end == -1)){
                            chromPass = true;
                        }
                    }
                }
            }else{
                chromPass = true;
            }

            if (column1 != null && s1 != null && value1 != null){
                double rowVal1;
                double val1 = 0;
                String stringVal1 = null;

                try{
                    val1 = Double.parseDouble(value1);
                }catch (NumberFormatException nfe){
                    stringVal1 = value1;
                }

                if (col1 == -1){
                    for (int j = 0; j < columnNames.size(); j++){
                        if(column1.equalsIgnoreCase((String)columnNames.get(j))){
                            col1 = j;
                            break;
                        }
                    }
                }

                if (col1 == HAPLOTYPE_COLUMN || col1 == A1_COLUMN || col1 == A2_COLUMN){
                    stringVal1 = value1;
                }

                if (getValueAt(i,col1) != null){
                    if (stringVal1 != null && getValueAt(i,col1) instanceof String){
                        if (((String)getValueAt(i,col1)).equalsIgnoreCase(stringVal1)){
                            genericPass1 = true;
                        }
                    }else{
                        if (getValueAt(i,col1) instanceof Double){
                            rowVal1 = ((Double)getValueAt(i,col1)).doubleValue();
                        }else{
                            rowVal1 = Double.NaN;
                        }
                        if (s1.equals(">=")){
                            if (rowVal1 >= val1){
                                genericPass1 = true;
                            }
                        }else if (s1.equals(">")){
                            if (rowVal1 > val1){
                                genericPass1 = true;
                            }
                        }else if (s1.equals("<=")){
                            if (rowVal1 <= val1){
                                genericPass1 = true;
                            }
                        }else if (s1.equals("<")){
                            if (rowVal1 < val1){
                                genericPass1 = true;
                            }
                        }else{
                            if (rowVal1 == val1){
                                genericPass1 = true;
                            }
                        }
                    }
                }
            }else{
                genericPass1 = true;
            }


            if (column2 != null && s2 != null && value2 != null){
                double rowVal2;
                double val2 = 0;
                String stringVal2 = null;

                try{
                    val2 = Double.parseDouble(value2);
                }catch (NumberFormatException nfe){
                    stringVal2 = value2;
                }

                if (col2 == -1){
                    for (int j = 0; j < columnNames.size(); j++){
                        if(column2.equalsIgnoreCase((String)columnNames.get(j))){
                            col2 = j;
                            break;
                        }
                    }
                }

                if (col2 == HAPLOTYPE_COLUMN || col2 == A1_COLUMN || col2 == A2_COLUMN){
                    stringVal2 = value2;
                }

                if (getValueAt(i,col2) != null){
                    if (stringVal2 != null && getValueAt(i,col2) instanceof String){
                        if (((String)getValueAt(i,col2)).equalsIgnoreCase(stringVal2)){
                            genericPass2 = true;
                        }
                    }else{
                        if (getValueAt(i,col2) instanceof Double){
                            rowVal2 = ((Double)getValueAt(i,col2)).doubleValue();
                        }else{
                            rowVal2 = Double.NaN;
                        }
                        if (s2.equals(">=")){
                            if (rowVal2 >= val2){
                                genericPass2 = true;
                            }
                        }else if (s2.equals(">")){
                            if (rowVal2 > val2){
                                genericPass2 = true;
                            }
                        }else if (s2.equals("<=")){
                            if (rowVal2 <= val2){
                                genericPass2 = true;
                            }
                        }else if (s2.equals("<")){
                            if (rowVal2 < val2){
                                genericPass2 = true;
                            }
                        }else{
                            if (rowVal2 == val2){
                                genericPass2 = true;
                            }
                        }
                    }
                }
            }else{
                genericPass2 = true;
            }


            if (chromPass && genericPass1 && genericPass2){
                newFiltered.add(new Integer(i));
            }

            chromPass = false;
            genericPass1 = false;
            genericPass2 = false;
        }
        filtered = newFiltered;
        if (Options.getSNPBased()){
            snps = new Vector();
            for (int i = 0; i < filtered.size(); i++){
                int realIndex = ((Integer)filtered.get(i)).intValue();
                AssociationResult result = (AssociationResult)data.get(realIndex);
                snps.add(result.getMarker().getMarkerID());
            }
        }
    }

    public void resetFilters(){
        filtered = new Vector();

        Collections.sort(data,new IndexComparator());

        for (int i = 0; i < data.size(); i++){
            filtered.add(new Integer(i));
        }
    }

    public Vector getSNPs(){
        return snps;
    }

    public int getFIDColumn(){
        return FID_COLUMN;
    }

    public int getIIDColumn(){
        return IID_COLUMN;
    }

    class IndexComparator implements Comparator {
        public int compare (Object o1, Object o2) {
            Integer i1 = new Integer(((AssociationResult)o1).getIndex());
            Integer i2 = new Integer(((AssociationResult)o2).getIndex());
            return i1.compareTo(i2);
        }
    }
}