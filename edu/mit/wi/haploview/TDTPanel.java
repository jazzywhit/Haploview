package edu.mit.wi.haploview;

import javax.swing.*;
import java.util.Vector;
import java.awt.*;

public class TDTPanel extends JPanel {

    Vector result;
    Vector tableColumnNames = new Vector();

    public TDTPanel(Vector chromosomes) {
        result = TDT.calcTDT(chromosomes);

        tableColumnNames.add("Name");
        tableColumnNames.add("T:U");
        tableColumnNames.add("Chi Squared");
        tableColumnNames.add("p value");

        refreshTable();
    }

    public void refreshTable(){
        this.removeAll();
        JTable table;
        Vector tableData = new Vector();

        int numRes = Chromosome.getFilteredSize();
        for (int i = 0; i < numRes; i++){
            Vector tempVect = new Vector();
            TDTResult currentResult = (TDTResult)result.get(Chromosome.realIndex[i]);
            tempVect.add(currentResult.getName());
            tempVect.add(currentResult.getTURatio());
            tempVect.add(new Double(currentResult.getChiSq()));
            tempVect.add(new Double(currentResult.getPValue()));

            tableData.add(tempVect.clone());
        }

        table = new JTable(tableData,tableColumnNames);
        JScrollPane tableScroller = new JScrollPane(table);
        add(tableScroller);

    }
}




