package edu.mit.wi.haploview.tagger;

import edu.mit.wi.tagger.*;
import edu.mit.wi.haploview.*;
import edu.mit.wi.haploview.SNP;

import java.util.Vector;
import java.util.Hashtable;
import java.io.File;
import java.io.IOException;


public class TaggerController {
    private HaploData theData;
    private Tagger tagger;
    private Vector results;
    private Vector taggerSNPs;
    private boolean taggingCompleted;
    private Hashtable snpHash;

    public TaggerController(HaploData hd, Vector included, Vector excluded, Vector sitesToCapture) {
        theData = hd;
        taggerSNPs = new Vector();

        snpHash = new Hashtable();

        for(int i=0;i<sitesToCapture.size();i++) {
            SNP tempSNP = (SNP) sitesToCapture.get(i);
            edu.mit.wi.tagger.SNP s = new edu.mit.wi.tagger.SNP(tempSNP.getName(),tempSNP.getPosition(),tempSNP.getMAF());
            taggerSNPs.add(s);
            snpHash.put(tempSNP.getName(),s);
        }

        Vector includedSNPs = new Vector();
        for(int i=0;i<included.size();i++) {
            includedSNPs.add(snpHash.get(included.get(i)));
        }

        Vector excludedSNPs = new Vector();
        for(int i=0;i<excluded.size();i++) {
            excludedSNPs.add(snpHash.get(excluded.get(i)));
        }

        Hashtable indicesByVarSeq = new Hashtable();
        for(int i=0;i<Chromosome.getSize();i++) {
            if(sitesToCapture.contains(Chromosome.getMarker(i))) {
                indicesByVarSeq.put(snpHash.get(Chromosome.getMarker(i).getName()),new Integer(i));
            }
        }

        for (int i = 0; i < sitesToCapture.size(); i++){
            SNP tempSNP = (SNP) sitesToCapture.get(i);
            edu.mit.wi.tagger.SNP taggerSNP = (edu.mit.wi.tagger.SNP) snpHash.get(tempSNP.getName());
            int p = ((Integer)indicesByVarSeq.get(taggerSNP)).intValue();
            for (int j = 1; j < theData.dpTable.getLength(p); j++){
                if (theData.dpTable.getLDStats(p,j+p).getLOD() >= Options.getTaggerLODCutoff()){
                    if (indicesByVarSeq.containsValue(new Integer(j+p))){
                        edu.mit.wi.tagger.SNP ldsnp =
                                (edu.mit.wi.tagger.SNP) snpHash.get(Chromosome.getMarker(j+p).getName());
                        taggerSNP.addToLDList(ldsnp);
                        ldsnp.addToLDList(taggerSNP);
                    }
                }
            }
        }

        HaploviewAlleleCorrelator hac = new HaploviewAlleleCorrelator(indicesByVarSeq,theData);
/*        edu.mit.wi.tagger.SNP s1 = (edu.mit.wi.tagger.SNP) snpHash.get("Marker 2");
        edu.mit.wi.tagger.SNP s2 = (edu.mit.wi.tagger.SNP) snpHash.get("Marker 3");
        Vector v = new Vector();
        v.add(s1);
        v.add(s2);
        Block b = new Block(v);
        hac.getCorrelation(s1,b);*/

        tagger = new Tagger(taggerSNPs,includedSNPs,excludedSNPs, hac, Options.getTaggerRsqCutoff());


    }

    public void runTagger() {
        TagThread tagThread = new TagThread(tagger);
        taggingCompleted = false;
        tagThread.start();
    }

    public Vector getTaggerSNPs() {
        return (Vector) taggerSNPs.clone();
    }

    public int getTaggedSoFar() {
        return tagger.taggedSoFar;
    }

    public Vector getForceIncludeds(){
        return tagger.getForceInclude();
    }

    public Vector getMarkerTagDetails(int i){
        //returns a vector with the details of how this marker was tagged:
        //name, tag_name, r^2 with its tag
        Vector res = new Vector();
        String name = Chromosome.getMarker(i).getName();
        res.add(name);
        if (snpHash.containsKey(name)){
            edu.mit.wi.tagger.SNP ts = (edu.mit.wi.tagger.SNP)snpHash.get(name);
            res.add(ts.getBestTag().getName());
            res.add(String.valueOf(tagger.getPairwiseCompRsq(ts,ts.getBestTag().getSequence())));
        }else{
            res.add(new String());
            res.add(new String());
        }

        return res;
    }

/*    public void setExcluded(Vector e) {
        if(e == null) {
            tagger.clearExclude();
        }else {
            Vector temp = new Vector();

            //we have a list of snp names, so we need to turn that into a list
            // of the Tagger.SNP objects corresponding to those names
            for(int i=0;i<e.size();i++) {
                if(snpHash.containsKey(e.get(i))){
                    temp.add(snpHash.get(e.get(i)));
                }
            }

            tagger.setExclude(temp);
        }
    }*/


   /* public void setIncluded(Vector e) {
        if(e == null) {
            tagger.clearInclude();
        }else {
            Vector temp = new Vector();

            //we have a list of snp names, so we need to turn that into a list
            // of the Tagger.SNP objects corresponding to those names
            for(int i=0;i<e.size();i++) {
                if(snpHash.containsKey(e.get(i))){
                    temp.add(snpHash.get(e.get(i)));
                }
            }
            tagger.setInclude(temp);
        }
    }*/
    private void taggingFinished() {
        taggingCompleted = true;
    }

    public boolean isTaggingCompleted() {
        return taggingCompleted;
    }

    public Vector getResults() {
        return results;
    }

    public void saveResultsToFile(File outFile) throws IOException {
        if(taggingCompleted) {
            tagger.saveResultToFile(outFile);
        }
    }

    public void newTagger(Vector excluded, Vector included, Vector sitesToCapture){

    }

    private class TagThread extends Thread{
        Tagger tagger;
        public TagThread(Tagger t) {
            tagger =t;
        }
        public void run() {
            results = tagger.findTags();
            taggingFinished();
        }
    }
}
