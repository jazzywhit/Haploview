package edu.mit.wi.pedparser;

import org._3pq.jgrapht.edge.UndirectedEdge;

public class PedEdge extends UndirectedEdge{

    private Individual ind;

    public PedEdge(Object o, Object o1, Individual i){
        super (o, o1);
        ind = i;
    }

    public PedEdge(Object o, Object o1) {
        super(o, o1);
    }

    public Individual getInd() {
        return ind;
    }

    public void setInd(Individual ind) {
        this.ind = ind;
    }

}
