package edu.mit.wi.haploview;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

public class GBrowseOptionDialog extends JDialog implements ActionListener, Constants{
    HaploView hv;
    JCheckBox[] optCheckBoxes;

    public GBrowseOptionDialog(HaploView h, String title){
        super(h,title);
        hv = h;

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));

        contents.add(new JLabel("Select tracks to display in HapMap Info image:"));

        String[] opts = GB_OPTS_NAMES;
        String curOpts = Options.getgBrowseTypes();
        optCheckBoxes = new JCheckBox[opts.length];
        for (int i = 0; i < opts.length; i++){
            optCheckBoxes[i] = new JCheckBox(opts[i]);
            contents.add(optCheckBoxes[i]);
            if (curOpts.indexOf(GB_TYPES[i]) != -1){
                optCheckBoxes[i].setSelected(true);
            }
        }

        JPanel buttonPanel = new JPanel();
        JButton cancelBut = new JButton("Cancel");
        cancelBut.addActionListener(this);
        JButton okBut = new JButton("OK");
        okBut.addActionListener(this);
        buttonPanel.add(okBut);
        buttonPanel.add(cancelBut);
        contents.add(buttonPanel);
        this.getRootPane().setDefaultButton(okBut);

        this.setContentPane(contents);
        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("OK")){
            StringBuffer osb = new StringBuffer();
            StringBuffer tsb = new StringBuffer();
            for (int i = 0; i < optCheckBoxes.length; i++){
                if (optCheckBoxes[i].isSelected()){
                    osb.append(GB_OPTS[i]+"+");
                    tsb.append(GB_TYPES[i]+"+");
                }
            }

            //strip the trailing plus signs
            while (osb.length() > 0 && osb.substring(osb.length()-1).equals("+")){
                osb.deleteCharAt(osb.length()-1);
            }
            while (tsb.length() > 0 && tsb.substring(tsb.length()-1).equals("+")){
                tsb.deleteCharAt(tsb.length()-1);
            }

            Options.setgBrowseOpts(osb.toString());
            Options.setgBrowseTypes(tsb.toString());
            this.dispose();

            hv.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    hv.dPrimeDisplay.computePreferredSize();
                    if (hv.dPrimeDisplay != null && hv.tabs.getSelectedIndex() == VIEW_D_NUM){
                        hv.dPrimeDisplay.repaint();
                    }
                    hv.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
        }else if (command.equals("Cancel")){
            this.dispose();
        }
    }
}
