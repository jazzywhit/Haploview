/*
 * $Id: MarkerResult.java,v 3.10 2007/03/12 19:20:15 djbender Exp $
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

import edu.mit.wi.haploview.Util;
import java.text.*;
import java.util.Locale;
import java.util.Vector;

/**
 * <p>Title: MarkerResult.java </p>
 * <p>Description: Gets the result for a marker
 * Result includes observed heterozyosity, predicted heterozygosity,
 * Hardy-Weinberg test p-value, genotyped percent, number of families with
 * a fully genotyped trio, number of Mendelian inheritance errors and rating.</p>
 * @author Hui Gong
 * @version $Revision 1.2 $
 */

public class MarkerResult {

	private double _obsHET;
	private double _predHET;
    private double _maf;
    private String _minorAllele;
    private String _majorAllele;
    private double _HWpval;
	private double _genoPercent;
	private int _famTrioNum;
	private int _mendErrNum;
	private int _rating;
    private Vector mendelErrors;

    private static NumberFormat nf = NumberFormat.getInstance(Locale.US);
    private static NumberFormat pctNF = NumberFormat.getInstance(Locale.US);

    static {
        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);

        pctNF.setMinimumFractionDigits(0);
        pctNF.setMaximumFractionDigits(1);
    }

	/**
	 * Sets observed heterozygosity
	 */
	public void setObsHet(double obsHet){
		this._obsHET = obsHet;
	}

	/**
	 * Sets predicted heterozygosity
	 */
	public void setPredHet(double predHet){
		this._predHET = predHet;
	}

	/**
	 * Sets Hardy-Weinberg test p-value
	 */
	public void setHWpvalue(double pvalue){
		this._HWpval = pvalue;
	}

	/**
	 * Sets percent of individuals genotyped
	 */
	public void setGenoPercent(double genoPct){
		this._genoPercent = genoPct;
	}

	/**
	 * Sets # of families with a fully genotyped trio
	 */
	public void setFamTrioNum(int num){
		this._famTrioNum = num;
	}

	/**
	 * Sets # of Mendelian inheritance errors
	 */
	public void setMendErrNum(int num){
		this._mendErrNum = num;
	}

    /**
     * Sets minor allele frequency
     * @param maf - minor allele frequency
     */
    public void setMAF(double maf) {
        this._maf = maf;
    }

    /**
     * Sets minor allele
     */
    public void setMinorAllele(String minallele) {
        this._minorAllele = minallele;
    }

    /**
     * Sets major allele
     */
    public void setMajorAllele(String majallele){
        this._majorAllele = majallele;
    }

    /**
     * Sets the data rating
     */
    public void setRating(int rating){
		this._rating = rating;
	}

    public void setMendelErrors(Vector mends){
        mendelErrors = mends;
    }

    /**
	 * Gets observed heterozygosity
	 */
	public double getObsHet(){
		return Double.parseDouble(nf.format(this._obsHET));
	}

    /**
     * returns minor allele frequency
     * @return  minor allele frequency
     */
    public double getMAF(){
        return Double.parseDouble(nf.format(this._maf));
    }

    /**
     * returns minor allele
     */
    public String getMinorAllele(){
        return this._minorAllele;
    }

    /**
     * returns major allele
     */
    public String getMajorAllele(){
        return this._majorAllele;
    }

    /**
     * Gets predicted heterozygosity
	 */
	public double getPredHet(){
		return Double.parseDouble(nf.format(this._predHET));
	}

	/**
	 * Gets Hardy-Weinberg test p-value
	 */
	public String getHWpvalue(){
        //the old formatting was cutting off anything less than 0.001
        return  Util.formatPValue(this._HWpval);
	}

	/**
	 * Gets percent of individuals genotyped
	 */
	public double getGenoPercent(){
        return Double.parseDouble(pctNF.format(this._genoPercent));
	}

	/**
	 * Gets # of families with a fully genotyped trio
	 */
	public int getFamTrioNum(){
		return this._famTrioNum;
	}

	/**
	 * Gets # of Mendelian inheritance errors
	 */
	public int getMendErrNum(){
		return this._mendErrNum;
	}


	/**
	 * Gets the data rating
	 */
	public int getRating(){
		return this._rating;
	}

    public Vector getMendelErrors(){
        return mendelErrors;
    }

    public String toString(){
		StringBuffer buffer = new StringBuffer();
		NumberFormat format=NumberFormat.getInstance();
		format.setMaximumFractionDigits(3);
		format.setMinimumFractionDigits(3);

        buffer.append(format.format(this._obsHET)).append("\t").append(format.format(this._predHET)).append("\t");

		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(0);
        buffer.append(format.format(this._HWpval)).append("\t");

		format.setMaximumFractionDigits(1);
		format.setMinimumFractionDigits(1);
        buffer.append(format.format(this._genoPercent)).append("\t");
        buffer.append(this._famTrioNum).append("\t");
		if(this._mendErrNum < 0) buffer.append("   \t");
		else buffer.append(this._mendErrNum).append("\t");
		buffer.append(this._rating);
		return buffer.toString();
	}
}
