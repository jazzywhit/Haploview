package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.Individual;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Vector;


public class FilteredIndividualsDialog extends JDialog implements ActionListener, Constants{

    public FilteredIndividualsDialog(HaploView h, String title) {
        super(h,title);

        FilteredIndividualsTableModel tableModel;
        JTable table;
        JPanel contents = new JPanel();

        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        Vector axedPeople = h.theData.getPedFile().getAxedPeople();

        String[] columnNames = {"FamilyID", "IndividualID", "Reason"};
        String[][] data = new String[axedPeople.size()][3];

        for(int i=0;i<axedPeople.size();i++) {
            Individual currentInd = (Individual) axedPeople.get(i);
            data[i][0] = currentInd.getFamilyID();
            data[i][1] = currentInd.getIndividualID();
            data[i][2] = currentInd.getReasonImAxed();
        }

        tableModel = new FilteredIndividualsTableModel(columnNames,data);
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);

        JScrollPane tableScroller = new JScrollPane(table);
        int tableHeight = (table.getRowHeight()+table.getRowMargin())*(table.getRowCount()+2);
        if (tableHeight > 300){
            tableScroller.setPreferredSize(new Dimension(400, 300));
        }else{
            tableScroller.setPreferredSize(new Dimension(400, tableHeight));
        }
        tableScroller.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));

        contents.add(tableScroller);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contents.add(okButton);
        setContentPane(contents);

        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("OK")) {
            this.dispose();
        }
    }

    //the only reason we implement this is so that the cells arent editable (default for AbstractTableModel)
    class FilteredIndividualsTableModel extends AbstractTableModel {
        private String[] columnNames;
        private String[][] data;

        public FilteredIndividualsTableModel(String[] names, String[][] d ) {
            columnNames = names;
            data = d;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public Class getColumnClass(int c) {
            return String.class;
        }

    }


}



