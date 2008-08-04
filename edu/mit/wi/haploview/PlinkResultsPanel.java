package edu.mit.wi.haploview;

import edu.mit.wi.plink.PlinkTableModel;
import edu.mit.wi.plink.PlinkGraph;
import edu.mit.wi.plink.PlotOptionDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.border.TitledBorder;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Arrays;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PlinkResultsPanel extends JPanel implements ActionListener, Constants, KeyListener {
    static final long serialVersionUID = -6824022261340524367L;
    private JTable table;
    private PlinkTableModel plinkTableModel;
    private TableSorter sorter;

    String[] chromNames = {"","1","2","3","4","5","6","7","8","9","10",
            "11","12","13","14","15","16","17","18","19","20","21","22","X","Y","XY","MT"};
    String[] signs = {"",">",">=","=","<=","<"};
    private JComboBox chromChooser, genericChooser, signChooser, removeChooser;
    private JButton viewFilters;
    private NumberTextField chromStart, chromEnd;
    private JTextField valueField, markerField;
    private JPanel filterPanel;
    private Vector originalColumns, genericFilters;
    private Hashtable removedColumns;

    private String chosenMarker, chromChoice;
    private int startPos, endPos;
    private HaploView hv;
    private PlinkGraph theGraph;


    public PlinkResultsPanel(HaploView h, Vector results, Vector colNames, boolean dups, Hashtable remove){
        hv = h;

        setLayout(new GridBagLayout());

        plinkTableModel = new PlinkTableModel(colNames,results);
        sorter = new TableSorter(plinkTableModel);
        removedColumns = new Hashtable();
        originalColumns = (Vector)plinkTableModel.getUnknownColumns().clone();
        genericFilters = new Vector();


        table = new JTable(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(Color.lightGray);
        table.getTableHeader().setReorderingAllowed(false);
        //due to an old JTable bug this is necessary to activate a horizontal scrollbar
        if (table.getColumnCount() >= 20 && table.getRowCount() > 60){
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
        //table.getModel().addTableModelListener(this);

        sorter.setTableHeader(table.getTableHeader());
        table.getColumnModel().getColumn(0).setPreferredWidth(60);

        if (Options.getSNPBased()){
            final PlinkCellRenderer renderer = new PlinkCellRenderer();
            try{
                table.setDefaultRenderer(Class.forName("java.lang.Double"), renderer);
                table.setDefaultRenderer(Class.forName("java.lang.Integer"), renderer);
                table.setDefaultRenderer(Class.forName("java.lang.Long"), renderer);
                table.setDefaultRenderer(Class.forName("java.lang.String"),renderer);
            }catch (Exception e){
            }
        }

        JScrollPane tableScroller = new JScrollPane(table);


        JPanel mainFilterPanel = new JPanel();
        JPanel extraFilterPanel = new JPanel();

        if (Options.getSNPBased()){
            mainFilterPanel.add(new JLabel("Chr:"));
            chromChooser = new JComboBox(chromNames);
            mainFilterPanel.add(chromChooser);
            mainFilterPanel.add(new JLabel("Start kb:"));
            chromStart = new NumberTextField("",6,false, false);
            mainFilterPanel.add(chromStart);
            mainFilterPanel.add(new JLabel("End kb:"));
            chromEnd = new NumberTextField("",6,false, false);
            mainFilterPanel.add(chromEnd);
            extraFilterPanel.add(new JLabel("Specify Marker:"));
            markerField = new JTextField(8);
            markerField.addActionListener(this);
            markerField.addKeyListener(this);
            extraFilterPanel.add(markerField);
            JButton doMarkerFilter = new JButton("Prune Table");
            doMarkerFilter.addActionListener(this);
            doMarkerFilter.setActionCommand("marker filter");
            doMarkerFilter.addActionListener(this);
            extraFilterPanel.add(doMarkerFilter);
        }
        
        mainFilterPanel.add(new JLabel("Filter:"));
        genericChooser = new JComboBox(plinkTableModel.getUnknownColumns());
        genericChooser.setSelectedIndex(-1);
        mainFilterPanel.add(genericChooser);
        signChooser = new JComboBox(signs);
        signChooser.setSelectedIndex(-1);
        mainFilterPanel.add(signChooser);
        valueField = new JTextField(8);
        mainFilterPanel.add(valueField);
        JButton doFilter = new JButton("Filter");
        doFilter.addActionListener(this);
        mainFilterPanel.add(doFilter);


        extraFilterPanel.add(new JLabel("Remove Column:"));
        removeChooser = new JComboBox(plinkTableModel.getUnknownColumns());
        extraFilterPanel.add(removeChooser);
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(this);
        extraFilterPanel.add(removeButton);

        JButton resetFilters = new JButton("Reset");
        resetFilters.addActionListener(this);

        JButton moreResults = new JButton("Load Additional Results");
        moreResults.addActionListener(this);
        if (dups){
            moreResults.setEnabled(false);
        }
        JButton fisherButton = new JButton("Combine P-Values");
        fisherButton.addActionListener(this);

        JButton plotButton = new JButton("Plot");
        plotButton.addActionListener(this);

        filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints a = new GridBagConstraints();
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                "Viewing " + plinkTableModel.getRowCount() + " results"));
        ((TitledBorder)(filterPanel.getBorder())).setTitleColor(Color.black);

        a.gridwidth = 5;
        a.anchor = GridBagConstraints.CENTER;
        a.weightx = 1;
        filterPanel.add(mainFilterPanel,a);
        a.gridy = 1;
        filterPanel.add(extraFilterPanel,a);
        a.gridy = 2;
        a.gridwidth = 1;
        a.anchor = GridBagConstraints.SOUTHWEST;
        a.insets = new Insets(5,0,0,0);
        if (Options.getSNPBased()){
            filterPanel.add(moreResults,a);
            a.gridx = 1;
            filterPanel.add(fisherButton,a);
            a.gridx = 2;
            a.anchor = GridBagConstraints.SOUTH;
        }
        filterPanel.add(plotButton,a);
        a.gridx = 3;
        a.anchor = GridBagConstraints.SOUTHEAST;
        filterPanel.add(resetFilters,a);

        viewFilters = new JButton("View Active Filters");
        viewFilters.setActionCommand("View Active Filters");
        viewFilters.addActionListener(this);
        viewFilters.setEnabled(false);
        a.gridx = 4;
        filterPanel.add(viewFilters,a);


        JPanel goPanel = new JPanel();
        JButton goButton = new JButton("<html><b>Go to Selected Region</b>");
        goButton.addActionListener(this);
        goButton.setActionCommand("Go to Selected Region");
        goPanel.add(goButton);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(10,10,10,10);
        add(tableScroller, c);
        c.weighty = 0;
        c.gridy = 1;
        add(filterPanel, c);
        if (Options.getSNPBased()){
            c.gridy = 2;
            add(goPanel,c);
        }

        if (remove != null){
            for (int i = 1; i < plinkTableModel.getUnknownColumns().size(); i++){
                if (remove.containsKey(plinkTableModel.getUnknownColumns().get(i))){
                    removeColumn((String)plinkTableModel.getUnknownColumns().get(i),i);
                }
            }
        }
    }

    public void jumpToMarker(String marker){

        boolean found = false;
        Pattern strpattern = Pattern.compile(marker, Pattern.CASE_INSENSITIVE | Pattern.LITERAL | Pattern.DOTALL);
        Matcher strmatcher;

        for (int i = 0; i < table.getRowCount(); i++){

            strmatcher = strpattern.matcher((String)table.getValueAt(i,1));

            if (strmatcher.find()){

                table.changeSelection(i,1,false,false);
                found = true;
                break;
            }
        }
        if (!found){

            table.clearSelection();

        }
   }

    public void jumpToNonSNP(String fid, String iid){
        if (plinkTableModel.getFIDColumn() != -1 && plinkTableModel.getIIDColumn() != -1){
            for (int i = 0; i < table.getRowCount(); i++){
                String currentFid = (String)table.getValueAt(i,plinkTableModel.getFIDColumn());
                String currentIid = (String)table.getValueAt(i,plinkTableModel.getIIDColumn());
                if (currentFid.equals(fid) && currentIid.equals(iid)){
                    table.changeSelection(i,1,false,false);
                    break;
                }
            }
            hv.requestFocus();
            hv.toFront();
        }
    }

    public void setChosenMarker(String chosenMarker) {
        this.chosenMarker = chosenMarker;
    }

    public String getChosenMarker() {
        return chosenMarker;
    }

    public Vector getSNPs(){
        return plinkTableModel.getSNPs();
    }

    public Object getValueAt(int row, int col){
        return plinkTableModel.getValueAt(row,col);
    }

    public void doFilters(String searchMarker){
        chromChoice = "";
        startPos = -1;
        endPos = -1;

        if (searchMarker.length() > 0){

            reSort();
            plinkTableModel.filterAll(chromChoice,startPos,endPos,genericFilters,searchMarker);
            countResults();

        }
    }

    public void doFilters(){
        chromChoice = "";
        startPos = -1;
        endPos = -1;

        if (Options.getSNPBased()){
            chromChoice = (String)chromChooser.getSelectedItem();

            if (!(chromStart.getText().equals(""))){
                startPos = Integer.parseInt(chromStart.getText());
            }

            if (!(chromEnd.getText().equals(""))){
                endPos = Integer.parseInt(chromEnd.getText());
            }
            if (startPos > endPos){
                JOptionPane.showMessageDialog(this.getParent(),
                        "End position must be greater then start position.",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String columnChoice, signChoice, val;

        if (genericChooser.getSelectedIndex() > 0){
            columnChoice = (String) genericChooser.getSelectedItem();

            if (signChooser.getSelectedIndex() > 0){
                signChoice = (String) signChooser.getSelectedItem();

                if (!(valueField.getText().equals(""))){
                    val = valueField.getText();
                    genericFilters.add(columnChoice + "\t" + signChoice + "\t" + val);
                }
            }
        }

        reSort();
        plinkTableModel.filterAll(chromChoice,startPos,endPos,genericFilters,"");
        countResults();
    }

    public void removeColumn(String col, int index){
        TableColumn deletedColumn = table.getColumn(col);
        removedColumns.put(col,deletedColumn);
        table.removeColumn(deletedColumn);
        removeChooser.removeItemAt(index);
        removeChooser.setSelectedIndex(removeChooser.getItemCount()-1);
        genericChooser.setSelectedIndex(0);
        if (table.getColumnCount() < 20){
            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        }
        repaint();
    }

    public void clearFilters(){
        if (removedColumns.size() > 0){
            for (int i = 1; i < removeChooser.getItemCount(); i++){
                TableColumn deletedColumn = table.getColumn(removeChooser.getItemAt(i));
                removedColumns.put(removeChooser.getItemAt(i),deletedColumn);
                table.removeColumn(deletedColumn);
            }

            removeChooser.removeAllItems();
            removeChooser.addItem("");

            for (int i = 1; i < originalColumns.size(); i++){
                TableColumn addedColumn = (TableColumn)removedColumns.get(originalColumns.get(i));
                table.addColumn(addedColumn);
                removeChooser.addItem(originalColumns.get(i));
            }
            removedColumns.clear();
        }

        if (table.getColumnCount() >= 20 && table.getRowCount() > 60){
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }

        plinkTableModel.resetFilters();
        if (Options.getSNPBased()){
            chromChooser.setSelectedIndex(0);
            chromStart.setText("");
            chromEnd.setText("");
            markerField.setText("");
        }
        genericChooser.setSelectedIndex(0);
        genericChooser.updateUI();
        signChooser.setSelectedIndex(0);
        valueField.setText("");
        genericFilters = new Vector();
        reSort();
        countResults();
    }

    public void reSort(){
        for (int i = 0; i < table.getColumnCount(); i++){
            sorter.setSortingStatus(i,sorter.getSortingStatus(i));
        }
    }

    public void countResults(){
        filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
                "Viewing " + plinkTableModel.getRowCount() + " results"));
        if (genericFilters.size() == 0){
            viewFilters.setEnabled(false);
            viewFilters.setText("View Active Filters");
        }else{
            viewFilters.setEnabled(true);
            viewFilters.setText("View " + genericFilters.size() + " Active Filters");
        }
        repaint();
    }

    public void makeChart(String title,int yPlotType,int yColumn,int xPlotType,int xColumn,double suggestive,double significant,boolean useSug,boolean useSig,int[] signs,int[] thresholds,int dotSize,int colorColumn,boolean grid, File svgFile, int width, int height){
        hv.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        theGraph = new PlinkGraph(title,yPlotType,yColumn,xPlotType,xColumn,suggestive,significant,useSug,useSig,signs,thresholds,dotSize,colorColumn,grid,svgFile,width,height,table,plinkTableModel,this);
        hv.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void gotoRegion(){
        if (table.getSelectedRow() == -1){
            JOptionPane.showMessageDialog(this,
                    "Please select an item.",
                    "No Selection",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String gotoChrom = (String)table.getValueAt(table.getSelectedRow(),0);

        if(gotoChrom.length() < 1){
            JOptionPane.showMessageDialog(this,
                    "Please choose an item with a known Chromosome",
                    "Invalid Data",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String gotoMarker = (String)table.getValueAt(table.getSelectedRow(),1);
        long markerPosition = ((Long)(table.getValueAt(table.getSelectedRow(),2))).longValue();

        try{
            RegionDialog rd = new RegionDialog(hv,gotoChrom,gotoMarker,
                    this,markerPosition,"Go to Region");
            rd.pack();
            rd.setVisible(true);
        }catch (HaploViewException hve){
            JOptionPane.showMessageDialog(this,
                    "Chromosome Unknown",
                    "Invalid Option",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    public Vector getUnknownColumns(){
        return plinkTableModel.getUnknownColumns();
    }

    public Vector getOriginalColumns(){
        return originalColumns;
    }

    public void exportTable(File outfile) throws IOException, HaploViewException{
        BufferedWriter plinkWriter = new BufferedWriter(new FileWriter(outfile));
        for (int i = 0; i < table.getColumnCount(); i++){
            if (table.getColumnName(i).equalsIgnoreCase("CHROM")){
                plinkWriter.write("CHR"+"\t");
            }else if (table.getColumnName(i).equalsIgnoreCase("MARKER")){
                plinkWriter.write("SNP"+"\t");
            }else{
                plinkWriter.write(table.getColumnName(i)+"\t");
            }
        }
        plinkWriter.newLine();

        for (int i = 0; i < table.getRowCount(); i++){
            for (int j = 0; j < table.getColumnCount(); j++){
                if (table.getValueAt(i,j) == null){
                    plinkWriter.write("-"+"\t");
                }else{
                    plinkWriter.write(table.getValueAt(i,j)+"\t");
                }
            }
            plinkWriter.newLine();
        }
        plinkWriter.close();
    }

    public void disposePlot(){
        if (theGraph != null){
            theGraph.disposePlot();
            theGraph = null;
        }
    }

    public void keyTyped(KeyEvent e) {

    }
    public void keyPressed(KeyEvent e) {

    }
    public void keyReleased(KeyEvent e) {
        if(e.getComponent().equals(markerField)){
            if(markerField.getText().length() > 0){
                String marker = markerField.getText();
                if (!(marker.equals(""))){
                    jumpToMarker(marker);
                }
            }else{
                table.clearSelection();   
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Filter")){
            doFilters();
        }else if (command.equals("marker filter")){
            String marker = markerField.getText();
            if (!(marker.equals(""))){
                doFilters(marker);
            }
        }else if (command.equals("Reset")){
            clearFilters();
        }else if (command.equals("Load Additional Results")){
            HaploView.fc.setSelectedFile(new File(""));
            int returned = HaploView.fc.showOpenDialog(this);
            if (returned != JFileChooser.APPROVE_OPTION) return;
            File file = HaploView.fc.getSelectedFile();
            String fullName = file.getParent()+File.separator+file.getName();
            String[] inputs = {null,null,fullName,null,null,null,"Y"};
            if (removedColumns.size() > 0){
                hv.setRemovedColumns(removedColumns);
            }
            hv.readWGA(inputs);
        }else if (command.equals("Combine P-Values")){
            FisherCombinedDialog fcd = new FisherCombinedDialog("Combine");
            fcd.pack();
            fcd.setVisible(true);
        }else if (command.equals("View Active Filters")){
            ActiveFiltersDialog afd = new ActiveFiltersDialog("Active Filters");
            afd.pack();
            afd.setVisible(true);
        }else if (command.equals("Remove")){
            if (removeChooser.getSelectedIndex() > 0){
                removeColumn((String)removeChooser.getSelectedItem(),removeChooser.getSelectedIndex());
            }
        }else if (command.equals("Plot")){
            PlotOptionDialog pod = new PlotOptionDialog(hv,this,"Plot Options",plinkTableModel);
            pod.pack();
            pod.setVisible(true);
        }else if (command.equals("Go to Selected Region")){
            gotoRegion();
        }
    }

    class FisherCombinedDialog extends JDialog implements ActionListener{
        static final long serialVersionUID = 3662196055616495550L;
        private JComboBox pval1, pval2, pval3, pval4, pval5;
        private Vector columnIndeces;
        FisherCombinedDialog(String title){
            super(hv,title);

            JPanel contents = new JPanel();
            contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
            //contents.setPreferredSize(new Dimension(150,200));

            if (removedColumns != null){
                if (removedColumns.size() > 0){
                    clearFilters();
                }
            }

            Vector doubleColumns = new Vector();
            columnIndeces = new Vector();
            doubleColumns.add("");
            for (int i = 1; i < plinkTableModel.getUnknownColumns().size(); i++){
                for (int j = 0; j < 50; j++){
                    if (table.getValueAt(j,i+2) != null){
                        if (table.getValueAt(j,i+2) instanceof Double){
                            doubleColumns.add(plinkTableModel.getUnknownColumns().get(i));
                            columnIndeces.add(new Integer(i-1));
                        }
                        break;
                    }else if (j == 49){
                        doubleColumns.add(plinkTableModel.getUnknownColumns().get(i));
                        columnIndeces.add(new Integer(i-1));
                    }
                }
            }

            JPanel pval1Panel = new JPanel();
            pval1Panel.add(new JLabel("Pval 1:"));
            pval1 = new JComboBox(doubleColumns);
            pval1Panel.add(pval1);
            JPanel pval2Panel = new JPanel();
            pval2Panel.add(new JLabel("Pval 2:"));
            pval2 = new JComboBox(doubleColumns);
            pval2Panel.add(pval2);
            JPanel pval3Panel = new JPanel();
            pval3Panel.add(new JLabel("Pval 3:"));
            pval3 = new JComboBox(doubleColumns);
            pval3Panel.add(pval3);
            JPanel pval4Panel = new JPanel();
            pval4Panel.add(new JLabel("Pval 4:"));
            pval4 = new JComboBox(doubleColumns);
            pval4Panel.add(pval4);
            JPanel pval5Panel = new JPanel();
            pval5Panel.add(new JLabel("Pval 5:"));
            pval5 = new JComboBox(doubleColumns);
            pval5Panel.add(pval5);
            JLabel fisherLabel = new JLabel("*Fisher's Combined");
            JPanel labelPanel = new JPanel();
            labelPanel.add(fisherLabel);

            JPanel choicePanel = new JPanel();
            JButton goButton = new JButton("Go");
            goButton.addActionListener(this);
            choicePanel.add(goButton);
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(this);
            choicePanel.add(cancelButton);

            contents.add(pval1Panel);
            contents.add(pval2Panel);
            contents.add(pval3Panel);
            contents.add(pval4Panel);
            contents.add(pval5Panel);
            contents.add(labelPanel);
            contents.add(choicePanel);

            setContentPane(contents);
            this.setLocation(this.getParent().getX() + 100,
                    this.getParent().getY() + 100);
            this.setModal(true);
            this.getRootPane().setDefaultButton(goButton);
        }

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if (command.equals("Cancel")){
                this.dispose();
            }else if (command.equals("Go")){
                int[] pCols = new int[5];
                Arrays.fill(pCols,-1);
                int numPvals = 0;

                if (pval1.getSelectedIndex() > 0){
                    numPvals++;
                    pCols[0] = ((Integer)columnIndeces.get(pval1.getSelectedIndex()-1)).intValue();
                }
                if (pval2.getSelectedIndex() > 0){
                    numPvals++;
                    pCols[1] = ((Integer)columnIndeces.get(pval2.getSelectedIndex()-1)).intValue();
                }
                if (pval3.getSelectedIndex() > 0){
                    numPvals++;
                    pCols[2] = ((Integer)columnIndeces.get(pval3.getSelectedIndex()-1)).intValue();
                }
                if (pval4.getSelectedIndex() > 0){
                    numPvals++;
                    pCols[3] = ((Integer)columnIndeces.get(pval4.getSelectedIndex()-1)).intValue();
                }
                if (pval5.getSelectedIndex() > 0){
                    numPvals++;
                    pCols[4] = ((Integer)columnIndeces.get(pval5.getSelectedIndex()-1)).intValue();
                }

                if (numPvals < 2){
                    JOptionPane.showMessageDialog(this,
                            "Please choose at least 2 pvalue columns.",
                            "Invalid selection",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String cols = "";
                for (int i = 0; i < pCols.length; i++){
                    if (pCols[i] > -1){
                        cols = cols + pCols[i] + " ";
                    }
                }

                String[] inputs = {null,null,null,null,cols,null,null};
                this.dispose();
                hv.readWGA(inputs);
            }
        }
    }

    class ActiveFiltersDialog extends JDialog implements ActionListener{
    static final long serialVersionUID = 1282427101754100571L;
        JPanel contents, chPanel;
        JCheckBox[] checks;
        JComboBox gChooser, sChooser;
        JTextField vField;

        ActiveFiltersDialog(String title){
            super(hv,title);
            contents = new JPanel();
            contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
            //contents.setPreferredSize(new Dimension(300,150));

            changeContents();
            this.setLocation(this.getParent().getX() + 100,
                    this.getParent().getY() + 100);
            this.setModal(true);

        }

        public void changeContents(){
            contents.removeAll();
            checks = new JCheckBox[genericFilters.size()];
            for (int i = 0; i < genericFilters.size(); i++){
                checks[i] = new JCheckBox((String)genericFilters.get(i));
                checks[i].setSelected(true);
            }

            chPanel = new JPanel();
            chPanel.setLayout(new BoxLayout(chPanel,BoxLayout.Y_AXIS));
            for (int i = 0; i < checks.length; i++){
                chPanel.add(checks[i]);
            }
            JScrollPane jsp = new JScrollPane(chPanel);

            JPanel filterPanel = new JPanel();
            filterPanel.add(new JLabel("Filter:"));
            gChooser = new JComboBox(plinkTableModel.getUnknownColumns());
            gChooser.setSelectedIndex(-1);
            filterPanel.add(gChooser);
            sChooser = new JComboBox(signs);
            sChooser.setSelectedIndex(-1);
            filterPanel.add(sChooser);
            vField = new JTextField(8);
            filterPanel.add(vField);
            JButton addFilter = new JButton("Add");
            addFilter.addActionListener(this);
            filterPanel.add(addFilter);


            JPanel choicePanel = new JPanel();
            JButton doneButton = new JButton("<html><b>Done</b>");
            doneButton.addActionListener(this);
            doneButton.setActionCommand("Done");
            choicePanel.add(doneButton);
            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(this);
            choicePanel.add(clearButton);

            contents.add(jsp);
            contents.add(filterPanel);
            contents.add(choicePanel);

            contents.repaint();
            this.setContentPane(contents);
        }

        public void actionPerformed(ActionEvent e){
            String command = e.getActionCommand();

            if (command.equals("Add")){
                String columnChoice, signChoice, val;

                if (gChooser.getSelectedIndex() > 0){
                    columnChoice = (String) gChooser.getSelectedItem();

                    if (sChooser.getSelectedIndex() > 0){
                        signChoice = (String) sChooser.getSelectedItem();

                        if (!(vField.getText().equals(""))){
                            val = vField.getText();
                            genericFilters.add(columnChoice + "\t" + signChoice + "\t" + val);
                            changeContents();
                            this.setSize(this.getWidth(),this.getHeight()+25);

                        }
                    }
                }
            }else if (command.equals("Clear")){
                genericFilters = new Vector();
                changeContents();
            }else if (command.equals("Done")){
                for (int i = checks.length-1; i >=0; i--){
                    if (!checks[i].isSelected()){
                        genericFilters.remove(i);
                    }
                }
                genericChooser.setSelectedIndex(0);
                genericChooser.updateUI();
                signChooser.setSelectedIndex(0);
                valueField.setText("");
                reSort();
                plinkTableModel.filterAll(chromChoice,startPos,endPos,genericFilters, "");
                countResults();
                this.dispose();
            }
        }

    }

    class PlinkCellRenderer extends DefaultTableCellRenderer {
        static final long serialVersionUID = -6099003102431307856L;
        public Component getTableCellRendererComponent
                (JTable table, Object value, boolean isSelected,
                 boolean hasFocus, int row, int column)
        {
            Component cell = super.getTableCellRendererComponent
                    (table, value, isSelected, hasFocus, row, column);
            String thisMarker = (String)table.getValueAt(row,1);
            cell.setForeground(Color.black);
            cell.setBackground(Color.white);

            if (isSelected){
                cell.setBackground(table.getSelectionBackground());
            }else{
                cell.setBackground(table.getBackground());
            }

            if (thisMarker.equals(hv.getChosenMarker())){
                cell.setBackground(Color.cyan);
            }

            if (table.getColumnName(column).startsWith("P_COMBINED")){
                if (((Double)table.getValueAt(row,column)).doubleValue() == 1.0E-16){
                    cell.setForeground(Color.red);
                }
            }
            return cell;
        }
    }
}

