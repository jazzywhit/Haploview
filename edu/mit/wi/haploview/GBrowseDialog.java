package edu.mit.wi.haploview;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class GBrowseDialog extends JDialog implements ActionListener, Constants{

    JComboBox cbox;
    JTextField minField, maxField;
    HaploView hv;

    public GBrowseDialog(HaploView h, String title){
        super (h,title);

        hv = h;

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));

        JPanel chromPanel = new JPanel();
        chromPanel.add(new JLabel("Chromosome"));
        String[] c = {"1", "2", "3", "4", "5", "6", "7",
                    "8", "9", "10", "11", "12", "13", "14",
                    "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
        cbox = new JComboBox(c);
        if (Chromosome.getDataChrom() != null){
            String which = Chromosome.getDataChrom().substring(3);
            for (int i = 0; i < c.length; i++){
                if (which.equals(c[i])){
                    cbox.setSelectedIndex(i);
                }
            }
        }
        chromPanel.add(cbox);
        contents.add(chromPanel);

        JPanel boundsPanel = new JPanel();
        boundsPanel.add(new JLabel("from"));
        minField = new JTextField(Long.toString(Chromosome.getMarker(0).getPosition()), 9);
        boundsPanel.add(minField);
        boundsPanel.add(new JLabel("  to"));
        maxField = new JTextField(Long.toString(Chromosome.getMarker(Chromosome.getSize()-1).getPosition()), 9);
        boundsPanel.add(maxField);
        contents.add(boundsPanel);

        JPanel buttonPanel = new JPanel();
        JButton cancelBut = new JButton("Cancel");
        cancelBut.addActionListener(this);
        JButton okBut = new JButton("OK");
        okBut.addActionListener(this);
        buttonPanel.add(okBut);
        buttonPanel.add(cancelBut);
        contents.add(buttonPanel);

        contents.add(new JLabel("(Note: this option requires an internet connection)"));

        this.setContentPane(contents);
        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("OK")){
            try{
                long minpos = Long.parseLong(minField.getText());
                long maxpos = Long.parseLong(maxField.getText());
                if (maxpos <= minpos){
                    throw new HaploViewException("Boundary positions out of order.");
                }
                Chromosome.setDataChrom("chr"+cbox.getSelectedItem());
                Options.setgBrowseLeft(minpos);
                Options.setgBrowseRight(maxpos);
                Options.setShowGBrowse(true);
                this.dispose();
                hv.dPrimeDisplay.computePreferredSize();
            }catch (NumberFormatException nfe){
                JOptionPane.showMessageDialog(this,
                    "Boundary positions formatted incorrectly",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            }catch (HaploViewException hve){
                JOptionPane.showMessageDialog(this,
                    hve.getMessage(),
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        }else if (command.equals("Cancel")){
            this.dispose();
        }
    }
}
