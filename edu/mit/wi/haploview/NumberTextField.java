package edu.mit.wi.haploview;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.beans.*;
import java.awt.Toolkit;

public class NumberTextField extends JTextField {
    Toolkit toolkit;


    public NumberTextField(){
	super();
	toolkit = Toolkit.getDefaultToolkit();
    }

    public NumberTextField(String str, int size){
	super(str, size);
	toolkit = Toolkit.getDefaultToolkit();
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
		if (length+i > 1){
		    toolkit.beep();
		    super.insertString(offs, to_insert, a);
		    return;
		}
		if (Character.isDigit(source[i])) to_insert+=source[i];
		else toolkit.beep();
	    }
	    
	    super.insertString(offs, to_insert, a);
	}
    }
}		    
