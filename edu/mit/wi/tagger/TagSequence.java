package edu.mit.wi.tagger;

import java.util.Vector;


public class TagSequence implements Tag {
    private Allele allele;
    private Vector tagged;
    private VariantSequence sequence;

    public TagSequence(Allele a) {
        allele = a;
        sequence = a.getLocus();
        tagged = new Vector();
    }

    public TagSequence(VariantSequence snp){
        tagged = new Vector();
        sequence = snp;
        allele = null;
    }

    public void addTagged(Taggable t) {
        tagged.add(t);
        t.addTag(this);
    }

    public Vector getTagged() {
        return tagged;
    }

    public Allele getAllele() {
        return allele;
    }

    public VariantSequence getSequence(){
        return sequence;
    }

    public String getName(){
        if (allele == null){
            return sequence.getName();
        }else{
            return sequence.getName() + " : " + allele.getGenotypeString();
        }
    }

    public String getTestName(){
        if (allele == null){
            return sequence.getName();
        }else{
            return sequence.getName() + "\t" + allele.getTestFileFormat();
        }
    }

    public Vector getBestTagged() {
        Vector result = new Vector();

        for (int i = 0; i < tagged.size(); i++) {
            Taggable taggable = (Taggable) tagged.elementAt(i);
            if(taggable.getBestTag() == this) {
                result.add(taggable);
            }
        }
        return result;
    }

    //TODO: should isTagged check if Taggable t is a subsequence of something that is tagged?
    public boolean isTagged(Taggable t) {
        return tagged.contains(t);
    }

}
