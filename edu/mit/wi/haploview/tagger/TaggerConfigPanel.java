package edu.mit.wi.haploview.tagger;

import edu.mit.wi.haploview.*;
import edu.mit.wi.tagger.Tagger;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.Hashtable;
import java.io.File;
import java.io.IOException;

public class TaggerConfigPanel extends JPanel implements TableModelListener, ActionListener{
	private JTable table;
    private TaggerController tagControl;

    private final static int NAME_COL = 1;
    private final static int INCLUDE_COL = 3;
    private final static int EXCLUDE_COL = 4;
    private final static int CAPTURE_COL = 5;

    private JButton runTaggerButton;
    private JButton resetTableButton;
    private Timer timer;
    private HaploData theData;
    private Hashtable snpsByName;
    private NumberTextField rsqField;

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
        columnNames.add("Tag this SNP?");

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
        table.getColumnModel().getColumn(0).setPreferredWidth(30);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setMaximumSize(scrollPane.getPreferredSize());
        add(scrollPane);

        runTaggerButton = new JButton("Run Tagger");
        runTaggerButton.addActionListener(this);

        resetTableButton = new JButton("Reset Table");
        resetTableButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(runTaggerButton);
        buttonPanel.add(resetTableButton);

        add(buttonPanel);

        JPanel configPanel = new JPanel();
        configPanel.add(new JLabel("r\u00b2 cutoff"));
        rsqField = new NumberTextField(String.valueOf(Options.getTaggerRsqCutoff()),5,true);
        configPanel.add(rsqField);
        add(configPanel);
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

            tagControl = new TaggerController(theData,include,exclude,capture);
            //tagControl.setIncluded(include);
            //tagControl.setExcluded(exclude);
            tagControl.runTagger();

            final TaggerConfigPanel tcp = this;
            timer = new Timer(100, new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                if(tagControl.isTaggingCompleted()) {
                                    runTaggerButton.setEnabled(true);
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
