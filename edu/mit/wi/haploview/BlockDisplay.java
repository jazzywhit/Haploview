package edu.mit.wi.haploview;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;


public class BlockDisplay extends JComponent{

    private Font markerNumFont = new Font("SansSerif", Font.BOLD, 12);
    private Font markerNameFont = new Font("Default", Font.PLAIN, 12);
    BasicStroke thickerStroke = new BasicStroke(1);
    BasicStroke thinnerStroke = new BasicStroke(0.35f);
    private Font boldMarkerNameFont = new Font("Default", Font.BOLD, 12);

    private long minpos;
    private long maxpos;
    private double spanpos;
    private int lineSpan;

    private static final int TICK_HEIGHT = 16;
    HaploData theData;

    public BlockDisplay(HaploData h){
        theData = h;
    }


    public void paintComponent(Graphics g){
        Vector blocks = theData.blocks;
        Graphics2D g2 = (Graphics2D)g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(markerNameFont);
        FontMetrics metrics = g2.getFontMetrics();

        //// draw the marker locations

        g2.setStroke(thinnerStroke);
        g2.setColor(Color.white);
        g2.fillRect(6, 6, lineSpan-1, TICK_HEIGHT-1);
        g2.setColor(Color.black);
        g2.drawRect(5, 5, lineSpan, TICK_HEIGHT);

        for (int i = 0; i < Chromosome.getFilteredSize(); i++) {
            double pos = (Chromosome.getFilteredMarker(i).getPosition() - minpos) / spanpos;
            int xx = (int) (5 + lineSpan*pos);
            g2.setStroke(thickerStroke);
            g2.drawLine(xx, 5, xx, 5 + TICK_HEIGHT);
            g2.setStroke(thinnerStroke);
            g2.drawLine(xx, 5 + TICK_HEIGHT,
                    xx, 5+2*TICK_HEIGHT);
        }

        //// draw the marker names
        PairwiseLinkage[][] dPrimeTable = theData.filteredDPrimeTable;
        int widestMarkerName = metrics.stringWidth(Chromosome.getFilteredMarker(0).getName());
        for (int x = 1; x < dPrimeTable.length; x++) {
            int thiswide = metrics.stringWidth(Chromosome.getFilteredMarker(x).getName());
            if (thiswide > widestMarkerName) widestMarkerName = thiswide;
        }

        g2.translate(5, 5 + widestMarkerName);
        g2.rotate(-Math.PI / 2.0);
        for (int x = 0; x < dPrimeTable.length; x++) {
            if (theData.isInBlock[Chromosome.realIndex[x]]){
                g2.setFont(boldMarkerNameFont);
            }else{
                g2.setFont(markerNameFont);
            }
            double pos = (Chromosome.getFilteredMarker(x).getPosition() - minpos) / spanpos;
            g2.drawString(Chromosome.getFilteredMarker(x).getName(),-3 - 2*TICK_HEIGHT , (int)(5+lineSpan*pos));
        }

        g2.rotate(Math.PI / 2.0);
        g2.translate(-5, -(5 + widestMarkerName));
                       /*
                // move everybody down
                top += widestMarkerName + TEXT_GAP;
            }
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        top  += blockDispHeight;

        //// draw the marker numbers
        if (printDetails){
            g2.setFont(markerNumFont);
            metrics = g2.getFontMetrics();
            ascent = metrics.getAscent();

            for (int x = 0; x < dPrimeTable.length; x++) {
                String mark = String.valueOf(Chromosome.realIndex[x] + 1);
                g2.drawString(mark,
                        left + x*boxSize - metrics.stringWidth(mark)/2,
                        top + ascent);
            }
            top += boxRadius/2; // give a little space between numbers and boxes
        }*/
    }



    public Dimension getPreferredSize(){
        Graphics2D g2 = (Graphics2D)this.getGraphics();
        g2.setFont(markerNameFont);
        FontMetrics metrics = g2.getFontMetrics();
        int ascent = metrics.getAscent();

        long minsep = Chromosome.getFilteredMarker(1).getPosition() - Chromosome.getFilteredMarker(0).getPosition();
        for (int i = 1; i < Chromosome.realIndex.length; i++){
            if (minsep > Chromosome.getFilteredMarker(i).getPosition() - Chromosome.getFilteredMarker(i-1).getPosition()){
                minsep = Chromosome.getFilteredMarker(i).getPosition() - Chromosome.getFilteredMarker(i-1).getPosition();
            }
        }
        minpos = Chromosome.getMarker(0).getPosition();
        maxpos = Chromosome.getMarker(Chromosome.getSize()-1).getPosition();
        spanpos = maxpos - minpos;

        lineSpan = (int)(spanpos/minsep)*ascent;
        return new Dimension(lineSpan+10,TICK_HEIGHT+110);
    }

}
