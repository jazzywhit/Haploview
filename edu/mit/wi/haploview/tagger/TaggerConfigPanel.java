package edu.mit.wi.haploview.tagger;

import edu.mit.wi.haploview.*;
import edu.mit.wi.tagger.Tagger;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import java.awt.Color;
import java.util.Vector;
import java.util.Hashtable;
import java.io.File;
import java.io.IOException;

public class TaggerConfigPanel extends JPanel implements TableModelListener, ActionListener{
    private JTable table;
    private TaggerController tagControl;

    private final static int NUM_COL = 0;
    private final static int NAME_COL = 1;
    private final static int INCLUDE_COL = 3;
    private final static int EXCLUDE_COL = 4;
    private final static int CAPTURE_COL = 5;

    private JButton runTaggerButton;
    private JButton resetTableButton;
    private Timer timer;
    private HaploData theData;
    private Hashtable snpsByName;
    private NumberTextField rsqField, lodField;
    private ButtonGroup aggressiveGroup;
    private NumberTextField maxNumTagsField;
    private JPanel buttonPanel = new JPanel();
    private JPanel taggerProgressPanel = new JPanel();
    JProgressBar taggerProgress = new JProgressBar();
    private JLabel taggerProgressLabel = new JLabel("Tagging...");

    public TaggerConfigPanel(HaploData hd)  {
        theData = hd;
        refreshTable();
    }

