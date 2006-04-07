package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.Haplotype;
import edu.mit.wi.haploview.Options;
import edu.mit.wi.haploview.SNP;

public class MarkerAssociationResult extends AssociationResult{

    SNP snp;

    public MarkerAssociationResult(Haplotype[] locusHaplos, String n, SNP snp) {
        nf.setGroupingUsed(false);

        for (int i = 0; i < locusHaplos.length; i++){
            alleles.add(locusHaplos[i]);
        }
        filterByFrequency(0);
        name = n;

        this.snp = snp;
    }

    public String getCountString(){
        return getCountString(0);
    }

    public String getFreqString(){
        return getFreqString(0);
    }

    public String getDisplayName(int i) {
        return this.getName();
    }

    public String getCountString(int i){
        Haplotype h1 = (Haplotype) alleles.get(0);
        Haplotype h2 = (Haplotype) alleles.get(1);

        if (Options.getAssocTest() == ASSOC_TRIO){
            if(Options.getTdtType() == TDT_STD) {
                if (h1.getTransCount() > h2.getTransCount()){
                    return (int)h1.getTransCount() + ":" + (int)h2.getTransCount();
                }else{
                    return (int)h2.getTransCount() + ":" + (int)h1.getTransCount();
                }
            }else{ // if(Options.getTdtType() == TDT_PAREN) {
                StringBuffer sb = new StringBuffer();
                double[] counts;
                if (h1.getTransCount() > h2.getTransCount()){
                    counts = h1.getDiscordantAlleleCounts();
                    sb.append((int)h1.getTransCount());
                    sb.append(":");
                    sb.append((int)h2.getTransCount());
                }else{
                    counts = h2.getDiscordantAlleleCounts();
                    sb.append((int)h2.getTransCount());
                    sb.append(":");
                    sb.append((int)h1.getTransCount());
                }
                sb.append(",");
                sb.append(counts[1] + 2*counts[2] + counts[5]).append(":");
                sb.append(counts[3] + 2*counts[6] + counts[7]);
                return sb.toString();
            }
        }else{
            if (h1.getCaseCount()/(h1.getCaseCount()+h2.getCaseCount()) >
                    h1.getControlCount()/(h1.getControlCount()+h2.getControlCount())){
                    return (int)h1.getCaseCount() + ":" + (int)h2.getCaseCount() +
                            ", " + (int)h1.getControlCount() + ":" + (int)h2.getControlCount();
            }else{
                    return (int)h2.getCaseCount() + ":" + (int)h1.getCaseCount() +
                            ", " + (int)h2.getControlCount() + ":" + (int)h1.getControlCount();
            }
        }
    }

    public String getFreqString(int i){
        Haplotype h1 = (Haplotype) alleles.get(0);
        Haplotype h2 = (Haplotype) alleles.get(1);

        if(Options.getAssocTest() == ASSOC_TRIO){
            return "";
        }

        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        if (h1.getCaseCount()/(h1.getCaseCount()+h2.getCaseCount()) >
                    h1.getControlCount()/(h1.getControlCount()+h2.getControlCount())){
                return nf.format(h1.getCaseCount()/ (h1.getCaseCount() + h2.getCaseCount())) +
                        ", " + nf.format(h1.getControlCount() / ( h1.getControlCount() + h2.getControlCount()));
        }else{
                return nf.format(h2.getCaseCount()/ (h2.getCaseCount() + h1.getCaseCount())) +
                        ", " + nf.format(h2.getControlCount() / ( h2.getControlCount() + h1.getControlCount()));
        }
    }

    public String getOverTransmittedAllele() {
        Haplotype h1 = (Haplotype) alleles.get(0);
        Haplotype h2 = (Haplotype) alleles.get(1);

        String retStr;

        if (Options.getAssocTest() == ASSOC_TRIO){
            if (h1.getTransCount() > h2.getTransCount()){
                retStr = getAlleleName(0);
            }else if (h1.getTransCount() == h2.getTransCount()){
                retStr = "-";
            }else{
                retStr = getAlleleName(1);
            }
        }else{
            if (h1.getCaseCount()/(h1.getCaseCount()+h2.getCaseCount()) >
                    h1.getControlCount()/(h1.getControlCount()+h2.getControlCount())){
                retStr = getAlleleName(0);
            }else if (h1.getCaseCount()/(h1.getCaseCount()+h2.getCaseCount()) ==
                    h1.getControlCount()/(h1.getControlCount()+h2.getControlCount())){
                retStr = "-";
            }else{
                retStr = getAlleleName(1);
            }
        }
        return retStr;
    }

    public SNP getSnp() {
        return snp;
    }

}
