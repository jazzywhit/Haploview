package edu.mit.wi.haploview;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.*;


class NumberTextField extends JTextField {

    int size;
    boolean decimal;

    public NumberTextField(String str, int s, boolean d){
        super(s);
        size = s;
        decimal = d;
        this.setText(str);
    }

    protected Document createDefaultModel(){
        return new NTFDocument(this);
    }

    protected class NTFDocument extends PlainDocument {
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
                if (Character.isDigit(source[i]) || (String.valueOf(source[i]).equals(".") && decimal)){
                    to_insert+=source[i];
                }else{
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            super.insertString(offs, to_insert, a);
        }
    }
}