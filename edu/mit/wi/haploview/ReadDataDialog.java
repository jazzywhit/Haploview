package edu.mit.wi.haploview;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;


public class ReadDataDialog extends JDialog implements ActionListener, Constants {

    static final String HAPMAP_DATA = "Browse HapMap data from HMP";
    static final String RAW_DATA = "Load genotypes (linkage format)";
    static final String PHASED_DATA = "Load phased haplotypes";
    static final String MARKER_DATA_EXT = ".info";
    static final String BROWSE_GENO = "browse for geno files";
    static final String BROWSE_INFO = "browse for info files";

    int fileType;
    JTextField genoFileField, infoFileField;
    JCheckBox doTDT;
    JRadioButton trioButton, ccButton;
    NumberTextField maxComparisonDistField;

    public ReadDataDialog(String title, HaploView h){
        super(h, title);

        JPanel contents = new JPanel();
        JButton hapmapButton = new JButton(HAPMAP_DATA);
        hapmapButton.addActionListener(this);
        JButton rawdataButton = new JButton(RAW_DATA);
        rawdataButton.addActionListener(this);
        JButton phaseddataButton = new JButton(PHASED_DATA);
        phaseddataButton.addActionListener(this);

        contents.add(Box.createRigidArea(new Dimension(10,10)));
        contents.add(rawdataButton);
        contents.add(Box.createRigidArea(new Dimension(10,10)));
        contents.add(phaseddataButton);
        contents.add(Box.createRigidArea(new Dimension(10,10)));
        contents.add(hapmapButton);
        contents.add(Box.createRigidArea(new Dimension(10,10)));

        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
        this.setContentPane(contents);
        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        //this.setLocation((getParent().getWidth() - this.getWidth()) / 2,
        //		   (getParent().getHeight() - this.getHeight()) / 2);
        this.setModal(true);
    }


    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals(RAW_DATA)){
            load(PED);
        }else if (command.equals(PHASED_DATA)){
            load(HAPS);
        }else if (command.equals(HAPMAP_DATA)){
            load(HMP);
        }else if (command.equals(BROWSE_GENO)){
            browse(GENO);
        }else if (command.equals(BROWSE_INFO)){
            browse(INFO);
        }else if (command.equals("OK")){
            HaploView caller = (HaploView)this.getParent();

            if (doTDT.isSelected()){
                if (trioButton.isSelected()){
                    Options.setAssocTest(ASSOC_TRIO);
                } else {
                    Options.setAssocTest(ASSOC_CC);
                }
            }else{
                Options.setAssocTest(ASSOC_NONE);
            }

            if (maxComparisonDistField.getText().equals("")){
                Options.setMaxDistance(0);
            }else{
                Options.setMaxDistance(Integer.parseInt(maxComparisonDistField.getText()));
            }
            
            String[] returnStrings = {genoFileField.getText(), infoFileField.getText()};
            if (returnStrings[1].equals("")) returnStrings[1] = null;

            //if a dataset was previously loaded during this session, discard the display panes for it.
            if (caller.dPrimeDisplay != null){
                caller.dPrimeDisplay.setVisible(false);
                caller.dPrimeDisplay = null;
            }
            if (caller.hapDisplay != null){
                caller.hapDisplay.setVisible(false);
                caller.hapDisplay = null;
            }
            caller.readGenotypes(returnStrings, fileType);

            this.dispose();
        }else if (command.equals("Cancel")){
            this.dispose();
        }else if (command.equals("tdt")){
            if(this.doTDT.isSelected()){
                trioButton.setEnabled(true);
                ccButton.setEnabled(true);
            }else{
                trioButton.setEnabled(false);
                ccButton.setEnabled(false);
            }
        }
    }


    void browse(int browseType){
        String name;
        String markerInfoName = "";
        HaploView h = (HaploView) this.getParent();
        h.fc.setSelectedFile(new File(""));
        int returned = h.fc.showOpenDialog(this);
        if (returned != JFileChooser.APPROVE_OPTION) return;
        File file = h.fc.getSelectedFile();

        if (browseType == GENO){
            name = file.getName();
            genoFileField.setText(file.getParent()+File.separator+name);

            if(infoFileField.getText().equals("")){
                //baseName should be everything but the final ".XXX" extension
                StringTokenizer st = new StringTokenizer(name,".");
                String baseName = st.nextToken();
                int numPieces = st.countTokens()-1;
                for (int i = 0; i < numPieces; i++){
                    baseName = baseName.concat(".").concat(st.nextToken());
                }

                //check for info file for original file sample.haps
                //either sample.haps.info or sample.info
                File maybeMarkers1 = new File(file.getParent(), name + MARKER_DATA_EXT);
                File maybeMarkers2 = new File(file.getParent(), baseName + MARKER_DATA_EXT);
                if (maybeMarkers1.exists()){
                    markerInfoName = maybeMarkers1.getName();
                }else if (maybeMarkers2.exists()){
                    markerInfoName = maybeMarkers2.getName();
                }else{
                    return;
                }
                infoFileField.setText(file.getParent()+File.separator+markerInfoName);
            }

        }else if (browseType==INFO){
            markerInfoName = file.getName();
            infoFileField.setText(file.getParent()+File.separator+markerInfoName);
        }
    }

    void load(int ft){
        fileType = ft;
        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));

        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        JPanel topFilePanel = new JPanel();
        JPanel botFilePanel = new JPanel();
        genoFileField = new JTextField("",20);

        //workaround for dumb Swing can't requestFocus until shown bug
        //this one seems to throw a harmless exception in certain versions of the linux JRE
        try{
            SwingUtilities.invokeLater( new Runnable(){
                public void run()
                {
                    genoFileField.requestFocus();
                }});
        }catch (RuntimeException re){
        }

        //this one seems to really fuck over the 1.3 version of the windows JRE
        //in short: Java sucks.
        /*genoFileField.dispatchEvent(
                new FocusEvent(
                        genoFileField,
                        FocusEvent.FOCUS_GAINED,
                        false
                )
        );*/

        infoFileField = new JTextField("",20);
        JButton browseGenoButton = new JButton("Browse");
        browseGenoButton.setActionCommand(BROWSE_GENO);
        browseGenoButton.addActionListener(this);
        JButton browseInfoButton = new JButton("Browse");
        browseInfoButton.setActionCommand(BROWSE_INFO);
        browseInfoButton.addActionListener(this);
        topFilePanel.add(new JLabel("Genotype file: "));
        topFilePanel.add(genoFileField);
        topFilePanel.add(browseGenoButton);
        botFilePanel.add(new JLabel("Locus information file: "));
        botFilePanel.add(infoFileField);
        botFilePanel.add(browseInfoButton);
        filePanel.add(topFilePanel);
        if (ft != HMP){
            filePanel.add(botFilePanel);
        }
        filePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        contents.add(filePanel);

        JPanel prefsPanel = new JPanel();
        maxComparisonDistField = new NumberTextField("500",4, false);
        prefsPanel.add(new JLabel("Ignore pairwise comparisons of markers >"));
        prefsPanel.add(maxComparisonDistField);
        prefsPanel.add(new JLabel("kb apart."));
        contents.add(prefsPanel);

        doTDT = new JCheckBox();//"Do association test?");
        doTDT.setSelected(false);
        doTDT.setActionCommand("tdt");
        doTDT.addActionListener(this);
        trioButton = new JRadioButton("Family trio data", true);
        trioButton.setEnabled(false);
        ccButton = new JRadioButton("Case/Control data");
        ccButton.setEnabled(false);
        ButtonGroup group = new ButtonGroup();
        group.add(trioButton);
        group.add(ccButton);
        if (ft == PED){
            JPanel tdtOptsPanel = new JPanel();
            JPanel tdtCheckBoxPanel = new JPanel();
            tdtCheckBoxPanel.add(doTDT);
            tdtCheckBoxPanel.add(new JLabel("Do association test?"));
            tdtOptsPanel.add(trioButton);
            tdtOptsPanel.add(ccButton);
            contents.add(tdtCheckBoxPanel);
            contents.add(tdtOptsPanel);
        }

        JPanel choicePanel = new JPanel();
        JButton okButton = new JButton("OK");
        this.getRootPane().setDefaultButton(okButton);
        okButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        choicePanel.add(okButton);
        choicePanel.add(cancelButton);
        contents.add(choicePanel);

        this.setContentPane(contents);
        this.pack();
    }
}

