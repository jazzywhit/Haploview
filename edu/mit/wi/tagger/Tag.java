package edu.mit.wi.tagger;



interface Tag {


    /**
     * Adds a Taggable object to the set of sites which are tagged.
     *
     * @param t The Taggable site to add to the list of sites tagged by this object
     */
    public void addTagged(Taggable t);



}
