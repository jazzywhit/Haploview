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
        Options.maxDistance = maxDistance;
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
        Options.spacingThreshold = spacingThreshold;
    }

    public static int getAssocTest() {
        return assocTest;
    }

    public static void setAssocTest(int assocTest) {
        Options.assocTest = assocTest;
    }
}
