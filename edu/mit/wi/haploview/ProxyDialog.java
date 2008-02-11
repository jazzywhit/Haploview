package edu.mit.wi.haploview;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class ProxyDialog extends JDialog implements ActionListener, Constants {
    static final long serialVersionUID = 8309981884313205960L;

    JTextField hostText = new JTextField("",10);
    JTextField portText = new JTextField("",4);

    public ProxyDialog (ReadDataDialog rd, String title){
        super(rd,title);

        JPanel contents = new JPanel();
        contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);

        JPanel host = new JPanel();
        host.add(new JLabel("HTTP Proxy:"));
        host.add(hostText);
        host.add(new JLabel("Port:"));
        host.add(portText);
        contents.add(host);

        JPanel choicePanel = new JPanel();
        choicePanel.add(okButton);
        choicePanel.add(cancelButton);
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
        }else if(command.equals("OK")){
            Options.setProxy(hostText.getText(), portText.getText());
            final SwingWorker worker = HaploView.showUpdatePanel();
            worker.start();
            this.dispose();
        }
    }

}
