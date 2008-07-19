/////////////////////////////////////////////
// Created with IntelliJ IDEA.             //
// User: Jesse Whitworth                   //
// Date: Apr 11, 2008                      //
// Time: 1:30:17 PM                        //
/////////////////////////////////////////////
package edu.mit.wi.haploview.genecruiser;
import edu.mit.wi.haploview.HaploViewException;

/**
 * Holds returned Genecruiser data for later use
 * @author Jesse Whitworth
 */
public class gcSNP {
    public
    String IdType, Id, IdDisplayName, VariationName, Source, Allele, ConsequenceType, Chromosome, Strand;
    double Start, End;

    gcSNP(String IdType, String Id, String IdDisplayName, String VariationName, String Source, String Allele, String ConsequenceType, String Chromosome,
          String Start, String End, String Strand) throws HaploViewException{

        this.IdType = IdType;
        this.Id = Id;
        this.IdDisplayName = IdDisplayName;
        this.VariationName = VariationName;
        this.Source = Source;
        this.Allele = Allele;
        this.ConsequenceType = ConsequenceType;
        this.Chromosome = Chromosome;
        this.Strand = Strand;

        try{
            this.Start = Double.parseDouble(Start);
            this.End = Double.parseDouble(End);

        }catch(NumberFormatException nfe){
            throw new HaploViewException("Start/End not a number");
        }
    }

    public void print(){
        System.out.println("IdType = " + IdType);
        System.out.println("Id = " + Id);
        System.out.println("IdDisplayName = " + IdDisplayName);
        System.out.println("VariationName = " + VariationName);
        System.out.println("Source = " + Source);
        System.out.println("Allele = " + Allele);
        System.out.println("ConsequenceType = " + ConsequenceType);
        System.out.println("Chromosome = " + Chromosome);
        System.out.println("Strand = " + Strand);
        System.out.println("Start = " + Start);
        System.out.println("End = " + End);
    }

    public String getIdType(){
        return IdType;
    }
    public String getId(){
        return Id;
    }
    public String getIdDisplayName(){
        return IdDisplayName;
    }
    public String getVariationName(){
        return VariationName;
    }
    public String getSource(){
        return Source;
    }
    public String getAllele(){
        return Allele;
    }
    public String getConsequenceType(){
        return ConsequenceType;
    }
    public String getChromosome(){
        return Chromosome;
    }
    public double getStart(){
        return Start;
    }
    public double getEnd(){
        return End;
    }
    public String getStrand(){
        return Strand;
    }
}

