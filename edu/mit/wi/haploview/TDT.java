package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.Individual;
import edu.mit.wi.pedfile.Family;
import edu.mit.wi.pedfile.PedFileException;

import java.util.Vector;
import java.util.Enumeration;


public class  TDT {
    public static Vector calcCCTDT(PedFile pf){
        Vector results = new Vector();
        int numMarkers = Chromosome.getUnfilteredSize();
        for (int i = 0; i < numMarkers; i++){
            TDTResult thisResult = new TDTResult(Chromosome.getUnfilteredMarker(i));
            Vector indList = pf.getUnrelatedIndividuals();
            Individual currentInd;
            Family currentFam;
            for (int j = 0; j < indList.size(); j++){
                //need to check below to make sure we don't include parents and kids of trios
                currentInd = (Individual)indList.elementAt(j);
                currentFam = pf.getFamily(currentInd.getFamilyID());
                if (!(currentFam.containsMember(currentInd.getMomID()) &&
                        currentFam.containsMember(currentInd.getDadID()))){
                    thisResult.tallyCCInd(currentInd.getMarker(i), currentInd.getAffectedStatus());
                }else{
                    try{
                        if (!(indList.contains(currentFam.getMember(currentInd.getMomID())) ||
                                indList.contains(currentFam.getMember(currentInd.getDadID())))){
                            thisResult.tallyCCInd(currentInd.getMarker(i), currentInd.getAffectedStatus());
                        }
                    }catch (PedFileException pfe){
                    }
                }
            }
            results.add(thisResult);
        }

        return results;
    }

    public static Vector calcTrioTDT(PedFile pf) throws PedFileException{

        Vector results = new Vector();
        int numMarkers = Chromosome.getUnfilteredSize();
        for (int i = 0; i < numMarkers; i++){
            TDTResult thisResult = new TDTResult(Chromosome.getUnfilteredMarker(i));
            Vector indList = pf.getAllIndividuals();
            Individual currentInd;
            Family currentFam;
            for (int j = 0; j < indList.size(); j++){
                currentInd = (Individual)indList.elementAt(j);
                currentFam = pf.getFamily(currentInd.getFamilyID());
                if (currentFam.containsMember(currentInd.getMomID()) &&
                        currentFam.containsMember(currentInd.getDadID()) &&
                        currentInd.getAffectedStatus() == 2){
                    //if he has both parents, and is affected, we can get a transmission
                    Individual mom = currentFam.getMember(currentInd.getMomID());
                    Individual dad = currentFam.getMember(currentInd.getDadID());
                    byte[] thisMarker = currentInd.getMarker(i);
                    byte kid1 = thisMarker[0];
                    byte kid2 = thisMarker[1];
                    thisMarker = dad.getMarker(i);
                    byte dad1 = thisMarker[0];
                    byte dad2 = thisMarker[1];
                    thisMarker = mom.getMarker(i);
                    byte mom1 = thisMarker[0];
                    byte mom2 = thisMarker[1];
                    byte momT=0, momU=0, dadT=0, dadU=0;
                    if (kid1 == 0 || kid2 == 0 || dad1 == 0 || dad2 == 0 || mom1 == 0 || mom2 == 0) {
                        continue;
                    } else if (kid1 == kid2) {
                        //kid homozygous
                        if (dad1 == kid1) {
                            dadT = dad1;
                            dadU = dad2;
                        } else {
                            dadT = dad2;
                            dadU = dad1;
                        }

                        if (mom1 == kid1) {
                            momT = mom1;
                            momU = mom2;
                        } else {
                            momT = mom2;
                            momU = mom1;
                        }
                    } else {
                        if (dad1 == dad2 && mom1 != mom2) {
                            //dad hom mom het
                            dadT = dad1;
                            dadU = dad2;
                            if (kid1 == dad1) {
                                momT = kid2;
                                momU = kid1;
                            } else {
                                momT = kid1;
                                momU = kid2;
                            }
                        } else if (mom1 == mom2 && dad1 != dad2) {
                            //dad het mom hom
                            momT = mom1;
                            momU = mom2;
                            if (kid1 == mom1) {
                                dadT = kid2;
                                dadU = kid1;
                            } else {
                                dadT = kid1;
                                dadU = kid2;
                            }
                        } else if (dad1 == dad2 && mom1 == mom2) {
                            //mom & dad hom
                            dadT = dad1;
                            dadU = dad1;
                            momT = mom1;
                            momU = mom1;
                        } else {
                            //everybody het
                            dadT = (byte)(4+dad1);
                            dadU = (byte)(4+dad2);
                            momT = (byte)(4+mom1);
                            momU = (byte)(4+mom2);
                        }
                    }
                    thisResult.tallyTrioInd(dadT, dadU);
                    thisResult.tallyTrioInd(momT, momU);
                }
            }
            results.add(thisResult);
        }
        return results;
    }
}
