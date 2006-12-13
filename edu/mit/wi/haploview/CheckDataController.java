package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.CheckData;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class CheckDataController extends JPanel implements ActionListener {

    private NumberTextField hwcut, genocut, mendcut, mafcut;
    CheckDataPanel cdp;

    public CheckDataController(CheckDataPanel cdp){
        this.cdp = cdp;
        JPanel failPanel = new JPanel();
        failPanel.setLayout(new BoxLayout(failPanel,BoxLayout.Y_AXIS));
        JPanel holdPanel = new JPanel();
        holdPanel.add(new JLabel("HW p-value cutoff: "));
        hwcut = new NumberTextField(String.valueOf(CheckData.hwCut),8,true,false);
        holdPanel.add(hwcut);
        failPanel.add(holdPanel);
        holdPanel = new JPanel();
        holdPanel.add(new JLabel("Min genotype %: "));
        genocut = new NumberTextField(String.valueOf(CheckData.failedGenoCut),3, false,false);
        holdPanel.add(genocut);
        failPanel.add(holdPanel);
        holdPanel = new JPanel();
        holdPanel.add(new JLabel("Max # mendel errors: "));
        mendcut = new NumberTextField(String.valueOf(CheckData.numMendErrCut),4,false,false);
        holdPanel.add(mendcut);
        failPanel.add(holdPanel);
        holdPanel = new JPanel();
        holdPanel.add(new JLabel("Minimum minor allele freq."));
        mafcut = new NumberTextField(String.valueOf(CheckData.mafCut),8,true,false);
        holdPanel.add(mafcut);
        failPanel.add(holdPanel);
        JPanel newPanel = new JPanel();
        newPanel.setLayout(new BoxLayout(newPanel,BoxLayout.X_AXIS));

        JButton selAll = new JButton("Select All");
        selAll.addActionListener(this);
        newPanel.add(selAll);
        JButton deSelAll = new JButton("Deselect All");
        deSelAll.addActionListener(this);
        newPanel.add(deSelAll);
        JButton setDefault = new JButton("Reset Values");
        setDefault.addActionListener(this);
        newPanel.add(setDefault);
        JButton rescore = new JButton("Rescore Markers");
        rescore.addActionListener(this);
        newPanel.add(rescore);

        this.add(failPanel);
        failPanel.add(newPanel);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Select All")){
            cdp.selectAll();
        }else if (command.equals("Deselect All")){
            cdp.deSelectAll();
        }else if (command.equals("Rescore Markers")){
            String cut = hwcut.getText();
            if (cut.equals("")){
                cut = "0";
            }
            CheckData.hwCut = Double.parseDouble(cut);

            cut = genocut.getText();
            if (cut.equals("")){
                cut="0";
            }
            CheckData.failedGenoCut = Integer.parseInt(cut);
            cut = mendcut.getText();
            if (cut.equals("")){
                cut="0";
            }
            CheckData.numMendErrCut = Integer.parseInt(cut);
            cut = mafcut.getText();
            if (cut.equals("")){
                cut="0";
            }
            CheckData.mafCut = Double.parseDouble(cut);

            cdp.redoRatings();
            cdp.getTable().repaint();
        }else if (command.equals("Reset Values")){
            hwcut.setText(String.valueOf(CheckData.defaultHwCut));
            genocut.setText(String.valueOf(CheckData.defaultFailedGenoCut));
            mendcut.setText(String.valueOf(CheckData.defaultNumMendErrCut));
            mafcut.setText(String.valueOf(CheckData.defaultMafCut));
        }
    }
}
