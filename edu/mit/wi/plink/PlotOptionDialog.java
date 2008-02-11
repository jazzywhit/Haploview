package edu.mit.wi.plink;

import edu.mit.wi.haploview.*;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Vector;
import java.io.File;

public class PlotOptionDialog extends JDialog implements ActionListener, Constants {
    static final long serialVersionUID = -6028138645962418263L;
    private JComboBox yColumnChooser, xColumnChooser, yPlotChooser, xPlotChooser, signChooser1, signChooser2, thresholdChooser1, thresholdChooser2, dotChooser, colorKeyChooser;
    private JLabel label1, label2;
    private NumberTextField sigThresh, sugThresh, widthField, heightField;
    private JTextField titleField;
    private JCheckBox showGrid, exportSVG;
    private JButton svgButton;
    private String[] signs = {">","<"};
    private String[] thresholds = {"Y-Axis","X-Axis"};
    private String[] dotSizes = {"Small","Medium","Large"};

    private PlinkTableModel theModel;
    private PlinkResultsPanel thePanel;

    private File svgFile;

    public PlotOptionDialog (HaploView hv, PlinkResultsPanel panel, String title, PlinkTableModel model) {
        super(hv,title);
        theModel = model;
        thePanel = panel;

        Vector columns = new Vector(model.getUnknownColumns());
        columns.add("Index");

        Vector xCols = new Vector();
        if (Options.getSNPBased()){
            xCols.add("Chromosomes");
            for (int i = 1; i < columns.size(); i++){
                xCols.add(columns.get(i));
            }
        }else{
            for (int i = 0; i < columns.size(); i++){
                xCols.add(columns.get(i));
            }
        }

        JPanel contents = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0,0,10,0);
        c.gridwidth = 2;

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        titleField = new JTextField(25);
        titlePanel.add(titleField);
        contents.add(titlePanel,c);
        c.insets = new Insets(0,0,0,0);
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 1;
        JPanel xPanel = new JPanel();
        xPanel.add(new JLabel("X-Axis:"));
        xColumnChooser = new JComboBox(xCols);
        xColumnChooser.addActionListener(this);
        xPanel.add(xColumnChooser);
        contents.add(xPanel, c);
        c.gridx = 1;
        JPanel xScalePanel = new JPanel();
        xScalePanel.add(new JLabel("Scale:"));
        xPlotChooser = new JComboBox(PLOT_TYPES);
        xScalePanel.add(xPlotChooser);
        contents.add(xScalePanel,c);
        c.gridy = 2;
        c.gridx = 0;
        JPanel yPanel = new JPanel();
        yPanel.add(new JLabel("Y-Axis:"));
        yColumnChooser = new JComboBox(columns);
        yColumnChooser.addActionListener(this);
        yPanel.add(yColumnChooser);
        contents.add(yPanel,c);
        c.gridx = 1;
        JPanel yScalePanel = new JPanel();
        yScalePanel.add(new JLabel("Scale:"));
        yPlotChooser = new JComboBox(PLOT_TYPES);
        yPlotChooser.addActionListener(this);
        yScalePanel.add(yPlotChooser);
        contents.add(yScalePanel,c);
        c.gridwidth = 2;
        c.gridy = 3;
        c.gridx = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(5,0,0,0);
        JPanel sugPanel = new JPanel();
        label1 = new JLabel("Threshold 1 (Blue Line)");
        sugPanel.add(label1);
        signChooser1 = new JComboBox(signs);
        sugPanel.add(signChooser1);
        sugThresh = new NumberTextField("",6,true,true);
        sugPanel.add(sugThresh);
        thresholdChooser1 = new JComboBox(thresholds);
        sugPanel.add(thresholdChooser1);
        contents.add(sugPanel,c);
        c.insets = new Insets(0,0,5,0);
        c.gridy = 4;
        JPanel sigPanel = new JPanel();
        label2 = new JLabel("Threshold 2 (Red Line) ");
        sigPanel.add(label2);
        signChooser2 = new JComboBox(signs);
        sigPanel.add(signChooser2);
        sigThresh = new NumberTextField("",6,true,true);
        sigPanel.add(sigThresh);
        thresholdChooser2 = new JComboBox(thresholds);
        sigPanel.add(thresholdChooser2);
        contents.add(sigPanel,c);
        c.gridy = 5;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0,0,0,0);
        JPanel dotPanel = new JPanel();
        dotPanel.add(new JLabel("Data Point Size:"));
        dotChooser = new JComboBox(dotSizes);
        dotPanel.add(dotChooser);
        dotPanel.add(new JLabel("Color Key:"));
        colorKeyChooser = new JComboBox(theModel.getUnknownColumns());
        dotPanel.add(colorKeyChooser);
        contents.add(dotPanel,c);
        c.gridy = 6;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        JPanel gridPanel = new JPanel();
        showGrid = new JCheckBox("Show Gridlines",true);
        gridPanel.add(showGrid);
        gridPanel.add(new JLabel("Width:"));
        widthField = new NumberTextField("750",5,false,false);
        gridPanel.add(widthField);
        gridPanel.add(new JLabel("Height:"));
        heightField = new NumberTextField("300",5,false,false);
        gridPanel.add(heightField);
        contents.add(gridPanel,c);
        c.gridy = 7;
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 2;
        JPanel svgPanel = new JPanel();
        exportSVG = new JCheckBox("Export to SVG:");
        exportSVG.setActionCommand("svg");
        exportSVG.addActionListener(this);
        exportSVG.setSelected(false);
        svgPanel.add(exportSVG);
        svgButton = new JButton("Browse");
        svgButton.addActionListener(this);
        svgButton.setEnabled(false);
        svgPanel.add(svgButton);
        contents.add(svgPanel,c);


        if (Options.getSNPBased()){
            yColumnChooser.setSize(xColumnChooser.getSize());
            xPlotChooser.setEnabled(false);
            thresholdChooser1.setEnabled(false);
            thresholdChooser2.setEnabled(false);
            colorKeyChooser.setEnabled(false);
        }

        JPanel choicePanel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        this.getRootPane().setDefaultButton(okButton);
        choicePanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        choicePanel.add(cancelButton);
        c.gridy = 8;
        c.insets = new Insets(10,0,0,0);
        contents.add(choicePanel,c);

        setContentPane(contents);

        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
        this.setResizable(false);
        this.getRootPane().setDefaultButton(okButton);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("Cancel")) {
            this.dispose();
        }else if (command.equals("OK")){
            if (xColumnChooser.getSelectedIndex() == 0 && !Options.getSNPBased()){
                JOptionPane.showMessageDialog(this,
                        "Please select a column to plot on the X-Axis.",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (yColumnChooser.getSelectedIndex() == 0){
                JOptionPane.showMessageDialog(this,
                        "Please select a column to plot on the Y-Axis.",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int yColumn = yColumnChooser.getSelectedIndex()-1; //accounts for ""
            int yPlotType = yPlotChooser.getSelectedIndex();
            int xColumn = xColumnChooser.getSelectedIndex();
            int xPlotType = xPlotChooser.getSelectedIndex();
            int colorColumn = colorKeyChooser.getSelectedIndex()-1;
            if (Options.getSNPBased()){
                yColumn += 3; //accounts for 3 known columns (chrom,marker,position)
                colorColumn += 3;
                xColumn += 2;
            }else{
                xColumn -= 1;
                if (theModel.getFIDColumn() != -1){
                    yColumn += 1;
                    xColumn += 1;
                    colorColumn += 1;
                }
                if (theModel.getIIDColumn() != -1){
                    yColumn += 1;
                    xColumn += 1;
                    colorColumn += 1;
                }
            }
            if ((xColumnChooser.getSelectedItem().equals("Index") && yColumnChooser.getSelectedItem().equals("Index")) ||
                    (xColumnChooser.getSelectedItem().equals("Chromosomes") && yColumnChooser.getSelectedItem().equals("Index"))){
                JOptionPane.showMessageDialog(this,
                        "You must have at least one explicit axis.",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }else{
                if (xColumnChooser.getSelectedItem().equals("Index")){
                    xColumn = -1;
                }else if (yColumnChooser.getSelectedItem().equals("Index")){
                    yColumn = -1;
                }
            }
            if (colorKeyChooser.getSelectedIndex() < 1){
                colorColumn = -1;
            }

            double suggestive, significant;
            boolean useSug, useSig;
            try{
                if (sugThresh.getText().equals("")){
                    useSug = false;
                    suggestive = -1;
                }else{
                    suggestive = Double.parseDouble(sugThresh.getText());
                    useSug = true;
                }

                if (sigThresh.getText().equals("")){
                    useSig = false;
                    significant = -1;
                }else{
                    significant = Double.parseDouble(sigThresh.getText());
                    useSig = true;
                }
            }catch(NumberFormatException nfe){
                JOptionPane.showMessageDialog(this,
                        "Thresholds must be numerical.",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int[] signs = new int[2];
            signs[0] = signChooser1.getSelectedIndex();
            signs[1] = signChooser2.getSelectedIndex();
            int[] thresholds = new int[2];
            thresholds[0] = thresholdChooser1.getSelectedIndex();
            thresholds[1] = thresholdChooser2.getSelectedIndex();
            int dotSize = 2 + (dotChooser.getSelectedIndex()*2);
            int width, height;
            if (!widthField.getText().equals("")){
                width = Integer.parseInt(widthField.getText());
            }else{
                width = 750;
            }
            if (!heightField.getText().equals("")){
                height = Integer.parseInt(heightField.getText());
            }else{
                height = 300;
            }

            if (!exportSVG.isSelected()){
                svgFile = null;
            }else{
                if (svgFile == null){
                   JOptionPane.showMessageDialog(this,
                        "Please specify a filename for the SVG export.",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
                }
            }

            this.dispose();
            thePanel.makeChart(titleField.getText(),yPlotType,yColumn,xPlotType,xColumn,suggestive,significant,useSug,useSig,signs,thresholds,dotSize,colorColumn,showGrid.isSelected(),svgFile,width,height);
        }else if (e.getSource() instanceof JComboBox){
            if (xColumnChooser.getSelectedItem().equals("Chromosomes")){
                xPlotChooser.setSelectedIndex(0);
                xPlotChooser.setEnabled(false);
                thresholdChooser1.setSelectedIndex(0);
                thresholdChooser1.setEnabled(false);
                thresholdChooser2.setSelectedIndex(0);
                thresholdChooser2.setEnabled(false);
                colorKeyChooser.setSelectedIndex(0);
                colorKeyChooser.setEnabled(false);
            }else if (xColumnChooser.getSelectedItem().equals("Index")){
                xPlotChooser.setSelectedIndex(0);
                xPlotChooser.setEnabled(false);
                thresholdChooser1.setEnabled(true);
                thresholdChooser2.setEnabled(true);
                colorKeyChooser.setEnabled(true);
            }else{
                xPlotChooser.setEnabled(true);
                thresholdChooser1.setEnabled(true);
                thresholdChooser2.setEnabled(true);
                colorKeyChooser.setEnabled(true);
            }

            if (yColumnChooser.getSelectedItem().equals("Index")){
                yPlotChooser.setSelectedIndex(0);
                yPlotChooser.setEnabled(false);
            }else{
                yPlotChooser.setEnabled(true);
            }
            if (yPlotChooser.getSelectedItem().equals("-log10")){
                label1.setText("Suggestive (Blue Line)");
                label2.setText("Significant (Red Line) ");
            }else{
                label1.setText("Threshold 1 (Blue Line)");
                label2.setText("Threshold 2 (Red Line) ");
            }
        }else if (command.equals("Browse")){
            HaploView.fc.setSelectedFile(new File(""));
            if (HaploView.fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
                svgFile = HaploView.fc.getSelectedFile();
            }
        }else if (command.equals("svg")){
            if (exportSVG.isSelected()){
                svgButton.setEnabled(true);
            }else{
                svgButton.setEnabled(false);
            }
        }
    }
}