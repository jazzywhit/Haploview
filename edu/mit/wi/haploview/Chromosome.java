package edu.mit.wi.haploview;

import java.util.*;

class Chromosome implements Enumeration{

    private String ped;
    private String individual;
    private Vector genotypes;
    private int current_geno = 0;
    private String origin;

    Chromosome(String p, String i, Vector g, String o){
	ped = p;
	individual = i;
	genotypes = g;
	origin = o;
    }

    Chromosome(String p, String i, Vector g){
	ped = p;
	individual = i;
	genotypes = g;
	origin = "unknown";
    }

    public boolean hasMoreElements(){
        return current_geno < genotypes.size();
    }
    
    public int size(){
	return genotypes.size();
    }

    public Object nextElement(){
        return genotypes.elementAt(current_geno++);
    }

    public Object elementAt(int i){
	return genotypes.elementAt(i);
    }

    public String getPed(){
	return ped;
    }

    public String getIndividual(){
	return individual;
    }

    public String getOrigin(){
	return origin;
    }
    
}


