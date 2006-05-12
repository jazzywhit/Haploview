package edu.mit.wi.haploview;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;


public class ReadDataDialog extends JDialog
        implements ActionListener, DocumentListener, Constants {

    static final String HAPMAP_DATA = "Load HapMap data";
    static final String RAW_DATA = "Load genotypes (linkage format)";
    static final String PHASED_DATA = "Load phased haplotypes";
    static final String MARKER_DATA_EXT = ".info";
    static final String BROWSE_GENO = "browse for geno files";
    static final String BROWSE_INFO = "browse for info files";
    static final String BROWSE_ASSOC = "browse for association test files";

    int fileType;
    JTextField genoFileField, infoFileField, testFileField;
    JCheckBox doAssociation, doGB, xChrom;
    JRadioButton trioButton, ccButton, standardTDT, parenTDT;
    JButton browseAssocButton;
    NumberTextField maxComparisonDistField;
    NumberTextField missingCutoffField;
    private JLabel testFileLabel;

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
        this.setModal(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals(RAW_DATA)){
            load(PED_FILE);
        }else if (command.equals(PHASED_DATA)){
            load(HAPS_FILE);
        }else if (command.equals(HAPMAP_DATA)){
            load(HMP_FILE);
        }else if (command.equals(BROWSE_GENO)){
            browse(GENO_FILE);
        }else if (command.equals(BROWSE_INFO)){
            browse(INFO_FILE);
        }else if (command.equals(BROWSE_ASSOC)) {
            browse(ASSOC_FILE);
        }else if (command.equals("OK")){
            HaploView caller = (HaploView)this.getParent();
            if(missingCutoffField.getText().equals("")) {
                Options.setMissingThreshold(1);
            } else {
                double missingThreshold = (double)(Integer.parseInt(missingCutoffField.getText())) / 100;
                if(missingThreshold > 1) {
                    JOptionPane.showMessageDialog(caller,
                                    "Missing cutoff must be between 0 and 100",
                                    "Invalid value",
                                    JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Options.setMissingThreshold(missingThreshold);
            }

            if (doAssociation.isSelected()){
                if (trioButton.isSelected()){
                    Options.setAssocTest(ASSOC_TRIO);
                    if(standardTDT.isSelected()){
                        Options.setTdtType(TDT_STD);
                    }else if(parenTDT.isSelected()) {
                        Options.setTdtType(TDT_PAREN);
                    }
                } else {
                    Options.setAssocTest(ASSOC_CC);
                }
            }else{
                Options.setAssocTest(ASSOC_NONE);
            }

            if (xChrom.isSelected()){
                Chromosome.setDataChrom("chrx");
            }else {
                Chromosome.setDataChrom("none");
            }


            if (doGB.isSelected()){
                Options.setShowGBrowse(true);
            }else{
                Options.setShowGBrowse(false);
            }
            Options.setgBrowseLeft(0);
            Options.setgBrowseRight(0);

            if (maxComparisonDistField.getText().equals("")){
                Options.setMaxDistance(0);
            }else{
                Options.setMaxDistance(Integer.parseInt(maxComparisonDistField.getText()));
            }

            String[] returnStrings = {genoFileField.getText(), infoFileField.getText(), testFileField.getText()};
            if (returnStrings[1].equals("")) returnStrings[1] = null;
            if (returnStrings[2].equals("") || !doAssociation.isSelected()) returnStrings[2] = null;


            //if a dataset was previously loaded during this session, discard the display panes for it.
            caller.clearDisplays();
            this.dispose();
            caller.readGenotypes(returnStrings, fileType);
        }else if (command.equals("Cancel")){
            this.dispose();
        }else if (command.equals("association")){
            switchAssoc(doAssociation.isSelected());
        }else if(command.equals("tdt")){
            standardTDT.setEnabled(true);
            parenTDT.setEnabled(true);
        }else if(command.equals("ccButton")){
            standardTDT.setEnabled(false);
            parenTDT.setEnabled(false);
        }else if (command.equals("xChrom")){
            if (xChrom.isSelected()){
                parenTDT.setEnabled(false);
                standardTDT.setSelected(true);
            }else{
                parenTDT.setEnabled(true);
            }
        }
    }

    void browse(int browseType){
        String name;
        String markerInfoName = "";
        HaploView.fc.setSelectedFile(new File(""));
        int returned = HaploView.fc.showOpenDialog(this);
        if (returned != JFileChooser.APPROVE_OPTION) return;
        File file = HaploView.fc.getSelectedFile();

        if (browseType == GENO_FILE){
            name = file.getName();
            genoFileField.setText(file.getParent()+File.separator+name);

            if(infoFileField.getText().equals("") && fileType != HMP_FILE){
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

        }else if (browseType==INFO_FILE){
            markerInfoName = file.getName();
            infoFileField.setText(file.getParent()+File.separator+markerInfoName);
        }else if (browseType == ASSOC_FILE) {
            testFileField.setText(file.getParent() + File.separator + file.getName());
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
        infoFileField.getDocument().addDocumentListener(this);
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
        if (ft != HMP_FILE){
            filePanel.add(botFilePanel);
        }
        filePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        contents.add(filePanel);

        JPanel compDistPanel = new JPanel();
        maxComparisonDistField = new NumberTextField(String.valueOf(Options.getMaxDistance()/1000),6, false);
        compDistPanel.add(new JLabel("Ignore pairwise comparisons of markers >"));
        compDistPanel.add(maxComparisonDistField);
        compDistPanel.add(new JLabel("kb apart."));
        contents.add(compDistPanel);

        JPanel missingCutoffPanel = new JPanel();
        missingCutoffField = new NumberTextField(String.valueOf(Options.getMissingThreshold()*100),3, false);
        missingCutoffPanel.add(new JLabel("Exclude individuals with >"));
        missingCutoffPanel.add(missingCutoffField);
        missingCutoffPanel.add(new JLabel("% missing genotypes."));
        contents.add(missingCutoffPanel);

        doGB = new JCheckBox();//show gbrowse pic from hapmap website?
        doGB.setSelected(false);
        if (ft == HMP_FILE){
            JPanel gBrowsePanel = new JPanel();
            gBrowsePanel.add(doGB);
            gBrowsePanel.add(new JLabel("Download and show HapMap info track? (requires internet connection)"));
            contents.add(gBrowsePanel);
        }

        doAssociation = new JCheckBox("Do association test?");
        doAssociation.setSelected(false);
        doAssociation.setEnabled(false);
        doAssociation.setActionCommand("association");
        doAssociation.addActionListener(this);
        xChrom = new JCheckBox();
        xChrom.setSelected(false);
        xChrom.setActionCommand("xChrom");
        xChrom.addActionListener(this);
        trioButton = new JRadioButton("Family trio data", true);
        trioButton.setEnabled(false);
        trioButton.setActionCommand("tdt");
        trioButton.addActionListener(this);
        ccButton = new JRadioButton("Case/Control data");
        ccButton.setEnabled(false);
        ccButton.setActionCommand("ccButton");
        ccButton.addActionListener(this);
        ButtonGroup group = new ButtonGroup();
        group.add(trioButton);
        group.add(ccButton);

        standardTDT = new JRadioButton("Standard TDT", true);
        standardTDT.setEnabled(false);
        parenTDT = new JRadioButton("ParenTDT", true);
        parenTDT.setEnabled(false);
        ButtonGroup tdtGroup = new ButtonGroup();
        tdtGroup.add(standardTDT);
        tdtGroup.add(parenTDT);

        testFileField = new JTextField("",20);
        testFileField.setEnabled(false);
        testFileField.setBackground(this.getBackground());
        browseAssocButton = new JButton("Browse");
        browseAssocButton.setActionCommand(BROWSE_ASSOC);
        browseAssocButton.addActionListener(this);
        browseAssocButton.setEnabled(false);

        if (ft == PED_FILE){
            JPanel tdtOptsPanel = new JPanel();
            JPanel tdtTypePanel = new JPanel();
            JPanel tdtCheckBoxPanel = new JPanel();
            tdtCheckBoxPanel.add(xChrom);
            tdtCheckBoxPanel.add(new JLabel("X Chromosome"));
            tdtCheckBoxPanel.add(doAssociation);
            tdtOptsPanel.add(trioButton);
            tdtOptsPanel.add(ccButton);
            tdtTypePanel.add(standardTDT);
            tdtTypePanel.add(parenTDT);
            contents.add(tdtCheckBoxPanel);
            contents.add(tdtOptsPanel);
            contents.add(tdtTypePanel);

            JPanel assocFilePanel = new JPanel();
            testFileLabel = new JLabel("Test list file (optional)");
            testFileLabel.setEnabled(false);
            assocFilePanel.add(testFileLabel);
            assocFilePanel.add(testFileField);
            assocFilePanel.add(browseAssocButton);
            contents.add(assocFilePanel);
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

    public void insertUpdate(DocumentEvent e) {
        checkInfo(e);
    }

    public void removeUpdate(DocumentEvent e) {
        checkInfo(e);
    }

    public void changedUpdate(DocumentEvent e) {
        //not fired by plain text components
    }

    private void switchAssoc(boolean b){
        if(b){
            doAssociation.setEnabled(true);
            trioButton.setEnabled(true);
            ccButton.setEnabled(true);
            browseAssocButton.setEnabled(true);
            testFileField.setEnabled(true);
            testFileField.setBackground(Color.white);
            testFileLabel.setEnabled(true);
            standardTDT.setEnabled(true);
            parenTDT.setEnabled(true);
        }else{
            doAssociation.setSelected(false);
            trioButton.setEnabled(false);
            ccButton.setEnabled(false);
            browseAssocButton.setEnabled(false);
            testFileField.setEnabled(false);
            testFileField.setBackground(this.getBackground());
            testFileLabel.setEnabled(false);
            standardTDT.setEnabled(false);
            parenTDT.setEnabled(false);
        }
    }

    private void checkInfo(DocumentEvent e){
        //the text in the info field has changed. if it is empty, disable assoc testing
        if (infoFileField.getText().equals("")){
            switchAssoc(false);
            doAssociation.setEnabled(false);
        }else{
            doAssociation.setEnabled(true);
        }
    }
}

