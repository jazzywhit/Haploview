package edu.mit.wi.haploview;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

public class TweakBlockDefsDialog extends JDialog implements ActionListener {
    static final long serialVersionUID = 1937924544508766506L;

    NumberTextField gamThresh, highLD, lowLD, highRec, informFrac, mafCut, spinedp;
    HaploView hv;

    public TweakBlockDefsDialog(String title, HaploView h){
        super(h, title);
        hv = h;

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));

        JPanel sfsPanel = new JPanel();
        sfsPanel.setBorder(new TitledBorder("Gabriel et al."));
        sfsPanel.setLayout(new BoxLayout(sfsPanel, BoxLayout.Y_AXIS));
        JPanel holdPanel = new JPanel();
        holdPanel.add(new JLabel("Confidence interval minima for strong LD:"));
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        JPanel top = new JPanel();
        highLD = new NumberTextField(String.valueOf(FindBlocks.cutHighCI),4, true, false);
        top.add(new JLabel("Upper:"));
        top.add(highLD);
        fieldPanel.add(top);
        JPanel bottom = new JPanel();
        lowLD = new NumberTextField(String.valueOf(FindBlocks.cutLowCI),4, true, false);
        bottom.add(new JLabel("Lower:"));
        bottom.add(lowLD);
        fieldPanel.add(bottom);
        holdPanel.add(fieldPanel);
        sfsPanel.add(holdPanel);
        holdPanel = new JPanel();
        holdPanel.add(new JLabel("Upper confidence interval maximum for strong recombination:"));
        highRec = new NumberTextField(String.valueOf(FindBlocks.recHighCI),4,true,false);
        holdPanel.add(highRec);
        sfsPanel.add(holdPanel);
        holdPanel = new JPanel();
        holdPanel.add(new JLabel("Fraction of strong LD in informative comparisons must be at least "));
        informFrac = new NumberTextField(String.valueOf(FindBlocks.informFrac),4,true,false);
        holdPanel.add(informFrac);
        sfsPanel.add(holdPanel);
        holdPanel = new JPanel();
        holdPanel.add(new JLabel("Exclude markers below "));
        mafCut = new NumberTextField(String.valueOf(FindBlocks.mafThresh),4,true,false);
        holdPanel.add(mafCut);
        holdPanel.add(new JLabel(" MAF."));
        sfsPanel.add(holdPanel);

        JPanel gamPanel = new JPanel();
        gamPanel.setBorder(new TitledBorder("4 Gamete Rule"));
        gamPanel.add(new JLabel("4th gamete must be observed at frequency > "));
        gamThresh = new NumberTextField(String.valueOf(FindBlocks.fourGameteCutoff), 5, true,false);
        gamPanel.add(gamThresh);

        JPanel spinePanel = new JPanel();
        spinePanel.setBorder(new TitledBorder("Strong LD Spine"));
        spinePanel.add(new JLabel("Extend spine if D' > "));
        spinedp = new NumberTextField(String.valueOf(FindBlocks.spineDP), 4, true,false);
        spinePanel.add(spinedp);

        JPanel butPanel = new JPanel();
        JButton button = new JButton("OK");
        button.addActionListener(this);
        butPanel.add(button);
        button = new JButton("Cancel");
        button.addActionListener(this);
        butPanel.add(button);

        contents.add(sfsPanel);
        contents.add(gamPanel);
        contents.add(spinePanel);
        contents.add(butPanel);
        this.setContentPane(contents);
        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("OK")){
            doTweak();
            this.dispose();
        }else if (command.equals("Cancel")){
            this.dispose();
        }
    }

    private void doTweak() {
        String gt = gamThresh.getText();
        if (gt.equals("")){
            gt = "0";
        }
        double gtVal = Double.parseDouble(gt);
        if (gtVal < 1){
            FindBlocks.fourGameteCutoff = gtVal;
        }

        String cih = highLD.getText();
        if (cih.equals("")){
            cih = "0";
        }
        double cihVal = Double.parseDouble(cih);
        if (cihVal < 1){
            FindBlocks.cutHighCI = cihVal;
        }

        String  cil = lowLD.getText();
        if (cil.equals("")){
            cil = "0";
        }
        double cilVal = Double.parseDouble(cil);
        if (cilVal < 1){
            FindBlocks.cutLowCI = cilVal;
        }

        String cirec = highRec.getText();
        if (cirec.equals("")){
             cirec = "0";
        }
        double  cirecVal = Double.parseDouble(cirec);
        if (cirecVal < 1){
            FindBlocks.recHighCI = cirecVal;
        }

        String  ifs = informFrac.getText();
        if (ifs.equals("")){
            ifs = "0";
        }
        double ifsV = Double.parseDouble(ifs);
        if (ifsV < 1){
            FindBlocks.informFrac = ifsV;
        }

        String mc = mafCut.getText();
        if (mc.equals("")){
            mc = "0";
        }
        double mcV = Double.parseDouble(mc);
        if (mcV < 1){
            FindBlocks.mafThresh = mcV;
        }

        String sdp = spinedp.getText();
        if (sdp.equals("")){
            sdp = "0";
        }
        double sdpV = Double.parseDouble(sdp);
        if (sdpV < 1){
            FindBlocks.spineDP = sdpV;
        }


        //note that this will only apply to the cursor in this dialog, but java seems touchy
        //about setting a "global" cursor
        setCursor(new Cursor(Cursor.WAIT_CURSOR));

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                hv.theData.guessBlocks(hv.getCurrentBlockDef());
                hv.dPrimeDisplay.colorDPrime();
                hv.changeBlocks(hv.getCurrentBlockDef());
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

}
