/*
 * $Id: CheckData.java,v 1.4 2003/09/26 21:11:05 jcbarret Exp $
 * WHITEHEAD INSTITUTE
 * SOFTWARE COPYRIGHT NOTICE AGREEMENT
 * This software and its documentation are copyright 2003 by the
 * Whitehead Institute for Biomedical Research.  All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support
 * whatsoever.  The Whitehead Institute can not be responsible for its
 * use, misuse, or functionality.
 */

package edu.mit.wi.pedfile;

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

    static public double hwCut = 0.001;
    static public double failedGenoCut = 75;
    static public int numMendErrCut = 1;
	//private int _size;
	//private Vector _pedFileEntries;
	//private Hashtable pedFileHash;

	public CheckData(PedFile pedFile) {
		this._pedFile = pedFile;
	}

	/**
	 * checks the pedigree file
	 * @return Vector includes information about observed heterozyosity,
	 * predicted heterozygosity, Hardy-Weinberg test p-value, genotyped percent,
	 * number of families with a fully genotyped trio and number of Mendelian inheritance errors.
	 */
	public Vector check() throws PedFileException{
		Vector results = new Vector();
		//_size = _pedFile.getNumIndividuals();

		int numOfMarkers = _pedFile.getNumMarkers();
		//not worrying about names right now
		//TODO: store and use marker names
		//Vector names = this._pedFile.getMarkerNames();
		Vector names = null;
		boolean withName=false;
		if(names!=null && names.size()==numOfMarkers) {
			withName = true;
		}
		for(int i= 0; i < numOfMarkers; i++){
			MarkerResult markerResult;
			if(withName) {
				markerResult = checkMarker(i, (String)names.get(i));
			}else{
				markerResult = checkMarker(i, new String("Marker " + (i+1)));
			}
			results.add(markerResult);
		}
		return results;
	}

	private MarkerResult checkMarker(int loc, String name)throws PedFileException{
		MarkerResult result = new MarkerResult();
		Individual currentInd;
		//int indivgeno=0,
        int missing=0, parenthet=0, mendErrNum=0;
		int allele1=0, allele2=0, hom=0, het=0;
		//Hashtable allgenos = new Hashtable();
		Hashtable numindivs=new Hashtable();
		Hashtable parentgeno = new Hashtable();
		Hashtable kidgeno = new Hashtable();
		Hashtable parenthom = new Hashtable();
		Hashtable count = new Hashtable();
		String allele1_string, allele2_string;

		//loop through each family, check data for marker loc
		Enumeration famList = _pedFile.getFamList();
		while(famList.hasMoreElements()){
			Family currentFamily = _pedFile.getFamily((String)famList.nextElement());
			Enumeration indList = currentFamily.getMemberList();
			//loop through each individual in the current Family
			while(indList.hasMoreElements()){
				currentInd = currentFamily.getMember((String)indList.nextElement());
				if (currentInd.getIsTyped()){
					byte[] markers = currentInd.getMarker(loc);
					allele1 = markers[0];
					allele1_string = Integer.toString(allele1);
					allele2 = markers[1];
					allele2_string = Integer.toString(allele2);

					String familyID = currentInd.getFamilyID();

					incOrSetOne(numindivs,familyID);

					//no allele data missing
					if(allele1 > 0 && allele2 >0){
						//make sure entry has parents
						if(!(currentInd.getMomID().equals("0") || currentInd.getDadID().equals("0"))){
							//do mendel check
							//byte[] marker = ((Individual)pedFileHash.get(familyID + " " + currentInd.getMomID())).getMarker(loc);
							byte[] marker = (currentFamily.getMember(currentInd.getMomID())).getMarker(loc);
							int momAllele1 = marker[0];
							int momAllele2 = marker[1];
							//marker = ((Individual)pedFileHash.get(familyID + " " + currentInd.getDadID())).getMarker(loc);
							marker = (currentFamily.getMember(currentInd.getDadID())).getMarker(loc);
							int dadAllele1 = marker[0];
							int dadAllele2 = marker[1];

							//don't check if parents are missing any data
							if (!(momAllele1 == 0 || momAllele2 == 0 || dadAllele1 == 0 || dadAllele2 ==0)){
								//mom hom
								if(momAllele1 == momAllele2){
									//both parents hom
									if (dadAllele1 == dadAllele2){
										//both parents hom same allele
										if (momAllele1 == dadAllele1){
											//kid must be hom same allele
											if (allele1 != momAllele1 || allele2 != momAllele1) {
                                                mendErrNum ++;
                                                currentInd.zeroOutMarker(loc);
                                                currentFamily.getMember(currentInd.getMomID()).zeroOutMarker(loc);
                                                currentFamily.getMember(currentInd.getDadID()).zeroOutMarker(loc);
                                            }
											//parents hom diff allele
										}else{
											//kid must be het
											if (allele1 == allele2) {
                                                mendErrNum++;
                                                currentInd.zeroOutMarker(loc);
                                                currentFamily.getMember(currentInd.getMomID()).zeroOutMarker(loc);
                                                currentFamily.getMember(currentInd.getDadID()).zeroOutMarker(loc);
                                            }
										}
										//mom hom dad het
									}else{
										//kid can't be hom for non-momallele
										if (allele1 != momAllele1 && allele2 != momAllele1){
                                            mendErrNum++;
                                            currentInd.zeroOutMarker(loc);
                                            currentFamily.getMember(currentInd.getMomID()).zeroOutMarker(loc);
                                            currentFamily.getMember(currentInd.getDadID()).zeroOutMarker(loc);
                                        }
									}
									//mom het
								}else{
									//dad hom
									if (dadAllele1 == dadAllele2){
										//kid can't be hom for non-dadallele
										if(allele1 != dadAllele1 && allele2 != dadAllele1){
                                            mendErrNum++;
                                            currentInd.zeroOutMarker(loc);
                                            currentFamily.getMember(currentInd.getMomID()).zeroOutMarker(loc);
                                            currentFamily.getMember(currentInd.getDadID()).zeroOutMarker(loc);
                                        }
									}
									//both parents het no mend err poss
								}
							}
						}
						//end mendel check
                    }
                }
            }
            indList = currentFamily.getMemberList();
            //loop through each individual in the current Family
            while(indList.hasMoreElements()){
                currentInd = currentFamily.getMember((String)indList.nextElement());
                if (currentInd.getIsTyped()){
                    byte[] markers = currentInd.getMarker(loc);
                    allele1 = markers[0];
                    allele1_string = Integer.toString(allele1);
                    allele2 = markers[1];
                    allele2_string = Integer.toString(allele2);

                    String familyID = currentInd.getFamilyID();

                    incOrSetOne(numindivs,familyID);

                    //no allele data missing
                    if(allele1 > 0 && allele2 >0){
                        //indiv has parents
                        if(currentInd.getMomID().compareTo(Individual.DATA_MISSING)==0 && currentInd.getDadID().compareTo(Individual.DATA_MISSING)==0){
                            //$parentgeno{$ped}++
							//set parentgeno
							incOrSetOne(parentgeno,familyID);
							if(allele1 != allele2) {
								parenthet++;
							}
							else{
								incOrSetOne(parenthom,allele1_string);
							}
						}
						else{//$kidgeno{$ped}++
							incOrSetOne(kidgeno,familyID);
						}
						if(allele1 == allele2) {
							hom++;
						}
						else {
							het++;
						}
						//count number of allele
						incOrSetOne(count,allele1_string);
						incOrSetOne(count,allele2_string);
					}
					//missing data
					else missing++;
				}
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
    * checks for a value with key key in the hashtable ht. if it exists, it is incremented
    * if there is no value the value is set to one
    * @param ht hashtable
    * @param key key of the value we want to increment or set to one
    */
	private void incOrSetOne(Hashtable ht, String key){
		if(ht.containsKey(key)){
			int value = Integer.parseInt((String)ht.get(key));
			ht.put(key, Integer.toString(++value));
		}
		else{
			ht.put(key, "1");
		}
	}

	/**
	 * Gets observed heterozygosity
	 */
	private double getObsHET(int het, int hom){
        double obsHET;
        if (het+hom == 0){
            obsHET = 0;
        }else{
            obsHET = het/(het+hom+0.0);
        }
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
		double preHet;
        if (sum == 0){
            preHet = 0;
        }else{
            preHet = 1.0 - (sumsq/((sum*sum)+0.0));
        }
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
		//HW hw = new HW((double)homA, (double)parentHet, (double)homB);
			//hw.caculate();
			pvalue = hwCalculate((double)homA, (double)parentHet, (double)homB);
		return pvalue;
	}


	/**
	 * Does the calculation
	 */
	private double hwCalculate(double obsAA, double obsAB, double obsBB){
		double obs[]={0.0, obsAA, obsAB, obsBB};
		double expect[]={0.0, 0.0, 0.0, 0.0};
		double sum_obs;
		//double csq, df, sum_expect;
        double prob, p, start, end;
		double best_prob =-1.0;
		double best_p=0;
		sum_obs = obs [1 ]+ obs [2 ]+ obs [3 ];
		for (p = 0.01 ; p <= .99 ; p += .01 ) {
			expect [1 ]= sum_obs * p * p ;
			expect [2 ]= sum_obs * 2.0 * p * (1.0 - p);
			expect [3 ]= sum_obs * (1.0 - p) * (1.0 - p);
			//Chsone chsone = new Chsone(obs , expect , 3 , 1);
			//chsone.caculate();
			//prob = chsone.getPvalue();
			prob = chsoneCalculate(obs,expect,3,1);
			if (prob > best_prob ) {
				best_prob = prob ;
				best_p = p ;
			}
		}
		start = (best_p - .025 > .001)? (best_p - .025): .001 ;
		end = (best_p + .025 < .999)? (best_p + .025): .999 ;
		for (p = start ; p <= end ; p += .001 ) {
			expect [1 ]= sum_obs * p * p ;
			expect [2 ]= sum_obs * 2.0 * p * (1.0 - p) ;
			expect [3 ]= sum_obs * (1.0 - p) * (1.0 - p) ;
			//Chsone chsone = new Chsone(obs , expect , 3 , 1);
			//chsone.caculate();
			//prob = chsone.getPvalue();
			prob = chsoneCalculate(obs,expect,3,1);
			if (prob > best_prob ) {
				best_prob = prob ;
				best_p = p ;
			}
		}
		p = best_p ;
		expect [1 ]= sum_obs * p * p ;
		expect [2 ]= sum_obs * 2.0 * p * (1.0 - p);
		expect [3 ]= sum_obs * (1.0 - p) * (1.0 - p);
		//Chsone chsone = new Chsone(obs , expect , 3 , 1);
		//chsone.caculate();
		//this._p = chsone.getPvalue();
		//return chsone.getPvalue();
		return chsoneCalculate(obs,expect,3,1);
	}

	/*
	* Description: Uses it to compare binned data to a model distribution.
	* This is converted from a numerical recipes class in c</p>
	* @author Hui Gong
	*/
	public double chsoneCalculate(double[] bins, double[] ebins, int nbins, int knstrn){
		double prob, df, chsq, temp;
		df = nbins - knstrn ;
		chsq = 0.0 ;
		for (int j = 1 ; j <= nbins ; j ++ ) {
			if (ebins [j ]<= 0.0 ){
                chsq=0;
            }else{
                temp = bins[j]- ebins[j];
                chsq += temp * temp / ebins [j];
            }
		}
		prob = MathUtil.gammq (0.5 * df, 0.5 * chsq );
		return prob;
		//this._chisq = chsq;
	}

	private double getGenoPercent(int het, int hom, int missing){
		double genoPct = 100.0*(het+hom)/(het+hom+missing);
		return genoPct;
	}

	private int getNumOfFamTrio(Hashtable numindivs, Hashtable parentgeno, Hashtable kidgeno){
		//int totalfams = 0;
        int tdtfams =0;
		Enumeration enuKey = numindivs.keys();
		while(enuKey.hasMoreElements()){
			//totalfams++;
			int parentGeno=0, kidsGeno =0;
			String key = (String)enuKey.nextElement();
			Object pGeno = parentgeno.get(key);
			Object kGeno = kidgeno.get(key);
			if(pGeno != null) parentGeno = Integer.parseInt((String)pGeno);
			if(kGeno != null) kidsGeno = Integer.parseInt((String)kGeno);
			if(parentGeno>=2 && kidsGeno>=1) tdtfams += parentGeno/2;
		}
		return tdtfams;
	}

	private int getRating(double genopct, double pval, double obsHet, int menderr){
		int rating = 0;
		if (obsHet < 0.01){
			rating -= 1;
		}
        if (genopct < failedGenoCut){
			rating -= 2;
		}
        if (pval < hwCut){
			rating -= 4;
		}
        if (menderr > numMendErrCut){
			rating -= 8;
		}
        if (rating == 0){
			rating = 1;
		}

		return rating;
	}
}
