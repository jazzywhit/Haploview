package edu.mit.wi.haploview;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class HaplotypePanel extends JPanel{
    //instance variables for each panel opened
    Haplotype[][] hapsInBlocks;
    private int mycolorThresh;
    private int mycrossThinThresh;
    private int mycrossThickThresh;
    private boolean myuseThickness;
    double[] gapDPrime;
    DrawingMethods dm;
    
    HaplotypePanel (Haplotype[][] h, boolean useThick,
		    int color, int thin, int thick,
		    double[] g){
	hapsInBlocks = h;
	gapDPrime = g;
	myuseThickness = useThick;
	mycolorThresh = color;
	mycrossThinThresh = thin;
	mycrossThickThresh = thick;
	dm = new DrawingMethods();
    }

    public Dimension getPreferredSize(){
	return dm.haploGetPreferredSize(hapsInBlocks, this.getGraphics());
    }
    
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	dm.haploDraw(g, myuseThickness,
		     mycolorThresh, mycrossThinThresh, mycrossThickThresh,
		     gapDPrime, hapsInBlocks);
    }
}
