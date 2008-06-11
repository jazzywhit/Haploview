package edu.mit.wi.haploview.genecruiser;

import edu.mit.wi.haploview.HaploViewException;

/////////////////////////////////////////////
// Created with IntelliJ IDEA.             //
// User: Jesse Whitworth                   //
// Date: Apr 11, 2008                      //
// Time: 1:30:17 PM                        //
/////////////////////////////////////////////

public class gcSNP {
    private
    String IdType, Id, IdDisplayName, VariationName, Source, Allele, ConsequenceType, Chromosome, Strand;
    double Start, End;

    public
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

        System.out.println("SNPs.get(i).getVariationName()" + VariationName);
        System.out.println("SNPs.get(i).getStart() = " + Start);
        System.out.println("SNPs.get(i).getEnd() = " + End);
        System.out.println("SNPs.get(i).getId() = " + Id);
        System.out.println("SNPs.get(i).getIdDisplayName() = " + IdDisplayName);
        
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

