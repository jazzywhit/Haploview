package edu.mit.wi.pedfile;



public class MendelError {

    private String family, child;

    public MendelError (String fam, String ind){
        family = fam;
        child = ind;
    }

    public String getFamilyID(){
        return family;
    }

    public String getChildID(){
        return child;
    }
}
