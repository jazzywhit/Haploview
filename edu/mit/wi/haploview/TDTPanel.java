package edu.mit.wi.haploview;

import javax.swing.*;
import java.util.Vector;
import java.awt.*;

public class TDTPanel extends JPanel {
    public TDTPanel(Vector chromosomes) {

        JTable table;
        TDT theTDT = new TDT();
        Vector result = theTDT.calcTDT(chromosomes);

        Vector tableColumnNames = new Vector();
        tableColumnNames.add("Name");
        tableColumnNames.add("Chi Squared");
        tableColumnNames.add("T/U Ratio");
        tableColumnNames.add("p value");

        Vector tableData = new Vector();

        int numRes = Chromosome.getFilteredSize();
        for (int i = 0; i < numRes; i++){
            Vector tempVect = new Vector();
            TDTResult currentResult = (TDTResult)result.get(Chromosome.realIndex[i]);
            tempVect.add(currentResult.getName());
            tempVect.add(new Double(currentResult.getChiSq()));
            tempVect.add(currentResult.getTURatio());
            tempVect.add(new Double(currentResult.getPValue()));

            tableData.add(tempVect.clone());
        }

        table = new JTable(tableData,tableColumnNames);
        //table.setPreferredSize(new Dimension(200,200));
        //for(int i=0;i<table.getColumnModel().getColumnCount();i++){
        //    table.getColumnModel().getColumn(i).setPreferredWidth(6);
        //}
        JScrollPane tableScroller = new JScrollPane(table);
        //tableScroller.getViewport().setPreferredSize(new Dimension(200, height));
        add(tableScroller);
    }

}




