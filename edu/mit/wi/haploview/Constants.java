package edu.mit.wi.haploview;


public interface Constants {

    //main jframe setup stuff & labels.
    public static final double VERSION = 2.05;
    public static final String TITLE_STRING = "Haploview "+VERSION;

    public static final String READ_GENOTYPES = "Open genotype data";
    public static final String READ_MARKERS = "Load marker data";
    public static final String READ_ANALYSIS_TRACK = "Load analysis track";
    public static final String READ_BLOCKS_FILE = "Load block definitions";

    public static final String EXPORT_TEXT = "Export current tab to text";
    public static final String EXPORT_PNG = "Export current tab to PNG";
    public static final String EXPORT_OPTIONS = "Export options";

    public static final String CLEAR_BLOCKS = "Clear all blocks";
    public static final String CUST_BLOCKS = "Customize Block Definitions";

    public static final String VIEW_DPRIME = "LD Plot";
    public static final String VIEW_HAPLOTYPES = "Haplotypes";
    public static final String VIEW_LOD = "Genotyping completeness";
    public static final String VIEW_CHECK_PANEL = "Check Markers";
    public static final String VIEW_TDT = "Association";

    //main frame tab numbers
    public static final int VIEW_D_NUM = 0;
    public static final int VIEW_HAP_NUM = 1;
    public static final int VIEW_CHECK_NUM = 2;
    public static final int VIEW_TDT_NUM = 3;

    //export modes
    public static final int PNG_MODE = 0;
    public static final int TXT_MODE = 1;
    public static final int COMPRESSED_PNG_MODE = 2;
    public static final int TABLE_TYPE = 0;
    public static final int LIVE_TYPE = 1;

    //block defs
    public static final int BLOX_GABRIEL = 0;
    public static final int BLOX_4GAM = 1;
    public static final int BLOX_SPINE = 2;
    public static final int BLOX_CUSTOM = 3;
    public static final int BLOX_ALL = 4;
    public static final int BLOX_NONE = 5;

    //filetypes
    static final int GENO = 0;
    static final int INFO = 1;
    static final int HAPS = 2;
    static final int PED = 3;
    static final int HMP = 4;

    //color modes
    static final int STD_SCHEME = 1;
    static final int RSQ_SCHEME = 2;
    static final int WMF_SCHEME = 3;
    static final int SFS_SCHEME = 4;
    static final int GAM_SCHEME = 5;

    //association test modes
    static final int ASSOC_NONE = 0;
    static final int ASSOC_TRIO = 1;
    static final int ASSOC_CC = 2;

    //single marker association display stuff
    static final int SHOW_SINGLE_COUNTS = 0;
    static final int SHOW_SINGLE_FREQS = 1;

    //haplotype association display stuff
    static final int SHOW_HAP_COUNTS = 0;
    static final int SHOW_HAP_RATIOS = 1;


    static final String HELP_OUTPUT = TITLE_STRING + " command line options\n" +
                        "-h, -help                       print this message\n" +
                        "-nogui                          command line output only\n" +
                        "-q, -quiet                      quiet mode- doesnt print any warnings or information to screen\n" +
                        "-pedfile <pedfile>              specify an input file in pedigree file format\n" +
                        //"         --ignoremarkers <markers> ignores the specified markers.<markers> is a comma\n" +
                        //"                                   seperated list of markers. eg. 1,5,7,19,25\n" +
                        "-hapmap <hapmapfile>            specify an input file in HapMap format\n" +
                        "-haps <hapsfile>                specify an input file in .haps format\n" +
                        "-info <infofile>                specify a marker info file\n" +
                        "-batch <batchfile>              batch mode. Each line in batch file should contain a genotype file \n"+
                        "                                followed by an optional info file, separated by a space.\n" +
                        "-blocks <blockfile>             blocks file, one block per line, will force output for these blocks\n" +
                        "-track <trackfile>              specify an input analysis track file.\n"+
                        "-skipcheck                      skips the various genotype file checks\n" +
                        "-dprime                         outputs LD text to <inputfile>.LD\n" +
                        "-png                            outputs LD display to <inputfile>.LD.PNG\n"+
                        "-compressedpng                  outputs compressed LD display to <inputfile>.LD.PNG\n"+
                        "-check                          outputs marker checks to <inputfile>.CHECK\n" +
                        "                                note: -dprime  and -check default to no blocks output. \n" +
                        "                                use -blockoutput to also output blocks\n" +
                        "-blockoutput <GAB,GAM,SPI,ALL>  output type. Gabriel, 4 gamete, spine output or all 3. default is Gabriel.\n" +
                        "-maxdistance <distance>         maximum comparison distance in kilobases (integer). default is 500\n" +
                        "-hapthresh <frequency>          Only output haps with at least this frequency\n" +
                        "-spacing <threshold>            Proportional spacing of markers in LD display. <threshold> is a value\n" +
                        "                                between 0 (no spacing) and 1 (max spacing).\n"  +
                        "-minMAF <threshold>             Minimum minor allele frequency to include a marker. <threshold> is a value\n" +
                        "                                between 0 and 1. \n" +
                        "-maxMendel <integer>            Markers with more than <integer> Mendel errors will be excluded.\n" +
                        "-minGenoPercent <threshold>     Exclude markers with less than <threshold> valid data. <threshold> is a value\n" +
                        "                                between 0 and 1. \n" +
                        "-hwcutoff <threshold>           Exclude markers with a HW p-value greater than <threshold>. <threshold> is a value\n" +
                        "                                between 0 and 1. \n";

}
