package edu.mit.wi.haploview;

import java.util.*;

/**
 * <p>Title: PedFile.java </p>
 * <p>Description: Stands for a pedigree file. </p>
 * @author Hui Gong
 * @version $Revision 1.2 $
 */

public class PedFile {

    private Hashtable _contents;
    private Vector _markerName=null;

    /**
     * Constructor
     */
    public PedFile() {
        _contents = new Hashtable();
	
    }

    /**
     * Adds PedFileEntry into the PedFile
     */
    public void addContent(PedFileEntry entry){
	String key = entry.getFamilyID() + " " + entry.getIndivID();
        this._contents.put(key, entry);
    }

    public void setMarkerNames(Vector names){
        this._markerName = names;
    }

    public Vector getMarkerNames(){
        return this._markerName;
    }

    /**
     * Gets a list of PedFileEntry
     * @return Vector a list of PedFileEntry
     */
    public Hashtable getContent(){
        return this._contents;
    }

    /**
     * Prints out the PedFile data as the original pedigree file format
     */
    public void print(){
        System.out.print("FamilyID\tIndividualID\tDadID\tMomID\tGender\tAffected\tLibility\t");
        System.out.println("Markers");
        Enumeration enu = this._contents.elements();
        while(enu.hasMoreElements()){
            PedFileEntry entry = (PedFileEntry)enu.nextElement();
            entry.print();
        }

    }


}
