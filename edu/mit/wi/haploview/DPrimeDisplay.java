package edu.mit.wi.haploview;

import java.awt.*;
import java.awt.event.*;
//import java.awt.font.*;
//import java.io.*;
//import java.text.*;
import javax.swing.*;
import java.util.*;

class DPrimeDisplay extends JComponent{
    static final int H_BORDER = 15;
    static final int V_BORDER = 15;

    static final int DEFAULT_BOX_SIZE = 50;
    static final int DEFAULT_BOX_RADIUS = 24;
    static final int TICK_HEIGHT = 8;
    static final int TICK_BOTTOM = 50;

    static final int TEXT_NUMBER_GAP = 3;

    int widestMarkerName = 80; //default size
    int boxSize = DEFAULT_BOX_SIZE;
    int boxRadius = DEFAULT_BOX_RADIUS;
    boolean printDetails = true;
    int lowX, highX, lowY, highY;
    Rectangle viewRect = new Rectangle();
    int left, top, clickXShift, clickYShift;

    Font boxFont = new Font("SansSerif", Font.PLAIN, 12);
    Font markerNumFont = new Font("SansSerif", Font.BOLD, 12);
    Font markerNameFont = new Font("Default", Font.PLAIN, 12);

    boolean markersLoaded;
    PairwiseLinkage dPrimeTable[][];
    Vector markers;

    DPrimeDisplay(PairwiseLinkage[][] t, boolean b, Vector v){
        markersLoaded = b;
        dPrimeTable = t;
        markers = v;
        this.setDoubleBuffered(true);
        addMouseListener(new popMouseListener(this));
    }

    public void loadMarkers(Vector v){
        markersLoaded = true;
        markers = v;
        repaint();
    }

    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        Dimension pref = getPreferredSize();
        Rectangle clipRect = (Rectangle)g.getClip();
        //first paint grab the cliprect for the whole viewport
        if (viewRect.width == 0){viewRect=clipRect;}

        //okay so this dumb if block is to prevent the ugly
        //repainting bug when loading markers after the data are already
        //being displayed, results in a little off-centering for small
        //datasets, but not too bad.
        //clickxshift and clickyshift are used later to translate from x,y coords to the
        //pair of markers comparison at those coords
        if (!(markersLoaded)){
            g2.translate((size.width - pref.width) / 2,
                    (size.height - pref.height) / 2);
            clickXShift = left + (size.width-pref.width)/2;
            clickYShift = top + (size.height - pref.height)/2;
        } else {
            g2.translate((size.width - pref.width) / 2,
                    0);
            clickXShift = left + (size.width-pref.width)/2;
            clickYShift = top;
        }

        FontMetrics boxFontMetrics = g.getFontMetrics(boxFont);

        int diamondX[] = new int[4];
        int diamondY[] = new int[4];
        Polygon diamond;

        left = H_BORDER;
        top = V_BORDER;

        FontMetrics metrics;
        int ascent;

        g2.setColor(this.getBackground());
        g2.fillRect(0,0,pref.width,pref.height);
        g2.setColor(Color.BLACK);


        if (markersLoaded) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            //// draw the marker locations

            BasicStroke thickerStroke = new BasicStroke(1);
            BasicStroke thinnerStroke = new BasicStroke(0.25f);

            int wide = (dPrimeTable.length-1) * boxSize;
            //TODO: talk to kirby about locusview scaling gizmo
            int lineLeft = wide/20;
            int lineSpan = (wide/10)*9;
            long minpos = ((SNP)markers.elementAt(0)).getPosition();
            long maxpos = ((SNP)markers.elementAt(markers.size()-1)).getPosition();
            double spanpos = maxpos - minpos;
            g2.setStroke(thinnerStroke);
            g2.setColor(Color.white);
            g2.fillRect(left + lineLeft, 5, lineSpan, TICK_HEIGHT);
            g2.setColor(Color.black);
            g2.drawRect(left + lineLeft, 5, lineSpan, TICK_HEIGHT);

