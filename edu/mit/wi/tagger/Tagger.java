package edu.mit.wi.tagger;

import java.util.*;
import java.io.*;

public class Tagger {
    public static final double DEFAULT_RSQ_CUTOFF = 0.8;

    //todo: SNPS?? should this be named (and store) variantseqs?
    //vector of SNP objects, which contains every SNP (tags and non-tags)
    private Vector snps;

    //vector of SNPs which must be included in the set of Tags
    //no object may be present in forceInclude and forceExclude concurrently.
    private Vector forceInclude;

    //vector of SNPs which can never be included in the set of Tags
    //no object may be present in forceInclude and forceExclude concurrently.
    private Vector forceExclude;

    private AlleleCorrelator alleleCorrelator;
    //private Hashtable pairwiseCompsHash;
    private double minRSquared;
    private Hashtable snpHash;

    //Vector of Tag objects determined by the most recent call to findTags()
    private Vector tags;

    //vector of sites which arent tagged by anything in the tags vector
    private Vector untagged;


    public int taggedSoFar;

    public Tagger(Vector s, Vector include, Vector exclude, AlleleCorrelator ac){
        this(s,include,exclude,ac,DEFAULT_RSQ_CUTOFF);
    }

    public Tagger(Vector s, Vector include, Vector exclude, AlleleCorrelator ac, double rsqCut) {
        minRSquared = rsqCut;

        if(s != null) {
            snps = s;
        } else {
            snps = new Vector();
        }

        if(include != null) {
            forceInclude = (Vector)include.clone();
        } else {
            forceInclude = new Vector();
        }

        if(exclude != null) {
            forceExclude = (Vector)exclude.clone();
        } else {
            forceExclude = new Vector();
        }

        alleleCorrelator = ac;


        for(int i=0;i<snps.size();i++) {
            VariantSequence curVarSeq = (VariantSequence) snps.get(i);
            if(curVarSeq.getTagComparator() == null) {
                TagRSquaredComparator trsc = new TagRSquaredComparator(curVarSeq);
                curVarSeq.setTagComparator(trsc);
            }
        }
        /*pairwiseCompsHash = new Hashtable(snps.size());
        for(int i=0;i<snps.size();i++) {
            Hashtable ht = new Hashtable();
            pairwiseCompsHash.put(snps.get(i), ht);
        }

        //add pairwiseComparison objects for each snp and itself, with rsquared 1
            for(int i=0;i<snps.size();i++) {
                addPairwiseComp(i,i,1);
            } */
    }

    //temporary constructor for testing purposes
    public Tagger(String infoFileName, String rfileName) {
        snps = new Vector();
        forceExclude = new Vector();
        forceInclude = new Vector();
        try {
            BufferedReader br = new BufferedReader(new FileReader(infoFileName));
            String line;
            StringTokenizer str;
            SNP tempSnp;
            snpHash = new Hashtable();

            while ((line = br.readLine()) != null) {
                str = new StringTokenizer(line);
                String name = str.nextToken();
                tempSnp = new SNP(name);
                snps.add(tempSnp);
                snpHash.put(name,tempSnp);
            }

            br.close();
           /*
            //pairwiseComps = new PairwiseComparison[snps.size()][snps.size()];
            pairwiseCompsHash = new Hashtable(snps.size());
            for(int i=0;i<snps.size();i++) {
                Hashtable ht = new Hashtable();
                pairwiseCompsHash.put(snps.get(i), ht);
            } */

            br = new BufferedReader(new FileReader(rfileName));

            //skip the header line
            br.readLine();

            while ((line = br.readLine()) != null) {
                str = new StringTokenizer(line);
                SNP snp1 = (SNP)snpHash.get(str.nextToken());
                SNP snp2 = (SNP)snpHash.get(str.nextToken());
                double rsquared = Double.parseDouble(str.nextToken());

                int snp1_idx = snps.indexOf(snp1);
                int snp2_idx = snps.indexOf(snp2);

                //addPairwiseComp(snp1_idx,snp2_idx, rsquared);
                //pairwiseComps[snp1_idx][snp2_idx] = new PairwiseComparison(snp1,snp2,rsquared);
                //pairwiseComps[snp2_idx][snp1_idx] = pairwiseComps[snp1_idx][snp2_idx];

            }
            br.close();

            //add pairwiseComparison objects for each snp and itself, with rsquared 1
            /*for(int i=0;i<snps.size();i++) {
                addPairwiseComp(i,i,1);
            } */
        } catch(IOException ioe) {
            System.err.println("your file == fux0red");
        }

    }

    public double getPairwiseComp(VariantSequence a, VariantSequence b) {
        return alleleCorrelator.getCorrelation(a,b);
    }

