package edu.mit.wi.tagger;

import java.util.Vector;


public class TagSequence implements Tag {
    VariantSequence sequence;
    Vector tagged;

    public TagSequence(VariantSequence s) {
        sequence = s;
        tagged = new Vector();
    }

    public void addTagged(Taggable t) {
        tagged.add(t);
        t.addTag(this);
    }

    public Vector getTagged() {
        return tagged;
    }

    public VariantSequence getTagSequence() {
        return sequence;
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