    public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == INCLUDE_COL){
            //if they check force include for some row, then we uncheck force exclude for that row
            if(((Boolean)table.getValueAt(e.getFirstRow(),e.getColumn())).booleanValue()) {
                table.setValueAt(new Boolean(false),e.getFirstRow(),EXCLUDE_COL);
            }
        }
        else if(e.getColumn() == EXCLUDE_COL) {
            //if they check force exclude for some row, then we uncheck force include for that row
            if(((Boolean)table.getValueAt(e.getFirstRow(),e.getColumn())).booleanValue()) {
                table.setValueAt(new Boolean(false),e.getFirstRow(),INCLUDE_COL);
            }
        }else if(e.getColumn() == CAPTURE_COL) {
            if(!((Boolean)table.getValueAt(e.getFirstRow(),e.getColumn())).booleanValue()) {
                table.setValueAt(new Boolean(false),e.getFirstRow(),EXCLUDE_COL);
                table.setValueAt(new Boolean(false),e.getFirstRow(),INCLUDE_COL);
            }
        }
    }

    public void refreshTable(){
        this.removeAll();

        snpsByName = new Hashtable();

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        Vector columnNames = new Vector();
        Vector tableData = new Vector();

        columnNames.add("#");
        columnNames.add("Name");
        columnNames.add("Position");
        columnNames.add("Force Include");
        columnNames.add("Force Exclude");
        columnNames.add("Capture this Allele?");

        for (int i = 0; i < Chromosome.getSize(); i++){
            SNP tempSNP = Chromosome.getMarker(i);
            snpsByName.put(tempSNP.getName(), tempSNP);
            Vector tempData = new Vector();

            tempData.add(Integer.toString(Chromosome.realIndex[i]+1));
            tempData.add(tempSNP.getName());
            tempData.add(String.valueOf(tempSNP.getPosition()));
            tempData.add(new Boolean(false));
            tempData.add(new Boolean(false));
            tempData.add(new Boolean(true));

            tableData.add(tempData);
        }
        TagConfigTableModel tableModel = new TagConfigTableModel(columnNames,tableData);
        tableModel.addTableModelListener(this);
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(NUM_COL).setPreferredWidth(30);
        table.getColumnModel().getColumn(CAPTURE_COL).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 700));
        scrollPane.setMaximumSize(scrollPane.getPreferredSize());
        add(scrollPane);

        JPanel optsRightPanel = new JPanel();
        optsRightPanel.setLayout(new BoxLayout(optsRightPanel, BoxLayout.Y_AXIS));

        JPanel rsqPanel = new JPanel();
        JLabel rsqLabel = new JLabel("r\u00b2 threshold");
        rsqPanel.add(rsqLabel);
        rsqField = new NumberTextField(String.valueOf(Options.getTaggerRsqCutoff()),5,true);
        rsqPanel.add(rsqField);
        optsRightPanel.add(rsqPanel);

        JPanel lodPanel = new JPanel();
        JLabel lodLabel = new JLabel("LOD threshold for multi-marker tests");
        lodPanel.add(lodLabel);
        lodField = new NumberTextField(String.valueOf(Options.getTaggerLODCutoff()),5,true);
        lodPanel.add(lodField);
        optsRightPanel.add(lodPanel);

        JPanel maxNumPanel = new JPanel();
        maxNumPanel.add(new JLabel("Maximum number of tags (blank for no limit)"));
        maxNumTagsField = new NumberTextField("",6,false);
        maxNumPanel.add(maxNumTagsField);
        optsRightPanel.add(maxNumPanel);

        JPanel optsLeftPanel = new JPanel();
        optsLeftPanel.setLayout(new BoxLayout(optsLeftPanel, BoxLayout.Y_AXIS));
        JRadioButton pairwiseButton = new JRadioButton("pairwise tagging only");
        pairwiseButton.setActionCommand(String.valueOf(Tagger.PAIRWISE_ONLY));
        optsLeftPanel.add(pairwiseButton);
        JRadioButton dupleButton = new JRadioButton("aggressive tagging: use 2-marker haplotypes");
        dupleButton.setActionCommand(String.valueOf(Tagger.AGGRESSIVE_DUPLE));
        optsLeftPanel.add(dupleButton);
        JRadioButton tripleButton = new JRadioButton("aggressive tagging: use 2- and 3-marker haplotypes");
        tripleButton.setActionCommand(String.valueOf(Tagger.AGGRESSIVE_TRIPLE));
        optsLeftPanel.add(tripleButton);
        aggressiveGroup = new ButtonGroup();
        aggressiveGroup.add(pairwiseButton);
        aggressiveGroup.add(dupleButton);
        aggressiveGroup.add(tripleButton);
        tripleButton.setSelected(true);

        JPanel optsPanel = new JPanel();
        optsPanel.add(optsLeftPanel);
        optsPanel.add(optsRightPanel);
        add(optsPanel);

        runTaggerButton = new JButton("Run Tagger");
        runTaggerButton.addActionListener(this);

        resetTableButton = new JButton("Reset Table");
        resetTableButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(runTaggerButton);
        buttonPanel.add(resetTableButton);

        add(buttonPanel);
    }

    public void addActionListener(ActionListener al){
        listenerList.add(ActionListener.class, al);
    }

    public void fireTaggerEvent(ActionEvent ae){
        Object listeners[] = listenerList.getListenerList();
        for (int i = 0; i <= listeners.length-1; i+=2){
            if (listeners[i] == ActionListener.class){
                ((ActionListener)listeners[i+1]).actionPerformed(ae);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Run Tagger")) {
            runTaggerButton.setEnabled(false);

            taggerProgress.setIndeterminate(true);
            taggerProgress.setForeground(Color.BLUE);
            taggerProgress.setMaximumSize(new Dimension(250,20));
            taggerProgressPanel.setLayout(new BoxLayout(taggerProgressPanel,BoxLayout.Y_AXIS));
            taggerProgressPanel.add(taggerProgressLabel);
            taggerProgressLabel.setAlignmentX(CENTER_ALIGNMENT);
            taggerProgressPanel.add(new JLabel("         "));
            taggerProgressPanel.add(taggerProgress);
            remove(buttonPanel);
            add(taggerProgressPanel);
            add(buttonPanel);
            revalidate();

            double rsqCut = new Double(rsqField.getText()).doubleValue();
            if (rsqCut > 1){
                Options.setTaggerRsqCutoff(1.0);
                rsqField.setText("1.0");
            }else if (rsqCut < 0){
                Options.setTaggerRsqCutoff(0.0);
                rsqField.setText("0.0");
            }else{
                Options.setTaggerRsqCutoff(rsqCut);
            }

            double lodCut = new Double(lodField.getText()).doubleValue();
            if (lodCut < 0){
                Options.setTaggerLODCutoff(0.0);
                lodField.setText("0.0");
            }else{
                Options.setTaggerLODCutoff(lodCut);
            }

            int maxNumTags;
            if (maxNumTagsField.getText().equals("")){
                maxNumTags = 0;
            }else{
                maxNumTags = new Integer(maxNumTagsField.getText()).intValue();
            }

            //build include/exclude lists
            Vector include = new Vector();
            Vector exclude = new Vector();
            Vector capture = new Vector();
            for(int i= 0;i <table.getRowCount(); i++) {
                if(((Boolean)table.getValueAt(i,INCLUDE_COL)).booleanValue()) {
                    include.add((String)table.getValueAt(i,NAME_COL));
                }else if(((Boolean)table.getValueAt(i,EXCLUDE_COL)).booleanValue()) {
                    exclude.add((String)table.getValueAt(i,NAME_COL));
                }
                if (((Boolean)table.getValueAt(i,CAPTURE_COL)).booleanValue()){
                    capture.add(snpsByName.get(table.getValueAt(i,NAME_COL)));
                }
            }

            tagControl = new TaggerController(theData,include,exclude,capture,
                    Integer.valueOf(aggressiveGroup.getSelection().getActionCommand()).intValue(),maxNumTags,true);
            tagControl.runTagger();

            final TaggerConfigPanel tcp = this;
            timer = new Timer(100, new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(tagControl.isTaggingCompleted()) {
                        remove(taggerProgressPanel);
                        runTaggerButton.setEnabled(true);
                        //the parent of this is a meta jpanel used to haxor the layout
                        //the parent of that jpanel is the jtabbedPane in the tagger tab of HV
                        ((JTabbedPane)(tcp.getParent().getParent())).setSelectedIndex(1);
                        fireTaggerEvent(new ActionEvent(tcp,ActionEvent.ACTION_PERFORMED,"taggingdone"));
                        timer.stop();
                    }
                }
            });

            timer.start();
        }else if (command.equals("Reset Table")){
            for (int i = 0; i < table.getRowCount(); i++){
                table.setValueAt(new Boolean(false), i, EXCLUDE_COL);
                table.setValueAt(new Boolean(false), i, INCLUDE_COL);
                table.setValueAt(new Boolean(true), i, CAPTURE_COL);
            }
            rsqField.setText(String.valueOf(Tagger.DEFAULT_RSQ_CUTOFF));
        }
    }

    public TaggerController getTaggerController() {
        return tagControl;
    }

    public void export(File outfile) throws IOException, HaploViewException{
        if (tagControl != null){
            tagControl.saveResultsToFile(outfile);
        }else{
            throw new HaploViewException("Tagger has not yet generated any results");
        }
    }

    class TagConfigTableModel extends AbstractTableModel {
        Vector columnNames; Vector data;

        public TagConfigTableModel(Vector c, Vector d){
            columnNames=c;
            data=d;
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

        public Class getColumnClass(int c){
            return getValueAt(0, c).getClass();
        }

        public String getColumnName(int n){
            return (String)columnNames.elementAt(n);
        }

        public boolean isCellEditable(int row, int col){
            if (col == CAPTURE_COL) {
                return true;
            }else if(col == INCLUDE_COL || col == EXCLUDE_COL){
                if(((Boolean)((Vector)data.get(row)).get(CAPTURE_COL)).booleanValue()) {
                    return true;
                }
            }
            return false;
        }

        public void setValueAt(Object value, int row, int col){
            ((Vector)data.elementAt(row)).set(col, value);
            fireTableCellUpdated(row, col);
        }
    }
}
