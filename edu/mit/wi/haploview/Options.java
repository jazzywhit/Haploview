package edu.mit.wi.haploview;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Aug 18, 2004
 * Time: 11:09:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class Options {
    private static int maxDistance;
    private static int genoFileType;
    private static double missingThreshold;
    private static double spacingThreshold;
    private static int assocTest;
    private static int haplotypeDisplayThreshold;
    private static int LDColorScheme;

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
}
