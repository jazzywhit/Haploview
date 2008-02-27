package edu.mit.wi.haploview;

import java.util.Vector;

public class SNP implements Comparable{

    private String name;
    private long position;
    private double MAF;
    private Vector extra;
    private byte minor, major;
    private int dup;

    SNP(String n, long p, double m, byte a1, byte a2){
        name = n;
        position = p;
        MAF = m;
        major = a1;
        minor = a2;
    }

    SNP(String n, long p, double m, byte a1, byte a2, String e){
        name = n;
        position = p;
        MAF = m;
        major = a1;
        minor = a2;
        if (e != null){
            extra = new Vector();
            extra.add(e);
        }
    }

    public String getDisplayName(){
        if (name != null){
            return name;
        }else{
            return "Marker " + position;
        }
    }

    public String getName(){
        return name;
    }

    public long getPosition(){
        return position;
    }

    public double getMAF(){
        return MAF;
    }

    public Vector getExtra(){
        return extra;
    }

    public void setExtra(Vector extra) {
        this.extra = extra;
    }

    public byte getMinor(){
        return minor;
    }

    public byte getMajor(){
        return major;
    }

    public void setDup(int i) {
        //0 for non dup
        //1 for "used" dup (has higher geno% than twin)
        //2 for "unused" dup
        dup = i;
    }

    public int getDupStatus(){
        return dup;
    }

    public boolean getStrandIssue(){
        return (major == 4 && minor == 1) || (major == 1 && minor == 4) || (major == 3 && minor == 2) || (major == 2 && minor == 3);
    }

    public int compareTo(Object o) {
        SNP s = (SNP)o;
        if(this.equals(s)) {
            return 0;
        } else if(this.position == s.position) {
            return name.compareTo(s.name);
        } else {
            return this.position > s.position ? 1 : -1;
        }
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o instanceof SNP) {
            SNP s = (SNP)o;
            if (name != null){
                if(this.name.equals(s.name) && this.position == s.position) {
                    return true;
                }
            }else{
                if (this.position == s.position){
                    return true;
                }
            }
        }
        return false;
    }

    public int hashCode() {
        //uses idea from Long hashcode to hash position
        if (name != null){
            return (name.hashCode() + (int)(position ^ (position >>> 32)));
        }else{
            //in this case all names are null and positions are unique integers from 1..N
            return (int)position;
        }
    }
}
