package edu.mit.wi.haploview;


import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;


public class RegionDialog extends JDialog implements ActionListener, Constants {
    static final long serialVersionUID = 8970225298794816733L;
    private HaploView hv;

    private JComboBox panelChooser, phaseChooser;
    private JCheckBox gBrowse, annotate;
    private NumberTextField rangeInput;
    private String chrom, marker;
    private long markerPosition;
    private PlinkResultsPanel prp;

    public RegionDialog (HaploView h, String chr, String mark, PlinkResultsPanel prp, long position, String title) {
        super(h,title);

        hv = h;
        chrom = chr;
        markerPosition = position;
        marker = mark;
        this.prp = prp;

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel chooserPanel = new JPanel();
        chooserPanel.add(new JLabel("Release:"));
        phaseChooser = new JComboBox(RELEASE_NAMES);
        chooserPanel.add(phaseChooser);
        phaseChooser.setSelectedIndex(2);
        chooserPanel.add(new JLabel("Chr"+chr));
        panelChooser = new JComboBox(PANEL_NAMES);
        chooserPanel.add(new JLabel("Analysis Panel:"));
        chooserPanel.add(panelChooser);
        chooserPanel.add(new JLabel("Position: " + Long.toString(position / 1000)));
        chooserPanel.add(new JLabel("+/-"));
        rangeInput = new NumberTextField("100",6,false,false);
        chooserPanel.add(rangeInput);
        chooserPanel.add(new JLabel("kb"));

        JPanel gBrowsePanel = new JPanel();
        annotate = new JCheckBox("Annotate LD Plot?");
        annotate.setSelected(true);
        gBrowsePanel.add(annotate);
        gBrowse = new JCheckBox("Show HapMap info track?");
        gBrowse.setSelected(true);
        gBrowsePanel.add(gBrowse);

        JPanel choicePanel = new JPanel();
        JButton goButton = new JButton("Go");
        goButton.addActionListener(this);
        this.getRootPane().setDefaultButton(goButton);
        choicePanel.add(goButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        choicePanel.add(cancelButton);

        contents.add(chooserPanel);
        contents.add(gBrowsePanel);
        contents.add(choicePanel);
        setContentPane(contents);

        this.getRootPane().setDefaultButton(goButton);
        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("Cancel")) {
            this.dispose();
        }
        if (command.equals("Go")){
            if(rangeInput.getText().equals("")){
                JOptionPane.showMessageDialog(this,
                        "Please enter a range",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            String panel = (String) panelChooser.getSelectedItem();
            int range = Integer.parseInt(rangeInput.getText());
            long start = (markerPosition/1000)-range;
            if (start < 0){
                start = 0;
            }
            long end = (markerPosition/1000)+range;
            String gotoStart = Long.toString(start);
            String gotoEnd = Long.toString(end);
            String phase = (String)phaseChooser.getSelectedItem();
            prp.setChosenMarker(marker);

            if (gBrowse.isSelected()){
                Options.setShowGBrowse(true);
            }

            String[] returnStrings;
            returnStrings = new String[]{"Chr" + chrom + ":" + panel + ":" + gotoStart + ".." +
                    gotoEnd, panel, gotoStart, gotoEnd, chrom, phase, "txt"};
            this.dispose();
            hv.readGenotypes(returnStrings, HMPDL_FILE);
            Vector chipSNPs = new Vector(prp.getSNPs());
            if (Chromosome.getUnfilteredSize() > 0){
                if (annotate.isSelected()){
                    for (int i = 0; i < Chromosome.getSize(); i++){
                        if (chipSNPs.contains(Chromosome.getMarker(i).getName())){
                            Vector extras = new Vector();
                            for (int j = 1; j < prp.getOriginalColumns().size(); j++){
                                extras.add(prp.getOriginalColumns().get(j) + ": " + String.valueOf(prp.getValueAt(chipSNPs.indexOf(Chromosome.getMarker(i).getName()),j+2)));
                            }
                            Chromosome.getMarker(i).setExtra(extras);
                        }
                    }
                }else{
                    for (int i = 0; i < Chromosome.getSize(); i++){
                        if (chipSNPs.contains(Chromosome.getMarker(i).getName())){
                            Vector plink = new Vector();
                            plink.add("PLINK");
                            Chromosome.getMarker(i).setExtra(plink);
                        }
                    }
                }
            }
        }
    }
}
