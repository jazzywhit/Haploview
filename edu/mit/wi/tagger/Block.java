package edu.mit.wi.tagger;

import java.util.Vector;
import java.util.Iterator;

public class Block extends VariantSequence{
    //this will have tagger SNP objects
    private Vector snps;

    public Block(Vector snps) {
        this.snps = snps;
    }

    public boolean equals(Object o) {
        if (o instanceof Block){
            Block b = (Block)o;
            if(b.snps.size() != snps.size()) {
                return false;
            }
            for (int i = 0; i < snps.size(); i++){
                if (!b.snps.contains(snps.get(i))){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        int h = 0;
        Iterator itr = snps.iterator();
        while(itr.hasNext()) {
            Object obj = itr.next();
            h += obj.hashCode();
        }
        return h;
    }

    public String getName() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < snps.size()-1; i++){
            sb.append(((SNP)snps.get(i)).getName()).append(",");
        }
        sb.append(((SNP)snps.get(snps.size()-1)).getName());

        return sb.toString();
    }

    public int getMarkerCount() {
        return snps.size();
    }

    public SNP getSNP(int i) {
        return (SNP) snps.get(i);
    }

    public String toString() {
        return getName();
    }

    public Vector getSnps() {
        return snps;
    }
}
