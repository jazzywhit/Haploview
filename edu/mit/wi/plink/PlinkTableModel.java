package edu.mit.wi.plink;


import javax.swing.table.AbstractTableModel;
import java.util.Vector;

public class PlinkTableModel extends AbstractTableModel{

    private Vector columnNames;
    private Vector data;
    private Vector filtered;

    private int CHI_COLUMN;
    private int PVAL_COLUMN;

    public PlinkTableModel(Vector c, Vector d){
        columnNames=c;
        data=d;

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
        if (column == 0){
            value = new Integer(row+1);
        }else if (column == 1){
            value = marker.getChromosome();
        }else if (column == 2){
            value = marker.getMarker();
        }else if (column == 3){
            value = new Long(marker.getPosition());
        }else if (column == 4){
            if (result instanceof CCAssociationResult){
                CHI_COLUMN = 8;
                PVAL_COLUMN = 9;
                value = Character.toString(result.getAllele1());
            }else if (result instanceof TDTAssociationResult){
                CHI_COLUMN = 7;
                PVAL_COLUMN = 8;
                value = result.getAllele1() + ":" + result.getAllele2();
            }
        }else if (column == 5){
            if (result instanceof CCAssociationResult){
                value = new Double(((CCAssociationResult)result).getFrequencyAffected());
            }else if (result instanceof TDTAssociationResult){
                value = ((TDTAssociationResult)result).getTransmitted() + ":" + ((TDTAssociationResult)result).getUntransmitted();
            }
        }else if (column == 6){
            if (result instanceof CCAssociationResult){
                value = new Double(((CCAssociationResult)result).getFrequencyUnaffected());
            }else if (result instanceof TDTAssociationResult){
                value = new Double(result.getOdds());
            }
        }else if (column == 7){
            if (result instanceof CCAssociationResult){
                value = Character.toString(result.getAllele2());
            }else if (result instanceof TDTAssociationResult){
                value = new Double(result.getChisq());
            }
        }else if (column == 8){
            if (result instanceof CCAssociationResult){
                value = new Double(result.getChisq());
            }else if (result instanceof TDTAssociationResult){
                value = new Double(result.getPval());
            }
        }else if (column == 9){
            if (result instanceof CCAssociationResult){
                value = new Double(result.getPval());
            }
        }else if (column == 10){
            if (result instanceof CCAssociationResult){
                value = new Double(result.getOdds());
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

            if (chisq > 0){
                if (((Double)getValueAt(i,CHI_COLUMN)).doubleValue() >= chisq){
                    chiPass = true;
                }
            }else{
                chiPass = true;
            }

            if (pval != -1){
                if (((Double)getValueAt(i,PVAL_COLUMN)).doubleValue() <= pval){
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