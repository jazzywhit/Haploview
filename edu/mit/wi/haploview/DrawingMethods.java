package edu.mit.wi.haploview;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.text.*;
import java.io.*;
import java.awt.image.*;
import java.awt.geom.*;
import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import com.sun.jimi.core.component.JimiCanvas;


class DrawingMethods {
    
    public Dimension haploGetPreferredSize(Haplotype[][] hapsInBlocks, Graphics tg){
	int windowX = 10;
	int windowY = 10;
	int height = 0;	 
	FontMetrics fm = tg.getFontMetrics(new Font("Lucida Bright", Font.PLAIN, 12));
	
	for (int i = 0; i < hapsInBlocks.length; i++){
	    height = ((fm.getHeight() + 5) * hapsInBlocks[i].length) + 53;
	    String sizeString = new String();
	    int[] theGeno = hapsInBlocks[i][0].getGeno();
	    for (int k = 0; k < theGeno.length; k++){
		sizeString += theGeno[k];
	    }
	    // add space for percentage which should be equal to or less than:
	    sizeString += "(8.888)";
	    
	    windowX += (fm.stringWidth(sizeString) + 40);
	    if (windowY < height){windowY = height;}
	}
	
	return new Dimension(windowX, windowY);
    }

    public void haploDraw(Graphics gr, boolean myuseThickness,
			  int mycolorThresh, int mycrossThinThresh, int mycrossThickThresh,
			  double[] gapDPrime, Haplotype[][] hapsInBlocks){

	NumberFormat nf = NumberFormat.getInstance();
	NumberFormat nfMulti = NumberFormat.getInstance();
	nf.setMinimumFractionDigits(3);
	nf.setMaximumFractionDigits(3);
	nfMulti.setMinimumFractionDigits(2);
	nfMulti.setMaximumFractionDigits(2);

	Graphics2D g = (Graphics2D) gr;
	final BasicStroke stroke = new BasicStroke(1.0f);
	final BasicStroke wideStroke = new BasicStroke(2.0f);
	final int verticalOffset = 43;
	final Font nonMonoFont = new Font("Lucida Bright", Font.PLAIN, 12);
	final Font regFont = new Font("Lucida Sans Typewriter", Font.PLAIN, 12);
	final Font smallFont = new Font("Lucida Sans Typewriter", Font.PLAIN, 7);
	final Font boldFont = new Font("Lucida Bright", Font.BOLD, 12);
	FontMetrics regfm = g.getFontMetrics(regFont);
	FontMetrics nonMonofm = g.getFontMetrics(nonMonoFont);
	FontMetrics boldfm = g.getFontMetrics(boldFont);
	String theHap = new String();
	int x = 10;
	int y = verticalOffset;
	int totalWidth = 0;
	
	int[][]lookupPos = new int[hapsInBlocks.length][];
	for (int p = 0; p < lookupPos.length; p++){
	    lookupPos[p] = new int[hapsInBlocks[p].length];
	    for (int q = 0; q < lookupPos[p].length; q++){
		lookupPos[p][hapsInBlocks[p][q].getListOrder()] = q;
		//System.out.println(p + " " + q + " " + hapsInBlocks[p][q].getListOrder());
	    }
	}

	Dimension theDimension = haploGetPreferredSize(hapsInBlocks, gr);
	int windowX = (int)theDimension.getWidth();
	int windowY = (int)theDimension.getHeight();
	g.setColor(Color.white);
	g.fillRect(0,0,windowX,windowY);
	g.setColor(Color.black);

	for (int i = 0; i < hapsInBlocks.length; i++){
	    int[] markerNums = hapsInBlocks[i][0].getMarkers();
	    boolean[] tags = hapsInBlocks[i][0].getTags();
	    int headerX = x;
	    for (int z = 0; z < markerNums.length; z++){
		//put tag snps in red
		if (tags[z]) {
		    g.setColor(Color.red);
		}
		//write labels with more than one digit vertically
		if (markerNums[z]+1 < 10){
		    g.setFont(regFont);
		    g.drawString(String.valueOf(markerNums[z]+1), headerX, 18);
		    headerX += (regfm.stringWidth(String.valueOf(markerNums[z]+1)));
		}else {
		    int ones = (markerNums[z]+1)%10;
		    int tens = (((markerNums[z]+1)-ones)%100)/10;
		    g.setFont(regFont);
		    g.drawString(String.valueOf(ones), headerX, 18);
		    g.setFont(smallFont);
		    g.drawString(String.valueOf(tens), headerX-2, 20-regfm.getAscent());
		    headerX += (regfm.stringWidth(String.valueOf(ones)));
		}
		g.setColor(Color.black);
	    }
	    for (int j = 0; j < hapsInBlocks[i].length; j++){
		int curHapNum = lookupPos[i][j];
		theHap = new String();
		String thePercentage = new String();
		int[] theGeno = hapsInBlocks[i][curHapNum].getGeno();
		for (int k = 0; k < theGeno.length; k++){
		    theHap += theGeno[k];
		}
		//draw the haplotype in mono font
		g.setFont(regFont);
		g.drawString(theHap, x, y);
		//draw the percentage value in non mono font
		thePercentage = " (" + nf.format(hapsInBlocks[i][curHapNum].getPercentage()) + ")";
		g.setFont(nonMonoFont);
		g.drawString(thePercentage, x+regfm.stringWidth(theHap), y);
		totalWidth = regfm.stringWidth(theHap) + nonMonofm.stringWidth(thePercentage);
		
		if (i < hapsInBlocks.length - 1){  //draw crossovers
		    for (int crossCount = 0; crossCount < hapsInBlocks[i+1].length; crossCount++){
			double crossVal = hapsInBlocks[i][curHapNum].getCrossover(crossCount);
			if (myuseThickness){
				//draw thin and thick lines
			    if (crossVal*100 > mycrossThinThresh){
				if (crossVal*100 > mycrossThickThresh){
				    g.setStroke(wideStroke);
				}else{
				    g.setStroke(stroke);
				}
				//this arcane formula draws lines neatly from one hap to another
				g.draw(new Line2D.Double((x+totalWidth+3),
							  (y-regfm.getAscent()/2),
							  (x+totalWidth+37),
							  (verticalOffset-regfm.getAscent()/2+((regfm.getHeight()+5)*hapsInBlocks[i+1][crossCount].getListOrder()))));
			    }
			}else{
				//draw colored lines
			    if(crossVal*100 > mycolorThresh){
				g.setStroke(stroke);
				double overThresh = crossVal*100 - mycolorThresh;
				float lineRed, lineGreen, lineBlue;
				if(overThresh < (50-mycolorThresh)/2){
				    //cold colors
				    lineRed=0.0f;
				    lineBlue=new Double(0.9-((overThresh/((50-mycolorThresh)/2))*0.9)).floatValue();
				    lineGreen=0.9f-lineBlue;
				}else{
				    //hot colors
				    lineBlue=0.0f;
				    lineRed=new Double(((overThresh-(50-mycolorThresh)/2)/((50-mycolorThresh)/2))*0.9).floatValue();
				    lineGreen=0.9f-lineRed;
				}
				Color lineColor = new Color(lineRed, lineGreen, lineBlue);
				g.setColor(lineColor);
				g.setStroke(new BasicStroke(1.5f));
				g.draw(new Line2D.Double((x+totalWidth+3),
							  (y-regfm.getAscent()/2),
							  (x+totalWidth+37),
							  (verticalOffset-regfm.getAscent()/2+((regfm.getHeight()+5)*hapsInBlocks[i+1][crossCount].getListOrder()))));
				g.setColor(Color.black);
				g.setStroke(stroke);
			    }
			}
		    }
		}
		y += (regfm.getHeight()+5);
	    }
	    //add the multilocus d prime if appropriate
	    if (i < hapsInBlocks.length - 1){
		int multiX = x +totalWidth+3;
		g.setStroke(wideStroke);
		g.setFont(boldFont);
		g.drawRect(multiX, windowY-boldfm.getAscent()-4, boldfm.stringWidth("8.88")+3, boldfm.getAscent()+3);
		g.drawString(String.valueOf(nfMulti.format(gapDPrime[i])), multiX+2, windowY - 3);
		g.setStroke(stroke);
	    }
	    x += (totalWidth + 40);
	    y = verticalOffset;
	}
    }


