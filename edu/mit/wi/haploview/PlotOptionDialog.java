package edu.mit.wi.haploview;


import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;


public class PlotOptionDialog extends JDialog implements ActionListener, Constants {
    private PlinkResultsPanel panel;

    private JComboBox columnChooser, plotChooser, signChooser1, signChooser2;
    private JLabel label1, label2;
    private NumberTextField sigThresh, sugThresh;
    private JTextField titleField;
    private String[] signs = {">","<"};

    public PlotOptionDialog (HaploView h, PlinkResultsPanel p, String title, Vector columns) {
       super(h,title);

        panel = p;

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        titleField = new JTextField(15);
        titlePanel.add(titleField);
        JPanel columnPanel = new JPanel();
        columnPanel.add(new JLabel("Column:"));
        columnChooser = new JComboBox(columns);
        columnPanel.add(columnChooser);
        columnPanel.add(new JLabel("Plot Type:"));
        plotChooser = new JComboBox(PLOT_TYPES);
        plotChooser.addActionListener(this);
        columnPanel.add(plotChooser);
        JPanel sugPanel = new JPanel();
        label1 = new JLabel("Threshold 1");
        sugPanel.add(label1);
        signChooser1 = new JComboBox(signs);
        sugPanel.add(signChooser1);
        sugThresh = new NumberTextField("",6,true,true);
        sugPanel.add(sugThresh);
        JPanel sigPanel = new JPanel();
        label2 = new JLabel("Threshold 2");
        sigPanel.add(label2);
        signChooser2 = new JComboBox(signs);
        sigPanel.add(signChooser2);
        sigThresh = new NumberTextField("",6,true,true);
        sigPanel.add(sigThresh);

        JPanel choicePanel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        this.getRootPane().setDefaultButton(okButton);
        choicePanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        choicePanel.add(cancelButton);

        contents.add(titlePanel);
        contents.add(columnPanel);
        contents.add(sugPanel);
        contents.add(sigPanel);
        contents.add(choicePanel);
        setContentPane(contents);

        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
        this.setResizable(false);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("Cancel")) {
            this.dispose();
        }else if (command.equals("OK")){
            if (columnChooser.getSelectedIndex() == 0){
                JOptionPane.showMessageDialog(this,
                        "Please select a column to plot.",
                        "Invalid value",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int column = columnChooser.getSelectedIndex() + 2;
            int plotType = plotChooser.getSelectedIndex();
            double suggestive, significant;

            if (sugThresh.getText().equals("")){
                suggestive = -1;
            }else{
                try{
                    suggestive = Double.parseDouble(sugThresh.getText());
                }catch (NumberFormatException nfe){
                    JOptionPane.showMessageDialog(this,
                            "Thresholds must be numerical.",
                            "Invalid value",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            if (sigThresh.getText().equals("")){
                significant = -1;
            }else{
                try{
                    significant = Double.parseDouble(sigThresh.getText());
                }catch(NumberFormatException nfe){
                    JOptionPane.showMessageDialog(this,
                            "Thresholds must be numerical.",
                            "Invalid value",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            int[] signs = new int[2];
            signs[0] = signChooser1.getSelectedIndex();
            signs[1] = signChooser2.getSelectedIndex();

            this.dispose();
            panel.makeChart(titleField.getText(),plotType,column,suggestive,significant,signs);
        }else if (e.getSource() instanceof JComboBox){
            if (plotChooser.getSelectedItem().equals("-log10")){
                label1.setText("Suggestive");
                label2.setText("Significant");
            }else{
                label1.setText("Threshold 1");
                label2.setText("Threshold 2");
            }
        }
    }
}
