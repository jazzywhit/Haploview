#include <stdio.h>
#include <math.h>
#include <string.h>

typedef struct {
  long h1;
  long h2;
  double p;
} RECOVERY;

typedef struct {
  int nposs;
  RECOVERY *poss;
} OBS;

typedef struct {
  int nblocks;
  int *nposs;
  RECOVERY **poss;
  int nsuper;
  RECOVERY *superposs;
} SUPER_OBS;


#define MAXLOCI 100
#define MAXLN 1000
#define PSEUDOCOUNT 0.1
char ln[MAXLN], ln1[MAXLN], ln2[MAXLN];
OBS *data;
SUPER_OBS *superdata;
int two_n[MAXLOCI];
double *prob;
char *haplo_str(int,int);
char *decode_haplo_str(int,int,int*,int**,int*);
char *pstr, *pstr2;

int full_em_breakup(int num_haplos, int num_loci, char **input_haplos, int max_missing, int *num_haplos_present, char **haplos_present, double *haplo_freq, int num_blocks, int *block_size, int dump_phased_haplos)
{
  int i, j, k, num_poss, iter, maxk, numk, **hlist, *num_hlist, *hint;
  double total, maxprob, **hprob;
  int block, start_locus, end_locus, biggest_block_size, num_indivs;
  int poss_full, best, h1, h2;
  double *superprob;
  FILE *fpdump;

  if (num_loci > MAXLOCI) return(-1);

  biggest_block_size=block_size[0];

  for (i=1; i<num_blocks; i++) {
    if (block_size[i] > biggest_block_size) biggest_block_size=block_size[i];
  }

  two_n[0]=1;
  for (i=1; i<31; i++) two_n[i]=2*two_n[i-1];
  pstr = (char*) malloc ((num_loci+5)*sizeof(char));
  pstr2 = (char*) malloc ((num_loci+5)*sizeof(char));
  /***** allocate full EM stuff for biggest block *****/
  num_poss = two_n[biggest_block_size];
  data = (OBS *) malloc ( (num_haplos/2)*sizeof(OBS) );
  for (i=0; i<num_haplos/2; i++) {
    data[i].poss = (RECOVERY *) malloc (num_poss * two_n[max_missing] * sizeof(RECOVERY));
  }

  superdata = (SUPER_OBS *) malloc ( (num_haplos/2)*sizeof(SUPER_OBS));
  for (i=0; i<num_haplos/2; i++) {
        superdata[i].poss = (RECOVERY **) malloc (num_blocks * sizeof(RECOVERY*));
        superdata[i].nposs = (int *) malloc (num_blocks * sizeof(int));
  }
  hprob = (double **) malloc (num_blocks * sizeof(double*));
  hlist = (int **) malloc (num_blocks * sizeof (int*));
  for (block=0; block<num_blocks; block++) {   
    hprob[block] = (double *) malloc (num_poss * sizeof(double));
    hlist[block] = (int *) malloc (num_poss * sizeof(int));
  }
  num_hlist = (int *) malloc (num_blocks * sizeof(int));

  /* temporary and reusable in each block */
  hint = (int *) malloc (num_poss * sizeof(int));
  prob = (double *) malloc (num_poss * sizeof(double));

  end_locus=-1;  
  for (block=0; block<num_blocks; block++) {
        
      start_locus=end_locus+1;
      end_locus=start_locus+block_size[block]-1;
      num_poss=two_n[block_size[block]];

      if ((num_indivs=read_observations(num_haplos,num_loci,input_haplos,start_locus,end_locus)) <= 0)
            return(-1);


      /* start prob array with probabilities from full observations */
      for (j=0; j<num_poss; j++) { prob[j]=PSEUDOCOUNT; }
      total=(double)num_poss;
      total *= PSEUDOCOUNT;
 
      /* starting prob is phase known haps + 0.1 (PSEUDOCOUNT) count of every haplotype -
           i.e., flat when nothing is known, close to phase known if a great deal is known */

      for (i=0; i<num_indivs; i++) {
        if (data[i].nposs==1) {
          prob[data[i].poss[0].h1]+=1.0;
          prob[data[i].poss[0].h2]+=1.0;
          total+=2.0;
        }
      }

      /* normalize */
      for (j=0; j<num_poss; j++) { 
        prob[j] /= total;
        /* if (prob[j] > .01) { printf("haplo %s, p=%.3lf\n",haplo_str(j),prob[j]); } */
      }

      /* EM LOOP: assign ambiguous data based on p, then re-estimate p */
      iter=0;
      while (iter<20) {
        /* compute probabilities of each possible observation */
        for (i=0; i<num_indivs; i++) {
          total=0.0;
          for (k=0; k<data[i].nposs; k++) {
              data[i].poss[k].p = 
                prob[data[i].poss[k].h1]*prob[data[i].poss[k].h2];
              total+=data[i].poss[k].p;
          }
          /* normalize */
          for (k=0; k<data[i].nposs; k++) {
              data[i].poss[k].p /= total;
          }
        }
  
        /* re-estimate prob */
    
        for (j=0; j<num_poss; j++) { prob[j]=1e-10; }
          total=num_poss*1e-10;

        for (i=0; i<num_indivs; i++) {
          for (k=0; k<data[i].nposs; k++) {
              prob[data[i].poss[k].h1]+=data[i].poss[k].p;
              prob[data[i].poss[k].h2]+=data[i].poss[k].p;
              total+=(2.0*data[i].poss[k].p);
          }
        }

        /* normalize */
        for (j=0; j<num_poss; j++) { 
          prob[j] /= total;
	  /*
	  if (prob[j] > .001) { 
	    printf("haplo %s   p = %.4lf\n",haplo_str(j,1+end_locus-start_locus),prob[j]); 
	    } */
        }
        iter++;
      }

      /* printf("FINAL PROBABILITIES:\n"); */
      k=0;
      for (j=0; j<num_poss; j++) { 
	hint[j]=-1; 
        if (prob[j] > .001) { 
          /* printf("haplo %s   p = %.4lf\n",haplo_str(j,block_size[block]),prob[j]); */
          hlist[block][k]=j; hprob[block][k]=prob[j];
	  hint[j]=k; 
	  k++;
        }
      }
      num_hlist[block]=k;

        /* store current block results in super obs structure */
      store_block_haplos(hlist, hprob, hint, block, num_indivs); 

  } /* for each block */

  poss_full=1;
  for (block=0; block<num_blocks; block++) {
    /*printf("BLOCK %d\n",block+1);
      for (i=0; i<num_hlist[block]; i++) {
      printf("%s\t%.4lf\n",haplo_str(hlist[block][i],block_size[block]),hprob[block][i]);
      }*/
    poss_full *= num_hlist[block];
  }

  /* LIGATE and finish this mess :) */

  if (poss_full > 1000000) {
    /* what we really need to do is go through and pare back
       to using a smaller number (e.g., > .002, .005) */
    printf("too many possibilities: %d\n",poss_full);
    return(-5);
  }
  superprob = (double *) malloc (poss_full * sizeof(double));

  create_super_haplos(num_indivs,num_blocks,num_hlist);

  /* run standard EM on supercombos */

  /* start prob array with probabilities from full observations */
  for (j=0; j<poss_full; j++) { superprob[j]=PSEUDOCOUNT; }
  total=(double)poss_full;
  total *= PSEUDOCOUNT;
 
  /* starting prob is phase known haps + 0.1 (PSEUDOCOUNT) count of every haplotype -
     i.e., flat when nothing is known, close to phase known if a great deal is known */

  for (i=0; i<num_indivs; i++) {
    if (superdata[i].nsuper==1) {
      superprob[superdata[i].superposs[0].h1]+=1.0;
      superprob[superdata[i].superposs[0].h2]+=1.0;
      total+=2.0;
    }
  }

  /* normalize */
  for (j=0; j<poss_full; j++) { 
    superprob[j] /= total;
  }

  /* EM LOOP: assign ambiguous data based on p, then re-estimate p */
  iter=0;
  while (iter<20) {
    /* compute probabilities of each possible observation */
    for (i=0; i<num_indivs; i++) {
      total=0.0;
      for (k=0; k<superdata[i].nsuper; k++) {
	superdata[i].superposs[k].p = 
	  superprob[superdata[i].superposs[k].h1]*
	  superprob[superdata[i].superposs[k].h2];
              total+=superdata[i].superposs[k].p;
      }
      /* normalize */
      for (k=0; k<superdata[i].nsuper; k++) {
	superdata[i].superposs[k].p /= total;
      }
    }
  
    /* re-estimate prob */
    
    for (j=0; j<poss_full; j++) { superprob[j]=1e-10; }
    total=poss_full*1e-10;

    for (i=0; i<num_indivs; i++) {
      for (k=0; k<superdata[i].nsuper; k++) {
	superprob[superdata[i].superposs[k].h1]+=superdata[i].superposs[k].p;
	superprob[superdata[i].superposs[k].h2]+=superdata[i].superposs[k].p;
	total+=(2.0*superdata[i].superposs[k].p);
      }
    }

    /* normalize */
    for (j=0; j<poss_full; j++) { 
      superprob[j] /= total;
    }
    iter++;
  }

  /* we're done - the indices of superprob now have to be
     decoded to reveal the actual haplotypes they represent */

  k=0;
  for (j=0; j<poss_full; j++) { 
    if (superprob[j] > .001) {
      sprintf(haplos_present[k],"%s",decode_haplo_str(j,num_blocks,block_size,hlist,num_hlist));
      haplo_freq[k]=superprob[j];
      k++;
    }
  }
  *num_haplos_present=k;

  if (dump_phased_haplos) {

    if ((fpdump=fopen("emphased.haps","w"))!=NULL) {
      for (i=0; i<num_indivs; i++) {
	best=0;
	for (k=0; k<superdata[i].nsuper; k++) {
	  if (superdata[i].superposs[k].p > superdata[i].superposs[best].p) {
	    best=k;
	  }
	}
	h1 = superdata[i].superposs[best].h1;
	h2 = superdata[i].superposs[best].h2;
	fprintf(fpdump,"%s\n",decode_haplo_str(h1,num_blocks,block_size,hlist,num_hlist));
	fprintf(fpdump,"%s\n",decode_haplo_str(h2,num_blocks,block_size,hlist,num_hlist));
      }
      fclose(fpdump);
    }
  }

}

        
         
