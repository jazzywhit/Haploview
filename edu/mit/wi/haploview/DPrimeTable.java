package edu.mit.wi.haploview;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Aug 12, 2004
 * Time: 1:35:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class DPrimeTable {
    private PairwiseLinkage[][] theTable;
    private PairwiseLinkage[] sample;

    public DPrimeTable(int numMarkers){
        theTable = new PairwiseLinkage[numMarkers][];
        sample = new PairwiseLinkage[1];
    }


    public void addMarker(Vector marker, int pos){
        theTable[pos] = (PairwiseLinkage[]) marker.toArray(sample);
    }


    public PairwiseLinkage getDPrime(int pos1, int pos2){
        //theTable[pos1] is an array of only the n markers which actually get compared to marker pos1,
        //labelled 0 to n,
        //while the parameter pos2 is an absolute marker number, so we shift to account for that
        return theTable[pos1][pos2-pos1-1];
    }
    public PairwiseLinkage getFilteredDPrime(int pos1, int pos2){
        //as above we need to convert the input of an absolute position into the relative position
        //to index into the DP array. here we jump through the additional hoop of un-filtering the input
        //numbers
        int x = Chromosome.realIndex[pos1];
        int y = Chromosome.realIndex[pos2] - x - 1;
        if (x < theTable.length-1){
            if (y < theTable[x].length){
                return theTable[x][y];
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

   /* public int getLength(int whichMarker){
        //this is the number of markers in one "row" of the table
        //that is, for whichmarker, the num of other markers it is compared to
        return theTable[whichMarker].length;
    }*/

    public int getFilteredLength(int x){
        //same as above but for the filtered dataset
        int whichMarker = Chromosome.realIndex[x];
        for (int m = theTable[whichMarker].length+whichMarker; m > whichMarker; m--){
            if (Chromosome.filterIndex[m] != -1){
                //length of array is one greater than difference of first and last elements
                return Chromosome.filterIndex[m] - Chromosome.filterIndex[whichMarker] + 1;
            }
        }
        return 0;
    }


}