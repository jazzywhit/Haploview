/*
* $Id: Individual.java,v 1.2 2003/09/02 14:58:39 jcbarret Exp $
* WHITEHEAD INSTITUTE
* SOFTWARE COPYRIGHT NOTICE AGREEMENT
* This software and its documentation are copyright 2002 by the
* Whitehead Institute for Biomedical Research.  All rights are reserved.
*
* This software is supplied without any warranty or guaranteed support
* whatsoever.  The Whitehead Institute can not be responsible for its
* use, misuse, or functionality.
*/

package edu.mit.wi.pedfile;

import java.util.Vector;
import java.util.Iterator;

/**
 * stores information about an individual from a pedigree file
 *
 * this class is not thread safe (untested)
 *
 * @author Julian Maller
 */
public class Individual {
    private String familyID;
    private String individualID;
    private String momID;
    private String dadID;
    private int gender;
    private int affectedStatus;
    private int liability; //optional
    private Vector markers;
    private boolean isTyped;

	public final static int FEMALE = 2;
	public final static int MALE = 1;
	public final static int AFFACTED = 2;
	public final static int UNAFFACTED = 1;
	public final static String DATA_MISSING ="0";


    public Individual(){
        this.markers = new Vector();
    }

    /**
     * gets the family ID
     * @return The familyID for this individual
     */
    public String getFamilyID() {
        return familyID;
    }
    /**
     * sets the family ID
     * @param familyID
     */
    public void setFamilyID(String familyID) {
        this.familyID = familyID;
    }
    /**
     * gets the Individual ID
     * @return The individualID for this individual
     */
    public String getIndividualID() {
        return individualID;
    }
    /**
     * sets the individual ID
     * @param individualID
     */
    public void setIndividualID(String individualID) {
        this.individualID = individualID;
    }
    /**
     * gets the momID for this individual
     * @return momID
     */
    public String getMomID() {
        return momID;
    }
    /**
     * sets the momid
     * @param momID
     */
    public void setMomID(String momID) {
        this.momID = momID;
    }
    /**
     * gets the dad ID for this individual
     * @return dadID
     */
    public String getDadID() {
        return dadID;
    }
    /**
     * sets the dadID
     * @param dadID
     */
    public void setDadID(String dadID) {
        this.dadID = dadID;
    }
    /**
     * gets the gender for this individual
     * @return gender
     */
    public int getGender() {
        return gender;
    }
    /**
     * sets the gender
     * @param gender
     */
    public void setGender(int gender) {
        this.gender = gender;
    }
    /**
     * gets the affected status for this individual
     * @return affectedStatus
     */
    public int getAffectedStatus() {
        return affectedStatus;
    }
    /**
     * sets the affected status
     * @param affectedStatus
     */
    public void setAffectedStatus(int affectedStatus) {
        this.affectedStatus = affectedStatus;
    }
    /**
     * gets the liability class for this individual
     * @return liability
     */
    public int getLiability() {
        return liability;
    }
    /**
     * sets the liability class
     * @param liability
     */
    public void setLiability(int liability) {
        this.liability = liability;
    }
    /**
     * returns if the individual has been genotyped
     * @return isTyped true if individual is typed false otherwise
     */
    public boolean getIsTyped() {
        return this.isTyped;
    }
    /**
     * sets whether the individual has been genotyped
     * @param isTyped
     */
    public void setIsTyped(boolean isTyped) {
        this.isTyped = isTyped;
    }
    /**
     * gets the markers vector for this individual
     * @return Vector markers
     */
    public Vector getMarkers() {
        return markers;
    }
    /**
     * sets the markers vector
     * @param markers
     */
    public void setMarkers(Vector markers) {
        this.markers = markers;
    }

    /**
     * returns the number of markers for this individual
     * @return integer count of markers
     */
    public int getNumMarkers(){
        return this.markers.size();
    }

    /**
     * returns a two byte array of the marker at the index specified by location
     * @param location the index in the markers vector
     * @return two byte array with the marker values
     */
    public byte[] getMarker(int location){
        return (byte[])this.markers.get(location);
    }

    /**
     * adds a marker to the markers vector
     * @param marker - two byte array with first marker in index 0 and second in index 1
     */
    public void addMarker(byte[] marker){
        this.markers.add(marker);
    }

    public void zeroOutMarker(int i){
        byte zeroArray[] = {0,0};
        this.markers.set(i, zeroArray);
    }
    /**
     * returns an iterator for the markers Vector
     * @return iterator for the markers Vector
     */
    public Iterator markerIterator(){
        return this.markers.iterator();
    }


}
