package edu.mit.wi.haploview;

/** * @(#)Functions.java * * Copyright (c) 2000 by Sundar Dorai-Raj
 * * @author Sundar Dorai-Raj
 * * Email: sdoraira@vt.edu
 * * This program is free software; you can redistribute it and/or
 * * modify it under the terms of the GNU General Public License
 * * as published by the Free Software Foundation; either version 2
 * * of the License, or (at your option) any later version,
 * * provided that any use properly credits the author.
 * * This program is distributed in the hope that it will be useful,
 * * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * * GNU General Public License for more details at http://www.gnu.org * * */

public class Functions {
    public static double lnfgamma(double c) {
        int j;
        double x,y,tmp,ser;
        double [] cof = {76.18009172947146     ,-86.50532032941677 ,
                24.01409824083091     ,-1.231739572450155 ,
                0.1208650973866179e-2,-0.5395239384953e-5};
        y = x = c;
        tmp = x + 5.5 - (x + 0.5) * Math.log(x + 5.5);
        ser = 1.000000000190015;
        for (j=0;j<=5;j++)
            ser += (cof[j] / ++y);
        return(Math.log(2.5066282746310005 * ser / x) - tmp);
    }

    public static double lnfbeta(double a,double b) {
        return(lnfgamma(a)+lnfgamma(b)-lnfgamma(a+b));
    }

    public static double fbeta(double a,double b) {
        return Math.exp(lnfbeta(a,b));
    }

    public static double fgamma(double c) {
        return Math.exp(lnfgamma(c));
    }

    public static double fact (int n) {
        return Math.exp(lnfgamma(n+1));
    }

    public static double lnfact(int n) {
        return lnfgamma(n+1);
    }

    public static double nCr(int n,int r) {
        return Math.exp(lnfact(n)-lnfact(r)-lnfact(n-r));
    }

    public static double nPr(int n,int r) {
        return Math.exp(lnfact(n)-lnfact(r));
    }

    public static double[] sort(double[] x) {
        int n=x.length,incr=n/2;
        while (incr >= 1) {
            for (int i=incr;i<n;i++) {
                double temp=x[i];
                int j=i;
                while (j>=incr && temp<x[j-incr]) {
                    x[j]=x[j-incr];
                    j-=incr;
                }
                x[j]=temp;
            }
            incr/=2;
        }
        return(x);
    }
}