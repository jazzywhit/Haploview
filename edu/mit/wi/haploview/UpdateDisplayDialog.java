package edu.mit.wi.haploview;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class UpdateDisplayDialog extends JDialog implements ActionListener, Constants{

    public UpdateDisplayDialog(HaploView h, String title, UpdateChecker uc) {
        super(h, title);

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));

        Font bigguns = new Font("Default", Font.PLAIN, 14);

        JTextArea announceArea = new JTextArea();
        announceArea.setFont(bigguns);
        announceArea.append("A newer version of Haploview is available: " + uc.getNewVersion() + "\n");
        announceArea.append(WEBSITE_STRING + "\n");
        announceArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        announceArea.setEditable(false);
        announceArea.setOpaque(false);
        JPanel announcePanel = new JPanel();
        announcePanel.add(announceArea);
        JScrollPane changeScroller = null;

        try {
            JEditorPane changePane = new JEditorPane();
            changePane.setEditable(false);
            changePane.setPage(new URL("http://www.broad.mit.edu/mpg/haploview/uc/changes.html"));
            changePane.setOpaque(false);
            changeScroller = new JScrollPane(changePane);
            changeScroller.setPreferredSize(new Dimension(250,150));
        } catch(IOException ioe) {
            //if were here then we were able to check for an update, so well just show them a dialog
            //without listing the changes
        }

        contents.add(announcePanel);
        if(changeScroller != null) {
            changeScroller.setAlignmentX(Component.CENTER_ALIGNMENT);
            JPanel scrollHolder = new JPanel();
            scrollHolder.add(changeScroller);
            contents.add(scrollHolder);
        }


        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        contents.add(okButton);



        this.setContentPane(contents);
        this.setLocation(this.getParent().getX() + 100,
                this.getParent().getY() + 100);
        this.setModal(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if(command.equals("OK")) {
            this.dispose();
        }
    }
}