    /**
    * This method finds Tags for the SNPs in the snps vector, and returns a vector of Tag objects
    */
    public Vector findTags() {
        tags = new Vector();
        untagged = new Vector();
        taggedSoFar = 0;

        //potentialTagsHash stores the PotentialTag objects keyed on the corresponding sequences
        Hashtable potentialTagHash = new Hashtable();
        PotentialTagComparator ptcomp = new PotentialTagComparator();
        VariantSequence currentVarSeq;

        //create SequenceComparison objects for each potential Tag, and
        //add any comparisons which have an r-squared greater than the minimum.
        for(int i=0;i<snps.size();i++) {
            currentVarSeq = (VariantSequence)snps.get(i);

            if(!(forceExclude.contains(currentVarSeq) ) ){//|| (currentVarSeq instanceof SNP && ((SNP)currentVarSeq).getMAF() < .05))) {
                PotentialTag tempPT = new PotentialTag(currentVarSeq);
                for(int j=0;j<snps.size();j++) {
                    if( getPairwiseComp(currentVarSeq,(VariantSequence) snps.get(j)) >= minRSquared) {
                        tempPT.addTagged((VariantSequence) snps.get(j));
                    }
                }

                potentialTagHash.put(currentVarSeq,tempPT);
            }
        }

        Vector sitesToCapture = (Vector) snps.clone();

        Iterator potItr = sitesToCapture.iterator();

        debugPrint("snps to tag: " + sitesToCapture.size());

        Vector potentialTags = new Vector(potentialTagHash.values());

        int countTagged = 0;
        //add Tags for the ones which are forced in.
        Vector includedPotentialTags = new Vector();
        //construct a list of PotentialTag objects for forced in sequences
        for (int i = 0; i < forceInclude.size(); i++) {
            VariantSequence variantSequence = (VariantSequence) forceInclude.elementAt(i);
            if(variantSequence != null && potentialTagHash.containsKey(variantSequence)) {
                includedPotentialTags.add((PotentialTag) potentialTagHash.get(variantSequence));
            }
        }

        //add each forced in sequence to the list of tags 
        for(int i=0;i<includedPotentialTags.size();i++) {
            PotentialTag curPT = (PotentialTag) includedPotentialTags.get(i);
            Vector newlyTagged = addTag(curPT,potentialTagHash,sitesToCapture);
            countTagged += newlyTagged.size();
            sitesToCapture.removeAll(newlyTagged);
            sitesToCapture.remove(curPT.sequence);

        }

        //loop until all snps are tagged
        System.out.println("start");
        while(sitesToCapture.size() > 0) {
            potentialTags = new Vector(potentialTagHash.values());
            if(potentialTags.size() == 0) {
                //we still have sites left to capture, but we have no more available tags.
                //this should only happen if the sites remaining in sitesToCapture were specifically
                //excluded from being tags. Since we can't add any more tags, break out of the loop.
                break;
            }

            //sorts the array of potential tags according to the number of untagged sites they can tag.
            //the last element is the one which tags the most untagged sites, so we choose that as our next tag.
            Collections.sort(potentialTags,ptcomp);
            PotentialTag currentBestTag = (PotentialTag) potentialTags.lastElement();

            Vector newlyTagged = addTag(currentBestTag,potentialTagHash,sitesToCapture);
            countTagged += newlyTagged.size();

            sitesToCapture.removeAll(newlyTagged);
            sitesToCapture.remove(currentBestTag.sequence);
            taggedSoFar = countTagged;
        }

        if(sitesToCapture.size() > 0) {
            //any sites left in sitesToCapture could not be tagged, so we add them all to the untagged Vector
            untagged.addAll(sitesToCapture);
        }

        System.out.println("tagged " + countTagged + " SNPS using " + tags.size() +" tags" );
        System.out.println("# of SNPs that could not be tagged: " + untagged.size());
        for (Iterator iterator = snps.iterator(); iterator.hasNext();) {
            SNP snp = (SNP) iterator.next();
            TagSequence bt = snp.getBestTag();
            if(bt == null) {
                //this snp is untagged
            } else {
                SNP bestTag = ((SNP)(bt).sequence);
                //this pc object shouldn't be null unless snp is the same object as bestTag
                //(the snp tags itself)
                //PairwiseComparison pc = getPairwiseComp(bestTag,snp);
                if(snp == bestTag) {
                    //System.out.println("same tizag");
                } else {
                    //double bestRsq = pc.getRSquared();
                    //System.out.println("SNP " + snp.getName() + " best tag: " + bestTag.getName() + "\trsq: " + bestRsq);
                }
            }

            /*if(snp.tags.size() > 1) {
                System.out.println("snp " + snp.getName() + " is tagged by " + snp.tags.size() + " tags");
            } */
        }

        return new Vector(tags);
    }

