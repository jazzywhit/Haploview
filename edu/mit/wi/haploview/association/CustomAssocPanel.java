package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.Options;
import edu.mit.wi.haploview.Constants;
import edu.mit.wi.haploview.BasicTableModel;

import javax.swing.*;
import java.util.Vector;
import java.util.Iterator;
import java.awt.*;

public class CustomAssocPanel extends JPanel implements Constants{

    AssociationTestSet testSet;

    public CustomAssocPanel(AssociationTestSet ats){
        testSet = ats;

        Vector colNames = new Vector();
        colNames.add("Test");
        colNames.add("Allele");
        colNames.add("Frequency");
        if (Options.getAssocTest() == ASSOC_TRIO){
            colNames.add("T:U");
        }else{
            colNames.add("Case, Control Ratios");
        }
        colNames.add("Chi Square");
        colNames.add("p value");

        Vector data = new Vector();
        Vector results = ats.getResults();
        Iterator vitr = results.iterator();
        while (vitr.hasNext()){
            AssociationResult ar = (AssociationResult) vitr.next();
            for (int i = 0; i < ar.getAlleleCount(); i++){
                Vector fields = new Vector();
                fields.add(ar.getName());
                if (ar instanceof MarkerAssociationResult){
                    fields.add(((MarkerAssociationResult)ar).getOverTransmittedAllele());
                    fields.add("");
                }else{
                    fields.add(ar.getAlleleName(i));
                    fields.add(ar.getFreq(i));
                }
                fields.add(ar.getCountString(i));
                fields.add(String.valueOf(ar.getChiSquare(i)));
                fields.add(ar.getPValue(i));
                data.add(fields);
                if (ar instanceof MarkerAssociationResult){
                    //only show one line for SNPs instead of one line per allele
                    break;
                }
            }
        }

        BasicTableModel btm = new BasicTableModel(colNames, data);
        JTable jt = new JTable(btm);
        jt.getColumnModel().getColumn(0).setPreferredWidth(100);
        jt.getColumnModel().getColumn(1).setPreferredWidth(100);
        jt.getColumnModel().getColumn(2).setPreferredWidth(50);

        //we need more space for the CC counts in the third column
        if(Options.getAssocTest() == ASSOC_CC) {
            jt.getColumnModel().getColumn(3).setPreferredWidth(200);
            jt.getColumnModel().getColumn(4).setPreferredWidth(75);
            jt.getColumnModel().getColumn(5).setPreferredWidth(75);
        } else {
            jt.getColumnModel().getColumn(3).setPreferredWidth(150);
            jt.getColumnModel().getColumn(4).setPreferredWidth(100);
            jt.getColumnModel().getColumn(5).setPreferredWidth(100);
        }
        jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jt.setPreferredScrollableViewportSize(new Dimension(600,jt.getPreferredScrollableViewportSize().height));


        JScrollPane tableScroller = new JScrollPane(jt);
        add(tableScroller);
    }

    public AssociationTestSet getTestSet() {
        return testSet;
    }
}