            for (int i = 0; i < markers.size(); i++) {
                double pos = (((SNP)markers.elementAt(i)).getPosition() - minpos) / spanpos;
                int xx = (int) (left + lineLeft + lineSpan*pos);
                g2.setStroke(thickerStroke);
                g.drawLine(xx, 5, xx, 5 + TICK_HEIGHT);
                g2.setStroke(thinnerStroke);
                g.drawLine(xx, 5 + TICK_HEIGHT,
                        left + i*boxSize, TICK_BOTTOM);
            }
            top += TICK_BOTTOM;
            //// draw the marker names
            if (printDetails){
                g.setFont(markerNameFont);
                metrics = g.getFontMetrics();
                ascent = metrics.getAscent();

                widestMarkerName = metrics.stringWidth(((SNP)markers.elementAt(0)).getName());
                for (int x = 1; x < dPrimeTable.length; x++) {
                    int thiswide = metrics.stringWidth(((SNP)markers.elementAt(x)).getName());
                    if (thiswide > widestMarkerName) widestMarkerName = thiswide;
                }
                //System.out.println(widest);

                g2.translate(left, top + widestMarkerName);
                g2.rotate(-Math.PI / 2.0);
                for (int x = 0; x < dPrimeTable.length; x++) {
                    g2.drawString(((SNP)markers.elementAt(x)).getName(),TEXT_NUMBER_GAP, x*boxSize + ascent/3);
                }

                g2.rotate(Math.PI / 2.0);
                g2.translate(-left, -(top + widestMarkerName));

                // move everybody down
                top += widestMarkerName + TEXT_NUMBER_GAP;
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        //// draw the marker numbers
        if (printDetails){
            g.setFont(markerNumFont);
            metrics = g.getFontMetrics();
            ascent = metrics.getAscent();
            for (int x = 0; x < dPrimeTable.length; x++) {
                String mark = String.valueOf(x + 1);
                g.drawString(mark,
                        left + x*boxSize - metrics.stringWidth(mark)/2,
                        top + ascent);
            }
            top += boxRadius/2; // give a little space between numbers and boxes
        }

        //if (pref.getWidth() > viewRect.width){
            //this means that the table is bigger than the display.
            //the following values are the bounds on the boxes we want to
            //display given that the current window is 'clipRect'

            lowX = (clipRect.x-clickXShift-(clipRect.y+clipRect.height-clickYShift))/boxSize;
            if (lowX < 0) {
                lowX = 0;
            }
            highX = ((clipRect.x + clipRect.width)/boxSize)+1;
            if (highX > dPrimeTable.length-1){
                highX = dPrimeTable.length-1;
            }
            lowY = ((clipRect.x-clickXShift)+(clipRect.y-clickYShift))/boxSize;
            if (lowY < lowX+1){
                lowY = lowX+1;
            }
            highY = (((clipRect.x-clickXShift+clipRect.width) + (clipRect.y-clickYShift+clipRect.height))/boxSize)+1;
            if (highY > dPrimeTable.length){
                highY = dPrimeTable.length;
            }
            /**
            boxSize = (int)((clipRect.width-2*H_BORDER)/dPrimeTable.length-1);
            if (boxSize < 12){boxSize=12;}
            if (boxSize < 25){
            printDetails = false;
            boxRadius = boxSize/2;
            }else{
            boxRadius = boxSize/2 - 1;
            }

        } else{
            lowX = 0;
            highX = dPrimeTable.length-1;
            lowY = 0;
            highY = dPrimeTable.length;
        }    **/

        // draw table column by column
        for (int x = lowX; x < highX; x++) {

            //always draw the fewest possible boxes
            if (lowY < x+1){
                lowY = x+1;
            }

            for (int y = lowY; y < highY; y++) {
                if (dPrimeTable[x][y] == null){
                    continue;
                }
                double d = dPrimeTable[x][y].getDPrime();
                //double l = dPrimeTable[x][y].getLOD();
                Color boxColor = dPrimeTable[x][y].getColor();

                // draw markers above
                int xx = left + (x + y) * boxSize / 2;
                int yy = top + (y - x) * boxSize / 2;

                diamondX[0] = xx; diamondY[0] = yy - boxRadius;
                diamondX[1] = xx + boxRadius; diamondY[1] = yy;
                diamondX[2] = xx; diamondY[2] = yy + boxRadius;
                diamondX[3] = xx - boxRadius; diamondY[3] = yy;

                diamond = new Polygon(diamondX, diamondY, 4);
                g.setColor(boxColor);
                g.fillPolygon(diamond);
                if (boxColor == Color.white) {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(Color.lightGray);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_OFF);
                }

                if(printDetails){
                    g.setFont(boxFont);
                    ascent = boxFontMetrics.getAscent();
                    int val = (int) (d * 100);
                    g.setColor((val < 50) ? Color.gray : Color.black);
                    if (val != 100) {
                        String valu = String.valueOf(val);
                        int widf = boxFontMetrics.stringWidth(valu);
                        g.drawString(valu, xx - widf/2, yy + ascent/2);
                    }
                }
            }
        }
    }


    public Dimension getPreferredSize() {
        //loop through table to find deepest non-null comparison
        int count = 0;
        for (int x = 0; x < dPrimeTable.length-1; x++){
           for (int y = x+1; y < dPrimeTable.length; y++){
               if (dPrimeTable[x][y] != null){
                 if (count < y-x){
                     count = y-x;
                 }
               }
           }
        }
        //add one so we don't clip bottom box
        count ++;

        int high = 2*V_BORDER + count*boxSize/2;
        if (markersLoaded){
            high += TICK_BOTTOM + widestMarkerName + TEXT_NUMBER_GAP;
        }
        return new Dimension(2*H_BORDER + boxSize*(dPrimeTable.length-1), high);
    }

    int[] centerString(String s, FontMetrics fm) {
        int[] returnArray = new int[2];
        returnArray[0] = (30-fm.stringWidth(s))/2;
        returnArray[1] = 10+(30-fm.getAscent())/2;
        return returnArray;
    }
    class popMouseListener implements MouseListener{
        JComponent caller;
        public popMouseListener(JComponent c){
              caller = c;
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed (MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) ==
                    InputEvent.BUTTON3_MASK){

                final int clickX = e.getX();
                final int clickY = e.getY();
                double dboxX = (double)(clickX - clickXShift - (clickY-clickYShift))/boxSize;
                double dboxY = (double)(clickX - clickXShift + (clickY-clickYShift))/boxSize;
                final int boxX, boxY;
                if (dboxX < 0){
                    boxX = (int)(dboxX - 0.5);
                } else{
                    boxX = (int)(dboxX + 0.5);
                }
                if (dboxY < 0){
                    boxY = (int)(dboxY - 0.5);
                }else{
                    boxY = (int)(dboxY + 0.5);
                }
                if ((boxX >= lowX && boxX <= highX) && (boxY > boxX && boxY < highY)){
                    if (dPrimeTable[boxX][boxY] != null){
                        final SwingWorker worker = new SwingWorker(){
                            public Object construct(){
                                final int leftMargin = 12;
                                String[] displayStrings = new String[5];
                                if (markersLoaded){
                                    displayStrings[0] = new String ("(" +((SNP)markers.elementAt(boxX)).getName() +
                                            ", " + ((SNP)markers.elementAt(boxY)).getName() + ")");
                                }else{
                                    displayStrings[0] = new String("(" + (boxX+1) + ", " + (boxY+1) + ")");
                                }
                                displayStrings[1] = new String ("D': " + dPrimeTable[boxX][boxY].getDPrime());
                                displayStrings[2] = new String ("LOD: " + dPrimeTable[boxX][boxY].getLOD());
                                displayStrings[3] = new String ("r^2: " + dPrimeTable[boxX][boxY].getRSquared());
                                displayStrings[4] = new String ("D' conf. bounds: " +
                                        dPrimeTable[boxX][boxY].getConfidenceLow() + "-" +
                                        dPrimeTable[boxX][boxY].getConfidenceHigh());
                                Graphics g = caller.getGraphics();
                                g.setFont(boxFont);
                                FontMetrics metrics = g.getFontMetrics();
                                int strlen = 0;
                                for (int x = 0; x < 5; x++){
                                    if (strlen < metrics.stringWidth(displayStrings[x])){
                                        strlen = metrics.stringWidth(displayStrings[x]);
                                    }
                                }
                                g.setColor(Color.WHITE);
                                g.fillRect(clickX+1,clickY+1,strlen+leftMargin+4,5*metrics.getHeight()+9);
                                g.setColor(Color.BLACK);
                                g.drawRect(clickX,clickY,strlen+leftMargin+5,5*metrics.getHeight()+10);
                                for (int x = 0; x < 5; x++){
                                    g.drawString(displayStrings[x],clickX + leftMargin, clickY+5+((x+1)*metrics.getHeight()));
                                }
                                return "";
                            }
                        };
                        worker.start();
                    }
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) ==
                    InputEvent.BUTTON3_MASK){
                caller.repaint();
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }
}

