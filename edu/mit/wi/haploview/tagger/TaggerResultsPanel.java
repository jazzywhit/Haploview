package edu.mit.wi.haploview.tagger;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.util.Vector;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import edu.mit.wi.tagger.*;

public class TaggerResultsPanel extends JPanel implements ListSelectionListener, ActionListener{
    private JList tagList;
    private DefaultListModel tagListModel;

    private JList taggedList;
    private DefaultListModel taggedListModel;

    Vector tags;

    public TaggerResultsPanel() {
    }

    public void setTags(Vector t) {
        //super(new BorderLayout());
        removeAll();

        tags = t;

        tagListModel = new DefaultListModel();
        for(int i=0;i<tags.size();i++){
            TagSequence ts = (TagSequence)tags.get(i);
            tagListModel.addElement(((edu.mit.wi.tagger.SNP)ts.getTagSequence()).getName());
        }

        tagList = new JList(tagListModel);
        tagList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tagList.setSelectedIndex(0);
        tagList.addListSelectionListener(this);
        tagList.setVisibleRowCount(8);

        taggedListModel = new DefaultListModel();

        taggedList = new JList(taggedListModel);
        taggedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagList.setSelectedIndex(0);
        tagList.addListSelectionListener(this);
        taggedList.setVisibleRowCount(8);


        JScrollPane listScrollPane = new JScrollPane(tagList);
        listScrollPane.setMinimumSize(new Dimension(160,100));

        JScrollPane taggedListScrollPane = new JScrollPane(taggedList);
        taggedListScrollPane.setMinimumSize(new Dimension(160,100));

        //this.setMaximumSize(new Dimension(400,400));
        add(listScrollPane);//,BorderLayout.WEST);
        add(taggedListScrollPane);//, BorderLayout.EAST);

        refresh();

    }

    public void valueChanged(ListSelectionEvent e) {
        if(e.getValueIsAdjusting() == false) {
            refresh();
        }
    }

    private void refresh() {
        if (tags.size() > 0){
            taggedListModel.removeAllElements();
            int[] selected = tagList.getSelectedIndices();
            for(int i=0;i<selected.length;i++) {
                TagSequence ts = (TagSequence) tags.get(selected[i]);
                Vector tagged = ts.getTagged();
                for(int j=0;j<tagged.size();j++) {
                    taggedListModel.addElement(((edu.mit.wi.tagger.SNP)tagged.get(j)).getName());
                }

            }
        }
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("taggingdone")){
            TaggerConfigPanel tcp = (TaggerConfigPanel) e.getSource();
            setTags(tcp.getResults());
        }
    }
}