    public Dimension dPrimeGetPreferredSize(int size){
	return new Dimension(size*30, size*30);
    }

    public void dPrimeDraw(String[][] table, Graphics g){
	int scale = table.length*30;
	float d, l, blgr;
	Color myColor;
	int[] shifts;
	Font regFont = new Font("Lucida Sans Regular", Font.PLAIN, 10);
	FontMetrics regfm = g.getFontMetrics(regFont);
	Font boldFont = new Font("Lucida Sans Bold", Font.BOLD, 14);
	FontMetrics boldfm = g.getFontMetrics(boldFont);
	//background color
	g.setColor(new Color(192,192,192));
	g.fillRect(0,0,scale,scale);

	//first label:
	g.setColor(Color.black);
	g.setFont(boldFont);
	shifts = centerString("1", boldfm);
	g.drawString("1", shifts[0], shifts[1]);

		//draw table column by column
	for (int x = 0; x < table.length-1; x++){
	    for (int y = x + 1; y < table.length; y++){
		StringTokenizer st = new StringTokenizer(table[x][y]);
		d = Float.parseFloat(st.nextToken());
		l = Float.parseFloat(st.nextToken());
		//set coloring based on LOD and D'
		if (l > 2){
		    if (d < 0.5) {
			//high LOD, low D' bluish color
			myColor = new Color(255, 224, 224);
		    } else {
			//high LOD, high D' shades of red
			blgr = (255-32)*2*(1-d);
			myColor = new Color(255, (int) blgr, (int) blgr);
		    } 
		}else if (d > 0.99){
		    //high D', low LOD gray color
		    myColor = new Color(192, 192, 240);
		}else {
		    //no LD
		    myColor = Color.white;
		}

		//draw the boxes
		g.setColor(myColor);
		g.fillRect(x*30+1, y*30+1, 28, 28);
		g.setColor(Color.black);
		g.drawRect(x*30, y*30, 30, 30);
		g.setFont(regFont);
		shifts=centerString(Float.toString(d), regfm);
		g.drawString(Float.toString(d), shifts[0]+(x*30) ,(y*30)+shifts[1]);
	    }
	    //draw the labels
	    g.setColor(Color.black);
	    g.setFont(boldFont);
	    shifts = centerString(Integer.toString(x+2), boldfm);
	    g.drawString(Integer.toString(x+2), shifts[0]+(x+1)*30, shifts[1]+(x+1)*30);
	}
    }

    int[] centerString(String s, FontMetrics fm){
	int[] returnArray = new int[2];
	returnArray[0] = (30-fm.stringWidth(s))/2;
	returnArray[1] = 10+(30-fm.getAscent())/2;

	return returnArray;
    }

    void saveImage(BufferedImage image, String filename) throws IOException{
	try {
	    if (! (filename.endsWith(".jpg") || filename.endsWith(".JPG"))){
		filename += ".jpg";
	    }
	    Jimi.putImage("image/jpg", (Image)image, filename);
	}catch (com.sun.jimi.core.JimiException e){
	    e.printStackTrace();
	}
    }
}
	
