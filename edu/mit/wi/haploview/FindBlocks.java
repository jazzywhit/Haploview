package edu.mit.wi.haploview;

import java.util.*;
import java.awt.Color;

class FindBlocks {
    PairwiseLinkage[][] dPrime;
    Vector markerInfo;
    double fourGameteCutoff = 0.01;

    FindBlocks(PairwiseLinkage[][] data){
        dPrime = data;
    }

    FindBlocks(PairwiseLinkage[][] data, Vector info){
        dPrime=data;
        markerInfo = info;
    }

    Vector do4Gamete(){
        Vector blocks = new Vector();
        Vector strongPairs = new Vector();

        //first make a list of marker pairs with < 4 gametes, sorted by distance apart
        for (int x = 0; x < dPrime.length-1; x++){
            for (int y = x+1; y < dPrime.length; y++){
                PairwiseLinkage thisPair = dPrime[x][y];
                if (thisPair == null){
                    continue;
                }

                double[] freqs = thisPair.getFreqs();
                int numGam = 0;
                for (int i = 0; i < freqs.length; i++){
                    if (freqs[i] > fourGameteCutoff) numGam++;
                }

                //color in squares
                if(numGam > 3){
                    thisPair.setColor(Color.white);
                }else{
                    thisPair.setColor(Color.darkGray);
                }

                if (numGam > 3){ continue; }

                Vector addMe = new Vector(); //a vector of x, y, separation
                int sep = y - x - 1; //compute separation of two markers
                addMe.add(String.valueOf(x)); addMe.add(String.valueOf(y)); addMe.add(String.valueOf(sep));
                if (strongPairs.size() == 0){ //put first pair first
                    strongPairs.add(addMe);
                }else{
                    //sort by descending separation of markers in each pair
                    boolean unplaced = true;
                    for (int v = 0; v < strongPairs.size(); v ++){
                        if (sep >= Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(2))){
                            strongPairs.insertElementAt(addMe, v);
                            unplaced = false;
                            break;
                        }
                    }
                    if (unplaced) {strongPairs.add(addMe);}
                }
            }
        }

