package edu.mit.wi.tagger;

import java.util.Vector;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

public abstract class VariantSequence implements Taggable{
    private Vector variants;
    private Vector tags;
    private Comparator tagComparator;

    public VariantSequence() {
        variants = new Vector();
        tags = new Vector();
    }

    public void addTag(Tag t) {
        tags.add(t);
    }

    public Vector getVariants() {
        return variants;
    }

    public List getTags(){
        return tags;
    }

    public void setTagComparator(Comparator tagComparator) {
        this.tagComparator = tagComparator;
    }

    public Comparator getTagComparator() {
        return tagComparator;
    }

    public abstract String getName();

    /**
     * finds and then returns the "best" tag for this VariantSequence.
     * Uses the Comparator tagComparator to decide the "best" tag.
     * If no comparator has been set, then an arbitrary tag is returned from the Vector tags.
     * @return Tag. The best Tag for this sequence. 
     */
    public TagSequence getBestTag() {
        if(tags!= null && tags.size()>0) {
            //if a comparator has been set, then use it to sort the tags. the "best" tag will be the
            //last one in the sorted vector.
            //if there isnt a comparator, we have no basis for choosing which tag is "best",
            //so we just return whatever is currently in the last spot in the
            if(tagComparator != null) {
                Collections.sort(tags,tagComparator);
            }
            return (TagSequence) tags.lastElement();
        }
        return null;
    }


}
