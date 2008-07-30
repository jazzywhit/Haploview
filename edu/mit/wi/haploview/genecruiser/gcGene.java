package edu.mit.wi.haploview.genecruiser;
import edu.mit.wi.haploview.HaploViewException;

import java.util.Vector;
public class gcGene {

/////////////////////////////////////////////
// User: Jesse Whitworth                   //
// Date: Apr 11, 2008                      //
// Time: 1:30:17 PM                        //
/////////////////////////////////////////////


    /**
     * Holds returned Genecruiser data for later use
     * @author Jesse Whitworth
     */
    public String GeneId, Description, Source, BioType, Chromosome, Strand, StableID;
    public double Start, End;

    gcGene(String GeneId, String Description, String Source, String BioType, String Chromosome, String Start, String End, String Strand,
          String StableID /*, Vector<String> GenomicIds*/) throws HaploViewException{

        this.GeneId = GeneId;
        this.Description = Description;
        this.Source = Source;
        this.BioType = BioType;
        this.Chromosome = Chromosome;
        this.Strand = Strand;
        this.StableID = StableID;

        try{
            this.Start = Double.parseDouble(Start);
            this.End = Double.parseDouble(End);

        }catch(NumberFormatException nfe){
            throw new HaploViewException("Start/End not a number");
        }
    }

    public void print(){

        System.out.println("GeneId = " + GeneId);
        System.out.println("Description = " + Description);
        System.out.println("Source = " + Source);
        System.out.println("BioType = " + BioType);
        System.out.println("Chromosome = " + Chromosome);
        System.out.println("Start = " + Start);
        System.out.println("End = " + End);
        System.out.println("Strand = " + Strand);
        System.out.println("StableID = " + StableID);

    }
}