        //now take this list of pairs with 3 gametes and construct blocks
        boolean[] usedInBlock = new boolean[dPrime.length + 1];
        for (int v = 0; v < strongPairs.size(); v++){
            boolean isABlock = true;
            int first = Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(0));
            int last = Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(1));
            //first see if this block overlaps with another:
            if (usedInBlock[first] || usedInBlock[last]) continue;
            //test this block.
            for (int y = first+1; y <= last; y++){
                //loop over columns in row y
                for (int x = first; x < y; x++){
                    PairwiseLinkage thisPair = dPrime[x][y];
                    double[] freqs = thisPair.getFreqs();
                    int numGam = 0;
                    for (int i = 0; i < freqs.length; i++){
                        if (freqs[i] > fourGameteCutoff) numGam++;
                    }
                    if (numGam > 3){ isABlock = false; }
                }
            }
            if (isABlock){
                //add to the block list, but in order by first marker number:
                if (blocks.size() == 0){ //put first block first
                    blocks.add(first + " " + last);
                }else{
                    //sort by ascending separation of markers in each pair
                    boolean placed = false;
                    for (int b = 0; b < blocks.size(); b ++){
                        StringTokenizer st = new StringTokenizer((String)blocks.elementAt(b));
                        if (first < Integer.parseInt(st.nextToken())){
                            blocks.insertElementAt(first + " " + last, b);
                            placed = true;
                            break;
                        }
                    }
                    //make sure to put in blocks which fall on the tail end
                    if (!placed) blocks.add(first + " " + last);
                }
                for (int used = first; used <= last; used++){
                    usedInBlock[used] = true;
                }
            }
        }
        return stringVec2intVec(blocks);
    }

    Vector doSFS(){
        double cutHighCI = 0.98;
        double cutLowCI = 0.70;
        double mafThresh = 0.10;
        double[] cutLowCIVar = {0,0,0.80,0.50,0.50};
        double[] maxDist = {0,0,20000,30000,1000000};
        double recHighCI = 0.90;

        int numStrong = 0; int numRec = 0; int numInGroup = 0;
        Vector blocks = new Vector();
        Vector strongPairs = new Vector();

        //first set up a filter of markers which fail the MAF threshhold
        boolean[] skipMarker = new boolean[dPrime.length];
        for (int x = 0; x < dPrime.length; x++){
            if (((SNP)markerInfo.elementAt(x)).getMAF() < mafThresh){
                skipMarker[x]=true;
            }else{
                skipMarker[x]=false;
            }
        }

        //next make a list of marker pairs in "strong LD", sorted by distance apart
        for (int x = 0; x < dPrime.length-1; x++){
            for (int y = x+1; y < dPrime.length; y++){
                PairwiseLinkage thisPair = dPrime[x][y];
                if (thisPair == null){
                        continue;
                }
                //get the right bits
                double lod = thisPair.getLOD();
                double lowCI = thisPair.getConfidenceLow();
                double highCI = thisPair.getConfidenceHigh();

                //color in squares
                if (lowCI > cutLowCI && highCI >= cutHighCI) {
                    thisPair.setColor(new Color(224, 0, 0));  //strong LD
                }else if (highCI > recHighCI) {
                    thisPair.setColor(new Color(192, 192, 240)); //uninformative
                } else {
                    thisPair.setColor(Color.white); //recomb
                }

                if (skipMarker[x] || skipMarker[y]) continue;
                if (lod < -90) continue; //missing data
                if (highCI < cutHighCI || lowCI < cutLowCI) continue; //must pass "strong LD" test

                Vector addMe = new Vector(); //a vector of x, y, separation

                long sep;
                //compute actual separation
                sep = ((SNP)markerInfo.elementAt(y)).getPosition() - ((SNP)markerInfo.elementAt(x)).getPosition();

                addMe.add(String.valueOf(x)); addMe.add(String.valueOf(y)); addMe.add(String.valueOf(sep));
                if (strongPairs.size() == 0){ //put first pair first
                    strongPairs.add(addMe);
                }else{
                    //sort by descending separation of markers in each pair
                    boolean unplaced = true;
                    for (int v = 0; v < strongPairs.size(); v ++){
                        if (sep >= Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(2))){
                            strongPairs.insertElementAt(addMe, v);
                            unplaced = false;
                            break;
                        }
                    }
                    if (unplaced){strongPairs.add(addMe);}
                }
            }
        }

        //now take this list of pairs with "strong LD" and construct blocks
        boolean[] usedInBlock = new boolean[dPrime.length + 1];
        Vector thisBlock;
        int[] blockArray;
        for (int v = 0; v < strongPairs.size(); v++){
            numStrong = 0; numRec = 0; numInGroup = 0;
            thisBlock = new Vector();
            int first = Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(0));
            int last = Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(1));
            int sep = Integer.parseInt((String)((Vector)strongPairs.elementAt(v)).elementAt(2));

            //first see if this block overlaps with another:
            if (usedInBlock[first] || usedInBlock[last]) continue;

            //next, count the number of markers in the block.
            for (int x = first; x <=last ; x++){
                if(!skipMarker[x]) numInGroup++;
            }

            //skip it if it is too long in bases for it's size in markers
            if (numInGroup < 4 && sep > maxDist[numInGroup]) continue;

            thisBlock.add(new Integer(first));
            //test this block. requires 95% of informative markers to be "strong"
            for (int y = first+1; y <= last; y++){
                if (skipMarker[y]) continue;
                thisBlock.add(new Integer(y));
                //loop over columns in row y
                for (int x = first; x < y; x++){
                    if (skipMarker[x]) continue;
                    PairwiseLinkage thisPair = dPrime[x][y];
                    //get the right bits
                    double lod = thisPair.getLOD();
                    double lowCI = thisPair.getConfidenceLow();
                    double highCI = thisPair.getConfidenceHigh();
                    if (lod < -90) continue;   //monomorphic marker error
                    if (lod == 0 && lowCI == 0 && highCI == 0) continue; //skip bad markers

                    //for small blocks use different CI cutoffs
                    if (numInGroup < 5){
                        if (lowCI > cutLowCIVar[numInGroup] && highCI >= cutHighCI) numStrong++;
                    }else{
                        if (lowCI > cutLowCI && highCI >= cutHighCI) numStrong++; //strong LD
                    }
                    if (highCI < recHighCI) numRec++; //recombination
                }
            }

            //change the definition somewhat for small blocks
            if (numInGroup > 3){
                if (numStrong + numRec < 6) continue;
            }else if (numInGroup > 2){
                if (numStrong + numRec < 3) continue;
            }else{
                if (numStrong + numRec < 1) continue;
            }

            blockArray = new int[thisBlock.size()];
            for (int z = 0; z < thisBlock.size(); z++){
                blockArray[z] = ((Integer)thisBlock.elementAt(z)).intValue();
            }
            //	    System.out.println(first + " " + last + " " + numStrong + " " + numRec);
            if ((double)numStrong/(double)(numStrong + numRec) > 0.95){ //this qualifies as a block
                //add to the block list, but in order by first marker number:
                if (blocks.size() == 0){ //put first block first
                    blocks.add(blockArray);
                }else{
                    //sort by ascending separation of markers in each pair
                    boolean placed = false;
                    for (int b = 0; b < blocks.size(); b ++){
                        if (first < ((int[])blocks.elementAt(b))[0]){
                            blocks.insertElementAt(blockArray, b);
                            placed = true;
                            break;
                        }
                    }
                    //make sure to put in blocks which fall on the tail end
                    if (!placed) blocks.add(blockArray);
                }
                for (int used = first; used <= last; used++){
                    usedInBlock[used] = true;
                }
            }
        }
        return blocks;
    }

    Vector doMJD(){
        // find blocks by searching for stretches between two markers A,B where
        // D prime is > 0.8 for all informative combinations of A, (A+1...B)

        // set coloring based on LOD and D'
        for (int i = 0; i < dPrime.length; i++){
            for (int j = i+1; j < dPrime[i].length; j++){
                PairwiseLinkage thisPair = dPrime[i][j];
                if (thisPair == null){
                    continue;
                }

                double d = thisPair.getDPrime();
                double l = thisPair.getLOD();
                Color boxColor = null;
                if (l > 2) {
                    if (d < 0.5) {
                        //high LOD, low D'
                        boxColor = new Color(255, 224, 224);
                    } else {
                        //high LOD, high D' shades of red
                        double blgr = (255-32)*2*(1-d);
                        //boxColor = new Color(255, (int) blgr, (int) blgr);
                        boxColor = new Color(224, (int) blgr, (int) blgr);
                    }
                } else if (d > 0.99) {
                    //high D', low LOD blueish color
                    boxColor = new Color(192, 192, 240);
                } else {
                    //no LD
                    boxColor = Color.white;
                }
                thisPair.setColor(boxColor);
            }
        }

        int baddies;
        int verticalExtent=0;
        int horizontalExtent=0;
        Vector blocks = new Vector();
        for (int i = 0; i < dPrime.length; i++){
            baddies=0;
            //find how far LD from marker i extends
            for (int j = i+1; j < dPrime[i].length; j++){
                PairwiseLinkage thisPair = dPrime[i][j];
                if (thisPair == null){
                    continue;
                }

                //LD extends if D' > 0.8
                if (thisPair.getDPrime() < 0.8){
                    //LD extends through one 'bad' marker
                    if (baddies < 1){
                        baddies++;
                    } else {
                        verticalExtent = j-1;
                        break;
                    }
                }
                verticalExtent=j;
            }
            //now we need to find a stretch of LD of all markers between i and j
            //start with the longest possible block of LD and work backwards to find
            //one which is good
            for (int m = verticalExtent; m > i; m--){
                for (int k = i; k < m; k++){
                    PairwiseLinkage thisPair = dPrime[k][m];
                    if(thisPair.getDPrime() < 0.8){
                        if (baddies < 1){
                            baddies++;
                        } else {
                            break;
                        }
                    }
                    horizontalExtent=k+1;
                }
                //is this a block of LD?

                //previously, this algorithm was more complex and made some calls better
                //but caused major problems in others. since the guessing is somewhat
                //arbitrary, this new and simple method is fine.

                if(horizontalExtent == m){
                    blocks.add(i + " " + m);
                    i=m;
                }
            }
        }
        return stringVec2intVec(blocks);
    }

    //Vector do4Gamete(){

    //}

    Vector stringVec2intVec(Vector inVec){
        //instead of strings with starting and ending positions convert blocks
        //to int arrays

        Vector outVec = new Vector();
        for (int i = 0; i < inVec.size(); i++){
            StringTokenizer st = new StringTokenizer((String)inVec.elementAt(i));
            int start = Integer.parseInt(st.nextToken()); int fin = Integer.parseInt(st.nextToken());
            int[] ma = new int[(fin-start)+1];
            for (int j = start; j <= fin; j++){
                ma[j-start]=j;
            }
            outVec.add(ma);
        }
        return outVec;
    }
}