int read_observations(int num_haplos, int num_loci, char **haplo, int start_locus, int end_locus)
{
  int i, j, a1, a2, h1, h2, two_n, num_poss, loc, num_indivs=0, ind;
  int dhet[MAXLOCI], missing1[MAXLOCI], missing2[MAXLOCI];
  char c1, c2;

  for (ind=0; ind<num_haplos; ind+=2) { 
 
    two_n=1; h1=h2=0; num_poss=1;

    for (loc=start_locus; loc<=end_locus; loc++) {
      i = loc-start_locus;

      c1=haplo[ind][loc]; c2=haplo[ind+1][loc];
      if (c1=='h' || c1=='9') {
          a1=0; a2=1; dhet[i]=1; missing1[i]=0; missing2[i]=0;
      } else {
          dhet[i]=0; missing1[i]=0; missing2[i]=0;
          if (c1 > '0' && c1 < '3') a1=c1-'1';
          else {        a1=0; missing1[i]=1; }

          if (c2 > '0' && c2 < '3') a2=c2-'1';
          else {  a2=0; missing2[i]=1; }

          if (c1 < '0' || c1 > '4' || c2 < '0' || c2 > '4') {
            printf("bad allele in data file (%s,%s)\n",ln1,ln2);
            return(-1);
          }
      }

      h1 += two_n*a1;
      h2 += two_n*a2;
      if (dhet[i]==1) num_poss*=2;
      if (missing1[i]==1) num_poss*=2;
      if (missing2[i]==1) num_poss*=2;

      two_n *= 2;
    }

    data[num_indivs].nposs = num_poss;
    data[num_indivs].poss[0].h1=h1;
    data[num_indivs].poss[0].h2=h2;
    data[num_indivs].poss[0].p=0.0;

    /*    printf("h1=%s, ",haplo_str(h1));
          printf("h2=%s, dhet=%d%d%d%d%d, nposs=%d\n",haplo_str(h2),dhet[0],dhet[1],dhet[2],dhet[3],dhet[4],num_poss); */

    two_n=1; num_poss=1;
    for (i=0; i<=end_locus-start_locus; i++) {
      if (dhet[i]) {
        for (j=0; j<num_poss; j++) {
          /* flip bits at this position and call this num_poss+j */
          h1 = data[num_indivs].poss[j].h1;
          h2 = data[num_indivs].poss[j].h2;
          /* printf("FLIP: position %d, two_n=%d, h1=%d, h2=%d, andh1=%d, andh2=%d\n",i,two_n,h1,h2,h1&two_n,h2&two_n);  */
          if ((h1&two_n)==two_n && (h2&two_n)==0) {
            h1 -= two_n; h2 += two_n; 
          } else if ((h1&two_n)==0 && (h2&two_n)==two_n) {
            h1 += two_n; h2 -= two_n; 
          } else {
            printf("error - attepmting to flip homozygous position\n");
          }
          data[num_indivs].poss[num_poss+j].h1=h1;
          data[num_indivs].poss[num_poss+j].h2=h2;
          data[num_indivs].poss[num_poss+j].p=0.0;
        }
        num_poss *= 2;
      }

      if (missing1[i]) {
        for (j=0; j<num_poss; j++) {
          /* flip bits at this position and call this num_poss+j */
          h1 = data[num_indivs].poss[j].h1;
          h2 = data[num_indivs].poss[j].h2;
          /* printf("MISS1: position %d, two_n=%d, h1=%d, h2=%d, newh1=%d, newh2=%d\n",i,two_n,h1,h2,h1+two_n,h2); */
          if ((h1&two_n)==0) {
            h1 += two_n; 
          } else {
            printf("error - attempting to flip missing !=0\n");
          }
          data[num_indivs].poss[num_poss+j].h1=h1;
          data[num_indivs].poss[num_poss+j].h2=h2;
          data[num_indivs].poss[num_poss+j].p=0.0;
        }
        num_poss *= 2;
      }

      if (missing2[i]) {
        for (j=0; j<num_poss; j++) {
          /* flip bits at this position and call this num_poss+j */
          h1 = data[num_indivs].poss[j].h1;
          h2 = data[num_indivs].poss[j].h2;
          /* printf("MISS2: position %d, two_n=%d, h1=%d, h2=%d, newh1=%d, newh2=%d\n",i,two_n,h1,h2,h1,h2+two_n);  */
          if ((h2&two_n)==0) {
            h2 += two_n; 
          } else {
            printf("error - attempting to flip missing !=0\n");
          }
          data[num_indivs].poss[num_poss+j].h1=h1;
          data[num_indivs].poss[num_poss+j].h2=h2;
          data[num_indivs].poss[num_poss+j].p=0.0;
        }
        num_poss *= 2;
      }

      two_n *= 2;
    }
    /* printf("num_poss = %d  also %d\n",num_poss, data[num_indivs].nposs); */
    num_indivs++;
  }

  return(num_indivs);
}

