package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.Constants;
import edu.mit.wi.haploview.NumberTextField;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Vector;

import org.jfree.data.statistics.HistogramDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

public class PermutationTestPanel extends JPanel implements Constants,ActionListener {
    private JLabel bestPermutationValueLabel;
    private JLabel blocksChangedLabel;
    private NumberTextField permCountField;

    private JProgressBar permProgressBar;
    private JButton doPermutationsButton;
    private JButton stopPermutationsButton;

    private PermutationTestSet testSet;
    private PermutationThread permThread;
    private ProgressBarUpdater progressUpdater;
    private Vector colNames;
    private JPanel resultsPanel;
    private JLabel bestObsValueLabel;

    private JLabel scoreBoardNumPassLabel;
    private JLabel scoreBoardNumTotalLabel;
    private JPanel scoreBoardPanel;

    public PermutationTestPanel(PermutationTestSet pts) {
        if(pts == null) {
            throw new NullPointerException();
        }

        testSet = pts;

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        JPanel permCountPanel = new JPanel();
        permCountPanel.setMaximumSize(new Dimension(600,30));
        JLabel permCountTextLabel = new JLabel("Number of Permutations To Perform: ");
        permCountField = new NumberTextField("", 10, false);
        permCountPanel.add(permCountTextLabel);
        permCountPanel.add(permCountField);
        this.add(permCountPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setMaximumSize(new Dimension(600,30));
        doPermutationsButton = new JButton("Do Permutations");
        doPermutationsButton.addActionListener(this);
        stopPermutationsButton = new JButton("Stop");
        stopPermutationsButton.addActionListener(this);
        stopPermutationsButton.setEnabled(false);
        buttonPanel.add(doPermutationsButton);
        buttonPanel.add(stopPermutationsButton);
        this.add(buttonPanel);

        JPanel bestObsPanel = new JPanel();
        bestObsPanel.setMaximumSize(new Dimension(300,30));
        JLabel bestObsTextLabel = new JLabel("Best Observed Chi-Square: ");
        bestObsValueLabel = new JLabel(testSet.getBestObsChiSq() + " (" + testSet.getBestObsName() + ")");
        bestObsPanel.add(bestObsTextLabel);
        bestObsPanel.add(bestObsValueLabel);
        this.add(bestObsPanel);

        JPanel bestPermPanel = new JPanel();
        bestPermPanel.setMaximumSize(new Dimension(300,30));
        JLabel bestPermTextLabel = new JLabel("Best Permutation Chi-Square: ");
        bestPermutationValueLabel = new JLabel("");
        bestPermPanel.add(bestPermTextLabel);
        bestPermPanel.add(bestPermutationValueLabel);
        this.add(bestPermPanel);

        scoreBoardPanel = new JPanel();
        scoreBoardNumPassLabel = new JLabel();
        scoreBoardNumTotalLabel = new JLabel();
        scoreBoardPanel.add(scoreBoardNumPassLabel);
        scoreBoardPanel.add(new JLabel(" permutations out of "));
        scoreBoardPanel.add(scoreBoardNumTotalLabel);
        scoreBoardPanel.add(new JLabel(" exceed highest observed chi square."));
        scoreBoardPanel.setMaximumSize(new Dimension(600,30));
        scoreBoardPanel.setVisible(false);
        add(scoreBoardPanel);

        colNames = new Vector();
        colNames.add("Name");
        colNames.add("Chi Square");
        colNames.add("Permutation p-value");

        blocksChangedLabel = new JLabel("The current blocks may have changed, so these values may not be accurate!");
        blocksChangedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        blocksChangedLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        blocksChangedLabel.setForeground(Color.RED);

    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("Do Permutations") ) {
            startPerms();
        }else if(command.equals("Stop")) {
            stopPerms();
        }
    }

    public void setTestSet(PermutationTestSet pts){
        testSet = pts;
    }

