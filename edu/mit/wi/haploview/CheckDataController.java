package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.CheckData;

import javax.swing.*;


public class CheckDataController extends JPanel{

    NumberTextField hwcut, genocut, mendcut, mafcut;

    public CheckDataController(HaploView parent){
        JPanel failPanel = new JPanel();
        failPanel.setLayout(new BoxLayout(failPanel,BoxLayout.Y_AXIS));
        JPanel holdPanel = new JPanel();
        holdPanel.add(new JLabel("HW p-value cutoff: "));
        hwcut = new NumberTextField(String.valueOf(CheckData.hwCut),6,true);
        holdPanel.add(hwcut);
        failPanel.add(holdPanel);
        holdPanel = new JPanel();
        holdPanel.add(new JLabel("Min genotype %: "));
        genocut = new NumberTextField(String.valueOf(CheckData.failedGenoCut),3, false);
        holdPanel.add(genocut);
        failPanel.add(holdPanel);
        holdPanel = new JPanel();
        holdPanel.add(new JLabel("Max # mendel errors: "));
        mendcut = new NumberTextField(String.valueOf(CheckData.numMendErrCut),2,false);
        holdPanel.add(mendcut);
        failPanel.add(holdPanel);
        holdPanel = new JPanel();
        holdPanel.add(new JLabel("Minimum minor allele freq."));
        mafcut = new NumberTextField(String.valueOf(CheckData.mafCut),6,true);
        holdPanel.add(mafcut);
        failPanel.add(holdPanel);
        JButton rescore = new JButton("Rescore Markers");
        rescore.addActionListener(parent);
        failPanel.add(rescore);

        JButton selAll = new JButton("Select All");
        selAll.addActionListener(parent);

        this.add(failPanel);
        this.add(selAll);
    }

}
