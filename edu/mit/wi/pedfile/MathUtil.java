/*
 * $Id: MathUtil.java,v 3.0 2005/01/27 18:19:03 jcbarret Exp $
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

/**
 * <p>Title: MathUtil.java </p>
 * <p>Description: Includes a list of math functions translated from numerical recipes in C</p>
 * @author Hui Gong
 * @version $Revision 1.1 $
 */

import java.lang.Math;

public class MathUtil {
	private static final int ITMAX = 100;
	private static double EPS = 3.0e-7;
	private static double FPMIN = 1.0e-30;

	public static double gammq (double a, double x) {
		double gamser, gammcf; //, gln;
		//if ((x < 0.0 )|| (a <= 0.0 ))
		//	throw new CheckDataException("Invalid arguments in routine gammq" );
        try{
            if (x < (a + 1.0) ) {
                gamser = gser(a , x);
                return (1.0 - gamser) ;
            }
            else {
                gammcf = gcf (a , x);
                return gammcf ;
            }
        }catch (CheckDataException e){
            //TODO: fix this
            return 0;
        }
    }

    public static double gcf (double a, double x) throws CheckDataException {
        int i;
        double an, gammcf, b, c, d, del, h, gln;
        gln = gammln (a );
        b = x + 1.0 - a ;
        c = 1.0 / FPMIN ;
        d = 1.0 / b ;
        h = d ;
        for (i = 1 ; i <= ITMAX ; i ++ ) {
            an = -i * (i - a);
            b += 2.0 ;
            d = an * d + b ;
            if (Math.abs(d )< FPMIN ) d = FPMIN ;
            c = b + an / c ;
            if (Math.abs (c )< FPMIN ) c = FPMIN ;
            d = 1.0 / d ;
            del = d * c ;
            h *= del ;
            if (Math.abs (del - 1.0 )< EPS ) break ;
        }

        if (i > ITMAX ){
            throw new CheckDataException("a too large, ITMAX too small in gcf");
        }
        gammcf = Math.exp (-x + a * Math.log (x )- gln)* h ;
        return gammcf;
    }

    public static double gammln (double xx){
        double x, y, tmp, ser;
        double cof[] ={76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5};
        int j;
        y = xx;
        x = xx;
        tmp = x + 5.5 ;
        tmp -= (x + 0.5) * Math.log (tmp );
        ser = 1.000000000190015 ;
        for (j = 0 ; j <= 5 ; j ++ ) ser += cof[j]/++y;
        return -tmp + Math.log (2.5066282746310005 * ser / x );
    }

    public static double gser (double a, double x) throws CheckDataException {
        int n;
        double sum, gln, del, ap, gamser = 0;
        gln = gammln(a);
        if (x <= 0.0 ) {
            if (x < 0.0 ) throw new CheckDataException("x less than 0 in routine gser" );
            gamser  = 0.0 ;
            return gamser;
        }
        else {
            ap = a ;
            del = sum = 1.0 / a ;
            for (n = 1 ; n <= ITMAX ; n ++ ) {
                ++ap;
                del *= x / ap ;
                sum += del ;
                if (Math.abs (del )< (Math.abs (sum )* EPS) ) {
                    gamser = sum * Math.exp (-x + a * Math.log (x )- gln);
                    return gamser;
                }
            }
            throw new CheckDataException("a too large, ITMAX too small in routine gser" );
        }
    }

}