    public void startPerms() {
        scoreBoardPanel.setVisible(true);

        if (resultsPanel != null){
            remove(resultsPanel);
            repaint();
        }
        this.remove(blocksChangedLabel);

        bestPermutationValueLabel.setText("");

        testSet.setPermutationCount(Integer.parseInt(permCountField.getText()));

        permThread = new PermutationThread(testSet);

        doPermutationsButton.setEnabled(false);
        stopPermutationsButton.setEnabled(true);

        permProgressBar = new JProgressBar(0,testSet.getPermutationCount());
        permProgressBar.setMaximumSize(new Dimension(200,30));
        permProgressBar.setStringPainted(true);
        this.add(permProgressBar);

        permThread.start();

        progressUpdater = new ProgressBarUpdater();
        progressUpdater.start();
    }

    public void stopPerms() {
        testSet.stopProcessing = true;
        progressUpdater.interrupt();
    }

    public void finishedPerms() {
        scoreBoardNumTotalLabel.setText(String.valueOf(testSet.getPermutationsPerformed()));
        scoreBoardNumPassLabel.setText(String.valueOf(testSet.getBestExceededCount()));
        doPermutationsButton.setEnabled(true);
        stopPermutationsButton.setEnabled(false);
        this.remove(permProgressBar);
        bestPermutationValueLabel.setText(String.valueOf(testSet.getBestPermChiSquare()));
        makeTable();
        resultsPanel.revalidate();
    }

    public void makeTable() {
        Vector tableData = testSet.getResults();
        TableModel tm = new PermResultTableModel(colNames, tableData);
        JTable jt = new JTable(tm);
        JScrollPane tableScroller = new JScrollPane(jt);
        //if the table is fairly short, override java's stupid insistence on growing it to fit the
        //container panel
        tableScroller.setMaximumSize(tableScroller.getPreferredSize());
        JLabel sigAssocLabel = new JLabel("Significant Associations:");
        sigAssocLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel,BoxLayout.Y_AXIS));
        resultsPanel.add(sigAssocLabel);
        resultsPanel.add(tableScroller);

        resultsPanel.add(Box.createRigidArea(new Dimension(0,5)));

        HistogramDataset resHist = new HistogramDataset();
        resHist.addSeries("Chi Squares", testSet.getPermBestChiSq(),100);
        JFreeChart jfc =  ChartFactory.createHistogram(null,
                "Chi Square",
                "Number Permutations",resHist,PlotOrientation.VERTICAL,false,false,false);
        jfc.setBorderVisible(true);
        XYPlot xyp = jfc.getXYPlot();
        xyp.getRenderer().setPaint(Color.blue);
        ChartPanel cp = new ChartPanel(jfc);
        cp.setMaximumSize(new Dimension(400, cp.getPreferredSize().height));
        resultsPanel.add(cp);
        add(resultsPanel);
    }

    public void setBlocksChanged() {
        if (resultsPanel != null){
            this.add(blocksChangedLabel);
        }
    }

    private class PermutationThread extends Thread{
        PermutationTestSet testSet;

        public PermutationThread(PermutationTestSet pts) {
            testSet = pts;
        }

        public void run() {
            testSet.doPermutations();
            finishedPerms();
        }
    }

    public PermutationTestSet getTestSet() {
        return testSet;
    }

    private class ProgressBarUpdater extends Thread{
        public void run() {
            try {
                while(testSet.getPermutationCount() - testSet.getPermutationsPerformed() != 0) {
                    permProgressBar.setValue(testSet.getPermutationsPerformed());
                    scoreBoardNumTotalLabel.setText(String.valueOf(testSet.getPermutationsPerformed()));
                    scoreBoardNumPassLabel.setText(String.valueOf(testSet.getBestExceededCount()));
                    bestPermutationValueLabel.setText(String.valueOf(testSet.getBestPermChiSquare()));
                    sleep(200);
                }
            } catch(InterruptedException ie) {}
        }
    }

    class PermResultTableModel extends AbstractTableModel {
        Vector columnNames; Vector data;

        public PermResultTableModel(Vector c, Vector d){
            columnNames=c;
            data=d;
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
            return data.size();
        }

        public Object getValueAt(int row, int column){
            return ((Vector)data.elementAt(row)).elementAt(column);
        }

    }

}
