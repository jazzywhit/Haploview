#include <stdio.h>
#include <string.h>
#include <jni.h>
#include "edu_mit_wi_haploview_HaploData.h"

/************************************************************
EM algorithm for phasing and resolving haps
************************************************************/

JNIEXPORT jstring JNICALL
Java_edu_mit_wi_haploview_HaploData_runEM(JNIEnv *env, jobject obj, jint num_haplos,
		     jint num_loci, jobjectArray jinput_haplos, jint num_blocks,
		     jintArray jblock_size)
{
  int max_missing = 5, num_final, i;
  char **final_haplos, **input_haplos;
  double *final_hprob;
  char *output_string;
  char *temp_string;
  jstring tempstr;
  
  jint *block_size = (*env)->GetIntArrayElements(env, jblock_size, 0);

  input_haplos = (char**) malloc (num_haplos * sizeof(char*));
  for (i=0; i<num_haplos; i++){
    input_haplos[i] = (char*) malloc ((num_loci+5) * sizeof(char));
    tempstr = (jstring)(*env)->GetObjectArrayElement(env, jinput_haplos, i);
    input_haplos[i] = (*env)->GetStringUTFChars(env, tempstr, 0);
  }
  
  final_haplos = (char**) malloc (1000 * sizeof(char*));
  for (i=0; i<1000; i++){
    final_haplos[i] = (char*) malloc ((num_loci+5) * sizeof(char));
  }
  
  final_hprob = (double *) malloc (1000 * sizeof(double));


  //printf("%i\t%i\t%s\t%i\t%i\t%i\n",num_haplos,num_loci,input_haplos[13],max_missing,num_blocks,block_size[0]);

  full_em_breakup(num_haplos, num_loci, input_haplos, max_missing,
		  &num_final, final_haplos, final_hprob,
		  num_blocks, block_size, 0);

  output_string = (char *) malloc ((8 + num_loci)*num_final);
  strcpy(output_string, "");
  temp_string = (char *) malloc (8+num_loci);
  
  for (i=0; i<num_final; i++){
    sprintf(temp_string, "%s\t%.3lf\n", final_haplos[i], final_hprob[i]);
    strcat(output_string, temp_string);
  }
  
  return (*env)->NewStringUTF(env, output_string);
}


/*****************************************************************
the following code is used to compute D' etc.
*****************************************************************/
char returnstring[40];
JNIEXPORT jstring JNICALL
Java_edu_mit_wi_haploview_HaploData_callComputeDPrime(JNIEnv *env, jclass jcl, jint a, jint b, jint c, jint d, jint e)
{
  double const_prob,Dprime,LOD,r2,CIlow,CIhigh;
  const_prob=0.1;

  compute_Dprime(a,b,c,d,e,const_prob,&Dprime,&LOD,&r2,&CIlow,&CIhigh);

  sprintf(returnstring, "%.2lf\t%.2lf\t%.2lf\t%.2lf\t%.2lf",Dprime,LOD,r2,CIlow,CIhigh);
  /*convert output to java string and send it back*/
  return(*env)->NewStringUTF(env, returnstring);
}
