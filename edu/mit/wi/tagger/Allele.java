package edu.mit.wi.tagger;

public class Allele{
    private static final char[] alleleNumChars = {'0','1','2','3','4'};
    private static final char[] alleleCodes = {'X','A','C','G','T'};

    private VariantSequence locus;
    private String numericGenotypeString;

    public Allele(VariantSequence locus, String genotypeString) {
        this.locus = locus;
        this.numericGenotypeString = genotypeString;
    }

    public VariantSequence getLocus() {
        return locus;
    }

    public String getGenotypeString(){
        String disp = new String(numericGenotypeString);
        for (int i = 0; i < alleleCodes.length; i++){
            disp = disp.replace(alleleNumChars[i],alleleCodes[i]);
        }

        return disp;
    }

    public String getTestFileFormat(){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < numericGenotypeString.length()-1; i++){
            sb.append(numericGenotypeString.charAt(i)).append(",");
        }
        sb.append(numericGenotypeString.charAt(numericGenotypeString.length()-1));
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof Allele){
            Allele a = (Allele)o;
            if (a.locus.equals(locus) && a.numericGenotypeString.equals(numericGenotypeString)){
                return true;
            }
        }
        return false;
    }

    public int hashCode(){
        return locus.hashCode() + numericGenotypeString.hashCode();
    }
}