store_block_haplos(int **hlist, double **hprob, int *hint, int block, int num_indivs)
{
  int i, j, k, num_poss, h1, h2;

  for (i=0; i<num_indivs; i++) {
        num_poss=0;
        for (j=0; j<data[i].nposs; j++) {
          h1 = data[i].poss[j].h1;
          h2 = data[i].poss[j].h2;
          if (hint[h1] >= 0 && hint[h2] >= 0) {
            /* valid combination, both haplos passed to 2nd round */ 
            num_poss++;
          }
	}
	/* allocate and store */ 
	superdata[i].nposs[block]=num_poss;
	if (num_poss > 0) {
	  superdata[i].poss[block] = (RECOVERY *) malloc (num_poss * sizeof(RECOVERY));
	  k=0;
	  for (j=0; j<data[i].nposs; j++) {
	    h1 = data[i].poss[j].h1;
	    h2 = data[i].poss[j].h2;
	    if (hint[h1] >= 0 && hint[h2] >= 0) {
	      superdata[i].poss[block][k].h1 = hint[h1];
	      superdata[i].poss[block][k].h2 = hint[h2];
	      k++;
	    }
	  }
	}
  }
}

char *haplo_str(int h, int num_loci)
{
  int i, val;
  
  for (i=0; i<num_loci; i++) {
    if ((h&two_n[i])==two_n[i]) { pstr[i]='2'; }
    else { pstr[i]='1'; }
  } 
  
  pstr[i]='\0';
  return(pstr);
}

