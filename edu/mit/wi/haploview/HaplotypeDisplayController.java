package edu.mit.wi.haploview;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;


public class HaplotypeDisplayController extends JPanel {
    // amount of spacing between elements
    static final int BETWEEN = 4;

    NumberTextField minDisplayField;
    NumberTextField minThickField;
    NumberTextField minThinField;
    ButtonGroup alleleDisplayGroup;
    JButton goButton;
    Dimension fieldSize;

    HaplotypeDisplay parent;

    public HaplotypeDisplayController(HaplotypeDisplay parent){
        this.parent = parent;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JPanel hapPercentPanel = new JPanel();
        hapPercentPanel.add(new JLabel("Examine haplotypes above "));
            hapPercentPanel.add(minDisplayField =
                    new NumberTextField(String.valueOf(parent.displayThresh), 3, false));

        hapPercentPanel.add(new JLabel("%"));
        leftPanel.add(hapPercentPanel);

        JPanel thinPanel = new JPanel();
        thinPanel.add(new JLabel("Connect with thin lines if > "));
        thinPanel.add(minThinField =
                new NumberTextField(String.valueOf(parent.thinThresh), 3, false));
        thinPanel.add(new JLabel("%"));
        leftPanel.add(thinPanel);

        JPanel thickPanel = new JPanel();
        thickPanel.add(new JLabel("Connect with thick lines if > "));
        thickPanel.add(minThickField =
                new NumberTextField(String.valueOf(parent.thickThresh), 3, false));
        thickPanel.add(new JLabel("%"));
        leftPanel.add(thickPanel);

        //numericAlleles = new JCheckBox("Display alleles as numbers.");
        //add(numericAlleles);
        JLabel dispLab = new JLabel("Display alleles as:");
        rightPanel.add(dispLab);

        JRadioButton letBut = new JRadioButton("letters");
        letBut.setActionCommand("0");
        letBut.setSelected(true);
        rightPanel.add(letBut);
        JRadioButton numBut = new JRadioButton("numbers");
        numBut.setActionCommand("1");
        rightPanel.add(numBut);
        JRadioButton sqBut = new JRadioButton("colored squares");
        sqBut.setActionCommand("2");
        rightPanel.add(sqBut);
        alleleDisplayGroup = new ButtonGroup();
        alleleDisplayGroup.add(letBut);
        alleleDisplayGroup.add(numBut);
        alleleDisplayGroup.add(sqBut);

        JPanel optionPanel = new JPanel();
        optionPanel.add(leftPanel);
        optionPanel.add(rightPanel);
        add(optionPanel);

        goButton = new JButton("Go");
        goButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setDisplayThresh(Integer.parseInt(minDisplayField.getText()));
                setThinThresh(Integer.parseInt(minThinField.getText()));
                setThickThresh(Integer.parseInt(minThickField.getText()));
                setNumericAlls(alleleDisplayGroup.getSelection().getActionCommand());
                paintIt();
            }
        });
        add(goButton);

        fieldSize = minDisplayField.getPreferredSize();
    }

    private void setNumericAlls(String selection) {
        parent.alleleDisp = Integer.parseInt(selection);
    }


    public void setDisplayThresh(int amount){
        if (parent.displayThresh != amount){
            parent.adjustDisplay(amount);
        }
    }

    public void setThinThresh(int amount) {
        parent.thinThresh = amount;
    }

    public void setThickThresh(int amount) {
        parent.thickThresh = amount;
    }

    public void paintIt(){
        parent.repaint();
    }


    public Dimension getMaximumSize() {
        return new Dimension(super.getPreferredSize().width,
                fieldSize.height*3 + BETWEEN*2);
    }



}
