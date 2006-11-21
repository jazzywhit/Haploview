package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.Individual;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Vector;
import java.util.Locale;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.text.NumberFormat;


/**
 * Custom Dialog showing Geno% by Individual ID
 *
 * this class is not thread safe (untested).
 * modified version of FilteredIndividualsDialog
 * @author David Bender
 */




public class IndividualDialog extends JDialog implements ActionListener, Constants {
    private BasicTableModel tableModel;
    private static NumberFormat nf = NumberFormat.getInstance(Locale.US);
    static {
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(1);
    }

    public IndividualDialog (HaploView h, String title) {
        super(h,title);

        JPanel contents = new JPanel();
        JTable table;

        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        Vector people = h.theData.getPedFile().getAllIndividuals();
        Vector excludedPeople = h.theData.getPedFile().getAxedPeople();

        Vector colNames = new Vector();
        colNames.add("FamilyID");
        colNames.add("IndividualID");
        colNames.add("Geno%");
        Vector data = new Vector();

        for (int i=0; i<excludedPeople.size(); i++){
            Vector tmpVecB = new Vector();
            Individual axedInd = (Individual)excludedPeople.get(i);
            tmpVecB.add(axedInd.getFamilyID());
            tmpVecB.add(axedInd.getIndividualID());
            tmpVecB.add(new Double(nf.format(axedInd.getGenoPC()*100)));
            data.add(tmpVecB);
        }

        for(int i=0;i<people.size();i++) {
            Vector tmpVec = new Vector();
            Individual currentInd = (Individual)people.get(i);
            tmpVec.add(currentInd.getFamilyID());
            tmpVec.add(currentInd.getIndividualID());
            tmpVec.add(new Double(nf.format(currentInd.getGenoPC()*100)));
            data.add(tmpVec);
        }

        tableModel = new BasicTableModel(colNames,data);
        TableSorter sorter = new TableSorter(tableModel);
        table = new JTable(sorter);
        sorter.setTableHeader(table.getTableHeader());

        IndividualCellRenderer renderer = new IndividualCellRenderer();
        try{
            table.setDefaultRenderer(Class.forName("java.lang.Double"), renderer);
            table.setDefaultRenderer(Class.forName("java.lang.Integer"), renderer);
            table.setDefaultRenderer(Class.forName("java.lang.Long"), renderer);
            table.setDefaultRenderer(Class.forName("java.lang.String"),renderer);
        }catch (Exception e){
        }
        table.getColumnModel().getColumn(2).setPreferredWidth(30);

        JScrollPane tableScroller = new JScrollPane(table);
        int tableHeight = (table.getRowHeight()+table.getRowMargin())*(table.getRowCount()+2);
        if (tableHeight > 300){
            tableScroller.setPreferredSize(new Dimension(400, 300));
        }else{
            tableScroller.setPreferredSize(new Dimension(400, tableHeight));
        }
        tableScroller.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));

        contents.add(tableScroller);

        JPanel buttonPanel = new JPanel();
        JButton exportButton = new JButton("Export to File");
        exportButton.addActionListener(this);
        JButton okButton = new JButton("Close");
        okButton.addActionListener(this);
        buttonPanel.add(exportButton);
        buttonPanel.add(okButton);
        contents.add(buttonPanel);
        setContentPane(contents);

        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
    }

    public IndividualDialog (HaploData hd){

        Vector people = hd.getPedFile().getAllIndividuals();
        Vector colNames = new Vector();
        colNames.add("FamilyID");
        colNames.add("IndividualID");
        colNames.add("Geno%");
        Vector data = new Vector();

        for(int i=0;i<people.size();i++) {
            Vector tmpVec = new Vector();
            Individual currentInd = (Individual)people.get(i);
            tmpVec.add(currentInd.getFamilyID());
            tmpVec.add(currentInd.getIndividualID());
            tmpVec.add(new Double(nf.format(currentInd.getGenoPC()*100)));
            data.add(tmpVec);
        }

        tableModel = new BasicTableModel(colNames, data);
    }

    public void printTable(File outfile) throws IOException {
        FileWriter checkWriter = null;
        Double dValue = new Double(0);
        double dVal;
        double minGeno = (1 - Options.getMissingThreshold())*100;
        if (outfile != null){
            checkWriter = new FileWriter(outfile);
        }

        int numCols = tableModel.getColumnCount();
        StringBuffer header = new StringBuffer();
        for (int i = 0; i < numCols; i++){
            header.append(tableModel.getColumnName(i)).append("\t");
        }
        header.append("\n");

        if (outfile != null){
            checkWriter.write(header.toString());
        }else{
            System.out.print(header.toString());
        }
        for (int i = 0; i < tableModel.getRowCount(); i++){
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < numCols; j++){
                sb.append(tableModel.getValueAt(i,j)).append("\t");
                if (j == 2){
                    dValue = new Double(tableModel.getValueAt(i,j).toString());
                    dVal = dValue.doubleValue();
                    if (dVal < minGeno){
                        sb.append("BAD");
                    }
                }
            }
            sb.append("\n");

            if (outfile != null){
                checkWriter.write(sb.toString());
            }else{
                System.out.print(sb.toString());
            }
        }
        if (outfile != null){
            checkWriter.close();
        }
    }

    class IndividualCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent
                (JTable table, Object value, boolean isSelected,
                 boolean hasFocus, int row, int column)
        {
            Component cell = super.getTableCellRendererComponent
                    (table, value, isSelected, hasFocus, row, column);
            boolean threshold = false;
            String thisColumnName = table.getColumnName(column);
            if (thisColumnName.equals("Geno%")){
                Double dval = new Double(value.toString());
                if (dval.doubleValue() < ((1 - Options.getMissingThreshold())*100)){
                    threshold = true;
                }
            }
            cell.setBackground(Color.white);
            cell.setForeground(Color.black);

            if (thisColumnName.equals("Geno%") && threshold){
                cell.setForeground(Color.red);
                threshold = false;
            }

            return cell;
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("Close")) {
            this.dispose();
        }else if (command.equals("Export to File")){
            HaploView.fc.setSelectedFile(new File(""));

            if (HaploView.fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
                File file = HaploView.fc.getSelectedFile();
                try{
                    printTable(file);
                }catch(IOException ioe){
                    JOptionPane.showMessageDialog(this,
                            ioe.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
