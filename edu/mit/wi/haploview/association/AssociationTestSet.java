package edu.mit.wi.haploview.association;

import edu.mit.wi.haploview.HaploData;
import edu.mit.wi.haploview.HaploViewException;
import edu.mit.wi.haploview.Chromosome;
import edu.mit.wi.haploview.Haplotype;

import java.util.Vector;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

public class AssociationTestSet {

    private Vector tests;
    private HaploData theData;

    public AssociationTestSet(HaploData hd) {
        if(hd != null) {
            theData = hd;
        }
        tests = new Vector();
    }

    public void readTestsFromFile(String fileName) throws IOException, HaploViewException {
        File testListFile = new File(fileName);
        BufferedReader in = new BufferedReader(new FileReader(testListFile));

        String currentLine;
        int lineCount =0;

        while ((currentLine = in.readLine()) != null){
            lineCount++;
            try {
                StringTokenizer st = new StringTokenizer(currentLine, " ");

                if (st.countTokens() == 0){
                    //skip blank lines
                    continue;
                } else {
                    AssociationTest t = new AssociationTest();
                    while (st.hasMoreTokens()) {
                        String currentToken = st.nextToken();

                        if(currentToken.indexOf("-") != -1) {
                            //this is a range of markers
                            int firstMarker = Integer.parseInt(currentToken.substring(0,currentToken.indexOf("-")));
                            int secondMarker = Integer.parseInt(currentToken.substring(currentToken.indexOf("-")+1));

                            for(int i=firstMarker;i<=secondMarker;i++) {
                                if(i > Chromosome.getSize()) {
                                    throw new HaploViewException("invalid marker on line " + lineCount + " in association test file");
                                }
                                t.addMarker(new Integer(i));
                            }
                        }else {
                            //if its not a range, then it should be a reference to a single marker
                            Integer nt = new Integer(currentToken);
                            if(nt.intValue() > Chromosome.getSize()) {
                                throw new HaploViewException("invalid marker on line " + lineCount + " in association test file");
                            }

                            t.addMarker(nt);
                        }
                    }

                    tests.add(t);
                }
            }catch (NumberFormatException nfe) {
                throw new HaploViewException("Format error on line " + lineCount + " in " + testListFile.getName());
            }

        }
    }

    public Haplotype[][] runTests() throws HaploViewException {
        if(tests == null || theData == null) {
            return null;
        }
        Vector blocks = new Vector();
        for(int i=0;i<tests.size();i++) {
            AssociationTest currentTest = (AssociationTest) tests.get(i);
            if(currentTest.getNumMarkers() >0) {
                blocks.add(currentTest.getMarkerArray());
            }
        }
        return theData.generateHaplotypes(blocks,false,false);
    }


    class AssociationTest {
        Vector markers;

        public AssociationTest() {
            markers = new Vector();
        }

        void addMarker(Integer m) {
            if(m != null) {
                markers.add(m);
            }
        }

        int getNumMarkers() {
            return markers.size();
        }

        int[] getMarkerArray() {
            int[] tempArray = new int[markers.size()];

            for(int i =0; i<tempArray.length;i++) {
                tempArray[i] = ((Integer)markers.get(i)).intValue()-1;
            }
            return tempArray;
        }
    }


}
