package edu.mit.wi.haploview;

import java.util.*;
import java.text.NumberFormat;

/**
 * <p>Title: PedResult.java </p>
 * <p>Description: Includes a list of MarkerResult for </p>
 * @author Hui Gong
 * @version $Revision 1.2 $
 */

public class PedResult {
    private Vector _results = new Vector();
    public static String DESCRIPTION ="obsHET = observed heterozygosity\n"+
        "predHET = heterozygosity predicted from allele frequencies\n"+
        "HWpval = Hardy-Weinberg test p-value\n"+
        "%geno = percent of individuals genotyped\n"+
        "FamTrio = # of families with a fully genotyped trio\n"+
        "MendErr = # of Mendelian inheritance errors\n"+
        "rating: BAD = if geno < 75 OR HWpval < .01, MONO if obsHET < 0.01\n";

    /**
     * Constructor
     */
    public PedResult() {
    }

    /**
     * Adds marker result into the PedResult
     */
    public void addMarkerResult(MarkerResult result){
        this._results.add(result);
    }

    /**
     *Finds number of marker results in this pedresult
     **/
    public int getNumResults(){
	return this._results.size();
    }
    
    /**
     * Gets the MarkerResult at certain location
     */
    public MarkerResult getMarkerResult(int location){
        return (MarkerResult)this._results.get(location);
    }

    /**
     * Prints the PedResult as standard output
     */
    public void printResults(){
        System.out.print(this.toString());
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("\t\tobsHET\tpredHET\tHWpval\t%geno\tFamTrio\trating\n");
        Iterator it = this._results.iterator();
        int i =0;
        String name;
        while(it.hasNext()){
            MarkerResult result = (MarkerResult)it.next();
            name = result.getName().trim();
            if(name.length()>0) buffer.append(name+":\t");
            else buffer.append("marker "+ ++i+":\t");
            buffer.append(result.toString()+"\n");
        }
        buffer.append("\n");
        buffer.append(PedResult.DESCRIPTION);
        return buffer.toString();
    }

}
