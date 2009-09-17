package edu.mit.wi.haploview;

import edu.mit.wi.pedfile.MarkerResult;
import edu.mit.wi.pedfile.PedFileException;
import edu.mit.wi.pedfile.CheckData;
import edu.mit.wi.haploview.association.*;
import edu.mit.wi.haploview.tagger.TaggerController;
import edu.mit.wi.tagger.Tagger;
import edu.mit.wi.tagger.TaggerException;

import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.MalformedURLException;

import com.sun.jimi.core.Jimi;
import com.sun.jimi.core.JimiException;
import org.apache.log4j.*;
import org.apache.log4j.varia.DenyAllFilter;
import org.apache.batik.svggen.SVGGraphics2D;

public class HaploText implements Constants {
    private boolean nogui = false;
    private String outputRootName;
    private String batchFileName;
    private String hapsFileName;
    private String infoFileName;
    private String pedFileName;
    private String hapmapFileName;
    private String phasedhmpdataFileName;
    private String phasedhmpsampleFileName;
    private String phasedhmplegendFileName;
    //private String fastphaseFileName;
    private String plinkFileName;
    private String mapFileName;
    private boolean phasedhapmapDownload = false;
    private boolean singlePhaseFile = false;
    private boolean hapmapPhase3 = false;
    private boolean SNPBased = true;
    private String selectCols;
    private String blockName;
    private String trackName;
    private String customAssocTestsFileName;
    private boolean skipCheck = false;
    private Vector excludedMarkers = new Vector();
    private boolean quietMode = false;
    private int blockOutputType;
    private boolean outputCheck;
    private boolean individualCheck;
    private boolean mendel;
    private boolean malehets;
    private boolean outputDprime;
    private boolean outputPNG;
    private boolean outputCompressedPNG;
    private boolean outputSVG;
    private boolean infoTrack;
    private boolean doPermutationTest;
    private boolean findTags;
    private boolean aggressiveTagging;
    private boolean outputConditionalHaps;
    private boolean randomizeAffection = false;
    private int permutationCount;
    private int tagging;
    private int maxNumTags;
    private int aggressiveNumMarkers = 0;
    private double tagRSquaredCutOff = -1;
    private Vector forceIncludeTags;
    private String forceIncludeName;
    private Vector forceExcludeTags;
    private String forceExcludeName;
    private Vector captureAlleleTags;
    private String captureAllelesName;
    private Hashtable designScores;
    private String designScoresName;
    private String minTagDistance;
    private Vector argHandlerMessages;
    private String chromosomeArg;
    private String[] phasedHapMapInfo;
    private String panelArg, startPos, endPos, release;
    private String logFileName, debugFileName;
    private boolean commandLineError;

    public static Logger logger = Logger.getLogger("logger");
    public static Logger commandLogger = Logger.getLogger("logger.command");

    public boolean isNogui() {
        return nogui;
    }

    public String getBatchMode() {
        return batchFileName;
    }

    public String getHapsFileName() {
        return hapsFileName;
    }

    public String getPedFileName() {
        return pedFileName;
    }

    public String getInfoFileName() {
        return infoFileName;
    }

    public String getHapmapFileName() {
        return hapmapFileName;
    }

    public String getPhasedHmpDataName() {
        return phasedhmpdataFileName;
    }

    public boolean getSinglePhasedFile() {
        return singlePhaseFile;
    }

    public String getPhasedHmpSampleName() {
        return phasedhmpsampleFileName;
    }

    public String getPhasedHmpLegendName() {
        return phasedhmplegendFileName;
    }

    /*public String getFastphaseFileName(){
        return fastphaseFileName;
    }*/

    public boolean getPhasedHmpDownload() {
        return phasedhapmapDownload;
    }

    public String getChromosome() {
        return chromosomeArg;
    }

    public String getPanel() {
        return panelArg;
    }

    public String getStartPos() {
        return startPos;
    }

    public String getEndPos() {
        return endPos;
    }

    public String getRelease() {
        return release;
    }

    public String getPlinkFileName() {
        return plinkFileName;
    }

    public String getMapFileName() {
        return mapFileName;
    }

    public String getSelectCols() {
        return selectCols;
    }

    public int getBlockOutputType() {
        return blockOutputType;
    }

    public boolean getCommandLineError() {
        return commandLineError;
    }

    private double getDoubleArg(String[] args, int valueIndex, double min, double max) {
        double argument = 0;
        String argName = args[valueIndex - 1];
        if (valueIndex >= args.length || ((args[valueIndex].charAt(0)) == '-')) {
            die(argName + " requires a value between " + min + " and " + max);
        }
        try {
            argument = Double.parseDouble(args[valueIndex]);
            if (argument < min || argument > max) {
                die(argName + " requires a value between " + min + " and " + max);
            }
        } catch (NumberFormatException nfe) {
            die(argName + " requires a value between " + min + " and " + max);
        }
        return argument;
    }

    private int getIntegerArg(String[] args, int valueIndex) {
        int argument = 0;
        String argName = args[valueIndex - 1];
        if (valueIndex >= args.length || ((args[valueIndex].charAt(0)) == '-')) {
            die(argName + " requires an integer argument");
        } else {
            try {
                argument = Integer.parseInt(args[valueIndex]);
                if (argument < 0) {
                    die(argName + " argument must be a positive integer");
                }
            } catch (NumberFormatException nfe) {
                die(argName + " argument must be a positive integer");
            }
        }
        return argument;
    }

    public HaploText(String[] args) {
        this.argHandler(args);

        if (this.batchFileName != null) {
            commandLogger.warn("*****************************************************");
            commandLogger.warn(TITLE_STRING + "\tJava Version: " + JAVA_VERSION);
            commandLogger.warn("*****************************************************\n\n");
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < args.length; i++) {
                buffer.append(args[i]).append("\t");
            }
            String arguments = buffer.toString();

            commandLogger.warn("Arguments:\t" + arguments + "\n\n");
            for (int i = 0; i < argHandlerMessages.size(); i++) {
                commandLogger.warn(argHandlerMessages.get(i));
            }
            this.doBatch();
        }

