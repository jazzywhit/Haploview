package edu.mit.wi.haploview;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: daly
 * Date: Aug 17, 2004
 * Time: 1:12:59 PM
 * To change this template use File | Settings | File Templates.
 */

public class HaploviewOptions {

    private static boolean nogui = false;
    private static String batchMode;
    private static String hapsFileName;
    private static String infoFileName;
    private static String pedFileName;
    private static String hapmapFileName;
    private static int genoFileType;
    private static String blockfile;
    private static String trackName;
    private static boolean showCheck = false;
    private static boolean skipCheck = false;
    private static Vector ignoreMarkers = new Vector();
    private static boolean quiet = false;
    private static int output;
    private static boolean check;
    private static int maxDist;
    private static boolean dprime;
    private static boolean png;
    private static boolean smallpng;



    public static int getGenoFileType() {
        return genoFileType;
    }

    public static void setGenoFileType(int genoFileType) {
        HaploviewOptions.genoFileType = genoFileType;
    }

    public static boolean isNogui() {
        return nogui;
    }

    public static void setNogui(boolean nogui) {
        HaploviewOptions.nogui = nogui;
    }

    public static String getBatchMode() {
        return batchMode;
    }

    public static void setBatchMode(String batchMode) {
        HaploviewOptions.batchMode = batchMode;
    }

    public static String getHapsFileName() {
        return hapsFileName;
    }

    public static void setHapsFileName(String hapsFileName) {
        HaploviewOptions.hapsFileName = hapsFileName;
    }

    public static String getInfoFileName() {
        return infoFileName;
    }

    public static void setInfoFileName(String infoFileName) {
        if (!infoFileName.equals("")){
            HaploviewOptions.infoFileName = infoFileName;
        }
    }

    public static String getPedFileName() {
        return pedFileName;
    }

    public static void setPedFileName(String pedFileName) {
        HaploviewOptions.pedFileName = pedFileName;
    }

    public static String getHapmapFileName() {
        return hapmapFileName;
    }

    public static void setHapmapFileName(String hapmapFileName) {
        HaploviewOptions.hapmapFileName = hapmapFileName;
    }

    public static String getBlockfile() {
        return blockfile;
    }

    public static void setBlockfile(String blockfile) {
        HaploviewOptions.blockfile = blockfile;
    }

    public static String getTrackName() {
        return trackName;
    }

    public static void setTrackName(String trackName) {
        HaploviewOptions.trackName = trackName;
    }

    public static boolean isShowCheck() {
        return showCheck;
    }

    public static void setShowCheck(boolean showCheck) {
        HaploviewOptions.showCheck = showCheck;
    }

    public static boolean isSkipCheck() {
        return skipCheck;
    }

    public static void setSkipCheck(boolean skipCheck) {
        HaploviewOptions.skipCheck = skipCheck;
    }

    public static Vector getIgnoreMarkers() {
        return ignoreMarkers;
    }

    public static void setIgnoreMarkers(Vector ignoreMarkers) {
        HaploviewOptions.ignoreMarkers = ignoreMarkers;
    }

    public static boolean isQuiet() {
        return quiet;
    }

    public static void setQuiet(boolean quiet) {
        HaploviewOptions.quiet = quiet;
    }

    public static int getOutput() {
        return output;
    }

    public static void setOutput(int output) {
        HaploviewOptions.output = output;
    }

    public static boolean isCheck() {
        return check;
    }

    public static void setCheck(boolean check) {
        HaploviewOptions.check = check;
    }

    public static int getMaxDist() {
        return maxDist;
    }

    public static void setMaxDist(int maxDist) {
        HaploviewOptions.maxDist = maxDist*1000;
    }

    public static void setMaxDist(String md){
        if (md.equals("")){
            maxDist = 0;
        }else{
            maxDist = Integer.parseInt(md)*1000;
        }
    }

    public static boolean isDprime() {
        return dprime;
    }

    public static void setDprime(boolean dprime) {
        HaploviewOptions.dprime = dprime;
    }

    public static boolean isPng() {
        return png;
    }

    public static void setPng(boolean png) {
        HaploviewOptions.png = png;
    }

    public static boolean isSmallpng() {
        return smallpng;
    }

    public static void setSmallpng(boolean smallpng) {
        HaploviewOptions.smallpng = smallpng;
    }



}
