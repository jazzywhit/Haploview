package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.PedFile;
import edu.mit.wi.pedfile.Individual;
import edu.mit.wi.pedfile.Family;

import java.io.*;
import java.text.NumberFormat;
import java.util.Vector;
//import java.util.Hashtable;

class TextMethods {
      //TODO: TOTALLY BROX0R3D

    public void saveHapsToText(Haplotype[][] finishedHaplos, double[] multidprime,  File saveHapsFile) throws IOException{

        if (finishedHaplos == null) return;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);

        //open file for saving haps text
        FileWriter saveHapsWriter = new FileWriter(saveHapsFile);

        //go through each block and print haplos
        for (int i = 0; i < finishedHaplos.length; i++){
            //write block header
            saveHapsWriter.write("BLOCK " + (i+1) + ".  MARKERS:");
            int[] markerNums = finishedHaplos[i][0].getMarkers();
            boolean[] tags = finishedHaplos[i][0].getTags();
            for (int j = 0; j < markerNums.length; j++){
                saveHapsWriter.write(" " + (markerNums[j]+1));
                if (tags[j]) saveHapsWriter.write("!");
            }
            saveHapsWriter.write("\n");
            //write haps and crossover percentages
            for (int j = 0; j < finishedHaplos[i].length; j++){
                String theHap = new String();
                int[] theGeno = finishedHaplos[i][j].getGeno();
                for (int k = 0; k < theGeno.length; k++){
                    theHap += theGeno[k];
                }
                saveHapsWriter.write(theHap + " (" + nf.format(finishedHaplos[i][j].getPercentage()) + ")");
                if (i < finishedHaplos.length-1){
                    saveHapsWriter.write("\t|");
                    for (int crossCount = 0; crossCount < finishedHaplos[i+1].length; crossCount++){
                        if (crossCount != 0) saveHapsWriter.write("\t");
                        saveHapsWriter.write(nf.format(finishedHaplos[i][j].getCrossover(crossCount)));
                    }
                    saveHapsWriter.write("|");
                }
                saveHapsWriter.write("\n");
            }
            if (i < finishedHaplos.length - 1){
                saveHapsWriter.write("Multiallelic Dprime: " + multidprime[i] + "\n");
            }else{
                saveHapsWriter.write("\n");
            }
        }
        saveHapsWriter.close();
    }

    public void saveDprimeToText(PairwiseLinkage[][] dPrimeTable, File dumpDprimeFile, boolean info, Vector markerinfo) throws IOException{
        FileWriter saveDprimeWriter = new FileWriter(dumpDprimeFile);


        if (info){
            saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\tDist\n");
            long dist;

            for (int i = 0; i < dPrimeTable.length; i++){
                for (int j = 0; j < dPrimeTable[i].length; j++){
                    //many "slots" in table aren't filled in because it is a 1/2 matrix
                    if (i < j){
                        dist = ((SNP)markerinfo.elementAt(j)).getPosition() - ((SNP)markerinfo.elementAt(i)).getPosition();
                        saveDprimeWriter.write((i+1) + "\t" + (j+1) + "\t" + dPrimeTable[i][j].toString() + "\t" + dist + "\n");
                    }
                }
            }

        }else{
            saveDprimeWriter.write("L1\tL2\tD'\tLOD\tr^2\tCIlow\tCIhi\n");

            for (int i = 0; i < dPrimeTable.length; i++){
                for (int j = 0; j < dPrimeTable[i].length; j++){
                    //many "slots" in table aren't filled in because it is a 1/2 matrix
                    if (i < j){
                        saveDprimeWriter.write((i+1) + "\t" + (j+1) + "\t" + dPrimeTable[i][j] + "\n");
                    }
                }
            }
        }

        saveDprimeWriter.close();
    }

    public void linkageToHaps(boolean[] markerResults, PedFile pedFile, String hapFileName)throws IOException{
        FileWriter linkageToHapsWriter = new FileWriter(new File(hapFileName));

        Vector indList = pedFile.getOrder();
        int numMarkers = 0;
        Vector usedParents = new Vector();
        Individual currentInd;
        Family currentFamily;


        for(int x=0; x < indList.size(); x++){

            String[] indAndFamID = (String[])indList.elementAt(x);
            currentFamily = pedFile.getFamily(indAndFamID[0]);
            currentInd = currentFamily.getMember(indAndFamID[1]);

            boolean begin = false;

            if(currentInd.getIsTyped()){
                //singleton
                if(currentFamily.getNumMembers() == 1){
                    String hap1 = new String(""); String hap2 = new String("");
                    hap1 += currentInd.getFamilyID() + "\t" + currentInd.getIndividualID() + "\t";
                    hap2 += currentInd.getFamilyID() + "\t" + currentInd.getIndividualID() + "\t";
                    numMarkers = currentInd.getNumMarkers();
                    for (int i = 0; i < numMarkers; i++){
                        if (markerResults[i]){
                            if (begin){
                                hap1+=" "; hap2+=" ";
                            }
                            byte[] thisMarker = currentInd.getMarker(i);
                            if (thisMarker[0] == thisMarker[1]){
                                hap1 += thisMarker[0];
                                hap2 += thisMarker[1];
                            }else{
                                hap1 += "h";
                                hap2 += "h";
                            }
                            begin=true;
                        }
                    }
                    hap1 += "\n"; hap2+= "\n";
                    linkageToHapsWriter.write(hap1 + hap2);
                }
               else{
                    //skip if indiv is parent in trio or unaffected
                    if (!(currentInd.getMomID().equals("0") || currentInd.getDadID().equals("0") || currentInd.getAffectedStatus() != 2)){
                        //trio
                        String dadT = new String("");
                        String dadU = new String("");
                        String momT = new String("");
                        String momU = new String("");
                        if (!(usedParents.contains( currentInd.getFamilyID() + " " + currentInd.getMomID()) ||
                                usedParents.contains(currentInd.getFamilyID() + " " + currentInd.getDadID()))){
                            //add 4 phased haps provided that we haven't used this trio already
                            numMarkers = currentInd.getNumMarkers();
                            for (int i = 0; i < numMarkers; i++){
                                if (markerResults[i]){
                                    if (begin){
                                        dadT+=" ";dadU+=" ";momT+=" ";momU+=" ";
                                    }
                                    byte[] thisMarker = currentInd.getMarker(i);
                                    int kid1 = thisMarker[0];
                                    int kid2 = thisMarker[1];

                                    thisMarker = (currentFamily.getMember(currentInd.getMomID())).getMarker(i);
                                    int mom1 = thisMarker[0];
                                    int mom2 = thisMarker[1];
                                    thisMarker = (currentFamily.getMember(currentInd.getDadID())).getMarker(i);
                                    int dad1 = thisMarker[0];
                                    int dad2 = thisMarker[1];

                                    if (kid1==0 || kid2==0){
                                        //kid missing
                                        if (dad1==dad2){dadT += dad1; dadU +=dad1;}
                                        else{dadT+="h"; dadU+="h";}
                                        if (mom1==mom2){momT+=mom1; momU+=mom1;}
                                        else{momT+="h"; momU+="h";}
                                    }else if (kid1==kid2){
                                        //kid homozygous
                                        if(dad1==0){dadT+=kid1;dadU+="0";}
                                        else if (dad1==kid1){dadT+=dad1;dadU+=dad2;}
                                        else {dadT+=dad2;dadU+=dad1;}

                                        if(mom1==0){momT+=kid1;momU+="0";}
                                        else if (mom1==kid1){momT+=mom1;momU+=mom2;}
                                        else {momT+=mom2;momU+=mom1;}
                                    }else{
                                        //kid heterozygous and this if tree's a bitch
                                        if(dad1==0 && mom1==0){
                                            //both missing
                                            dadT+="0";dadU+="0";momT+="0";momU+="0";
                                        }else if (dad1==0 && mom1 != mom2){
                                            //dad missing mom het
                                            dadT+="0";dadU+="0";momT+="h";momU+="h";
                                        }else if (mom1==0 && dad1 != dad2){
                                            //dad het mom missing
                                            dadT+="h"; dadU+="h"; momT+="0"; momU+="0";
                                        }else if (dad1==0 && mom1 == mom2){
                                            //dad missing mom hom
                                            momT += mom1; momU += mom1; dadU+="0";
                                            if(kid1==mom1){dadT+=kid2;}else{dadT+=kid1;}
                                        }else if (mom1==0 && dad1==dad2){
                                            //mom missing dad hom
                                            dadT+=dad1;dadU+=dad1;momU+="0";
                                            if(kid1==dad1){momT+=kid2;}else{momT+=kid1;}
                                        }else if (dad1==dad2 && mom1 != mom2){
                                            //dad hom mom het
                                            dadT+=dad1; dadU+=dad2;
                                            if(kid1==dad1){momT+=kid2;momU+=kid1;
                                            }else{momT+=kid1;momU+=kid2;}
                                        }else if (mom1==mom2 && dad1!=dad2){
                                            //dad het mom hom
                                            momT+=mom1; momU+=mom2;
                                            if(kid1==mom1){dadT+=kid2;dadU+=kid1;
                                            }else{dadT+=kid1;dadU+=kid2;}
                                        }else if (dad1==dad2 && mom1==mom2){
                                            //mom & dad hom
                                            dadT+=dad1; dadU+=dad1; momT+=mom1; momU+=mom1;
                                        }else{
                                            //everybody het
                                            dadT+="h";dadU+="h";momT+="h";momU+="h";
                                        }
                                    }
                                    begin=true;
                                }
                            }
                            momT+="\n";momU+="\n";dadT+="\n";dadU+="\n";
                            linkageToHapsWriter.write(currentInd.getFamilyID()+"-"+currentInd.getDadID()+"\tT\t" + dadT);
                            linkageToHapsWriter.write(currentInd.getFamilyID()+"-"+currentInd.getDadID()+"\tU\t" + dadU);
                            linkageToHapsWriter.write(currentInd.getFamilyID()+"-"+currentInd.getMomID()+"\tT\t" + momT);
                            linkageToHapsWriter.write(currentInd.getFamilyID()+"-"+currentInd.getMomID()+"\tU\t" + momU);

                            usedParents.add(currentInd.getFamilyID()+" "+currentInd.getDadID());
                            usedParents.add(currentInd.getFamilyID()+" "+currentInd.getMomID());
                        }
                    }
                }
            }
        }
        linkageToHapsWriter.close();

    }



}

