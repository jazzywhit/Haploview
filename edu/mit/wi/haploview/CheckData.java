package edu.mit.wi.haploview;

import java.util.*;

/**
 * <p>Title: CheckData.java </p>
 * <p>Description: Used to check pedigree file and return information about observed heterozyosity,
 * predicted heterozygosity, Hardy-Weinberg test p-value, genotyped percent,
 * number of families with a fully genotyped trio and number of Mendelian inheritance errors.</p>
 * @author Hui Gong
 * @version $Revision 1.2 $
 */

public class CheckData {
    private PedFile _pedFile;
    private int _size;
    private Vector _pedFileEntries;
    private Hashtable pedFileHash;

    public CheckData(PedFile pedFile) {
        this._pedFile = pedFile;
    }

    /**
     * checks the pedigree file
     * @return PedResult includes information about observed heterozyosity,
     * predicted heterozygosity, Hardy-Weinberg test p-value, genotyped percent,
     * number of families with a fully genotyped trio and number of Mendelian inheritance errors.
     */
    public PedResult check(){
        PedResult results = new PedResult();
	pedFileHash = this._pedFile.getContent();
        _pedFileEntries = new Vector(pedFileHash.values());
        _size = _pedFileEntries.size();
        int numOfMarkers = ((PedFileEntry)_pedFileEntries.get(0)).getAllMarkers().size();
        Vector names = this._pedFile.getMarkerNames();
        boolean withName=false;
        if(names!=null && names.size()==numOfMarkers) withName = true;
        for(int i= 0; i < numOfMarkers; i++){
            MarkerResult markerResult;
            if(withName) markerResult = checkMarker(i, (String)names.get(i));
            else markerResult = checkMarker(i, new String("Marker " + (i+1)));
            results.addMarkerResult(markerResult);
        }
        return results;
    }

    private MarkerResult checkMarker(int loc, String name){
        MarkerResult result = new MarkerResult();
        PedFileEntry entry;
        int indivgeno=0, missing=0, parenthet=0, mendErrNum=0;
        int allele1=0, allele2=0, hom=0, het=0;
	Hashtable allgenos = new Hashtable();
        Hashtable numindivs=new Hashtable();
        Hashtable parentgeno = new Hashtable();
        Hashtable kidgeno = new Hashtable();
        Hashtable parenthom = new Hashtable();
        Hashtable count = new Hashtable();
        String allele1_string, allele2_string;

        //loop through the pedFileEntry, check data for marker loc
        for(int i=0; i<_size; i++){
            entry = (PedFileEntry)_pedFileEntries.get(i);
	    if (entry.getIsTyped()){
		PedMarker markers = entry.getMarker(loc);
		allele1 = markers.getAllele1();
		allele1_string = Integer.toString(allele1);
		allele2 = markers.getAllele2();
		allele2_string = Integer.toString(allele2);
		
		String ped = entry.getFamilyID();
		if(numindivs.containsKey(ped)){
		    int value = Integer.parseInt((String)numindivs.get(ped));
		    numindivs.put(ped, Integer.toString(++value));
		}
		else{
		    numindivs.put(ped, "1");
		}

		//no allele data missing
		if(allele1 > 0 && allele2 >0){
		    //make sure entry has parents
		    if(!(entry.getMomID().equals("0") || entry.getDadID().equals("0"))){
			//do mendel check
			int momAllele1 = ((PedFileEntry)pedFileHash.get(ped + " " + entry.getMomID())).getMarker(loc).getAllele1();
			int momAllele2 = ((PedFileEntry)pedFileHash.get(ped + " " + entry.getMomID())).getMarker(loc).getAllele2();
			int dadAllele1 = ((PedFileEntry)pedFileHash.get(ped + " " + entry.getDadID())).getMarker(loc).getAllele1();
			int dadAllele2 = ((PedFileEntry)pedFileHash.get(ped + " " + entry.getDadID())).getMarker(loc).getAllele2();

			//don't check if parents are missing any data
			if (!(momAllele1 == 0 || momAllele2 == 0 || dadAllele1 == 0 || dadAllele2 ==0)){
			    //mom hom
			    if(momAllele1 == momAllele2){
				//both parents hom
				if (dadAllele1 == dadAllele2){
				//both parents hom same allele
				    if (momAllele1 == dadAllele1){
					//kid must be hom same allele
					if (allele1 != momAllele1 || allele2 != momAllele1) mendErrNum ++;
				//parents hom diff allele
				    }else{
					//kid must be het
					if (allele1 == allele2) mendErrNum++;
				    }
				    //mom hom dad het
				}else{
				//kid can't be hom for non-momallele
				    if (allele1 != momAllele1 && allele2 != momAllele1) mendErrNum++;
				}
				//mom het
			    }else{
				//dad hom
				if (dadAllele1 == dadAllele2){
				//kid can't be hom for non-dadallele
				    if(allele1 != dadAllele1 && allele2 != dadAllele1) mendErrNum++;
				}
				//both parents het no mend err poss
			    }
			}
		    }
		    //end mendel check

		    indivgeno++;
		    //indiv has parents
		    if(entry.getMomID().compareTo(PedFileEntry.DATA_MISSING)==0 && entry.getDadID().compareTo(PedFileEntry.DATA_MISSING)==0){

			//$parentgeno{$ped}++
			//set parentgeno
			if(parentgeno.containsKey(ped)){
			    int value = Integer.parseInt((String)parentgeno.get(ped));
			    parentgeno.put(ped, Integer.toString(++value));
			}
			else{
			    parentgeno.put(ped, "1");
			}
			
			if(allele1 != allele2) parenthet++;
			else{
			    if(parenthom.containsKey(allele1_string)){
				int value = Integer.parseInt((String)parenthom.get(allele1_string));
				parenthom.put(allele1_string, Integer.toString(++value));
			    }
			    else{
				parenthom.put(Integer.toString(allele1), "1");
			    }
			}
		    }
		    else{//$kidgeno{$ped}++
			if(kidgeno.containsKey(ped)){
			    int value = Integer.parseInt((String)kidgeno.get(ped));
			    kidgeno.put(ped, Integer.toString(++value));
			}
			else{
			    kidgeno.put(ped, "1");
			}
		    }
		    if(allele1 == allele2) hom++;
		    else het++;
		    //count number of allele
		    if(count.containsKey(allele1_string)){
			int value = Integer.parseInt((String)count.get(allele1_string));
			count.put(allele1_string, Integer.toString(++value));
		    }
		    else{
			count.put(allele1_string, "1");
		    }
		    if(count.containsKey(allele2_string)){
			int value = Integer.parseInt((String)count.get(allele2_string));
			count.put(allele2_string, Integer.toString(++value));
		    }
		    else{
			count.put(allele2_string, "1");
		    }
		}
		//missing data
		else missing++;
	    }
        }
        double obsHET = getObsHET(het, hom);
        double preHET = getPreHET(count);

        //HW p value
        double pvalue = getPValue(parenthom, parenthet);

        //geno percent
        double genopct = getGenoPercent(het, hom, missing);

        // num of families with a fully genotyped trio
        //int famTrio =0;
        int famTrio = getNumOfFamTrio(numindivs, parentgeno, kidgeno);

        //rating
        int rating = this.getRating(genopct, pvalue, obsHET, mendErrNum);

        result.setObsHet(obsHET);
        result.setPredHet(preHET);
        result.setHWpvalue(pvalue);
        result.setGenoPercent(genopct);
        result.setFamTrioNum(famTrio);
        result.setMendErrNum(mendErrNum);
        result.setRating(rating);
        result.setName(name);
        return result;
    }

