package edu.mit.wi.haploview;

import edu.mit.wi.plink.PlinkTableModel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.Vector;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PlinkResultsPanel extends JPanel implements ActionListener, Constants {
    private JTable table;
    private PlinkTableModel plinkTableModel;
    private TableSorter sorter;

    String[] pvalues = {"","<html>10<sup>-2</sup>","<html>10<sup>-3</sup>","<html>10<sup>-4</sup>",
            "<html>10<sup>-5</sup>"};
    String[] chromNames = {"","1","2","3","4","5","6","7","8","9","10",
            "11","12","13","14","15","16","17","18","19","20","21","22","X"};
    private JComboBox chromChooser, pvalChooser;
    private NumberTextField chromStart, chromEnd, topField, chiField;

    private long startPos, endPos;
    private int numResults;
    private double chisq, pval;
    private String chromChoice;
    private HaploView hv;


    public PlinkResultsPanel(HaploView h, Vector results, Vector colNames){
        hv = h;

        setLayout(new GridBagLayout());

        plinkTableModel = new PlinkTableModel(colNames,results);
        sorter = new TableSorter(plinkTableModel);


        table = new JTable(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        //table.getModel().addTableModelListener(this);

        sorter.setTableHeader(table.getTableHeader());
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);

        JScrollPane tableScroller = new JScrollPane(table);
        //tableScroller.setMinimumSize(new Dimension(800,600));
        //tableScroller.setMaximumSize(new Dimension(800,(int)tableScroller.getPreferredSize().getHeight()));


        JPanel chromFilterPanel = new JPanel();
        chromFilterPanel.setMinimumSize(new Dimension(700,40));
        chromFilterPanel.add(new JLabel("Chromosome:"));
        chromChooser = new JComboBox(chromNames);
        chromFilterPanel.add(chromChooser);
        chromFilterPanel.add(new JLabel("Start kb:"));
        chromStart = new NumberTextField("",6,false);
        chromFilterPanel.add(chromStart);
        chromFilterPanel.add(new JLabel("End kb:"));
        chromEnd = new NumberTextField("",6,false);
        chromFilterPanel.add(chromEnd);
        chromFilterPanel.add(new JLabel("Minimum chisq:"));
        chiField = new NumberTextField("0.0",3,true);
        chromFilterPanel.add(chiField);
        chromFilterPanel.add(new JLabel("Maximum pval:"));
        pvalChooser = new JComboBox(pvalues);
        chromFilterPanel.add(pvalChooser);
        JButton doFilter = new JButton("Filter");
        doFilter.addActionListener(this);
        chromFilterPanel.add(doFilter);

        JPanel topResultsFilterPanel = new JPanel();
        topResultsFilterPanel.add(new JLabel("View top"));
        topField = new NumberTextField("100",6,false);
        topField.setEnabled(false);  //TODO: demo
        topResultsFilterPanel.add(topField);
        topResultsFilterPanel.add(new JLabel("results"));
        JButton doTopFilter = new JButton("Filter");
        doTopFilter.setActionCommand("top filter");
        doTopFilter.addActionListener(this);
        doTopFilter.setEnabled(false); //TODO: demo
        topResultsFilterPanel.add(doTopFilter);

        JButton resetFilters = new JButton("Reset Filters");
        resetFilters.addActionListener(this);
        JButton moreFilters = new JButton("Advanced Filters");
        moreFilters.addActionListener(this);
        moreFilters.setEnabled(false); //TODO: demo

        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints a = new GridBagConstraints();
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Filters"));
        ((TitledBorder)(filterPanel.getBorder())).setTitleColor(Color.black);

        //a.gridx = 1;
        a.gridwidth = 5;
        a.anchor = GridBagConstraints.CENTER;
        a.weightx = 1;
        filterPanel.add(chromFilterPanel,a);
        a.gridy = 1;
        filterPanel.add(topResultsFilterPanel,a);
        a.gridy = 2;
        //a.gridx = 0;
        a.gridwidth = 1;
        a.anchor = GridBagConstraints.SOUTHWEST;
        filterPanel.add(moreFilters,a);
        a.gridx = 2;
        a.anchor = GridBagConstraints.SOUTHEAST;
        filterPanel.add(resetFilters,a);


        JPanel goPanel = new JPanel();
        JButton goButton = new JButton("<html><b>Go to Selected Region</b>");
        goButton.addActionListener(this);
        goButton.setActionCommand("Go to Selected Region");
        goPanel.add(goButton);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(10,50,10,50);
        add(tableScroller, c);
        c.weighty = 0;
        c.gridy = 1;
        add(filterPanel, c);
        c.gridy = 2;
        c.weighty = 0;
        add(goPanel,c);

    }

    public void doTopFilter(){
        clearSorting();
        plinkTableModel.filterTop(numResults);
        repaint();
    }

    public void doFilters(){
        clearSorting();
        plinkTableModel.filterAll(chromChoice,startPos,endPos,chisq,pval);
        repaint();
    }

    public void clearFilters(){
        clearSorting();
        plinkTableModel.resetFilters();
        chromChooser.setSelectedIndex(0);
        chromStart.setText("");
        chromEnd.setText("");
        topField.setText("100");
        chiField.setText("0.0");
        pvalChooser.setSelectedIndex(0);
        repaint();
    }

    public void clearSorting(){
        for (int i = 0; i < table.getColumnCount(); i++){
            sorter.setSortingStatus(i,TableSorter.NOT_SORTED);
        }
    }

    public void gotoRegion(){
        if (table.getSelectedRow() == -1){
            JOptionPane.showMessageDialog(this,
                    "Please select a region.",
                    "Invalid value",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String gotoChrom = (String)table.getValueAt(table.getSelectedRow(),1);
        long markerPosition = ((Long)(table.getValueAt(table.getSelectedRow(),3))).longValue();
        RegionDialog rd = new RegionDialog(hv,gotoChrom,markerPosition,"Go to Region");
        rd.pack();
        rd.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Filter")){

            chromChoice = (String)chromChooser.getSelectedItem();

            if (chromStart.getText().equals("")){
                startPos = -1;
            }else{
                startPos = (Long.parseLong(chromStart.getText()))*1000;
            }

            if (chromEnd.getText().equals("")){
                endPos = -1;
            }else{
                endPos = (Long.parseLong(chromEnd.getText()))*1000;
            }
            if (startPos > endPos){
                JOptionPane.showMessageDialog(this.getParent(),
                        "End position must be greater then start position.",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (chiField.getText().equals("")){
                chisq = -1;
            }else{
                chisq = Double.parseDouble(chiField.getText());
            }

            if (pvalChooser.getSelectedIndex() == 0){
                pval = -1;
            }else if (pvalChooser.getSelectedIndex() == 1){
                pval = .01;
            }else if (pvalChooser.getSelectedIndex() == 2){
                pval = .001;
            }else if (pvalChooser.getSelectedIndex() == 3){
                pval = .0001;
            }else if (pvalChooser.getSelectedIndex() == 4){
                pval = .00001;
            }
            doFilters();
        }else if (command.equals("top filter")){
            numResults = Integer.parseInt(topField.getText());
            doTopFilter();
        }else if (command.equals("Reset Filters")){
            clearFilters();
        }else if (command.equals("Advanced Filters")){
            //advanced filters dialog
        }else if (command.equals("Go to Selected Region")){
            gotoRegion();
        }
    }

}