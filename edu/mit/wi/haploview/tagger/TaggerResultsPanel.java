package edu.mit.wi.haploview.tagger;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.Vector;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import edu.mit.wi.tagger.*;
import edu.mit.wi.haploview.Chromosome;
import edu.mit.wi.haploview.BasicTableModel;

public class TaggerResultsPanel extends JPanel implements ListSelectionListener, ActionListener{
    private JList tagList;
    private JList taggedList;
    private JTable markerTable;

    private Vector tags;
    private Vector forceIncluded;

    public TaggerResultsPanel() {
    }

    public void setTags(TaggerController t) {
        removeAll();
        setLayout(new BoxLayout(this,BoxLayout.X_AXIS));

        Vector colNames = new Vector();
        Vector tableData = new Vector();

        colNames.add("Marker");
        colNames.add("Tag");
        colNames.add("r\u00b2 w/tag");


        for (int i = 0; i < Chromosome.getSize(); i++){
            tableData.add(t.getMarkerTagDetails(i));
        }

        BasicTableModel btm = new BasicTableModel(colNames, tableData);
        markerTable = new JTable(btm);
        GreyedOutRenderer gor = new GreyedOutRenderer();
        markerTable.setDefaultRenderer(String.class,gor);

        JScrollPane tableScroller = new JScrollPane(markerTable);
        JPanel markerPanel = new JPanel();
        markerPanel.add(tableScroller);

        tags = t.getResults();
        forceIncluded = new Vector();
        Vector fi = t.getForceIncludeds();
        for (int i = 0; i < fi.size(); i++){
            forceIncluded.add(((edu.mit.wi.tagger.SNP)fi.get(i)).getName());
        }

        DefaultListModel tagListModel = new DefaultListModel();
        for(int i=0;i<tags.size();i++){
            TagSequence ts = (TagSequence)tags.get(i);
            tagListModel.addElement(ts.getName());
        }

        tagList = new JList(tagListModel);
        tagList.setCellRenderer(new TagListRenderer());
        tagList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tagList.setSelectedIndex(0);
        tagList.addListSelectionListener(this);
        tagList.setPreferredSize(new Dimension(tagList.getPreferredSize().width + 10,
                tagList.getPreferredSize().height));
        JScrollPane listScrollPane = new JScrollPane(tagList);
        JPanel topListPanel = new JPanel();
        topListPanel.setLayout(new BoxLayout(topListPanel,BoxLayout.Y_AXIS));
        JLabel tagLabel = new JLabel("Tags");
        Font defaultFont = tagLabel.getFont();
        //make the word 'tags' nice and big.
        tagLabel.setFont(new Font(defaultFont.getName(),Font.BOLD,(int)(defaultFont.getSize()*1.5)));
        topListPanel.add(tagLabel);
        topListPanel.add(listScrollPane);
        if (forceIncluded.size() > 0){
            //let them know why some are in bold 
            topListPanel.add(new JLabel("(forced-in markers shown in bold)"));
        }

        DefaultListModel taggedListModel = new DefaultListModel();
        taggedList = new JList(taggedListModel);
        taggedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagList.setSelectedIndex(0);
        tagList.addListSelectionListener(this);
        JScrollPane taggedListScrollPane = new JScrollPane(taggedList);
        JPanel bottomListPanel = new JPanel();
        bottomListPanel.setLayout(new BoxLayout(bottomListPanel, BoxLayout.Y_AXIS));
        bottomListPanel.add(new JLabel("Markers Tagged by Current Selection"));
        bottomListPanel.add(taggedListScrollPane);

        JPanel listsPanel = new JPanel();
        listsPanel.setLayout(new BoxLayout(listsPanel,BoxLayout.Y_AXIS));
        listsPanel.add(topListPanel);
        listsPanel.add(Box.createRigidArea(new Dimension(0,10)));
        listsPanel.add(bottomListPanel);

        String resString = "Tagged " + t.getTaggedSoFar() + " SNPS using " + t.getResults().size() + " tags.";
        JLabel resultsLabel = new JLabel(resString);
        listsPanel.add(Box.createRigidArea(new Dimension(0,10)));
        listsPanel.add(resultsLabel);
        if(t.getUntaggableCount() > 0) {
            String cantTag = "Unable to tag " + t.getUntaggableCount() + " SNPS (shown in red).";
            JLabel cantTagLabel = new JLabel(cantTag);
            listsPanel.add(cantTagLabel);
        }


        JPanel bufferPanel = new JPanel();
        bufferPanel.add(listsPanel);

        add(bufferPanel);
        add(new JSeparator(JSeparator.VERTICAL));
        add(markerPanel);
        refresh();
    }

    public void valueChanged(ListSelectionEvent e) {
        if(e.getValueIsAdjusting() == false) {
            refresh();
        }
    }

    private void refresh() {
        if (tags.size() > 0){
            ((DefaultListModel)taggedList.getModel()).removeAllElements();
            int[] selected = tagList.getSelectedIndices();
            for(int i=0;i<selected.length;i++) {
                TagSequence ts = (TagSequence) tags.get(selected[i]);
                Vector tagged = ts.getTagged();
                for(int j=0;j<tagged.size();j++) {
                    ((DefaultListModel)taggedList.getModel()).addElement(((edu.mit.wi.tagger.SNP)tagged.get(j)).getName());
                }

            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("taggingdone")){
            TaggerConfigPanel tcp = (TaggerConfigPanel) e.getSource();
            setTags(tcp.getTaggerController());
        }
    }

    class GreyedOutRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent
                (JTable table, Object value, boolean isSelected,
                 boolean hasFocus, int row, int column)
        {
            Component cell = super.getTableCellRendererComponent
                    (table, value, isSelected, hasFocus, row, column);

            if (column == 0 && table.getValueAt(row,1).equals("")){
                cell.setForeground(Color.lightGray);
            }else if ((column == 0 || column == 1) && table.getValueAt(row,1).equals("Untaggable")){
                cell.setForeground(Color.red);
            }else if (!isSelected){
                cell.setForeground(Color.black);
            }

            return cell;
        }
    }

    class TagListRenderer extends DefaultListCellRenderer{
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
            Component cell = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

            Font defaultFont = list.getFont();
            if (forceIncluded.contains(value)){
                cell.setFont(new Font(defaultFont.getName(),Font.BOLD,defaultFont.getSize()));
            }else{
                cell.setFont(defaultFont);
            }

            return cell;
        }
    }
}
