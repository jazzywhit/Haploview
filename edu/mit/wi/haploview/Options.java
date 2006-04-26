package edu.mit.wi.haploview;

import edu.mit.wi.tagger.Tagger;

public class Options implements Constants{
    private static int maxDistance = MAXDIST_DEFAULT*1000;
    private static double missingThreshold = 0.5;
    private static double spacingThreshold = 0.0;
    private static int assocTest = ASSOC_NONE;
    private static int tdtType = TDT_STD;
    private static double haplotypeDisplayThreshold = 0.01;
    private static int LDColorScheme = STD_SCHEME;
    private static boolean showGBrowse = false;
    private static long gBrowseLeft = 0;
    private static long gBrowseRight = 0;
    private static String gBrowseOpts = GB_DEFAULT_OPTS;
    private static String gBrowseTypes = GB_DEFAULT_TYPES;
    private static double taggerRsqCutoff = Tagger.DEFAULT_RSQ_CUTOFF;
    private static double taggerLODCutoff = Tagger.DEFAULT_LOD_CUTOFF;
    private static int printWhat = D_PRIME;
    private static boolean showBlockTags = false;

    public static int getLDColorScheme() {
        return LDColorScheme;
    }

    public static void setLDColorScheme(int LDColorScheme) {
        Options.LDColorScheme = LDColorScheme;
    }

    public static int getMaxDistance() {
        return maxDistance;
    }

    public static void setMaxDistance(int maxDistance) {
        //takes in max separation in kilobases and converts it to bp
        Options.maxDistance = maxDistance*1000;
    }

    public static double getMissingThreshold() {
        return missingThreshold;
    }

    public static void setMissingThreshold(double missingThreshold) {
        Options.missingThreshold = missingThreshold;
    }

    public static double getSpacingThreshold() {
        return spacingThreshold;
    }

    public static void setSpacingThreshold(double spacingThreshold) {
        //we scale from (0 to 1) to (0 to .5) since values greater than .5 cause the display to look really stupid 
        Options.spacingThreshold = spacingThreshold*0.5;
    }

    public static int getAssocTest() {
        return assocTest;
    }

    public static void setAssocTest(int assocTest) {
        Options.assocTest = assocTest;
    }

    public static double getHaplotypeDisplayThreshold() {
        return haplotypeDisplayThreshold;
    }

    public static void setHaplotypeDisplayThreshold(double haplotypeDisplayThreshold) {
        Options.haplotypeDisplayThreshold = haplotypeDisplayThreshold;
    }

    public static boolean isGBrowseShown() {
        return showGBrowse;
    }

    public static void setShowGBrowse(boolean showGBrowse) {
        Options.showGBrowse = showGBrowse;
    }

    public static long getgBrowseLeft() {
        return gBrowseLeft;
    }

    public static void setgBrowseLeft(long gBrowseLeft) {
        Options.gBrowseLeft = gBrowseLeft;
    }

    public static long getgBrowseRight() {
        return gBrowseRight;
    }

    public static void setgBrowseRight(long gBrowseRight) {
        Options.gBrowseRight = gBrowseRight;
    }

    public static String getgBrowseOpts() {
        if (gBrowseOpts.equals("")){
            return "null";
        }else{
            return gBrowseOpts;
        }
    }

    public static void setgBrowseOpts(String gBrowseOpts) {
        Options.gBrowseOpts = gBrowseOpts;
    }

    public static String getgBrowseTypes() {
        if (gBrowseTypes.equals("")){
            return "null";
        }else{
            return gBrowseTypes;
        }
    }

    public static void setgBrowseTypes(String gBrowseTypes) {
        Options.gBrowseTypes = gBrowseTypes;
    }

    public static double getTaggerRsqCutoff() {
        return taggerRsqCutoff;
    }

    public static void setTaggerRsqCutoff(double taggerRsqCutoff) {
        Options.taggerRsqCutoff = taggerRsqCutoff;
    }

    public static double getTaggerLODCutoff() {
        return taggerLODCutoff;
    }

    public static void setTaggerLODCutoff(double taggerLODCutoff) {
        Options.taggerLODCutoff = taggerLODCutoff;
    }

    public static int getTdtType() {
        return tdtType;
    }

    public static void setTdtType(int tdtType) {
        Options.tdtType = tdtType;
    }

    public static int getPrintWhat() {
        return printWhat;
    }

    public static void setPrintWhat(int printWhat) {
        Options.printWhat = printWhat;
    }

    public static boolean isShowBlockTags() {
        return showBlockTags;
    }

    public static void setShowBlockTags(boolean showBlockTags) {
        Options.showBlockTags = showBlockTags;
    }
}
