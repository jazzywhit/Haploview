package edu.mit.wi.haploview;

/**
 * <p>Title: Chsone.java </p>
 * <p>Description: Uses it to compare binned data to a model distribution.
 * This is converted from a numerical recipes class in c</p>
 * @author Hui Gong
 * @version $Revision 1.1 $
 */

public class Chsone {
    private double[] _bins;
    private double[] _ebins;
    private int _nbins;
    private int _knstrn;
    private double _pvalue;
    private double _chisq;

    /**
     * constructor
     * @param bins observed result
     * @param ebins expected result
     */
    public Chsone(double[] bins, double[] ebins, int nbins, int knstrn) {
        this._bins = bins;
        this._ebins = ebins;
        this._nbins = nbins;
        this._knstrn = knstrn;
    }

    public void caculate() throws CheckDataException{
        double prob, df, chsq, temp;
        df = _nbins - _knstrn ;
        chsq = 0.0 ;
        for (int j = 1 ; j <= _nbins ; j ++ ) {
            if (_ebins [j ]<= 0.0 )
                throw new CheckDataException("Bad expected number in chsone" );
            temp = _bins [j ]- _ebins [j ];
            chsq += temp * temp / _ebins [j];
        }
        prob = MathUtil.gammq (0.5 * df, 0.5 * chsq );
        this._pvalue = prob;
        this._chisq = chsq;

    }

    /**
     * gets P value
     */
    public double getPvalue(){
        return this._pvalue;

    }

    /**
     * Gets Chisquare
     */
    public double getChisq(){
        return this._chisq;
    }
}