char *decode_haplo_str(int chap, int num_blocks, int *block_size, int **hlist, int *num_hlist)
{
  int i, val;

  strcpy(pstr2,"");
  for (i=0; i<num_blocks; i++) {
    val = chap % num_hlist[i];
    strcat(pstr2,haplo_str(hlist[i][val],block_size[i]));
    chap -= val;
    chap /= num_hlist[i];
  }
  return(pstr2);
}

create_super_haplos(int num_indivs, int num_blocks, int *num_hlist)
{
  int i, j, num_poss, h1, h2;

  for (i=0; i<num_indivs; i++) {
    num_poss=1;
    for (j=0; j<num_blocks; j++) {
      num_poss *= superdata[i].nposs[j];
    }
    
    superdata[i].nsuper=0;
    superdata[i].superposs = (RECOVERY *) malloc (num_poss * sizeof(RECOVERY));
    
    /* block 0 */
    for (j=0; j<superdata[i].nposs[0]; j++) {
      h1 = superdata[i].poss[0][j].h1;
      h2 = superdata[i].poss[0][j].h2;
      recursive_superposs(h1,h2,1,num_blocks,num_hlist,i);
    }

    if (superdata[i].nsuper != num_poss) {
      //printf("error in superfill\n");
    }
  }
}

recursive_superposs(int h1, int h2, int block, int num_blocks, int *num_hlist, int indiv)
{
  int j, curr_prod, newh1, newh2;

  if (block == num_blocks) {
    superdata[indiv].superposs[superdata[indiv].nsuper].h1 = h1;
    superdata[indiv].superposs[superdata[indiv].nsuper].h2 = h2;
    superdata[indiv].nsuper++;
  } else {
    curr_prod=1;
    for (j=0; j<block; j++) { curr_prod*=num_hlist[j]; }

    for (j=0; j<superdata[indiv].nposs[block]; j++) {
      newh1 = h1 + (superdata[indiv].poss[block][j].h1 * curr_prod);
      newh2 = h2 + (superdata[indiv].poss[block][j].h2 * curr_prod);
      recursive_superposs(newh1,newh2,block+1,num_blocks,num_hlist,indiv);
    }
  }
}
