package edu.mit.wi.haploview;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class DPrimePanel extends JPanel{
    
    private String[][] table;

    DPrimePanel(String[][] t){
	table = t;
    }

    public Dimension getPreferredSize(){
	return new DrawingMethods().dPrimeGetPreferredSize(table.length);
    }

    public void paintComponent(Graphics g){
	super.paintComponent(g);
	new DrawingMethods().dPrimeDraw(table, g);
    }

    public void showBlock(int[] markers){
	/**for (int i = 0; i < markers.length; i ++){
	   if (i == 0) numlabel[markers[0]].setText("[ " + (markers[i] + 1));
	   if (i == markers.length - 1) numlabel[markers[i]].setText((markers[i] + 1) + " ]");
	   numlabel[markers[i]].setForeground(maroon);
	    }**/
    }

    public void hideBlock(int[] markers){
	/**for (int i = 0; i < markers.length; i ++){
	   if (i == 0) numlabel[markers[0]].setText(String.valueOf(markers[i] + 1));
	   if (i == markers.length - 1) numlabel[markers[i]].setText(String.valueOf(markers[i] + 1));
	   numlabel[markers[i]].setForeground(Color.black);
	   }**/
    }
}






