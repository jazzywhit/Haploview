package edu.mit.wi.haploview;

/**
 * <p>Title: PedMarker.java </p>
 * <p>Description: Stands for a marker in the ped file</p>
 * @author Hui Gong
 * @version $Revision 1.1 $
 */

public class PedMarker {
    private int _allele1;
    private int _allele2;

    public PedMarker() {
    }

    /**
     * Constructor
     */
    public PedMarker(int allele1, int allele2){
        this._allele1 = allele1;
        this._allele2 = allele2;
    }

    /**
     * Sets the first allele
     */
    public void setAllele1(int allele){
        this._allele1 = allele;
    }

    /**
     * Gets the first allele
     */
    public int getAllele1(){
        return this._allele1;
    }

    /**
     * Sets the second allele
     */
    public void setAllele2(int allele){
        this._allele2 = allele;
    }

    /**
     * Gets the second allele
     */
    public int getAllele2(){
        return this._allele2;
    }

    /**
     * Prints out the allele1 and allele2
     */
    public void print(){
        System.out.print(this._allele1+" "+this._allele2+"\t");
    }

}