    private Vector addTag(PotentialTag theTag,Hashtable potentialTagHash, Vector sitesToCapture) {
        Vector potentialTags = new Vector(potentialTagHash.values());

        potentialTags.remove(theTag);
        potentialTagHash.remove(theTag.sequence);
        //newlyTagged contains alleles which were not tagged by anything in the set of tags before,
        //and are now tagged by theTag.
        Vector newlyTagged = ((PotentialTag)theTag).tagged;

        TagSequence tagSeq = new TagSequence(theTag.sequence);
        tags.add(tagSeq);

        Iterator itr = potentialTagHash.keySet().iterator();
        Vector toRemove = new Vector();
        //iterate through the list of available tags, and remove the newly tagged alleles from
        //the list of alleles that each PotentialTag can tag. (since we want to choose our next tag
        // according to which will tag the most untagged alleles )
        while(itr.hasNext()) {
            PotentialTag pt = (PotentialTag) potentialTagHash.get(itr.next());
            pt.removeTagged(newlyTagged);
            pt.removeTagged(theTag.sequence);
            //if a PotentialTag cannot tag any other uncaptured sites, then we want to remove it from contention,
            //unless its sequence still needs to be captured.
            if(pt.taggedCount() == 0 && !sitesToCapture.contains(pt.sequence)) {
                toRemove.add(pt.sequence);
            }
        }

        for(int i=0;i<toRemove.size();i++) {
            potentialTags.remove(potentialTagHash.remove(toRemove.get(i)));
        }

        //loop through the list of alleles the newly added tag can capture, and
        //add them to the TagSequence object.
        //we add all the alleles the tag can capture, _not_ just the newly tagged alleles.
        Iterator ptitr = theTag.allTagged.iterator();
        while(ptitr.hasNext()) {
            tagSeq.addTagged((VariantSequence)ptitr.next());
        }

        return newlyTagged;
    }

    class PotentialTag {
        VariantSequence sequence;
        // tagged contains the sequences which this sequence can tag, which are not yet tagged
        //(this is used in the while loop in findTags() )
        Vector tagged;
        //allTagged contains all sequences that this sequence can tag, regardless of what tags have already been chosen
        Vector allTagged;

        public PotentialTag(VariantSequence s) {
            sequence = s;
            tagged = new Vector();
            allTagged = new Vector();
        }

        public void addTagged(VariantSequence vs) {
            tagged.add(vs);
            allTagged.add(vs);
        }

        public void removeTagged(VariantSequence vs) {
            tagged.remove(vs);
        }

        public void removeTagged(Collection c) {
            tagged.removeAll(c);
        }

        //this returns the number of sequences that havent been tagged this sequence can tag
        public int taggedCount() {
            return tagged.size();
        }

    }


    public void setExclude(Vector e) {
        if(e != null) {
            forceExclude = (Vector) e.clone();
        }
    }

    public void setInclude(Vector e) {
        if(e != null) {
            forceInclude = (Vector) e.clone();
        }
    }

    public void clearExclude() {
        forceExclude.removeAllElements();
    }

    public void clearInclude() {
        forceInclude.removeAllElements();
    }

    public Vector getTags() {
        return tags;
    }


    public Vector getForceInclude() {
        return forceInclude;
    }


    public void saveResultToFile(File outFile) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

        bw.write("Marker\tBest Tag\tr^2 w/tag");
        bw.newLine();
        for (int i = 0; i < snps.size(); i++) {
            StringBuffer line = new StringBuffer();
            SNP snp = (SNP) snps.elementAt(i);
            line.append(snp.getName()).append("\t");
            TagSequence theTag = snp.getBestTag();
            line.append(theTag.getTagSequence().getName()).append("\t");
            line.append(getPairwiseComp(snp,theTag.sequence)).append("\t");
            bw.write(line.toString());
            bw.newLine();
        }

        bw.newLine();

        bw.write("Tag\tMarkers Tagged");
        bw.newLine();
        for(int i=0;i<tags.size();i++) {
            StringBuffer line = new StringBuffer();
            TagSequence theTag = (TagSequence) tags.get(i);
            line.append(theTag.getTagSequence().getName()).append("\t");
            Vector tagged = theTag.getBestTagged();
            for (int j = 0; j < tagged.size(); j++) {
                VariantSequence varSeq = (VariantSequence) tagged.elementAt(j);
                if(j !=0){
                    line.append(",");
                }
                line.append(varSeq.getName());
            }
            bw.write(line.toString());
            bw.newLine();
        }

        bw.close();
    }




    public static void main(String[] args) {

        if(args.length == 2) {
            Tagger tagger = new Tagger(args[0], args[1]);
            Vector tags = tagger.findTags();
            //tagger.printStuff();

        }  else {
            System.err.println("Yarr, I needs me two parameters!");
        }
    }


    class PotentialTagComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((PotentialTag)o1).taggedCount() -  ((PotentialTag)o2).taggedCount();
        }
    }

    class TagRSquaredComparator implements Comparator {
        VariantSequence seq;

        public TagRSquaredComparator(VariantSequence s) {
            seq = s;
        }

        public int compare(Object o1, Object o2) {
            if(getPairwiseComp(((TagSequence)o1).sequence,seq) == getPairwiseComp(((TagSequence)o2).sequence,seq)) {
                return 0;
            } else if (getPairwiseComp(((TagSequence)o1).sequence,seq) > getPairwiseComp(((TagSequence)o2).sequence,seq)) {
                return 1;
            } else {
                return -1;
            }
        }
    }




    boolean debug = true;

    void debugPrint(String s) {
        if(debug) {
            System.out.println(s);
        }
    }

}
