package edu.mit.wi.haploview;


import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class PlotOptionDialog extends JDialog implements ActionListener, Constants {
    private PlinkResultsPanel panel;

    private JComboBox plotChooser;
    private int column;
    private NumberTextField sigThresh,sugThresh;

    public PlotOptionDialog (HaploView h, PlinkResultsPanel p, String title, int col) {
       super(h,title);

        panel = p;
        column = col;

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JPanel plotPanel = new JPanel();
        plotPanel.add(new JLabel("Plot Type:"));
        plotChooser = new JComboBox(PLOT_TYPES);
        plotPanel.add(plotChooser);
        JPanel sugPanel = new JPanel();
        sugPanel.add(new JLabel("Suggestive Threshold:"));
        sugThresh = new NumberTextField("3",6,true);
        sugPanel.add(sugThresh);
        JPanel sigPanel = new JPanel();
        sigPanel.add(new JLabel("Significant Threshold:"));
        sigThresh = new NumberTextField("5",6,true);
        sigPanel.add(sigThresh);

        JPanel choicePanel = new JPanel();
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        this.getRootPane().setDefaultButton(okButton);
        choicePanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        choicePanel.add(cancelButton);

        contents.add(plotPanel);
        contents.add(sugPanel);
        contents.add(sigPanel);
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
        if (command.equals("OK")){
            int plotType = plotChooser.getSelectedIndex();
            double suggestive, significant;

            if (sugThresh.getText().equals("")){
                suggestive = -1;
            }else{
                suggestive = Double.parseDouble(sugThresh.getText());
            }
            if (sigThresh.getText().equals("")){
                significant = -1;
            }else{
                significant = Double.parseDouble(sigThresh.getText());
            }

            this.dispose();
            panel.makeChart(plotType,column,significant,suggestive);
        }
    }
}