        if (!(this.pedFileName == null) || !(this.hapsFileName == null) || !(this.hapmapFileName == null) || !(this.phasedhmpdataFileName == null) /*|| !(this.fastphaseFileName == null)*/ || phasedhapmapDownload) {
            if (nogui) {
                commandLogger.warn("*****************************************************");
                commandLogger.warn(TITLE_STRING + "\tJava Version: " + JAVA_VERSION);
                commandLogger.warn("*****************************************************\n\n");
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < args.length; i++) {
                    buffer.append(args[i]).append("\t");
                }
                String arguments = buffer.toString();

                commandLogger.info("Arguments:\t" + arguments + "\n\n");
                for (int i = 0; i < argHandlerMessages.size(); i++) {
                    commandLogger.warn(argHandlerMessages.get(i));
                }
                processTextOnly();
            }
        }

    }

    private void argHandler(String[] args) {

        argHandlerMessages = new Vector();
        int maxDistance = -1;
        //this means that user didn't specify any output type if it doesn't get changed below
        blockOutputType = -1;
        double hapThresh = -1;
        double minimumMAF = -1;
        double spacingThresh = -1;
        double minimumGenoPercent = -1;
        double hwCutoff = -1;
        double missingCutoff = -1;
        int maxMendel = -1;
        boolean assocTDT = false;
        boolean assocCC = false;
        permutationCount = 0;
        tagging = Tagger.NONE;
        maxNumTags = Tagger.DEFAULT_MAXNUMTAGS;
        findTags = true;

        double cutHighCI = -1;
        double cutLowCI = -1;
        double mafThresh = -1;
        double recHighCI = -1;
        double informFrac = -1;
        double fourGameteCutoff = -1;
        double spineDP = -1;


        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-h")) {
                System.out.println(HELP_OUTPUT);
                System.exit(0);
            } else if (args[i].equalsIgnoreCase("-version") || args[i].equalsIgnoreCase("-v")) {
                System.out.println(VERSION);
                System.exit(0);
            } else if (args[i].equalsIgnoreCase("-n") || args[i].equalsIgnoreCase("-nogui")) {
                nogui = true;
            } else if (args[i].equalsIgnoreCase("-log")) {
                i++;
                if (i >= args.length || args[i].charAt(0) == '-') {
                    logFileName = "haploview.log";
                    i--;
                } else {
                    logFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-debug")) {
                i++;
                if (i >= args.length || args[i].charAt(0) == '-') {
                    debugFileName = "";
                    i--;
                } else {
                    debugFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-out")) {
                i++;
                if (i >= args.length || (args[i].charAt(0) == '-')) {
                    die(args[i - 1] + " requires a fileroot");
                } else {
                    if (outputRootName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last fileroot listed will be used");
                    }
                    outputRootName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("-pedfile")) {
                i++;
                if (i >= args.length || (args[i].charAt(0) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (pedFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last pedfile listed will be used");
                    }
                    pedFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-pcloadletter")) {
                die("PC LOADLETTER?! What the fuck does that mean?!");
            } else if (args[i].equalsIgnoreCase("-skipcheck") || args[i].equalsIgnoreCase("--skipcheck")) {
                skipCheck = true;
            } else if (args[i].equalsIgnoreCase("-excludeMarkers")) {
                i++;
                if (i >= args.length || (args[i].charAt(0) == '-')) {
                    die("-excludeMarkers requires a list of markers");
                } else {
                    StringTokenizer str = new StringTokenizer(args[i], ",");
                    try {
                        StringBuffer sb = new StringBuffer();
                        if (!quietMode) sb.append("Excluding markers: ");
                        while (str.hasMoreTokens()) {
                            String token = str.nextToken();
                            if (token.indexOf("..") != -1) {
                                int lastIndex = token.indexOf("..");
                                int rangeStart = Integer.parseInt(token.substring(0, lastIndex));
                                int rangeEnd = Integer.parseInt(token.substring(lastIndex + 2, token.length()));
                                for (int j = rangeStart; j <= rangeEnd; j++) {
                                    if (!quietMode) sb.append(j).append(" ");
                                    excludedMarkers.add(new Integer(j));
                                }
                            } else {
                                if (!quietMode) sb.append(token).append(" ");
                                excludedMarkers.add(new Integer(token));
                            }
                        }
                        argHandlerMessages.add(sb.toString());
                    } catch (NumberFormatException nfe) {
                        die("-excludeMarkers argument should be of the format: 1,3,5..8,12");
                    }
                }
            } else if (args[i].equalsIgnoreCase("-ha") || args[i].equalsIgnoreCase("-haps")) {
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (hapsFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last haps file listed will be used");
                    }
                    hapsFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-i") || args[i].equalsIgnoreCase("-info")) {
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (infoFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last info file listed will be used");
                    }
                    infoFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-a") || args[i].equalsIgnoreCase("-hapmap")) {
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (hapmapFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last hapmap file listed will be used");
                    }
                    hapmapFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-phasedhmpdata")) {
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (phasedhmpdataFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last phased hapmap data file listed will be used");
                    }
                    phasedhmpdataFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-singlephased")) {
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (phasedhmpdataFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last phased hapmap data file listed will be used");
                    }
                    phasedhmpdataFileName = args[i];
                    singlePhaseFile = true;
                }
            } else if (args[i].equalsIgnoreCase("-phasedhmpsample")) {
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (phasedhmpsampleFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last phased hapmap sample file listed will be used");
                    }
                    phasedhmpsampleFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-phasedhmplegend")) {
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (phasedhmplegendFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last phased hapmap legend file listed will be used");
                    }
                    phasedhmplegendFileName = args[i];
                }
            }
            /*  else if (args[i].equalsIgnoreCase("-fastphase")){
                i++;
                if(i>=args.length || ((args[i].charAt(0)) == '-')){
                    die(args[i-1] + " requires a filename");
                }
                else{
                    if(fastphaseFileName != null){
                        argHandlerMessages.add("multiple "+args[i-1] + " arguments found. only last phased hapmap data file listed will be used");
                    }
                    fastphaseFileName = args[i];
                }
            }*/
            else if (args[i].equalsIgnoreCase("-hapmapDownload")) {
                phasedhapmapDownload = true;
            } else if (args[i].equalsIgnoreCase("-hapmapPhase3")) {
                hapmapPhase3 = true;
            } else if (args[i].equalsIgnoreCase("-plink")) {
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (plinkFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last PLINK file listed will be used");
                    }
                    plinkFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-map")) {
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (mapFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last map file listed will be used");
                    }
                    mapFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-nonSNP")) {
                SNPBased = false;
            } else if (args[i].equalsIgnoreCase("-selectCols")) {
                selectCols = "Y";
            } else if (args[i].equalsIgnoreCase("-k") || args[i].equalsIgnoreCase("-blocks")) {
                i++;
                if (!(i >= args.length) && !((args[i].charAt(0)) == '-')) {
                    blockName = args[i];
                    blockOutputType = BLOX_CUSTOM;
                } else {
                    die(args[i - 1] + " requires a filename");
                }
            } else if (args[i].equalsIgnoreCase("-png")) {
                outputPNG = true;
            } else if (args[i].equalsIgnoreCase("-smallpng") || args[i].equalsIgnoreCase("-compressedPNG")) {
                outputCompressedPNG = true;
            } else if (args[i].equalsIgnoreCase("-svg")) {
                outputSVG = true;
            } else if (args[i].equalsIgnoreCase("-infoTrack")) {
                infoTrack = true;
            } else if (args[i].equalsIgnoreCase("-track")) {
                i++;
                if (!(i >= args.length) && !((args[i].charAt(0)) == '-')) {
                    trackName = args[i];
                } else {
                    die("-track requires a filename");
                }
            } else
            if (args[i].equalsIgnoreCase("-o") || args[i].equalsIgnoreCase("-output") || args[i].equalsIgnoreCase("-blockoutput")) {
                i++;
                if (!(i >= args.length) && !((args[i].charAt(0)) == '-')) {
                    if (blockOutputType != -1) {
                        die("Only one block output type argument is allowed.");
                    }
                    if (args[i].equalsIgnoreCase("SFS") || args[i].equalsIgnoreCase("GAB")) {
                        blockOutputType = BLOX_GABRIEL;
                    } else if (args[i].equalsIgnoreCase("GAM")) {
                        blockOutputType = BLOX_4GAM;
                    } else if (args[i].equalsIgnoreCase("MJD") || args[i].equalsIgnoreCase("SPI")) {
                        blockOutputType = BLOX_SPINE;
                    } else if (args[i].equalsIgnoreCase("ALL")) {
                        blockOutputType = BLOX_ALL;
                    }
                } else {
                    //defaults to SFS output
                    blockOutputType = BLOX_GABRIEL;
                    i--;
                }
            } else if (args[i].equalsIgnoreCase("-showBlockTags")) { //This option is undocumented to discourage its use
                Options.setShowBlockTags(true);
            } else
            if (args[i].equalsIgnoreCase("-d") || args[i].equalsIgnoreCase("--dprime") || args[i].equalsIgnoreCase("-dprime")) {
                outputDprime = true;
            } else if (args[i].equalsIgnoreCase("-c") || args[i].equalsIgnoreCase("-check")) {
                outputCheck = true;
            } else if (args[i].equalsIgnoreCase("-indcheck")) {
                individualCheck = true;
            } else if (args[i].equalsIgnoreCase("-mendel")) {
                mendel = true;
            } else if (args[i].equalsIgnoreCase("-malehets")) {
                malehets = true;
            } else if (args[i].equalsIgnoreCase("-m") || args[i].equalsIgnoreCase("-maxdistance")) {
                i++;
                maxDistance = getIntegerArg(args, i);
            } else if (args[i].equalsIgnoreCase("-b") || args[i].equalsIgnoreCase("-batch")) {
                //batch mode
                i++;
                if (i >= args.length || ((args[i].charAt(0)) == '-')) {
                    die(args[i - 1] + " requires a filename");
                } else {
                    if (batchFileName != null) {
                        argHandlerMessages.add("multiple " + args[i - 1] + " arguments found. only last batch file listed will be used");
                    }
                    batchFileName = args[i];
                }
            } else if (args[i].equalsIgnoreCase("-hapthresh")) {
                i++;
                hapThresh = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-spacing")) {
                i++;
                spacingThresh = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-minMAF")) {
                i++;
                minimumMAF = getDoubleArg(args, i, 0, 0.5);
            } else if (args[i].equalsIgnoreCase("-minGeno") || args[i].equalsIgnoreCase("-minGenoPercent")) {
                i++;
                minimumGenoPercent = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-hwcutoff")) {
                i++;
                hwCutoff = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-maxMendel")) {
                i++;
                maxMendel = getIntegerArg(args, i);
            } else if (args[i].equalsIgnoreCase("-missingcutoff")) {
                i++;
                missingCutoff = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-assoctdt")) {
                assocTDT = true;
            } else if (args[i].equalsIgnoreCase("-assoccc")) {
                assocCC = true;
            } else if (args[i].equalsIgnoreCase("-randomcc")) {
                assocCC = true;
                randomizeAffection = true;
            } else if (args[i].equalsIgnoreCase("-ldcolorscheme")) {
                i++;
                if (!(i >= args.length) && !((args[i].charAt(0)) == '-')) {
                    if (args[i].equalsIgnoreCase("default")) {
                        Options.setLDColorScheme(STD_SCHEME);
                    } else if (args[i].equalsIgnoreCase("RSQ")) {
                        Options.setLDColorScheme(RSQ_SCHEME);
                    } else if (args[i].equalsIgnoreCase("DPALT")) {
                        Options.setLDColorScheme(WMF_SCHEME);
                    } else if (args[i].equalsIgnoreCase("GAB")) {
                        Options.setLDColorScheme(GAB_SCHEME);
                    } else if (args[i].equalsIgnoreCase("GAM")) {
                        Options.setLDColorScheme(GAM_SCHEME);
                    } else if (args[i].equalsIgnoreCase("GOLD")) {
                        Options.setLDColorScheme(GOLD_SCHEME);
                    }
                } else {
                    //defaults to STD color scheme
                    Options.setLDColorScheme(STD_SCHEME);
                    i--;
                }
            } else if (args[i].equalsIgnoreCase("-ldvalues")) {
                i++;
                if (!(i >= args.length) && !((args[i].charAt(0)) == '-')) {
                    if (args[i].equalsIgnoreCase("RSQ")) {
                        Options.setPrintWhat(R_SQ);
                    } else if (args[i].equalsIgnoreCase("DPRIME")) {
                        Options.setPrintWhat(D_PRIME);
                    } else if (args[i].equalsIgnoreCase("NONE")) {
                        Options.setPrintWhat(LD_NONE);
                    }
                } else {
                    //defaults to printing DPRIME
                    Options.setPrintWhat(D_PRIME);
                    i--;
                }
            } else if (args[i].equalsIgnoreCase("-blockCutHighCI")) {
                i++;
                cutHighCI = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-blockCutLowCI")) {
                i++;
                cutLowCI = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-blockMafThresh")) {
                i++;
                mafThresh = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-blockRecHighCI")) {
                i++;
                recHighCI = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-blockInformFrac")) {
                i++;
                informFrac = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-block4GamCut")) {
                i++;
                fourGameteCutoff = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-blockSpineDP")) {
                i++;
                spineDP = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-permtests")) {
                i++;
                doPermutationTest = true;
                permutationCount = getIntegerArg(args, i);
            } else if (args[i].equalsIgnoreCase("-customassoc")) {
                i++;
                if (!(i >= args.length) && !((args[i].charAt(0)) == '-')) {
                    customAssocTestsFileName = args[i];
                } else {
                    die(args[i - 1] + " requires a filename");
                }
            } else if (args[i].equalsIgnoreCase("-aggressiveTagging")) {
                //tagging = Tagger.AGGRESSIVE_TRIPLE;
                aggressiveTagging = true;
            } else if (args[i].equalsIgnoreCase("-aggressiveNumMarkers")) {
                i++;
                aggressiveNumMarkers = getIntegerArg(args, i);
                if (aggressiveNumMarkers != 2 && aggressiveNumMarkers != 3) {
                    die(args[i - 1] + " requires a value of either 2 or 3");
                }
            } else if (args[i].equalsIgnoreCase("-pairwiseTagging")) {
                tagging = Tagger.PAIRWISE_ONLY;
            } else if (args[i].equalsIgnoreCase("-printalltags")) {
                Options.setPrintAllTags(true);
            } else if (args[i].equalsIgnoreCase("-maxNumTags")) {
                i++;
                maxNumTags = getIntegerArg(args, i);
            } else if (args[i].equalsIgnoreCase("-tagrSqCutoff")) {
                i++;
                tagRSquaredCutOff = getDoubleArg(args, i, 0, 1);
            } else if (args[i].equalsIgnoreCase("-dontaddtags")) {
                findTags = false;
            } else if (args[i].equalsIgnoreCase("-tagLODCutoff")) {
                i++;
                Options.setTaggerLODCutoff(getDoubleArg(args, i, 0, 100000));
            } else if (args[i].equalsIgnoreCase("-includeTags")) {
                i++;
                if (i >= args.length || args[i].charAt(0) == '-') {
                    die(args[i - 1] + " requires a list of marker names.");
                }
                StringTokenizer str = new StringTokenizer(args[i], ",");
                forceIncludeTags = new Vector();
                while (str.hasMoreTokens()) {
                    forceIncludeTags.add(str.nextToken());
                }
            } else if (args[i].equalsIgnoreCase("-includeTagsFile")) {
                i++;
                if (!(i >= args.length) && !(args[i].charAt(0) == '-')) {
                    forceIncludeName = args[i];
                } else {
                    die(args[i - 1] + " requires a filename");
                }
            } else if (args[i].equalsIgnoreCase("-excludeTags")) {
                i++;
                if (i >= args.length || args[i].charAt(0) == '-') {
                    die("-excludeTags requires a list of marker names.");
                }
                StringTokenizer str = new StringTokenizer(args[i], ",");
                forceExcludeTags = new Vector();
                while (str.hasMoreTokens()) {
                    forceExcludeTags.add(str.nextToken());
                }
            } else if (args[i].equalsIgnoreCase("-excludeTagsFile")) {
                i++;
                if (!(i >= args.length) && !(args[i].charAt(0) == '-')) {
                    forceExcludeName = args[i];
                } else {
                    die(args[i - 1] + " requires a filename");
                }
            } else if (args[i].equalsIgnoreCase("-captureAlleles")) {
                i++;
                if (!(i >= args.length) && !(args[i].charAt(0) == '-')) {
                    captureAllelesName = args[i];
                } else {
                    die(args[i - 1] + " requires a filename");
                }
            } else if (args[i].equalsIgnoreCase("-designScores")) {
                i++;
                if (!(i >= args.length) && !(args[i].charAt(0) == '-')) {
                    designScoresName = args[i];
                } else {
                    die(args[i - 1] + " requires a filename");
                }
            } else if (args[i].equalsIgnoreCase("-mindesignscores")) {
                i++;
                Options.setTaggerMinDesignScore(getDoubleArg(args, i, 0, Double.MAX_VALUE));
            } else if (args[i].equalsIgnoreCase("-mintagdistance")) {
                i++;
                minTagDistance = args[i];
            } else if (args[i].equalsIgnoreCase("-tagrsqcounts")) {
                outputConditionalHaps = true;
            } else if (args[i].equalsIgnoreCase("-chromosome") || args[i].equalsIgnoreCase("-chr")) {
                i++;
                if (!(i >= args.length) && !(args[i].charAt(0) == '-')) {
                    chromosomeArg = args[i];
                } else {
                    die(args[i - 1] + " requires a chromosome name");
                }

                if (!(chromosomeArg.equalsIgnoreCase("X")) && !(chromosomeArg.equalsIgnoreCase("Y"))) {
                    try {
                        if (Integer.parseInt(chromosomeArg) > 22) {
                            die("-chromosome requires a chromosome name of 1-22, X, or Y");
                        }
                    } catch (NumberFormatException nfe) {
                        die("-chromosome requires a chromosome name of 1-22, X, or Y");
                    }
                }

            } else if (args[i].equalsIgnoreCase("-panel")) {
                i++;
                if (!(i >= args.length) && !(args[i].charAt(0) == '-')) {
                    panelArg = args[i];
                } else {
                    die(args[i - 1] + "requires an analysis panel name");
                }
            } else if (args[i].equalsIgnoreCase("-startpos")) {
                i++;
                startPos = args[i];
            } else if (args[i].equalsIgnoreCase("-endPos")) {
                i++;
                endPos = args[i];
            } else if (args[i].equalsIgnoreCase("-release")) {
                i++;
                release = args[i];
            } else if (args[i].equalsIgnoreCase("-q") || args[i].equalsIgnoreCase("-quiet")) {
                quietMode = true;
            } else if (args[i].equalsIgnoreCase("-gzip")) {
                Options.setGzip(true);
            } else {
                die("invalid parameter specified: " + args[i]);
            }
        }

        ConsoleAppender nullAppender = new ConsoleAppender();
        nullAppender.addFilter(new DenyAllFilter());
        if (debugFileName != null) {
            if (logFileName != null) {
                System.err.println("You may specify either -log or -debug but not both, ignoring -log.");
            }
            if (debugFileName.equals("")) {
                logger.addAppender(new ConsoleAppender(new PatternLayout()));
                logger.setLevel(Level.DEBUG);
                commandLogger.addAppender(nullAppender);
            } else {
                try {
                    logger.addAppender(new FileAppender(new PatternLayout(), debugFileName, false));
                } catch (IOException ioe) {
                    System.err.println("An error occurred while writing to the debug file.");
                }
                logger.setLevel(Level.DEBUG);
                commandLogger.addAppender(new ConsoleAppender(new PatternLayout()));
                commandLogger.setLevel(Level.INFO);
            }
        } else if (logFileName != null) {
            try {
                logger.addAppender(new FileAppender(new PatternLayout(), logFileName, false));
            } catch (IOException ioe) {
                System.err.println("An error occurred while writing to the log file.");
            }
            logger.setLevel(Level.INFO);
            commandLogger.addAppender(new ConsoleAppender(new PatternLayout()));
            commandLogger.setLevel(Level.INFO);
        } else {
            logger.addAppender(nullAppender);
            commandLogger.addAppender(new ConsoleAppender(new PatternLayout()));
            if (quietMode) {
                commandLogger.setLevel(Level.WARN);
            } else {
                commandLogger.setLevel(Level.INFO);
            }
        }

        logger.setAdditivity(false);
        commandLogger.setAdditivity(true);
        //TODO: Convert all active System.out.println statements to commandLogger.info()
        //TODO: Convert all active System.err.println statements to commandLogger.error()
        //TODO: Convert all debug statements to logger.debug()


        int countOptions = 0;
        if (pedFileName != null) {
            countOptions++;
        }
        if (hapsFileName != null) {
            countOptions++;
        }
        if (hapmapFileName != null) {
            countOptions++;
        }
        if (phasedhmpdataFileName != null) {
            countOptions++;

            if (!singlePhaseFile) {
                if (phasedhmpsampleFileName == null) {
                    die("You must specify a sample file for phased hapmap input.");
                } else if (phasedhmplegendFileName == null) {
                    die("You must specify a legend file for phased hapmap input.");
                }
            }
        }
        /*   if(fastphaseFileName != null) {
            countOptions++;
            if (infoFileName == null) {
                die("You must specify an info file for PHASE format input.");
            }
        }*/
        if (phasedhapmapDownload) {
            countOptions++;
        }
        if (plinkFileName != null) {
            countOptions++;
            Options.setSNPBased(SNPBased);
            if (mapFileName == null && Options.getSNPBased()) {
                die("You must specify a map file for plink format input.");
            }
        }
        if (batchFileName != null) {
            countOptions++;
        }
        if (countOptions > 1) {
            die("Only one genotype input file may be specified on the command line.");
        } else if (countOptions == 0 && nogui) {
            die("You must specify a genotype input file.");
        }

        //mess with vars, set defaults, etc
        if (skipCheck) {
            argHandlerMessages.add("Skipping genotype file check");
        }
        if (maxDistance == -1) {
            maxDistance = MAXDIST_DEFAULT;
        } else {
            argHandlerMessages.add("Max LD comparison distance = " + maxDistance + "kb");
        }

        Options.setMaxDistance(maxDistance);

        if (hapThresh != -1) {
            Options.setHaplotypeDisplayThreshold(hapThresh);
            argHandlerMessages.add("Haplotype display threshold = " + hapThresh);
        }

        if (minimumMAF != -1) {
            CheckData.mafCut = minimumMAF;
            argHandlerMessages.add("Minimum MAF = " + minimumMAF);
        }

        if (minimumGenoPercent != -1) {
            CheckData.failedGenoCut = (int) (minimumGenoPercent * 100);
            argHandlerMessages.add("Minimum SNP genotype % = " + minimumGenoPercent);
        }

        if (hwCutoff != -1) {
            CheckData.hwCut = hwCutoff;
            argHandlerMessages.add("Hardy Weinberg equilibrium p-value cutoff = " + hwCutoff);
        }

        if (maxMendel != -1) {
            CheckData.numMendErrCut = maxMendel;
            argHandlerMessages.add("Maximum number of Mendel errors = " + maxMendel);
        }

        if (spacingThresh != -1) {
            Options.setSpacingThreshold(spacingThresh);
            argHandlerMessages.add("LD display spacing value = " + spacingThresh);
        }

        if (missingCutoff != -1) {
            Options.setMissingThreshold(missingCutoff);
            argHandlerMessages.add("Maximum amount of missing data allowed per individual = " + missingCutoff);
        }

        if (cutHighCI != -1) {
            FindBlocks.cutHighCI = cutHighCI;
        }

        if (cutLowCI != -1) {
            FindBlocks.cutLowCI = cutLowCI;
        }
        if (mafThresh != -1) {
            FindBlocks.mafThresh = mafThresh;
        }
        if (recHighCI != -1) {
            FindBlocks.recHighCI = recHighCI;
        }
        if (informFrac != -1) {
            FindBlocks.informFrac = informFrac;
        }
        if (fourGameteCutoff != -1) {
            FindBlocks.fourGameteCutoff = fourGameteCutoff;
        }
        if (spineDP != -1) {
            FindBlocks.spineDP = spineDP;
        }

        if (assocTDT) {
            Options.setAssocTest(ASSOC_TRIO);
        } else if (assocCC) {
            Options.setAssocTest(ASSOC_CC);
        }

        if (Options.getAssocTest() != ASSOC_NONE && infoFileName == null && hapmapFileName == null) {
            die("A marker info file must be specified when performing association tests.");
        }

        if (doPermutationTest) {
            if (!assocCC && !assocTDT) {
                die("An association test type must be specified for permutation tests to be performed.");
            }
        }

        if (customAssocTestsFileName != null) {
            if (!assocCC && !assocTDT) {
                die("An association test type must be specified when using a custom association test file.");
            }
            if (infoFileName == null) {
                die("A marker info file must be specified when using a custom association test file.");
            }
        }

        if (aggressiveTagging) {
            if (aggressiveNumMarkers == 3) {
                tagging = Tagger.AGGRESSIVE_TRIPLE;
            } else {
                tagging = Tagger.AGGRESSIVE_DUPLE;
            }
        }

        if (tagging != Tagger.NONE) {
            if (infoFileName == null && hapmapFileName == null && batchFileName == null && phasedhmpdataFileName == null && !phasedhapmapDownload) {
                die("A marker info file must be specified when tagging.");
            }

            if (forceExcludeTags == null) {
                forceExcludeTags = new Vector();
            } else if (forceExcludeName != null) {
                die("-excludeTags and -excludeTagsFile cannot both be used");
            }

            if (forceExcludeName != null) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream(forceExcludeName)));
                    forceExcludeTags = new Vector();
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            forceExcludeTags.add(line);
                        }
                    }
                } catch (IOException ioe) {
                    die("An error occured while reading the file specified by -excludeTagsFile.");
                }
            }

            if (forceIncludeTags == null) {
                forceIncludeTags = new Vector();
            } else if (forceIncludeName != null) {
                die("-includeTags and -includeTagsFile cannot both be used");
            }

            if (forceIncludeName != null) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream(forceIncludeName)));
                    forceIncludeTags = new Vector();
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            forceIncludeTags.add(line);
                        }
                    }
                } catch (IOException ioe) {
                    die("An error occured while reading the file specified by -includeTagsFile.");
                }
            }

            if (captureAllelesName != null) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream(captureAllelesName)));
                    captureAlleleTags = new Vector();
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            line = line.trim();
                            captureAlleleTags.add(line);
                        }
                    }
                } catch (IOException ioe) {
                    die("An error occured while reading the file specified by -captureAlleles.");
                }
            }

            if (designScoresName != null) {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(getInputStream(designScoresName)));
                    designScores = new Hashtable(1, 1);
                    String line;
                    int lines = 0;
                    while ((line = br.readLine()) != null) {
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            StringTokenizer st = new StringTokenizer(line);
                            int length = st.countTokens();
                            if (length != 2) {
                                die("Invalid formatting on line " + lines);
                            }
                            String marker = st.nextToken();
                            Double score = new Double(st.nextToken());
                            designScores.put(marker, score);
                        }
                        lines++;
                    }
                } catch (IOException ioe) {
                    die("An error occured while reading the file specified by -designScores.");
                }
            }

            if (minTagDistance != null) {
                try {
                    if (Integer.parseInt(minTagDistance) < 0) {
                        die("minimum tag distance cannot be negative");
                    }
                } catch (NumberFormatException nfe) {
                    die("minimum tag distance must be a positive integer");
                }
                Options.setTaggerMinDistance(Integer.parseInt(minTagDistance));
            }

            //check that there isn't any overlap between include/exclude lists
            Vector tempInclude = (Vector) forceIncludeTags.clone();
            tempInclude.retainAll(forceExcludeTags);
            if (tempInclude.size() > 0) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < tempInclude.size(); i++) {
                    String s = (String) tempInclude.elementAt(i);
                    sb.append(s).append(",");
                }
                die("The following markers appear in both the include and exclude lists: " + sb.toString());
            }

            if (tagRSquaredCutOff != -1) {
                Options.setTaggerRsqCutoff(tagRSquaredCutOff);
            }

        } else if (forceExcludeTags != null || forceIncludeTags != null || tagRSquaredCutOff != -1) {
            die("-tagrSqCutoff, -excludeTags, -excludeTagsFile, -includeTags and -includeTagsFile cannot be used without a tagging option");
        }


        if (chromosomeArg != null && hapmapFileName != null) {
            argHandlerMessages.add("-chromosome flag ignored when loading hapmap file");
            chromosomeArg = null;
        }

        if (chromosomeArg != null) {
            if ((chromosomeArg.equalsIgnoreCase("X") || chromosomeArg.equalsIgnoreCase("Y")) && hapsFileName != null) {
                die("Chromosome X and Chromosome Y are not supported in the phased haplotypes file format.");
            }
            Chromosome.setDataChrom("chr" + chromosomeArg);
        } else {
            chromosomeArg = "";
        }

        if (phasedhapmapDownload) {
            if (chromosomeArg == null) {
                die("-hapmapDownload requires a chromosome specification");
            }
            if (!checkReleaseName())
                System.exit(1);
            if (!checkPanelName())
                System.exit(1);

            System.out.println("release = " + release);
            
            try {
                if (Integer.parseInt(startPos) > Integer.parseInt(endPos)) {
                    die("-endpos must be greater then -startpos");
                }
            } catch (NumberFormatException nfe) {
                die("-startpos and -endpos must be integer values");
            }


        }
    }

    private boolean checkReleaseName() {

        if (release == null) {
            if (hapmapPhase3) {
                release = DEFAULT_HM3_RELEASE;
                return true;
            } else {
                release = DEFAULT_HM_RELEASE;
                return true;
            }
        }

        String errorString = "--You have specified an invalid release. Available releases:\n" +
                "\t[Hapmap2] ";

        for (int i = 0; i < RELEASE_NAMES.length; i++) {
            errorString += (RELEASE_NAMES[i]);
            if (i != RELEASE_NAMES.length - 1) {
                errorString += ", ";
            }

            if (RELEASE_NAMES[i].equalsIgnoreCase(release)) {
                hapmapPhase3 = false;
                release = release.toUpperCase();
                return true;
            }
        }

        errorString += "\n\t[Hapmap3] ";
        for (int i = 0; i < RELEASE_NAMES_HM3.length; i++) {
            errorString += (RELEASE_NAMES_HM3[i]);
            if (i != RELEASE_NAMES_HM3.length - 1) {
                errorString += ", ";
            }

            if (RELEASE_NAMES_HM3[i].equalsIgnoreCase(release)) {
                if (!hapmapPhase3) {
                    System.out.println("Specified Release is in HapMap Phase 3, adding option -hapmapPhase3");
                    hapmapPhase3 = true;
                }
                release = release.toUpperCase();
                return true;
            }
        }

        System.err.println(errorString);
        return false;
    }

    private boolean checkPanelName() {

        if (panelArg == null) {
            if (hapmapPhase3) {
                panelArg = DEFAULT_HM3_PANEL;
                return true;
            } else {
                panelArg = DEFAULT_HM_PANEL;
                return true;
            }
        }

        String errorString = "--Please check your Panel Names\n";

        if (hapmapPhase3) {
            errorString += "\t[Hapmap3] Available Panels: ";
            for (String panel : PANEL_NAMES_HM3_HAPLOTEXT) {
                errorString += (panel + " ");
            }
            StringTokenizer st = new StringTokenizer(panelArg, "+");
            String token = "", finalPanel = "";
            int panelcount = st.countTokens();
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                for (String option : PANEL_NAMES_HM3_HAPLOTEXT) {
                    if (option.equalsIgnoreCase(token)) {
                        finalPanel += (token);
                        if (st.hasMoreTokens())
                            finalPanel += ",";
                    }
                }
            }
            errorString += "\n\tPanels may be combined with a \"+\" sign; ex: CEU+TSI, CHD+LWK+YRI, etc..";

            if ((finalPanel.split(",") != null) && (finalPanel.split(",").length == panelcount)) {
                panelArg = panelArg.toUpperCase();
                return true;
            } else if (finalPanel.length() > 1) {
                errorString += ("\n--Accepted Panels: " + finalPanel);
                errorString += "\n--All panels must be accepted";
            }
            System.err.println(errorString);
            return false;
        } else {
            errorString += "[Hapmap2] Available releases: ";
            for (int i = 0; i < PANEL_NAMES.length; i++) {
                errorString += (PANEL_NAMES[i]);
                if (i != PANEL_NAMES.length - 1) {
                    errorString += ", ";
                }
                if (PANEL_NAMES[i].equalsIgnoreCase(panelArg)) {
                    panelArg =  panelArg.toUpperCase();
                    return true;
                }
            }
            System.err.println(errorString);
            return false;
        }
    }

    private void die(String msg) {
        System.err.println(TITLE_STRING + " Fatal Error");
        System.err.println(msg);
        commandLineError = true;
        if (isNogui()) {
            System.exit(1);
        }
    }

    private void doBatch() {
        Vector files;
        File batchFile;
        File dataFile;
        String line;
        StringTokenizer tok;
        String infoMaybe;

        files = new Vector();
        if (batchFileName == null) {
            return;
        }
        batchFile = new File(this.batchFileName);

        if (!batchFile.exists()) {
            commandLogger.warn("batch file " + batchFileName + " does not exist");
            System.exit(1);
        }

        commandLogger.info("Processing batch input file: " + batchFile);

        try {
            BufferedReader br = new BufferedReader(new FileReader(batchFile));
            while ((line = br.readLine()) != null) {
                files.add(line);
            }
            br.close();

            for (int i = 0; i < files.size(); i++) {
                line = (String) files.get(i);
                tok = new StringTokenizer(line);
                infoMaybe = null;
                if (tok.hasMoreTokens()) {
                    dataFile = new File(tok.nextToken());
                    if (tok.hasMoreTokens()) {
                        infoMaybe = tok.nextToken();
                    }

                    if (dataFile.exists()) {
                        String name = dataFile.getName();
                        //TODO: are there other things which need to be reset?
                        Chromosome.setDataChrom("none");
                        if (name.substring(name.length() - 4, name.length()).equalsIgnoreCase(".ped")) {
                            processFile(name, PED_FILE, infoMaybe);
                        } else if (name.substring(name.length() - 5, name.length()).equalsIgnoreCase(".haps")) {
                            processFile(name, HAPS_FILE, infoMaybe);
                        } else if (name.substring(name.length() - 4, name.length()).equalsIgnoreCase(".hmp")) {
                            processFile(name, HMP_FILE, null);
                        } else {
                            commandLogger.info("Filenames in batch file must end in .ped, .haps or .hmp\n" +
                                    name + " is not properly formatted.");
                        }
                    } else {
                        commandLogger.info("file " + dataFile.getName() + " listed in the batch file could not be found");
                    }
                }

            }
        }
        catch (FileNotFoundException e) {
            System.out.println("the following error has occured:\n" + e.toString());
        }
        catch (IOException e) {
            System.out.println("the following error has occured:\n" + e.toString());
        }

    }

    private File validateOutputFile(String fn) {
        File f;
        try {
            new URL(fn);
            f = new File(fn.substring(fn.lastIndexOf("/") + 1));
        } catch (MalformedURLException mfe) {
            f = new File(fn);
        }
        if (f.exists()) {
            commandLogger.info("File " + f.getName() + " already exists and will be overwritten.");
        }
        commandLogger.info("Writing output to " + f.getName());
        return f;
    }

    private InputStream getInputStream(String name) {
        InputStream theStream = null;
        if (name != null) {
            try {
                try {
                    URL streamURL = new URL(name);
                    theStream = streamURL.openStream();
                } catch (MalformedURLException mfe) {
                    File streamFile = new File(name);
                    theStream = new FileInputStream(streamFile);
                } catch (IOException ioe) {
                    die("Could not connect to " + name);
                }
            } catch (IOException ioe) {
                die("Error reading " + name);
            }
        }
        return theStream;
    }

    /**
     * this method finds haplotypes and caclulates dprime without using any graphics
     */
    private void processTextOnly() {
        String fileName;
        int fileType;
        if (hapsFileName != null) {
            fileName = hapsFileName;
            fileType = HAPS_FILE;
        } else if (pedFileName != null) {
            fileName = pedFileName;
            fileType = PED_FILE;
        } else if (phasedhmpdataFileName != null) {
            fileName = phasedhmpdataFileName;

            if (singlePhaseFile) {
                fileType = SINGLEPHASE_FILE;
            } else {
                fileType = PHASEHMP_FILE;
            }

            phasedHapMapInfo = new String[]{phasedhmpdataFileName, phasedhmpsampleFileName, phasedhmplegendFileName, chromosomeArg};
        }
        /* else if (fastphaseFileName != null){
            fileName = fastphaseFileName;
            fileType = FASTPHASE_FILE;
            phasedHapMapInfo = new String[]{fastphaseFileName, infoFileName, null, chromosomeArg};
        }*/
        else if (phasedhapmapDownload) {
            fileName = "Chromosome" + chromosomeArg + panelArg;
            fileType = HMPDL_FILE;
            phasedHapMapInfo = new String[]{fileName, panelArg, startPos, endPos, chromosomeArg, release, "max"};
        } else {
            fileName = hapmapFileName;
            fileType = HMP_FILE;
        }

        processFile(fileName, fileType, infoFileName);
    }

    /**
     * this
     *
     * @param fileName     name of the file to process
     * @param fileType     true means pedfilem false means hapsfile
     * @param infoFileName
     */
    private void processFile(String fileName, int fileType, String infoFileName) {
        try {
            HaploData textData;
            File outputFile;
            AssociationTestSet customAssocSet;

            if (fileName != null) {
                if (phasedhapmapDownload) {
                    commandLogger.info("Downloading chromosome " + chromosomeArg + ", analysis panel " + panelArg + ", " +
                            startPos + ".." + endPos + " from HapMap release " + release + ".");
                } else {
                    commandLogger.info("Using data file: " + fileName);
                }
            }

            if (outputRootName == null) {
                outputRootName = fileName;
            } else {
                commandLogger.info("Using output fileroot: " + outputRootName);
            }

            /* inputFile = new File(fileName);
            if(!inputFile.exists() && !phasedhapmapDownload){
                commandLogger.warn("input file: " + fileName + " does not exist");
                System.exit(1);
            }*/

            textData = new HaploData();
            //Vector result = null;

            if (fileType == HAPS_FILE) {
                //read in haps file
                textData.prepareHapsInput(fileName);
            } else if (fileType == PED_FILE) {
                //read in ped file
                textData.linkageToChrom(fileName, PED_FILE);

                if (textData.getPedFile().isBogusParents()) {
                    commandLogger.warn("Error: One or more individuals in the file reference non-existent parents.\nThese references have been ignored.");
                }
                if (textData.getPedFile().getHaploidHets() != null) {
                    commandLogger.warn("Error: At least one male in the file is heterozygous.\nThese genotypes have been ignored.");
                }
            } else
            if (fileType == PHASEHMP_FILE || fileType == HMPDL_FILE || fileType == SINGLEPHASE_FILE /*|| fileType == FASTPHASE_FILE*/) {
                //read in phased data
                textData.phasedToChrom(phasedHapMapInfo, fileType);
            } else {
                //read in hapmapfile
                textData.linkageToChrom(fileName, HMP_FILE);
            }


            InputStream markerStream = getInputStream(infoFileName);

            textData.prepareMarkerInput(markerStream, textData.getPedFile().getHMInfo());

            HashSet whiteListedCustomMarkers = new HashSet();
            if (customAssocTestsFileName != null) {
                customAssocSet = new AssociationTestSet(customAssocTestsFileName);
                whiteListedCustomMarkers = customAssocSet.getWhitelist();
            } else {
                customAssocSet = null;
            }

            Hashtable snpsByName = new Hashtable();
            for (int i = 0; i < Chromosome.getUnfilteredSize(); i++) {
                SNP snp = Chromosome.getUnfilteredMarker(i);
                snpsByName.put(snp.getDisplayName(), snp);
            }

            if (forceIncludeTags != null) {
                for (int i = 0; i < forceIncludeTags.size(); i++) {
                    if (snpsByName.containsKey(forceIncludeTags.get(i))) {
                        whiteListedCustomMarkers.add(snpsByName.get(forceIncludeTags.get(i)));
                    }
                }
            }

            if (captureAllelesName != null) {  //TODO: This is causing alleles to not show up as BAD in the check output even though they fail thresholds
                for (int i = 0; i < captureAlleleTags.size(); i++) {
                    if (snpsByName.containsKey(captureAlleleTags.get(i))) {
                        whiteListedCustomMarkers.add(snpsByName.get(captureAlleleTags.get(i)));
                    }
                }
            }

            textData.getPedFile().setWhiteList(whiteListedCustomMarkers);

            boolean[] markerResults = new boolean[Chromosome.getUnfilteredSize()];
            Vector result;
            result = textData.getPedFile().getResults();
            //once check has been run we can filter the markers
            int mafFails = 0;
            int mendelFails = 0;
            int genoFails = 0;
            int hwFails = 0;
            for (int i = 0; i < result.size(); i++) {
                if (((((MarkerResult) result.get(i)).getRating() > 0 || skipCheck) &&
                        Chromosome.getUnfilteredMarker(i).getDupStatus() != 2)) {
                    markerResults[i] = true;
                } else {
                    markerResults[i] = false;
                    int rating = ((MarkerResult) result.get(i)).getRating();
                    if (rating <= -16) {
                        mafFails++;
                        rating += 16;
                    }
                    if (rating <= -8) {
                        mendelFails++;
                        rating += 8;
                    }
                    if (rating <= -4) {
                        hwFails++;
                        rating += 4;
                    }
                    if (rating <= -2) {
                        genoFails++;
                        rating += 2;
                    }
                }
            }

            for (int i = 0; i < excludedMarkers.size(); i++) {
                int cur = ((Integer) excludedMarkers.elementAt(i)).intValue();
                if (cur < 1 || cur > markerResults.length) {
                    commandLogger.warn("Excluded marker out of bounds: " + cur +
                            "\nMarkers must be between 1 and N, where N is the total number of markers.");
                    System.exit(1);
                } else {
                    markerResults[cur - 1] = false;
                }
            }


            for (int i = 0; i < Chromosome.getUnfilteredSize(); i++) {
                if (textData.getPedFile().isWhiteListed(Chromosome.getUnfilteredMarker(i))) {
                    markerResults[i] = true;
                }
            }

            Chromosome.doFilter(markerResults);

            if (markerStream != null) {
                commandLogger.info("Using marker information file: " + infoFileName);
            }
            if (outputCheck && result != null) {
                textData.getPedFile().saveCheckDataToText(validateOutputFile(outputRootName + ".CHECK"));
            }
            if (individualCheck && result != null) {
                IndividualDialog id = new IndividualDialog(textData);
                id.printTable(validateOutputFile(outputRootName + ".INDCHECK"));
            }
            if (mendel && result != null) {
                if (textData.getPedFile().getMendelsExist()) {
                    MendelDialog md = new MendelDialog(textData);
                    md.printTable(validateOutputFile(outputRootName + ".MENDEL"));
                }
            }
            if (malehets && result != null) {
                if (textData.getPedFile().getHaploidHets() != null) {
                    HetsDialog hd = new HetsDialog(textData);
                    hd.printTable(validateOutputFile(outputRootName + ".MALEHETS"));
                }
            }

            logger.info((Chromosome.getUnfilteredSize() - mafFails) + " out of " + Chromosome.getUnfilteredSize() + " markers passed the MAF threshold.");
            logger.info((Chromosome.getUnfilteredSize() - mendelFails) + " out of " + Chromosome.getUnfilteredSize() + " markers passed the Mendel threshold.");
            logger.info((Chromosome.getUnfilteredSize() - genoFails) + " out of " + Chromosome.getUnfilteredSize() + " markers passed the genotyping threshold.");
            logger.info((Chromosome.getUnfilteredSize() - hwFails) + " out of " + Chromosome.getUnfilteredSize() + " markers passed the Hardy Weinberg threshold.");
            logger.info(Chromosome.getSize() + " out of " + Chromosome.getUnfilteredSize() + " markers passed all thresholds.");

            Vector cust = new Vector();
            AssociationTestSet blockTestSet = null;

            if (blockOutputType != -1) {
                textData.generateDPrimeTable();
                Haplotype[][] haplos;
                Haplotype[][] filtHaplos;
                switch (blockOutputType) {
                    case BLOX_GABRIEL:
                        outputFile = validateOutputFile(outputRootName + ".GABRIELblocks");
                        break;
                    case BLOX_4GAM:
                        outputFile = validateOutputFile(outputRootName + ".4GAMblocks");
                        break;
                    case BLOX_SPINE:
                        outputFile = validateOutputFile(outputRootName + ".SPINEblocks");
                        break;
                    case BLOX_CUSTOM:
                        outputFile = validateOutputFile(outputRootName + ".CUSTblocks");
                        //read in the blocks file
                        commandLogger.info("Using custom blocks file: " + blockName);
                        cust = textData.readBlocks(getInputStream(blockName));
                        break;
                    case BLOX_ALL:
                        //handled below, so we don't do anything here
                        outputFile = null;
                        break;
                    default:
                        outputFile = validateOutputFile(outputRootName + ".GABRIELblocks");
                        break;

                }

                //this handles output type ALL
                if (blockOutputType == BLOX_ALL) {
                    outputFile = validateOutputFile(outputRootName + ".GABRIELblocks");
                    textData.guessBlocks(BLOX_GABRIEL);

                    haplos = textData.generateBlockHaplotypes(textData.blocks);
                    if (haplos != null) {
                        filtHaplos = filterHaplos(haplos);
                        textData.pickTags(filtHaplos);
                        textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), outputFile);
                    } else {
                        commandLogger.info("Skipping block output: no valid Gabriel blocks.");
                    }

                    outputFile = validateOutputFile(outputRootName + ".4GAMblocks");
                    textData.guessBlocks(BLOX_4GAM);

                    haplos = textData.generateBlockHaplotypes(textData.blocks);
                    if (haplos != null) {
                        filtHaplos = filterHaplos(haplos);
                        textData.pickTags(filtHaplos);
                        textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), outputFile);
                    } else {
                        commandLogger.info("Skipping block output: no valid 4 Gamete blocks.");
                    }

                    outputFile = validateOutputFile(outputRootName + ".SPINEblocks");
                    textData.guessBlocks(BLOX_SPINE);

                    haplos = textData.generateBlockHaplotypes(textData.blocks);
                    if (haplos != null) {
                        filtHaplos = filterHaplos(haplos);
                        textData.pickTags(filtHaplos);
                        textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), outputFile);
                    } else {
                        commandLogger.info("Skipping block output: no valid LD Spine blocks.");
                    }

                } else {
                    //guesses blocks based on output type determined above.
                    textData.guessBlocks(blockOutputType, cust);

                    haplos = textData.generateBlockHaplotypes(textData.blocks);
                    if (haplos != null) {
                        filtHaplos = filterHaplos(haplos);
                        textData.pickTags(filtHaplos);
                        textData.saveHapsToText(haplos, textData.computeMultiDprime(filtHaplos), outputFile);
                    } else {
                        commandLogger.info("Skipping block output: no valid blocks.");
                    }
                }

                if (Options.getAssocTest() == ASSOC_TRIO || Options.getAssocTest() == ASSOC_CC) {
                    if (blockOutputType == BLOX_ALL) {
                        commandLogger.warn("Haplotype association results cannot be used with block output \"ALL\"");
                    } else {
                        if (haplos != null) {
                            blockTestSet = new AssociationTestSet(haplos, null);
                            blockTestSet.saveHapsToText(validateOutputFile(outputRootName + ".HAPASSOC"));

                        } else {
                            commandLogger.info("Skipping block association output: no valid blocks.");
                        }
                    }
                }
            }

            if (outputDprime) {
                outputFile = validateOutputFile(outputRootName + ".LD");
                if (textData.dpTable != null) {
                    textData.saveDprimeToText(outputFile, TABLE_TYPE, 0, Chromosome.getSize());
                } else {
                    textData.saveDprimeToText(outputFile, LIVE_TYPE, 0, Chromosome.getSize());
                }
            }

            if (outputPNG || outputCompressedPNG) {
                outputFile = validateOutputFile(outputRootName + ".LD.PNG");
                if (textData.dpTable == null) {
                    textData.generateDPrimeTable();
                    textData.guessBlocks(BLOX_CUSTOM, new Vector());
                }
                if (trackName != null) {
                    textData.readAnalysisTrack(getInputStream(trackName));
                    commandLogger.info("Using analysis track file: " + trackName);
                }
                if (infoTrack) {
                    if (chromosomeArg.equals("") && (fileType == PED_FILE || fileType == HAPS_FILE || fileType == PHASEHMP_FILE)) {
                        commandLogger.warn("-infoTrack requires a -chromosome specification when used with this filetype");
                    } else {
                        Options.setShowGBrowse(true);
                    }
                }
                DPrimeDisplay dpd = new DPrimeDisplay(textData);
                BufferedImage i = dpd.export(0, Chromosome.getUnfilteredSize(), outputCompressedPNG);
                try {
                    Jimi.putImage("image/png", i, outputFile.getAbsolutePath());
                } catch (JimiException je) {
                    System.out.println(je.getMessage());
                }
            }

            if (outputSVG) {
                outputFile = validateOutputFile(outputRootName + ".LD.SVG");
                if (textData.dpTable == null) {
                    textData.generateDPrimeTable();
                    textData.guessBlocks(BLOX_CUSTOM, new Vector());
                }
                if (trackName != null) {
                    textData.readAnalysisTrack(getInputStream(trackName));
                    commandLogger.info("Using analysis track file: " + trackName);
                }
                if (infoTrack) {
                    if (chromosomeArg.equals("") && (fileType == PED_FILE || fileType == HAPS_FILE || fileType == PHASEHMP_FILE)) {
                        commandLogger.warn("-infoTrack requires a -chromosome specification when used with this filetype");
                    } else {
                        Options.setShowGBrowse(true);
                    }
                }
                DPrimeDisplay dpd = new DPrimeDisplay(textData);
                SVGGraphics2D svg = dpd.exportSVG(0, Chromosome.getUnfilteredSize());
                try {
                    Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
                    svg.stream(out, true);
                } catch (IOException ioe) {
                    commandLogger.error("An error occured writing the LD SVG file.");
                }
            }

            AssociationTestSet markerTestSet = null;
            if (Options.getAssocTest() == ASSOC_TRIO || Options.getAssocTest() == ASSOC_CC) {
                if (randomizeAffection) {
                    Vector aff = new Vector();
                    for (int i = 0; i < textData.getPedFile().getNumIndividuals(); i++) {
                        if (i % 2 == 0) {
                            aff.add(new Integer(1));
                        } else {
                            aff.add(new Integer(2));
                        }
                    }
                    Collections.shuffle(aff);
                    markerTestSet = new AssociationTestSet(textData.getPedFile(), aff, null, Chromosome.getAllMarkers());
                } else {
                    markerTestSet = new AssociationTestSet(textData.getPedFile(), null, null, Chromosome.getAllMarkers());
                }
                markerTestSet.saveSNPsToText(validateOutputFile(outputRootName + ".ASSOC"));
            }

            if (customAssocSet != null) {
                commandLogger.info("Using custom association test file " + customAssocTestsFileName);
                try {
                    customAssocSet.setPermTests(doPermutationTest);
                    customAssocSet.runFileTests(textData, markerTestSet.getMarkerAssociationResults());
                    customAssocSet.saveResultsToText(validateOutputFile(outputRootName + ".CUSTASSOC"));

                } catch (IOException ioe) {
                    commandLogger.error("An error occured writing the custom association results file.");
                    customAssocSet = null;
                }
            }

            if (doPermutationTest) {
                AssociationTestSet permTests = new AssociationTestSet();
                permTests.cat(markerTestSet);
                if (blockTestSet != null) {
                    permTests.cat(blockTestSet);
                }
                final PermutationTestSet pts = new PermutationTestSet(permutationCount, textData.getPedFile(), customAssocSet, permTests);
                Thread permThread = new Thread(new Runnable() {
                    public void run() {
                        if (pts.isCustom()) {
                            pts.doPermutations(PermutationTestSet.CUSTOM);
                        } else {
                            pts.doPermutations(PermutationTestSet.SINGLE_PLUS_BLOCKS);
                        }
                    }
                });

                permThread.start();


                commandLogger.info("Starting " + permutationCount + " permutation tests (each . printed represents 1% of tests completed)");

                int dotsPrinted = 0;
                while (pts.getPermutationCount() - pts.getPermutationsPerformed() > 0) {
                    while (((double) pts.getPermutationsPerformed() / pts.getPermutationCount()) * 100 > dotsPrinted) {
                        System.out.print(".");
                        dotsPrinted++;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                    }
                }
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < dotsPrinted; i++) {
                    buffer.append(".");
                }
                logger.info(buffer.toString());
                System.out.println();

                try {
                    pts.writeResultsToFile(validateOutputFile(fileName + ".PERMUT"));
                } catch (IOException ioe) {
                    commandLogger.error("An error occured while writing the permutation test results to file.");
                }
            }


            if (tagging != Tagger.NONE) {

                if (textData.dpTable == null) {
                    textData.generateDPrimeTable();
                }
                Vector snps = Chromosome.getAllMarkers();
                HashSet names = new HashSet();
                for (int i = 0; i < snps.size(); i++) {
                    SNP snp = (SNP) snps.elementAt(i);
                    names.add(snp.getDisplayName());
                }

                HashSet filteredNames = new HashSet();
                for (int i = 0; i < Chromosome.getSize(); i++) {
                    filteredNames.add(Chromosome.getMarker(i).getDisplayName());
                }

                Vector sitesToCapture = new Vector();
                Vector allSNPs = new Vector();
                if (captureAlleleTags == null) {
                    for (int i = 0; i < Chromosome.getSize(); i++) {
                        sitesToCapture.add(Chromosome.getMarker(i).getDisplayName());
                        allSNPs.add(Chromosome.getMarker(i));
                    }
                } else {
                    for (int i = 0; i < captureAlleleTags.size(); i++) {
                        if (snpsByName.containsKey(captureAlleleTags.get(i))) {
                            sitesToCapture.add(snpsByName.get(captureAlleleTags.get(i)));
                        }
                    }
                }

                if (forceIncludeName != null) {
                    commandLogger.info("Using force include tags file: " + forceIncludeName);
                }
                if (forceExcludeName != null) {
                    commandLogger.info("Using force exclude tags file: " + forceExcludeName);
                }
                if (designScoresName != null) {
                    commandLogger.info("Using design scores file: " + designScoresName);
                }
                if (captureAllelesName != null) {
                    commandLogger.info("Using capture alleles file: " + captureAllelesName);
                }

                for (int i = 0; i < forceIncludeTags.size(); i++) {
                    String s = (String) forceIncludeTags.elementAt(i);
                    if (!names.contains(s)) {
                        commandLogger.info("Warning: skipping marker " + s + " in the list of forced included tags since I don't know about it.");
                    }
                }

                for (int i = 0; i < forceExcludeTags.size(); i++) {
                    String s = (String) forceExcludeTags.elementAt(i);
                    if (!names.contains(s)) {
                        commandLogger.info("Warning: skipping marker " + s + " in the list of forced excluded tags since I don't know about it.");
                    }
                }

                //chuck out filtered jazz from excludes, and nonexistent markers from both
                forceExcludeTags.retainAll(filteredNames);
                forceIncludeTags.retainAll(names);

                commandLogger.info("Starting tagging.");

                TaggerController tc = new TaggerController(textData, allSNPs, forceIncludeTags, forceExcludeTags, sitesToCapture,
                        designScores, tagging, maxNumTags, findTags);
                tc.runTagger();

                while (!tc.isTaggingCompleted()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                    }
                }

                tc.saveResultsToFile(validateOutputFile(outputRootName + ".TAGS"));
                tc.dumpTests(validateOutputFile(outputRootName + ".TESTS"));
                if (outputConditionalHaps) {
                    tc.dumpConditionalHaps(validateOutputFile(outputRootName + ".CHAPS"));
                }
                //todo: I don't like this at the moment, removed subject to further consideration.
                //tc.dumpTags(validateOutputFile(outputRootName + ".TAGSNPS"));
            }
        }
        catch (IOException e) {
            System.err.println("An error has occured:");
            System.err.println(e.getMessage());
        }
        catch (HaploViewException e) {
            System.err.println(e.getMessage());
        }
        catch (PedFileException pfe) {
            System.err.println(pfe.getMessage());
        }
        catch (TaggerException te) {
            System.err.println(te.getMessage());
        }
    }


    public Haplotype[][] filterHaplos(Haplotype[][] haplos) {
        if (haplos == null) {
            return null;
        }
        Haplotype[][] filteredHaplos = new Haplotype[haplos.length][];
        for (int i = 0; i < haplos.length; i++) {
            Vector tempVector = new Vector();
            for (int j = 0; j < haplos[i].length; j++) {
                if (haplos[i][j].getPercentage() > Options.getHaplotypeDisplayThreshold()) {
                    tempVector.add(haplos[i][j]);
                }
            }
            filteredHaplos[i] = new Haplotype[tempVector.size()];
            tempVector.copyInto(filteredHaplos[i]);
        }

        return filteredHaplos;

    }
}
