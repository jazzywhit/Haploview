package edu.mit.wi.haploview;

import java.awt.*;
//import java.awt.event.*;
//import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;


public class HaplotypeDisplayController extends JPanel {
    // amount of spacing between elements
    static final int BETWEEN = 4;

    NumberTextField minDisplayField;
    NumberTextField minThickField;
    NumberTextField minThinField;
    Dimension fieldSize;

    HaplotypeDisplay parent;


    public HaplotypeDisplayController(HaplotypeDisplay parent){
        this.parent = parent;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel hapPercentPanel = new JPanel();
        hapPercentPanel.add(new JLabel("Examine haplotypes above "));
        hapPercentPanel.add(minDisplayField =
                new NumberTextField(String.valueOf(parent.displayThresh), 3));
        minDisplayField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){
                    setDisplayThresh(Integer.parseInt(minDisplayField.getText()));
            }
            public void changedUpdate(DocumentEvent e) { }
            public void removeUpdate(DocumentEvent e) { }
        });
        hapPercentPanel.add(new JLabel("%"));
        add(hapPercentPanel);

        JPanel thinPanel = new JPanel();
        thinPanel.add(new JLabel("Connect with thin lines if > "));
        thinPanel.add(minThinField =
                new NumberTextField(String.valueOf(parent.thinThresh), 3));
        minThinField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                setThinThresh(Integer.parseInt(minThinField.getText()));
            }
            public void changedUpdate(DocumentEvent e) { }
            public void removeUpdate(DocumentEvent e) { }
        });
        thinPanel.add(new JLabel("%"));
        add(thinPanel);

        JPanel thickPanel = new JPanel();
        thickPanel.add(new JLabel("Connect with thick lines if > "));
        thickPanel.add(minThickField =
                new NumberTextField(String.valueOf(parent.thickThresh), 3));
        minThickField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                setThickThresh(Integer.parseInt(minThickField.getText()));
            }
            public void changedUpdate(DocumentEvent e) { }
            public void removeUpdate(DocumentEvent e) { }
        });
        thickPanel.add(new JLabel("%"));
        add(thickPanel);

        fieldSize = minDisplayField.getPreferredSize();
    }


    public void setDisplayThresh(int amount) {
        parent.displayThresh = amount;
        parent.adjustDisplay();
    }

    public void setThinThresh(int amount) {
        parent.thinThresh = amount;
        parent.repaint();
    }

    public void setThickThresh(int amount) {
        parent.thickThresh = amount;
        parent.repaint();
    }


    public Dimension getMaximumSize() {
        return new Dimension(super.getPreferredSize().width,
                fieldSize.height*3 + BETWEEN*2);
    }


    class NumberTextField extends JTextField {

        public NumberTextField(String str, int size) {
            super(str, size);
        }

        protected Document createDefaultModel(){
            return new NumberTextFieldDocument(this);
        }

        protected class NumberTextFieldDocument extends PlainDocument {
            NumberTextField ntf;

            public NumberTextFieldDocument(NumberTextField ntf) {
                super();
                this.ntf = ntf;
            }

            public void insertString(int offs, String str, AttributeSet a)
                    throws BadLocationException {

                int length = ntf.getText().length();
                char[] source = str.toCharArray();
                int index = 0;

                for (int i = 0; i < source.length; i++){
                    if (length+i > 1) {
                        Toolkit.getDefaultToolkit().beep();
                        super.insertString(offs, new String(source, 0, index), a);
                        return;
                    }
                    if (Character.isDigit(source[i])) {
                        source[index++] = source[i];
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
                super.insertString(offs, new String(source, 0, index), a);
            }
        }
    }
}
