package edu.mit.wi.haploview;

import java.io.*;
import java.text.*;
import java.util.*;

class TextMethods {

    public void saveHapsToText(Haplotype[][] finishedHaplos, File saveHapsFile) throws IOException{

	NumberFormat nf = NumberFormat.getInstance();
	nf.setMinimumFractionDigits(3);
	nf.setMaximumFractionDigits(3);
	
	//open file for saving haps text
	FileWriter saveHapsWriter = new FileWriter(saveHapsFile);
	
	int[][]lookupPos = new int[finishedHaplos.length][];
	for (int p = 0; p < lookupPos.length; p++){
	    lookupPos[p] = new int[finishedHaplos[p].length];
	    for (int q = 0; q < lookupPos[p].length; q++){
		lookupPos[p][finishedHaplos[p][q].getListOrder()] = q;
		//System.out.println(p + " " + q + " " + finishedHaplos[p][q].getListOrder());
	    }
	}

	//go through each block and print haplos
	for (int i = 0; i < finishedHaplos.length; i++){
	    //write block header
	    saveHapsWriter.write("BLOCK " + (i+1) + ".  MARKERS:");
	    int[] markerNums = finishedHaplos[i][0].getMarkers();
	    for (int j = 0; j < markerNums.length; j++){
		saveHapsWriter.write(" " + (markerNums[j]+1));
	    }
	    saveHapsWriter.write("\n");
	    //write haps and crossover percentages
	    for (int j = 0; j < finishedHaplos[i].length; j++){
		int curHapNum = lookupPos[i][j];
		String theHap = new String();
		int[] theGeno = finishedHaplos[i][curHapNum].getGeno();
		for (int k = 0; k < theGeno.length; k++){
		    theHap += theGeno[k];
		}
		saveHapsWriter.write(theHap + " (" + nf.format(finishedHaplos[i][curHapNum].getPercentage()) + ")");
		if (i < finishedHaplos.length-1){
		    saveHapsWriter.write("\t|");
		    for (int crossCount = 0; crossCount < finishedHaplos[i+1].length; crossCount++){
			if (crossCount != 0) saveHapsWriter.write("\t");
			saveHapsWriter.write(nf.format(finishedHaplos[i][curHapNum].getCrossover(crossCount)));
		    }
		    saveHapsWriter.write("|");
		}
		saveHapsWriter.write("\n");
	    }
	    saveHapsWriter.write("\n");
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
	Hashtable pedSizeHash = new Hashtable();
	Hashtable pedHash = pedFile.getContent();
	Enumeration pedEnum = pedHash.elements();
	Vector keys = pedFile.getKeys();
	//fill up pedSizeHash with the number of genotyped indivs in each ped
	while (pedEnum.hasMoreElements()){
	    PedFileEntry entry = (PedFileEntry)pedEnum.nextElement();
	    if (entry.getIsTyped()){
		String ped = entry.getFamilyID();
		if (pedSizeHash.containsKey(ped)){
		    int value = Integer.parseInt((String)pedSizeHash.get(ped));
		    pedSizeHash.put(ped, Integer.toString(++value));
		}else{
		    pedSizeHash.put(ped, "1");
		}
	    }
	}
	Vector usedParents = new Vector();
	for (int x = 0; x < keys.size(); x++){
	    PedFileEntry entry = (PedFileEntry)pedHash.get(keys.elementAt(x));
	    boolean begin = false;    //this boolean lets us do spacing correctly
	    if (entry.getIsTyped()){
		String ped = entry.getFamilyID();
		Vector markers = entry.getAllMarkers();
		//singleton
		if (Integer.parseInt((String)pedSizeHash.get(ped)) == 1){
		    String hap1 = new String(""); String hap2 = new String("");
		    hap1 += ped + "\t" + entry.getIndivID() + "\t";
		    hap2 += ped + "\t" + entry.getIndivID() + "\t";
		    for (int i = 0; i < markers.size(); i++){
			if (markerResults[i]){
			    if (begin){
				hap1+=" "; hap2+=" ";
			    }
			    PedMarker thisMarker = (PedMarker)markers.elementAt(i);
			    if (thisMarker.getAllele1() == thisMarker.getAllele2()){
				hap1 += thisMarker.getAllele1();
				hap2 += thisMarker.getAllele2();
			    }else{
				hap1 += "h";
				hap2 += "h";
			    }
			    begin=true;
			}
		    }
		    hap1 += "\n"; hap2+= "\n";
		    linkageToHapsWriter.write(hap1 + hap2);
		}else{
		    //skip if indiv is parent in trio or unaffected
		    if (!(entry.getMomID().equals("0") || entry.getDadID().equals("0") || entry.getAffectedStatus() != 2)){
			//trio
			String dadT = new String(""); String dadU = new String("");
			String momT = new String(""); String momU = new String("");
			if (!(usedParents.contains(ped + " " + entry.getMomID()) || 
			      usedParents.contains(ped + " " + entry.getDadID()))){
			    //add 4 phased haps provided that we haven't used this trio already
			    
			    for (int i = 0; i < markers.size(); i++){
				if (markerResults[i]){
				    if (begin){
					dadT+=" ";dadU+=" ";momT+=" ";momU+=" ";
				    }
				    PedMarker thisMarker = (PedMarker)markers.elementAt(i);
				    int kid1 = thisMarker.getAllele1();
				    int kid2 = thisMarker.getAllele2();
				    //System.out.println(entry.getMomID());
				    int mom1 = ((PedFileEntry)pedHash.get(ped + " " + entry.getMomID())).getMarker(i).getAllele1();
				    int mom2 = ((PedFileEntry)pedHash.get(ped + " " + entry.getMomID())).getMarker(i).getAllele2();
				    int dad1 = ((PedFileEntry)pedHash.get(ped + " " + entry.getDadID())).getMarker(i).getAllele1();
				    int dad2 = ((PedFileEntry)pedHash.get(ped + " " + entry.getDadID())).getMarker(i).getAllele2();
				    
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
			    linkageToHapsWriter.write(ped+"-"+entry.getDadID()+"\tT\t" + dadT);
			    linkageToHapsWriter.write(ped+"-"+entry.getDadID()+"\tU\t" + dadU);
			    linkageToHapsWriter.write(ped+"-"+entry.getMomID()+"\tT\t" + momT);
			    linkageToHapsWriter.write(ped+"-"+entry.getMomID()+"\tU\t" + momU);
			    
			    usedParents.add(ped+" "+entry.getDadID());
			    usedParents.add(ped+" "+entry.getMomID());
			}
		    }
		}
	    }
	}
	linkageToHapsWriter.close();
    }
		
}

