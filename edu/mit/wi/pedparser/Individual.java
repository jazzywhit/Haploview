package edu.mit.wi.pedparser;

import java.util.HashSet;

public class Individual {

    HashSet kids, spouses;
    Individual mom, dad;
    String id;
    boolean missing;
    double genotypePercent;
    int gender;

    Individual(String i, boolean m, int g, double gpct){
        id = i;
        missing = m;
        gender = g;
        genotypePercent = gpct;

        kids = new HashSet();
        spouses = new HashSet();
    }

    void addMom(Individual i) throws PedigreeException{
        if (mom != null){
            throw new PedigreeException("Individual " + id + " has more than one mother.");
        }
        mom = i;
    }

    void addDad(Individual i) throws PedigreeException{
        if (dad != null){
            throw new PedigreeException("Individual " + id + " has more than one mother.");
        }
        dad = i;
    }

    void addKid(Individual i){
        kids.add(i);
    }

    void addSpouse(Individual i){
        spouses.add(i);
    }

    public String toString(){
        return id;
    }
}
