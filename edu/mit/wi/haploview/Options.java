package edu.mit.wi.haploview;

public class Options {
    private static int maxDistance;
    private static int genoFileType;
    private static double missingThreshold;
    private static double spacingThreshold;
    private static int assocTest;
    private static int haplotypeDisplayThreshold;
    private static int LDColorScheme;
    private static boolean showGBrowse;
    private static long gBrowseLeft;
    private static long gBrowseRight;
    private static String gBrowseOpts;
    private static String gBrowseTypes;
    private static double taggerRsqCutoff;

    public static int getLDColorScheme() {
        return LDColorScheme;
    }

    public static void setLDColorScheme(int LDColorScheme) {
        Options.LDColorScheme = LDColorScheme;
    }

    public static int getGenoFileType() {
        return genoFileType;
    }

    public static void setGenoFileType(int genoFileType) {
        Options.genoFileType = genoFileType;
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

    public static int getHaplotypeDisplayThreshold() {
        return haplotypeDisplayThreshold;
    }

    public static void setHaplotypeDisplayThreshold(int haplotypeDisplayThreshold) {
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
}
