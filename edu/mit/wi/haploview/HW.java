package edu.mit.wi.haploview;

/**
 * <p>Title: HW.java </p>
 * <p>Description: This is used to caculate Hardy Weinberg p value</p>
 * @author Hui Gong
 * @version $Revision 1.1 $
 */

public class HW {
    private double _obsAA;
    private double _obsAB;
    private double _obsBB;
    private double _p;
    private double _chisq;

    public HW(double obsAA, double obsAB, double obsBB) {
        this._obsAA = obsAA;
        this._obsAB = obsAB;
        this._obsBB = obsBB;
    }

    /**
     * Gets Hardy Weinberg p value
     */
    public double getPvalue(){
        return this._p;
    }

    /**
     * Gets Chisquare
     */
    public double getChisq(){
        return this._chisq;
    }

    /**
     * Does the caculation, run this before get the p value and chisquare
     */
    public void caculate() throws CheckDataException{
            double obs[]={0.0, this._obsAA, this._obsAB, this._obsBB};
            double expect[]={0.0, 0.0, 0.0, 0.0};
            double sum_obs;
            double sum_expect, df, csq, prob, p, start, end;
            double best_prob =-1.0;
            double best_p=0;
            sum_obs = obs [1 ]+ obs [2 ]+ obs [3 ];
            for (p = 0.01 ; p <= .99 ; p += .01 ) {
                expect [1 ]= sum_obs * p * p ;
                expect [2 ]= sum_obs * 2.0 * p * (1.0 - p);
                expect [3 ]= sum_obs * (1.0 - p) * (1.0 - p);
                Chsone chsone = new Chsone(obs , expect , 3 , 1);
                chsone.caculate();
                prob = chsone.getPvalue();
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
                Chsone chsone = new Chsone(obs , expect , 3 , 1);
                chsone.caculate();
                prob = chsone.getPvalue();
                if (prob > best_prob ) {
                    best_prob = prob ;
                    best_p = p ;
                }
            }
            p = best_p ;
            expect [1 ]= sum_obs * p * p ;
            expect [2 ]= sum_obs * 2.0 * p * (1.0 - p);
            expect [3 ]= sum_obs * (1.0 - p) * (1.0 - p);
            Chsone chsone = new Chsone(obs , expect , 3 , 1);
            chsone.caculate();
            this._p = chsone.getPvalue();
    }
}
