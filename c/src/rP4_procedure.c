/* Like reconstruct.c, but instead of computing haplotype frequencies 
   on genotype data, it does it on already somewhat phased data -
   version 2 produces new statistic - X = 95% confidence D' exceeds X */

#include <stdio.h>
#include <math.h>

#define AA 0
#define AB 1
#define BB 2
#define BA 3

#define TOLERANCE .00000001

void count_haps(double,double,double,double,double*,double*,double*,double*,int);
void estimate_p(double,double,double,double,double*,double*,double*,double*);
int unknownDH, total_chroms;
double const_prob;
double known[5];

/***** ARGUMENTS to compute_Dprime()
       a = numAA haplotypes, b = numAB haplotypes, c = numBA haplotypes, d = numBB haplotypes
       e = num double het individuals not resolved into a,b,c,d counts
       f = "pseudocount" of sorts - 
           if positive, defines initial p[haplo] flat and defines pseudocount for nAA, nAB, etc.
	   (so that double hets are considered initial conditions)
	   if negative, starts initial p[haplos] at the product of the allele frequencies
	       (that is, the values that would occur at D'=0)
	   0.1 is a good value to plug in

       Fills in the values of D', LOD, r^2, and the 95% upper and lower 
       confidence bounds on D'

*****/


compute_Dprime(int a, int b, int c, int d, int e, double f, 
	       double *finalDprime, double *finalLOD, double *finalr2, 
	       double *finalCIlow, double *finalCIhigh)
{ 
  int i,j,k,count,itmp,low_i,high_i,do_sim,sim,simhit[105],sim_tot,simsum;
  double nAA, nBB, nAB, nBA, pAA, pBB, pAB, pBA;
  double loglike, oldloglike, meand, mean2d, sd;
  double g,h,m,tmp,r;
  double num, denom1, denom2, denom, dprime, real_dprime;
  double pA1, pB1, pA2, pB2, loglike1, loglike0, r2;
  double tmpAA, tmpAB, tmpBA, tmpBB, dpr, lsurface[105], tmp2AA, tmp2AB, tmp2BA, tmp2BB;
  double total_prob, sum_prob;


  /* store arguments in externals and compute allele frequencies */
  known[AA]=(double)a; known[AB]=(double)b; known[BA]=(double)c; known[BB]=(double)d; 
  unknownDH=(double)e;
  
  total_chroms= a+b+c+d+(2*unknownDH);
  pA1 = (double) (a+b+unknownDH) / (double) total_chroms;
  pB1 = 1-pA1;
  pA2 = (double) (a+c+unknownDH) / (double) total_chroms;
  pB2 = 1-pA2;

  const_prob = f;


  /* set initial conditions */

  if (const_prob < 0.00) {
    pAA=pA1*pA2;
    pAB=pA1*pB2;
    pBA=pB1*pA2;
    pBB=pB1*pB2;
  } else {

    pAA=pBB=pAB=pBA=const_prob;   /* so that the first count step will produce an
			     initial estimate without inferences (this should
			     be closer and therefore speedier than assuming 
			     they are all at equal frequency) */

    count_haps(pAA,pAB,pBA,pBB,&nAA,&nAB,&nBA,&nBB,0);

    estimate_p(nAA,nAB,nBA,nBB,&pAA,&pAB,&pBA,&pBB);
  }


  /* now we have an initial reasonable guess at p we can
     start the EM - let the fun begin */

  const_prob=0.0;
  count=1; loglike=-999999999.0;

  do {

    oldloglike=loglike;

    count_haps(pAA,pAB,pBA,pBB,&nAA,&nAB,&nBA,&nBB,count);

    loglike = known[AA]*log10(pAA) + known[AB]*log10(pAB) + known[BA]*log10(pBA) + known[BB]*log10(pBB) + (double)unknownDH*log10(pAA*pBB + pAB*pBA);

    /* printf("Round %2d: pAA=%.4lf, pAB=%.4lf, pBA=%.4lf, pBB=%.4lf, logL=%.4lf\n",
       count, pAA, pAB, pBA, pBB, loglike); */

    if (fabs(loglike-oldloglike) < TOLERANCE) break;

    estimate_p(nAA,nAB,nBA,nBB,&pAA,&pAB,&pBA,&pBB);
    count++;

  } while(count < 1000);
  /* in reality I've never seen it need more than 10 or so iterations 
     to converge so this is really here just to keep it from running off into eternity */

  /* printf("CONVERGED!\n"); */

  loglike1 = known[AA]*log10(pAA) + known[AB]*log10(pAB) + known[BA]*log10(pBA) + known[BB]*log10(pBB) + (double)unknownDH*log10(pAA*pBB + pAB*pBA);
  loglike0 = known[AA]*log10(pA1*pA2) + known[AB]*log10(pA1*pB2) + known[BA]*log10(pB1*pA2) + known[BB]*log10(pB1*pB2) + (double)unknownDH*log10(2*pA1*pA2*pB1*pB2);

  num = pAA*pBB - pAB*pBA;

  if (num < 0) { 
    /* flip matrix so we get the positive D' */
    /* flip AA with AB and BA with BB */
    tmp=pAA; pAA=pAB; pAB=tmp;
    tmp=pBB; pBB=pBA; pBA=tmp; 
    /* flip frequency of second allele */
    tmp=pA2; pA2=pB2; pB2=tmp;
    /* flip counts in the same fashion as p's */
    tmp=nAA; nAA=nAB; nAB=tmp;
    tmp=nBB; nBB=nBA; nBA=tmp;
    /* num has now undergone a sign change */
    num = pAA*pBB - pAB*pBA;
    /* flip known array for likelihood computation */
    tmp=known[AA]; known[AA]=known[AB]; known[AB]=tmp;
    tmp=known[BB]; known[BB]=known[BA]; known[BA]=tmp;
  }

  denom1 = (pAA+pBA)*(pBA+pBB);
  denom2 = (pAA+pAB)*(pAB+pBB);
  if (denom1 < denom2) { denom = denom1; }
  else { denom = denom2; }
  dprime = num/denom;

  /* add computation of r^2 = (D^2)/p(1-p)q(1-q) */
  r2 = num*num/(pA1*pB1*pA2*pB2);

  /* printf ("DPRIME = %.4lf      LOD = %.2lf    r^2 = %.4lf   ",dprime,loglike1-loglike0,r2); */

  /* we've computed D', its' LOD, and r^2 - let's store them and then compute confidence intervals */

  *finalDprime = dprime;
  *finalLOD = loglike1-loglike0;
  *finalr2 = r2;

  real_dprime=dprime;

  for (i=0; i<=100; i++) {
    dpr = (double)i*0.01;
    tmpAA = dpr*denom + pA1*pA2; 
    tmpAB = pA1-tmpAA;
    tmpBA = pA2-tmpAA;
    tmpBB = pB1-tmpBA;
    if (i==100) {
      /* one value will be 0 */
      if (tmpAA < 1e-10) tmpAA=1e-10;
      if (tmpAB < 1e-10) tmpAB=1e-10;
      if (tmpBA < 1e-10) tmpBA=1e-10;
      if (tmpBB < 1e-10) tmpBB=1e-10;
    }
      
    lsurface[i] = known[AA]*log10(tmpAA) + known[AB]*log10(tmpAB) + known[BA]*log10(tmpBA) + known[BB]*log10(tmpBB) + (double)unknownDH*log10(tmpAA*tmpBB + tmpAB*tmpBA);
  }


  /* Confidence bounds #2 - used in Gabriel et al (2002) - translate into posterior dist of D' - 
     assumes a flat prior dist. of D' - someday we may be able to make
     this even more clever by adjusting given the distribution of observed
     D' values for any given distance after some large scale studies are complete */

  total_prob=sum_prob=0.0;

  for (i=0; i<=100; i++) {
    lsurface[i] -= loglike1;
    lsurface[i] = pow(10.0,lsurface[i]);
    total_prob += lsurface[i];
  }
  
  for (i=0; i<=100; i++) {
    sum_prob += lsurface[i];
    if (sum_prob > 0.05*total_prob &&
	sum_prob-lsurface[i] < 0.05*total_prob) {
      low_i = i-1;
      break;
    }
  }
  
  sum_prob=0.0;
  for (i=100; i>=0; i--) {
    sum_prob += lsurface[i];
    if (sum_prob > 0.05*total_prob &&
	sum_prob-lsurface[i] < 0.05*total_prob) {
      high_i = i+1;
      break;
    }
  }
  /* printf("95%% CI %d to %d\n",low_i,high_i);  */
  
  *finalCIlow = (double) low_i/100.0;
  *finalCIhigh = (double) high_i/100.0;
  
}

