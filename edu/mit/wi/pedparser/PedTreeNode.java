package edu.mit.wi.pedparser;

import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;

public class PedTreeNode {

    HashSet parents, kids;

    PedTreeNode (){
        parents = new HashSet();
        kids = new HashSet();
    }

    void addParent(Individual i){
        parents.add(i);
    }

    void addChildren(Vector v){
        kids.addAll(v);
    }

    HashSet getNodeMembers(){
        HashSet h = new HashSet();
        h.addAll(kids);
        h.addAll(parents);

        return h;
    }

    HashSet getRelatedMembers(Individual i)throws PedigreeException{
        //if he's a kid in this node
        HashSet h = new HashSet();
        h.add(i);
        if (kids.contains(i)){
            if (i.mom != null){
                h.addAll(i.mom.kids);
                h.add(i.mom);
            }
            if (i.dad != null){
                h.addAll(i.dad.kids);
                h.add(i.dad);
            }
            return h;
        }else if (parents.contains(i)){
            h.addAll(i.kids);
            return h;
        }
        throw new PedigreeException("Individual " + i + " is not in this node.");
    }

    HashSet getUnrelatedMembers(Individual i) throws PedigreeException{
        HashSet h = getNodeMembers();
        h.removeAll(getRelatedMembers(i));
        return h;
    }


    public String toString(){
        StringBuffer retStr = new StringBuffer("p: ");
        Iterator pi = parents.iterator();
        Iterator ki = kids.iterator();

        while(pi.hasNext()){
            retStr.append(((Individual)pi.next()).id);
            retStr.append(" ");
        }

        retStr.append("k: ");
        while (ki.hasNext()){
            retStr.append(((Individual)ki.next()).id);
            retStr.append(" ");
        }

        return retStr.toString();
    }
}