    /**
     * Gets observed heterozygosity
     */
    private double getObsHET(int het, int hom){
        double obsHET = het/(het+hom+0.0);
        return obsHET;
    }

    private double getPreHET(Hashtable count){
        int sumsq=0, sum=0, num=0;
        Enumeration enu = count.elements();
        while(enu.hasMoreElements()){
            num = Integer.parseInt((String)enu.nextElement());
            sumsq += num*num;
            sum += num;
        }
        double preHet = 1.0 - (sumsq/((sum*sum)+0.0));
        return preHet;
    }


    private double getPValue(Hashtable parentHom, int parentHet){
        //ie: 11 13 31 33 -> homA =1 homB = 1 parentHet=2
        int homA=0, homB=0, num;
        double pvalue=0;
        Enumeration enu = parentHom.elements();
        while(enu.hasMoreElements()){
             num = Integer.parseInt((String)enu.nextElement());
             if(homA>0) homB = num;
             else homA = num;
        }
        //caculate p value from homA, parentHet and homB
        // using hw
        //System.out.println("homA="+homA+" homB="+homB+" parentHet="+parentHet);
        HW hw = new HW((double)homA, (double)parentHet, (double)homB);
        try{
            hw.caculate();
            pvalue = hw.getPvalue();
        }
        catch(CheckDataException e){
            System.out.println("input data error in caculating p value");
            System.out.println(e.getMessage());
        }
        return pvalue;
    }

    private double getGenoPercent(int het, int hom, int missing){
        double genoPct = 100.0*(het+hom)/(het+hom+missing);
        return genoPct;
    }

    private int getNumOfFamTrio(Hashtable numindivs, Hashtable parentgeno, Hashtable kidgeno){
        int totalfams = 0, tdtfams =0;
        Enumeration enuKey = numindivs.keys();
        while(enuKey.hasMoreElements()){
            totalfams++;
            int parentGeno=0, kidsGeno =0;
            String key = (String)enuKey.nextElement();
            Object pGeno = parentgeno.get(key);
            Object kGeno = kidgeno.get(key);
            if(pGeno != null) parentGeno = Integer.parseInt((String)pGeno);
            if(kGeno != null) kidsGeno = Integer.parseInt((String)kGeno);
            if(parentGeno>=2 && kidsGeno>=1) tdtfams++;
        }
        return tdtfams;
    }

    private int getRating(double genopct, double pval, double obsHet, int menderr){
        int rating;
	if (obsHet < 0.01){
	    rating = -1;
	}else if (genopct < 75.00){
	    rating = -2;
	}else if (pval < 0.01){
	    rating=-3;
	}else if (menderr > 0){
	    rating=-4;
	}else{
	    rating=1;
	}

        return rating;
    }
}
