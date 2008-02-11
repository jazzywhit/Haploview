package edu.mit.wi.haploview;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.*;


public class NumberTextField extends JTextField {
    static final long serialVersionUID = -4453143860561756291L;

    int size;
    boolean decimal;
    boolean negative;

    public NumberTextField(String str, int s, boolean allowDecimal, boolean allowNegative){
        super(s);
        size = s;
        decimal = allowDecimal;
        negative = allowNegative;
        this.setText(str);
    }

    protected Document createDefaultModel(){
        return new NTFDocument(this);
    }

    protected class NTFDocument extends PlainDocument {
        static final long serialVersionUID = 4966113743009411598L;
        NumberTextField ntf;

        public NTFDocument(NumberTextField ntf){
            super();
            this.ntf = ntf;
        }

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            int length = ntf.getText().length();
            char[] source = str.toCharArray();
            String to_insert = "";
            for (int i=0; i<source.length; i++){
                if (length+i > size-1){
                    Toolkit.getDefaultToolkit().beep();
                    super.insertString(offs, to_insert, a);
                    return;
                }
                if (Character.isDigit(source[i]) || (String.valueOf(source[i]).equals(".") && decimal) || (String.valueOf(source[i]).equals("-") && negative)){
                    to_insert+=source[i];
                }else{
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            super.insertString(offs, to_insert, a);
        }
    }
}