void count_haps(double pAA, double pAB, double pBA, double pBB,
		double *nAA, double *nAB, double *nBA, double *nBB,
		int em_round)
{
    /* only the double heterozygote [AB][AB] results in 
       ambiguous reconstruction, so we'll count the obligates 
       then tack on the [AB][AB] for clarity */

    *nAA = (double) (known[AA]);
    *nAB = (double) (known[AB]);
    *nBA = (double) (known[BA]);
    *nBB = (double) (known[BB]);

    if (em_round > 0) {
      *nAA += unknownDH* (pAA*pBB)/((pAA*pBB)+(pAB*pBA));
      *nBB += unknownDH* (pAA*pBB)/((pAA*pBB)+(pAB*pBA));

      *nAB += unknownDH* (pAB*pBA)/((pAA*pBB)+(pAB*pBA));
      *nBA += unknownDH* (pAB*pBA)/((pAA*pBB)+(pAB*pBA));
    }
}

void estimate_p(double nAA, double nAB, double nBA, double nBB,
		double *pAA, double *pAB, double *pBA, double *pBB)
{
    double total;

    total = nAA+nAB+nBA+nBB+(4*const_prob);

    *pAA=(nAA+const_prob)/total; if (*pAA < 1e-10) *pAA=1e-10;
    *pAB=(nAB+const_prob)/total; if (*pAB < 1e-10) *pAB=1e-10;
    *pBA=(nBA+const_prob)/total; if (*pBA < 1e-10) *pBA=1e-10;
    *pBB=(nBB+const_prob)/total; if (*pBB < 1e-10) *pBB=1e-10;
}


