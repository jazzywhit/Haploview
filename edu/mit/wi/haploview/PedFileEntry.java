package edu.mit.wi.haploview;

import java.util.*;

/**
 * <p>Title: PedFileEntry.java </p>
 * <p>Description: Stands for one row in a pedigree file.
 * The columns of pedigree file are:
 * column 1: familyID
 * column 2: individual ID
 * column 3: dad ID
 * column 4: mom ID
 * column 5: gender
 * column 6: affected status
 * column 7: (optional) libility
 * column 8 and 9: marker 1 (seperated by space)
 * column 2n and 2n+1: marker n (seperated by space)
 * </p>
 * @author Hui Gong
 * @version $Revision 1.1 $
 */

public class PedFileEntry {
    private String _famID;
    private String _indivID;
    private String _momID;
    private String _dadID;
    private int _gender;
    private int _affectedStatus;
    private String _libility="";
    private Vector _markers;
    private boolean _isTyped = false;


    public final static int FEMALE = 2;
    public final static int MALE = 1;
    public final static int AFFACTED = 2;
    public final static int UNAFFACTED = 1;
    public final static String DATA_MISSING ="0";

    public PedFileEntry() {
        _markers = new Vector();
    }

    /**
     * Sets family ID
     * @param id
     */
    public void setFamilyID(String id){
        this._famID = id;
    }

    /**
     * Gets family ID
     * @return family id
     */
    public String getFamilyID(){
        return this._famID;
    }

    /**
     * Sets individual ID
     */
    public void setIndivID(String id){
        this._indivID = id;
    }

    /**
     * Sets whether this indiv has any genotypes
     */
    public void setIsTyped(boolean type){
	this._isTyped = type;
    }

    /**
     *gets whether the indiv has any genotypes
     */
    public boolean getIsTyped(){
	return this._isTyped;
    }

    /**
     * Gets individual id
     */
    public String getIndivID(){
        return this._indivID;
    }

    /**
     * Sets mom id
     */
    public void setMomID(String id){
        this._momID = id;
    }

    /**
     * Gets mom id
     */
    public String getMomID(){
        return this._momID;
    }

    /**
     * Sets dad id
     */
    public void setDadID(String id){
        this._dadID = id;
    }

    /**
     * Gets dad id
     */
    public String getDadID(){
        return this._dadID;
    }

    /**
     * Sets gender
     * @param gender PedFileEntry.FEMALE, PedFileEntry.MALE or PedFileEntry.MISSING
     */
    public void setGender(int gender){
        this._gender = gender;
    }

    /**
     * Gets gender
     * @return gender PedFileEntry.FEMALE, PedFileEntry.MALE or PedFileEntry.MISSING
     */
    public int getGender(){
        return this._gender;
    }

    /**
     * Sets affected status
     * @param status PedFileEntry.AFFACTED or PedFileEntry.UNAFFACTED
     */
    public void setAffectedStatus(int status){
        this._affectedStatus = status;
    }

    /**
     * Gets affected status
     * @return status PedFileEntry.AFFACTED or PedFileEntry.UNAFFACTED
     */
    public int getAffectedStatus(){
        return this._affectedStatus;
    }

    /**
     * Sets libility
     */
    public void setLibility(String libility){
        this._libility = libility;
    }

    /**
     * Gets libility
     */
    public String getLibility(){
        return this._libility;
    }

    /**
     * Adds marker into the PedFileEntry
     */
    public void addMarker(PedMarker marker){
        this._markers.add(marker);
    }

    /**
     * Gets all markers in the PedFileEntry
     * @return Vector a list of PedMarker object.
     */
    public Vector getAllMarkers(){
        return this._markers;
    }

    /**
     * Gets marker at loc
     * @return PedMarker
     */
    public PedMarker getMarker(int loc){
        return (PedMarker)this._markers.get(loc);
    }

    /**
     * Prints out the PedFileEntry data as original pedigree file format.
     */
    public void print(){
        System.out.print(this._famID+"\t");
        System.out.print(this._indivID+"\t");
        System.out.print(this._dadID+"\t");
        System.out.print(this._momID+"\t");
        System.out.print(this._gender+"\t");
        System.out.print(this._affectedStatus+"\t");
        System.out.print(this._libility+"\t");
        Iterator iter = this._markers.iterator();
        while(iter.hasNext()){
            PedMarker marker = (PedMarker)iter.next();
            marker.print();
        }
        System.out.print("\n");
    }
}
