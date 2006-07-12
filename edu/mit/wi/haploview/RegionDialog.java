package edu.mit.wi.haploview;


import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class RegionDialog extends JDialog implements ActionListener, Constants {
    private HaploView hv;

    private JComboBox popChooser;
    private NumberTextField rangeInput;
    private String chrom;
    private long markerPosition;

    public RegionDialog (HaploView h, String chr, long position, String title) {
        super(h,title);

        hv = h;
        chrom = chr;
        markerPosition = position;

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel chooserPanel = new JPanel();
        chooserPanel.add(new JLabel("Chr"+chr));
        popChooser = new JComboBox(POP_NAMES);
        popChooser.setSelectedIndex(-1);
        chooserPanel.add(new JLabel("Pop:"));
        chooserPanel.add(popChooser);
        chooserPanel.add(new JLabel("Position: " + new Long(position/1000).toString()));
        chooserPanel.add(new JLabel("+/-"));
        rangeInput = new NumberTextField("100",6,false);
        chooserPanel.add(rangeInput);
        chooserPanel.add(new JLabel("kb"));

        JPanel choicePanel = new JPanel();
        JButton goButton = new JButton("GO");
        goButton.addActionListener(this);
        this.getRootPane().setDefaultButton(goButton);
        choicePanel.add(goButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        choicePanel.add(cancelButton);

        contents.add(chooserPanel);
        contents.add(choicePanel);
        setContentPane(contents);

        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("Cancel")) {
            this.dispose();
        }
        if (command.equals("GO")){
            if (popChooser.getSelectedIndex() == -1){
                JOptionPane.showMessageDialog(this,
                        "Please select a population",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }else if(rangeInput.getText().equals("")){
                JOptionPane.showMessageDialog(this,
                        "Please enter a range",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            String pop = (String)popChooser.getSelectedItem();
            int range = Integer.parseInt(rangeInput.getText());
            long start = (markerPosition/1000)-range;
            long end = (markerPosition/1000)+range;
            String gotoStart = new Long(start).toString();
            String gotoEnd = new Long(end).toString();

            String[] returnStrings;
            returnStrings = new String[]{"Chr " + chrom + ":" + pop + ":" + gotoStart + ".." +
                    gotoEnd, pop, gotoStart, gotoEnd, chrom};
            this.dispose();
            hv.readGenotypes(returnStrings, PHASEDHMPDL_FILE, true);
        }
    }
}
