package edu.mit.wi.tagger;

import java.util.List;
import java.util.Comparator;

interface Taggable {

    /**
     * Adds Tag t to the list of sites that tag this Taggable object.
     * @param t Tag object which tags this Taggable object 
     */
    public void addTag(Tag t);

    public void removeTag(Tag t);

    public void setTagComparator(Comparator tc);

    public Comparator getTagComparator();

    /**
     * returns a list of the Tags which capture this site
     * @return List list of Tag objects
     */
    public List getTags();

    /**
     * returns the "best" Tag that captures this site. best is
     * defined by the particular implementation
     * @return TagSequence
     */
    public TagSequence getBestTag();
}
