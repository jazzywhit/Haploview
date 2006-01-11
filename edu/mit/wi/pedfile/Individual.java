/*
* $Id: Individual.java,v 3.2 2006/01/11 20:44:22 jmaller Exp $
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

    private String reasonImAxed;
    private int liability; //optional
    //private Vector markers;
    private byte[] markersa;
    private byte[] markersb;
    private double numGoodMarkers;
    private boolean[] zeroed;
    //this is used to keep track of the index of the last marker added
    private int currMarker;

	public final static int FEMALE = 2;
	public final static int MALE = 1;
	public final static int AFFACTED = 2;
	public final static int UNAFFACTED = 1;
	public final static String DATA_MISSING ="0";



    public Individual(int numMarkers) {
        //this.markers = new Vector(numMarkers);
        markersa = new byte[numMarkers];
        markersb = new byte[numMarkers];
        //this.zeroed = new Vector(numMarkers);
        this.zeroed = new boolean[numMarkers];
        this.currMarker = 0;
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
     * gets the markers vector for this individual
     * @return Vector markers
     */
    public Vector getMarkers() {
        return null;
    }

    public void setMarkers(byte[] ma, byte[] mb){
        markersa = ma;
        markersb = mb;
    }

    /**
     * returns the number of markers for this individual
     * @return integer count of markers
     */
    public int getNumMarkers(){
        return this.markersa.length;
    }


    public byte getMarkerA(int location) {
        return markersa[location];
    }

    public byte getMarkerB(int location) {
        return markersb[location];
    }

 /*   public void addMarker(byte[] marker){
        this.markers.add(marker);
        //this.zeroed.add(new Boolean(false));
        this.zeroed[currMarker] = false;
        this.currMarker++;
        if (!(marker[0] == 0 || marker[1] == 0)){
            numGoodMarkers++;
        }
    }*/

    public void addMarker(byte markera, byte markerb) {
        markersa[currMarker] = markera;
        markersb[currMarker] = markerb;
        zeroed[currMarker] = false;
        currMarker++;
         if (!(markera == 0 || markerb == 0)){
            numGoodMarkers++;
        }
    }

    /**
     * checks to see if a marker has been zeroed out
     * @param location - which marker to check
     * @return true if marker is zeroed, false otherwise
     */
    public boolean getZeroed(int location){
        //return ((Boolean)zeroed.get(location)).booleanValue();
        return zeroed[location];
    }

    /**
     * sets the bit that this marker has been zeroed out for this indiv (e.g. because it has a mendel error)
     * @param i - marker to be zeroed
     */
    public void zeroOutMarker(int i){
        //this.zeroed.set(i, new Boolean(true));
        this.zeroed[i] = true;
    }

    public String getReasonImAxed() {
        return reasonImAxed;
    }

    public void setReasonImAxed(String reasonImAxed) {
        this.reasonImAxed = reasonImAxed;
    }

    public double getGenoPC(){
        return numGoodMarkers/markersa.length;
    }

    public boolean[] getZeroedArray() {
        return zeroed;
    }

    public void setZeroedArray(boolean[] z) {
        zeroed = z;
    }
}

