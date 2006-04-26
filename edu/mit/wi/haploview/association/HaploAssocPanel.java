package edu.mit.wi.haploview.association;

import java.util.Vector;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import edu.mit.wi.haploview.TreeTable.*;
import edu.mit.wi.haploview.Constants;
import edu.mit.wi.haploview.Options;
import edu.mit.wi.haploview.HaploviewTab;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;


public class HaploAssocPanel extends HaploviewTab implements Constants,ActionListener {
    public double initialHaplotypeDisplayThreshold;
    private AssociationTestSet testSet;
    public JTreeTable jtt;
    private boolean expand = true;


    public HaploAssocPanel(AssociationTestSet ats){
        testSet = ats;

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        makeTable(ats);
    }

    public void makeTable(AssociationTestSet ats) {
        testSet = ats;

        this.removeAll();

        if(ats == null) {
            return ;
        }

        Vector results = ats.getResults();

        initialHaplotypeDisplayThreshold = Options.getHaplotypeDisplayThreshold();
        Vector colNames = new Vector();

        colNames.add("Haplotype");
        colNames.add("Freq.");
        if (Options.getAssocTest() == ASSOC_TRIO){
            if(Options.getTdtType() == TDT_STD) {
                colNames.add("T:U");
            } else if(Options.getTdtType() == TDT_PAREN) {
                colNames.add("T:U, PA:PU");
            }

        }else{
            colNames.add("Case, Control Ratios");
        }
        colNames.add("Chi Square");
        colNames.add("p value");

        HaplotypeAssociationNode root = new HaplotypeAssociationNode("Haplotype Associations");

        for(int i=0; i < results.size(); i++){
            HaplotypeAssociationResult ar = (HaplotypeAssociationResult) results.get(i);
            HaplotypeAssociationNode han = new HaplotypeAssociationNode(ar.getName());

            for(int j=0;j< ar.getAlleleCount(); j++) {
                han.add(new HaplotypeAssociationNode(ar,j));
            }
            root.add(han);
        }
        int countsOrRatios = SHOW_HAP_COUNTS;
        if(jtt != null) {
            //if were just updating the table, then we want to retain the current status of countsOrRatios
            HaplotypeAssociationModel ham = (HaplotypeAssociationModel) jtt.getTree().getModel();
            countsOrRatios = ham.getCountsOrRatios();
        }

        jtt = new JTreeTable(new HaplotypeAssociationModel(colNames, root));

        ((HaplotypeAssociationModel)(jtt.getTree().getModel())).setCountsOrRatios(countsOrRatios);

        jtt.getColumnModel().getColumn(0).setPreferredWidth(200);
        jtt.getColumnModel().getColumn(1).setPreferredWidth(50);

        //we need more space for the CC counts in the third column
        if(Options.getAssocTest() == ASSOC_CC) {
            jtt.getColumnModel().getColumn(2).setPreferredWidth(200);
            jtt.getColumnModel().getColumn(3).setPreferredWidth(75);
            jtt.getColumnModel().getColumn(4).setPreferredWidth(75);
        } else {
            jtt.getColumnModel().getColumn(2).setPreferredWidth(150);
            jtt.getColumnModel().getColumn(3).setPreferredWidth(100);
            jtt.getColumnModel().getColumn(4).setPreferredWidth(100);
        }
        jtt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        Font monoFont = new Font("Monospaced",Font.PLAIN,12);
        jtt.setFont(monoFont);
        JTree theTree = jtt.getTree();
        theTree.setFont(monoFont);

        DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
        r.setLeafIcon(null);
        r.setOpenIcon(null);
        r.setClosedIcon(null);
        theTree.setCellRenderer(r);

        //jtt.setPreferredScrollableViewportSize(new Dimension(600,jtt.getPreferredScrollableViewportSize().height));
        jtt.setPreferredScrollableViewportSize(new Dimension(600, 1000));

        JScrollPane treeScroller = new JScrollPane(jtt);
        treeScroller.setMaximumSize(treeScroller.getPreferredSize());
        add(treeScroller);

        if(Options.getAssocTest() == ASSOC_CC) {
            JRadioButton countsButton = new JRadioButton("Show CC counts");
            JRadioButton ratiosButton = new JRadioButton("Show CC frequencies");
            JButton expandCollapseAll = new JButton("Expand/Collapse All");

            ButtonGroup bg = new ButtonGroup();

            bg.add(countsButton);
            bg.add(ratiosButton);
            countsButton.addActionListener(this);
            ratiosButton.addActionListener(this);
            expandCollapseAll.addActionListener(this);
            JPanel butPan = new JPanel();
            butPan.add(countsButton);
            butPan.add(ratiosButton);
            butPan.add(expandCollapseAll);
            add(butPan);
            if(countsOrRatios == SHOW_HAP_RATIOS) {
                ratiosButton.setSelected(true);
            }else{
                countsButton.setSelected(true);
            }
        }else{
            JPanel butPanB = new JPanel();
            JButton expandCollaseAllB = new JButton("Expand/Collapse All");
            expandCollaseAllB.addActionListener(this);
            butPanB.add(expandCollaseAllB);
            add(butPanB);
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("Show CC counts")) {
            HaplotypeAssociationModel ham = (HaplotypeAssociationModel)jtt.getTree().getModel();
            ham.setCountsOrRatios(SHOW_HAP_COUNTS);
            jtt.repaint();
        }
        else if (command.equals("Show CC frequencies")) {
            HaplotypeAssociationModel ham = (HaplotypeAssociationModel)jtt.getTree().getModel();
            ham.setCountsOrRatios(SHOW_HAP_RATIOS);
            jtt.repaint();
        }
        else if (command.equals("Expand/Collapse All")){
            if (expand){
                for (int i = 0; i < jtt.getTree().getHeight(); i++){
                    jtt.getTree().expandRow(i);
                }
                expand = !expand;
                jtt.repaint();
            }else {
                for (int i = 1; i < jtt.getTree().getHeight(); i++){
                    jtt.getTree().collapsePath(jtt.getTree().getPathForRow(i));
                }
                expand = !expand;
                jtt.repaint();
            }
        }


    }

    public AssociationTestSet getTestSet() {
        return testSet;
    }
}
