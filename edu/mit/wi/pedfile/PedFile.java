/*
* $Id: PedFile.java,v 3.61 2009/09/10 20:54:28 jcwhitworth Exp $
* WHITEHEAD INSTITUTE
* SOFTWARE COPYRIGHT NOTICE AGREEMENT
* This software and its documentation are copyright 2002 by the
* Whitehead Institute for Biomedical Research.  All rights are reserved.
*
* This software is supplied without any warranty or guaranteed support
* whatsoever.  The Whitehead Institute can not be responsible for its
* use, misuse, or functionality.
*/
package edu.mit.wi.pedfile;


import edu.mit.wi.haploview.*;
import edu.mit.wi.pedparser.PedParser;
import edu.mit.wi.pedparser.PedigreeException;

import java.util.*;
import java.util.zip.GZIPInputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;

import org._3pq.jgrapht.graph.SimpleGraph;

/**
 * Handles input and storage of Pedigree files
 * <p/>
 * this class is not thread safe (untested).
 * modified from original Pedfile and checkdata classes by Hui Gong
 *
 * @author Julian Maller
 */
public class PedFile {
    private Hashtable families;

    private Vector axedPeople = new Vector();
    //stores the individuals found by parse() in allIndividuals. this is useful for outputting Pedigree information to a file of another type.
    private Vector allIndividuals;
    //stores the individuals chosen by pedparser
    private Vector unrelatedIndividuals;
    private Vector results = null;
    private String[][] hminfo;
    //bogusParents is true if someone in the file referenced a parent not in the file
    private boolean bogusParents = false;
    private Vector haploidHets;
    private boolean mendels = false;
    private static Hashtable hapMapTranslate;
    private int[] markerRatings;
    private int[] dups;
    private HashSet whitelist;


    public PedFile() {

        //hardcoded hapmap info
        this.families = new Hashtable();

        hapMapTranslate = new Hashtable(90, 1);
        hapMapTranslate.put("NA10846", "1334 NA10846 NA12144 NA12145 1 0");
        hapMapTranslate.put("NA12144", "1334 NA12144 0 0 1 0");
        hapMapTranslate.put("NA12145", "1334 NA12145 0 0 2 0");
        hapMapTranslate.put("NA10847", "1334a NA10847 NA12146 NA12239 2 0");
        hapMapTranslate.put("NA12146", "1334a NA12146 0 0 1 0");
        hapMapTranslate.put("NA12239", "1334a NA12239 0 0 2 0");
        hapMapTranslate.put("NA07029", "1340 NA07029 NA06994 NA07000 1 0");
        hapMapTranslate.put("NA06994", "1340 NA06994 0 0 1 0");
        hapMapTranslate.put("NA07000", "1340 NA07000 0 0 2 0");
        hapMapTranslate.put("NA07019", "1340a NA07019 NA07022 NA07056 2 0");
        hapMapTranslate.put("NA07022", "1340a NA07022 0 0 1 0");
        hapMapTranslate.put("NA07056", "1340a NA07056 0 0 2 0");
        hapMapTranslate.put("NA07048", "1341 NA07048 NA07034 NA07055 1 0");
        hapMapTranslate.put("NA07034", "1341 NA07034 0 0 1 0");
        hapMapTranslate.put("NA07055", "1341 NA07055 0 0 2 0");
        hapMapTranslate.put("NA06991", "1341a NA06991 NA06993 NA06985 2 0");
        hapMapTranslate.put("NA06993", "1341a NA06993 0 0 1 0");
        //hapMapTranslate.put("NA06993.dup", "dup NA06993.dup 0 0 1 0");
        hapMapTranslate.put("NA06985", "1341a NA06985 0 0 2 0");
        hapMapTranslate.put("NA10851", "1344 NA10851 NA12056 NA12057 1 0");
        hapMapTranslate.put("NA12056", "1344 NA12056 0 0 1 0");
        hapMapTranslate.put("NA12057", "1344 NA12057 0 0 2 0");
        hapMapTranslate.put("NA07348", "1345 NA07348 NA07357 NA07345 2 0");
        hapMapTranslate.put("NA07357", "1345 NA07357 0 0 1 0");
        hapMapTranslate.put("NA07345", "1345 NA07345 0 0 2 0");
        hapMapTranslate.put("NA10857", "1346 NA10857 NA12043 NA12044 1 0");
        hapMapTranslate.put("NA12043", "1346 NA12043 0 0 1 0");
        hapMapTranslate.put("NA12044", "1346 NA12044 0 0 2 0");
        hapMapTranslate.put("NA10859", "1347 NA10859 NA11881 NA11882 2 0");
        hapMapTranslate.put("NA11881", "1347 NA11881 0 0 1 0");
        hapMapTranslate.put("NA11882", "1347 NA11882 0 0 2 0");
        hapMapTranslate.put("NA10854", "1349 NA10854 NA11839 NA11840 2 0");
        hapMapTranslate.put("NA11839", "1349 NA11839 0 0 1 0");
        hapMapTranslate.put("NA11840", "1349 NA11840 0 0 2 0");
        hapMapTranslate.put("NA10856", "1350 NA10856 NA11829 NA11830 1 0");
        hapMapTranslate.put("NA11829", "1350 NA11829 0 0 1 0");
        hapMapTranslate.put("NA11830", "1350 NA11830 0 0 2 0");
        hapMapTranslate.put("NA10855", "1350a NA10855 NA11831 NA11832 2 0");
        hapMapTranslate.put("NA11831", "1350a NA11831 0 0 1 0");
        hapMapTranslate.put("NA11832", "1350a NA11832 0 0 2 0");
        hapMapTranslate.put("NA12707", "1358 NA12707 NA12716 NA12717 1 0");
        hapMapTranslate.put("NA12716", "1358 NA12716 0 0 1 0");
        hapMapTranslate.put("NA12717", "1358 NA12717 0 0 2 0");
        hapMapTranslate.put("NA10860", "1362 NA10860 NA11992 NA11993 1 0");
        hapMapTranslate.put("NA11992", "1362 NA11992 0 0 1 0");
        hapMapTranslate.put("NA11993", "1362 NA11993 0 0 2 0");
        // hapMapTranslate.put("NA11993.dup", "dup NA11993.dup 0 0 2 0");
        hapMapTranslate.put("NA10861", "1362a NA10861 NA11994 NA11995 2 0");
        hapMapTranslate.put("NA11994", "1362a NA11994 0 0 1 0");
        hapMapTranslate.put("NA11995", "1362a NA11995 0 0 2 0");
        hapMapTranslate.put("NA10863", "1375 NA10863 NA12264 NA12234 2 0");
        hapMapTranslate.put("NA12264", "1375 NA12264 0 0 1 0");
        hapMapTranslate.put("NA12234", "1375 NA12234 0 0 2 0");
        hapMapTranslate.put("NA10830", "1408 NA10830 NA12154 NA12236 1 0");
        hapMapTranslate.put("NA12154", "1408 NA12154 0 0 1 0");
        hapMapTranslate.put("NA12236", "1408 NA12236 0 0 2 0");
        hapMapTranslate.put("NA10831", "1408a NA10831 NA12155 NA12156 2 0");
        hapMapTranslate.put("NA12155", "1408a NA12155 0 0 1 0");
        hapMapTranslate.put("NA12156", "1408a NA12156 0 0 2 0");
        //hapMapTranslate.put("NA12156.dup", "dup NA12156.dup 0 0 2 0");
        hapMapTranslate.put("NA10835", "1416 NA10835 NA12248 NA12249 1 0");
        hapMapTranslate.put("NA12248", "1416 NA12248 0 0 1 0");
        // hapMapTranslate.put("NA12248.dup", "dup NA1248.dup 0 0 1 0");
        hapMapTranslate.put("NA12249", "1416 NA12249 0 0 2 0");
        hapMapTranslate.put("NA10838", "1420 NA10838 NA12003 NA12004 1 0");
        hapMapTranslate.put("NA12003", "1420 NA12003 0 0 1 0");
        //hapMapTranslate.put("NA12003.dup", "dup NA12003.dup 0 0 1 0");
        hapMapTranslate.put("NA12004", "1420 NA12004 0 0 2 0");
        hapMapTranslate.put("NA10839", "1420a NA10839 NA12005 NA12006 2 0");
        hapMapTranslate.put("NA12005", "1420a NA12005 0 0 1 0");
        hapMapTranslate.put("NA12006", "1420a NA12006 0 0 2 0");
        hapMapTranslate.put("NA12740", "1444 NA12740 NA12750 NA12751 2 0");
        hapMapTranslate.put("NA12750", "1444 NA12750 0 0 1 0");
        hapMapTranslate.put("NA12751", "1444 NA12751 0 0 2 0");
        hapMapTranslate.put("NA12752", "1447 NA12752 NA12760 NA12761 1 0");
        hapMapTranslate.put("NA12760", "1447 NA12760 0 0 1 0");
        hapMapTranslate.put("NA12761", "1447 NA12761 0 0 2 0");
        hapMapTranslate.put("NA12753", "1447a NA12753 NA12762 NA12763 2 0");
        hapMapTranslate.put("NA12762", "1447a NA12762 0 0 1 0");
        hapMapTranslate.put("NA12763", "1447a NA12763 0 0 2 0");
        hapMapTranslate.put("NA12801", "1454 NA12801 NA12812 NA12813 1 0");
        hapMapTranslate.put("NA12812", "1454 NA12812 0 0 1 0");
        hapMapTranslate.put("NA12813", "1454 NA12813 0 0 2 0");
        hapMapTranslate.put("NA12802", "1454a NA12802 NA12814 NA12815 2 0");
        hapMapTranslate.put("NA12814", "1454a NA12814 0 0 1 0");
        hapMapTranslate.put("NA12815", "1454a NA12815 0 0 2 0");
        hapMapTranslate.put("NA12864", "1459 NA12864 NA12872 NA12873 1 0");
        hapMapTranslate.put("NA12872", "1459 NA12872 0 0 1 0");
        hapMapTranslate.put("NA12873", "1459 NA12873 0 0 2 0");
        hapMapTranslate.put("NA12865", "1459a NA12865 NA12874 NA12875 2 0");
        hapMapTranslate.put("NA12874", "1459a NA12874 0 0 1 0");
        hapMapTranslate.put("NA12875", "1459a NA12875 0 0 2 0");
        hapMapTranslate.put("NA12878", "1463 NA12878 NA12891 NA12892 2 0");
        hapMapTranslate.put("NA12891", "1463 NA12891 0 0 1 0");
        hapMapTranslate.put("NA12892", "1463 NA12892 0 0 2 0");
        hapMapTranslate.put("NA18526", "chi1 NA18526 0 0 2 0");
        hapMapTranslate.put("NA18524", "chi2 NA18524 0 0 1 0");
        hapMapTranslate.put("NA18529", "chi3 NA18529 0 0 2 0");
        hapMapTranslate.put("NA18558", "chi4 NA18558 0 0 1 0");
        hapMapTranslate.put("NA18532", "chi5 NA18532 0 0 2 0");
        hapMapTranslate.put("NA18561", "chi6 NA18561 0 0 1 0");
        hapMapTranslate.put("NA18942", "jap1 NA18942 0 0 2 0");
        hapMapTranslate.put("NA18940", "jap2 NA18940 0 0 1 0");
        hapMapTranslate.put("NA18951", "jap3 NA18951 0 0 2 0");
        hapMapTranslate.put("NA18943", "jap4 NA18943 0 0 1 0");
        hapMapTranslate.put("NA18947", "jap5 NA18947 0 0 2 0");
        hapMapTranslate.put("NA18944", "jap6 NA18944 0 0 1 0");
        hapMapTranslate.put("NA18562", "chi7 NA18562 0 0 1 0");
        hapMapTranslate.put("NA18537", "chi8 NA18537 0 0 2 0");
        hapMapTranslate.put("NA18603", "chi9 NA18603 0 0 1 0");
        hapMapTranslate.put("NA18540", "chi10 NA18540 0 0 2 0");
        hapMapTranslate.put("NA18605", "chi11 NA18605 0 0 1 0");
        hapMapTranslate.put("NA18542", "chi12 NA18542 0 0 2 0");
        hapMapTranslate.put("NA18945", "jap7 NA18945 0 0 1 0");
        hapMapTranslate.put("NA18949", "jap8 NA18949 0 0 2 0");
        hapMapTranslate.put("NA18948", "jap9 NA18948 0 0 1 0");
        hapMapTranslate.put("NA18952", "jap10 NA18952 0 0 1 0");
        hapMapTranslate.put("NA18956", "jap11 NA18956 0 0 2 0");
        hapMapTranslate.put("NA18545", "chi13 NA18545 0 0 2 0");
        hapMapTranslate.put("NA18572", "chi46 NA18572 0 0 1 0");
        hapMapTranslate.put("NA18547", "chi15 NA18547 0 0 2 0");
        hapMapTranslate.put("NA18609", "chi16 NA18609 0 0 1 0");
        hapMapTranslate.put("NA18550", "chi17 NA18550 0 0 2 0");
        hapMapTranslate.put("NA18608", "chi18 NA18608 0 0 1 0");
        hapMapTranslate.put("NA18964", "jap12 NA18964 0 0 2 0");
        hapMapTranslate.put("NA18953", "jap13 NA18953 0 0 1 0");
        hapMapTranslate.put("NA18968", "jap14 NA18968 0 0 2 0");
        hapMapTranslate.put("NA18959", "jap15 NA18959 0 0 1 0");
        hapMapTranslate.put("NA18969", "jap16 NA18969 0 0 2 0");
        hapMapTranslate.put("NA18960", "jap17 NA18960 0 0 1 0");
        hapMapTranslate.put("NA18552", "chi19 NA18552 0 0 2 0");
        hapMapTranslate.put("NA18611", "chi20 NA18611 0 0 1 0");
        hapMapTranslate.put("NA18555", "chi21 NA18555 0 0 2 0");
        hapMapTranslate.put("NA18564", "chi22 NA18564 0 0 2 0");
        hapMapTranslate.put("NA18961", "jap18 NA18961 0 0 1 0");
        hapMapTranslate.put("NA18972", "jap19 NA18972 0 0 2 0");
        hapMapTranslate.put("NA18965", "jap20 NA18965 0 0 1 0");
        hapMapTranslate.put("NA18973", "jap21 NA18973 0 0 2 0");
        hapMapTranslate.put("NA18966", "jap22 NA18966 0 0 1 0");
        hapMapTranslate.put("NA18975", "jap23 NA18975 0 0 2 0");
        hapMapTranslate.put("NA18566", "chi23 NA18566 0 0 2 0");
        hapMapTranslate.put("NA18563", "chi24 NA18563 0 0 1 0");
        hapMapTranslate.put("NA18570", "chi25 NA18570 0 0 2 0");
        hapMapTranslate.put("NA18612", "chi26 NA18612 0 0 1 0");
        hapMapTranslate.put("NA18571", "chi27 NA18571 0 0 2 0");
        hapMapTranslate.put("NA18620", "chi28 NA18620 0 0 1 0");
        hapMapTranslate.put("NA18976", "jap24 NA18976 0 0 2 0");
        hapMapTranslate.put("NA18967", "jap25 NA18967 0 0 1 0");
        hapMapTranslate.put("NA18978", "jap26 NA18978 0 0 2 0");
        hapMapTranslate.put("NA18970", "jap27 NA18970 0 0 1 0");
        hapMapTranslate.put("NA18980", "jap28 NA18980 0 0 2 0");
        hapMapTranslate.put("NA18995", "jap29 NA18995 0 0 1 0");
        hapMapTranslate.put("NA18621", "chi29 NA18621 0 0 1 0");
        hapMapTranslate.put("NA18594", "chi30 NA18594 0 0 2 0");
        //  hapMapTranslate.put("NA18594.dup", "dup 0 0 0 0 0");
        //  hapMapTranslate.put("NA18603.dup", "dup 0 0 0 0 0");
        //  hapMapTranslate.put("NA18609.dup", "dup 0 0 0 0 0");
        //  hapMapTranslate.put("NA18951.dup", "dup 0 0 0 0 0");
        //  hapMapTranslate.put("NA18995.dup", "dup 0 0 0 0 0");
        hapMapTranslate.put("NA18622", "chi31 NA18622 0 0 1 0");
        hapMapTranslate.put("NA18573", "chi32 NA18573 0 0 2 0");
        hapMapTranslate.put("NA18623", "chi33 NA18623 0 0 1 0");
        hapMapTranslate.put("NA18576", "chi34 NA18576 0 0 2 0");
        hapMapTranslate.put("NA18971", "jap30 NA18971 0 0 1 0");
        hapMapTranslate.put("NA18981", "jap31 NA18981 0 0 2 0");
        hapMapTranslate.put("NA18974", "jap32 NA18974 0 0 1 0");
        hapMapTranslate.put("NA18987", "jap33 NA18987 0 0 2 0");
        hapMapTranslate.put("NA18990", "jap34 NA18990 0 0 1 0");
        hapMapTranslate.put("NA18991", "jap35 NA18991 0 0 2 0");
        hapMapTranslate.put("NA18577", "chi35 NA18577 0 0 2 0");
        hapMapTranslate.put("NA18624", "chi36 NA18624 0 0 1 0");
        hapMapTranslate.put("NA18579", "chi37 NA18579 0 0 2 0");
        hapMapTranslate.put("NA18632", "chi38 NA18632 0 0 1 0");
        hapMapTranslate.put("NA18582", "chi39 NA18582 0 0 2 0");
        hapMapTranslate.put("NA18633", "chi40 NA18633 0 0 1 0");
        hapMapTranslate.put("NA18994", "jap36 NA18994 0 0 1 0");
        hapMapTranslate.put("NA18992", "jap37 NA18992 0 0 2 0");
        hapMapTranslate.put("NA18997", "jap38 NA18997 0 0 2 0");
        hapMapTranslate.put("NA18996", "jap39 NA18996 0 0 1 0");
        hapMapTranslate.put("NA18635", "chi41 NA18635 0 0 1 0");
        hapMapTranslate.put("NA18592", "chi42 NA18592 0 0 2 0");
        hapMapTranslate.put("NA18636", "chi43 NA18636 0 0 1 0");
        hapMapTranslate.put("NA18593", "chi44 NA18593 0 0 2 0");
        hapMapTranslate.put("NA18637", "chi45 NA18637 0 0 1 0");
        hapMapTranslate.put("NA19000", "jap40 NA19000 0 0 1 0");
        hapMapTranslate.put("NA18998", "jap41 NA18998 0 0 2 0");
        hapMapTranslate.put("NA19005", "jap42 NA19005 0 0 1 0");
        hapMapTranslate.put("NA18999", "jap43 NA18999 0 0 2 0");
        hapMapTranslate.put("NA19007", "jap44 NA19007 0 0 1 0");
        hapMapTranslate.put("NA19003", "jap45 NA19003 0 0 2 0");
        hapMapTranslate.put("NA19012", "jap46 NA19012 0 0 1 0");
        hapMapTranslate.put("NA18500", "Yoruba004 NA18500 NA18501 NA18502 1 0");
        hapMapTranslate.put("NA18501", "Yoruba004 NA18501 0 0 1 0");
        hapMapTranslate.put("NA18502", "Yoruba004 NA18502 0 0 2 0");
        hapMapTranslate.put("NA18503", "Yoruba005 NA18503 NA18504 NA18505 1 0");
        hapMapTranslate.put("NA18504", "Yoruba005 NA18504 0 0 1 0");
        hapMapTranslate.put("NA18505", "Yoruba005 NA18505 0 0 2 0");
        hapMapTranslate.put("NA18506", "Yoruba009 NA18506 NA18507 NA18508 1 0");
        hapMapTranslate.put("NA18507", "Yoruba009 NA18507 0 0 1 0");
        hapMapTranslate.put("NA18508", "Yoruba009 NA18508 0 0 2 0");
        hapMapTranslate.put("NA18860", "Yoruba012 NA18860 NA18859 NA18858 1 0");
        hapMapTranslate.put("NA18859", "Yoruba012 NA18859 0 0 1 0");
        hapMapTranslate.put("NA18858", "Yoruba012 NA18858 0 0 2 0");
        hapMapTranslate.put("NA18515", "Yoruba013 NA18515 NA18516 NA18517 1 0");
        hapMapTranslate.put("NA18516", "Yoruba013 NA18516 0 0 1 0");
        hapMapTranslate.put("NA18517", "Yoruba013 NA18517 0 0 2 0");
        hapMapTranslate.put("NA18521", "Yoruba016 NA18521 NA18522 NA18523 1 0");
        hapMapTranslate.put("NA18522", "Yoruba016 NA18522 0 0 1 0");
        hapMapTranslate.put("NA18523", "Yoruba016 NA18523 0 0 2 0");
        hapMapTranslate.put("NA18872", "Yoruba017 NA18872 NA18871 NA18870 1 0");
        hapMapTranslate.put("NA18871", "Yoruba017 NA18871 0 0 1 0");
        hapMapTranslate.put("NA18870", "Yoruba017 NA18870 0 0 2 0");
        hapMapTranslate.put("NA18854", "Yoruba018 NA18854 NA18853 NA18852 1 0");
        hapMapTranslate.put("NA18853", "Yoruba018 NA18853 0 0 1 0");
        hapMapTranslate.put("NA18852", "Yoruba018 NA18852 0 0 2 0");
        hapMapTranslate.put("NA18857", "Yoruba023 NA18857 NA18856 NA18855 1 0");
        hapMapTranslate.put("NA18856", "Yoruba023 NA18856 0 0 1 0");
        hapMapTranslate.put("NA18855", "Yoruba023 NA18855 0 0 2 0");
        hapMapTranslate.put("NA18863", "Yoruba024 NA18863 NA18862 NA18861 1 0");
        hapMapTranslate.put("NA18862", "Yoruba024 NA18862 0 0 1 0");
        hapMapTranslate.put("NA18861", "Yoruba024 NA18861 0 0 2 0");
        hapMapTranslate.put("NA18914", "Yoruba028 NA18914 NA18913 NA18912 1 0");
        hapMapTranslate.put("NA18913", "Yoruba028 NA18913 0 0 1 0");
        hapMapTranslate.put("NA18912", "Yoruba028 NA18912 0 0 2 0");
        hapMapTranslate.put("NA19094", "Yoruba040 NA19094 NA19092 NA19093 2 0");
        hapMapTranslate.put("NA19092", "Yoruba040 NA19092 0 0 1 0");
        hapMapTranslate.put("NA19093", "Yoruba040 NA19093 0 0 2 0");
        hapMapTranslate.put("NA19103", "Yoruba042 NA19103 NA19101 NA19102 1 0");
        hapMapTranslate.put("NA19101", "Yoruba042 NA19101 0 0 1 0");
        hapMapTranslate.put("NA19102", "Yoruba042 NA19102 0 0 2 0");
        hapMapTranslate.put("NA19139", "Yoruba043 NA19139 NA19138 NA19137 1 0");
        hapMapTranslate.put("NA19138", "Yoruba043 NA19138 0 0 1 0");
        hapMapTranslate.put("NA19137", "Yoruba043 NA19137 0 0 2 0");
        hapMapTranslate.put("NA19202", "Yoruba045 NA19202 NA19200 NA19201 2 0");
        hapMapTranslate.put("NA19200", "Yoruba045 NA19200 0 0 1 0");
        hapMapTranslate.put("NA19201", "Yoruba045 NA19201 0 0 2 0");
        hapMapTranslate.put("NA19173", "Yoruba047 NA19173 NA19171 NA19172 1 0");
        hapMapTranslate.put("NA19171", "Yoruba047 NA19171 0 0 1 0");
        hapMapTranslate.put("NA19172", "Yoruba047 NA19172 0 0 2 0");
        hapMapTranslate.put("NA19205", "Yoruba048 NA19205 NA19203 NA19204 1 0");
        hapMapTranslate.put("NA19203", "Yoruba048 NA19203 0 0 1 0");
        hapMapTranslate.put("NA19204", "Yoruba048 NA19204 0 0 2 0");
        hapMapTranslate.put("NA19211", "Yoruba050 NA19211 NA19210 NA19209 1 0");
        hapMapTranslate.put("NA19210", "Yoruba050 NA19210 0 0 1 0");
        hapMapTranslate.put("NA19209", "Yoruba050 NA19209 0 0 2 0");
        hapMapTranslate.put("NA19208", "Yoruba051 NA19208 NA19207 NA19206 1 0");
        hapMapTranslate.put("NA19207", "Yoruba051 NA19207 0 0 1 0");
        hapMapTranslate.put("NA19206", "Yoruba051 NA19206 0 0 2 0");
        hapMapTranslate.put("NA19161", "Yoruba056 NA19161 NA19160 NA19159 1 0");
        hapMapTranslate.put("NA19160", "Yoruba056 NA19160 0 0 1 0");
        hapMapTranslate.put("NA19159", "Yoruba056 NA19159 0 0 2 0");
        hapMapTranslate.put("NA19221", "Yoruba058 NA19221 NA19223 NA19222 2 0");
        hapMapTranslate.put("NA19223", "Yoruba058 NA19223 0 0 1 0");
        hapMapTranslate.put("NA19222", "Yoruba058 NA19222 0 0 2 0");
        hapMapTranslate.put("NA19120", "Yoruba060 NA19120 NA19119 NA19116 1 0");
        hapMapTranslate.put("NA19119", "Yoruba060 NA19119 0 0 1 0");
        hapMapTranslate.put("NA19116", "Yoruba060 NA19116 0 0 2 0");
        hapMapTranslate.put("NA19142", "Yoruba071 NA19142 NA19141 NA19140 1 0");
        hapMapTranslate.put("NA19141", "Yoruba071 NA19141 0 0 1 0");
        hapMapTranslate.put("NA19140", "Yoruba071 NA19140 0 0 2 0");
        hapMapTranslate.put("NA19154", "Yoruba072 NA19154 NA19153 NA19152 1 0");
        hapMapTranslate.put("NA19153", "Yoruba072 NA19153 0 0 1 0");
        hapMapTranslate.put("NA19152", "Yoruba072 NA19152 0 0 2 0");
        hapMapTranslate.put("NA19145", "Yoruba074 NA19145 NA19144 NA19143 1 0");
        hapMapTranslate.put("NA19144", "Yoruba074 NA19144 0 0 1 0");
        hapMapTranslate.put("NA19143", "Yoruba074 NA19143 0 0 2 0");
        hapMapTranslate.put("NA19129", "Yoruba077 NA19129 NA19128 NA19127 2 0");
        hapMapTranslate.put("NA19128", "Yoruba077 NA19128 0 0 1 0");
        hapMapTranslate.put("NA19127", "Yoruba077 NA19127 0 0 2 0");
        hapMapTranslate.put("NA19132", "Yoruba101 NA19132 NA19130 NA19131 2 0");
        hapMapTranslate.put("NA19130", "Yoruba101 NA19130 0 0 1 0");
        hapMapTranslate.put("NA19131", "Yoruba101 NA19131 0 0 2 0");
        hapMapTranslate.put("NA19100", "Yoruba105 NA19100 NA19098 NA19099 2 0");
        hapMapTranslate.put("NA19098", "Yoruba105 NA19098 0 0 1 0");
        hapMapTranslate.put("NA19099", "Yoruba105 NA19099 0 0 2 0");
        hapMapTranslate.put("NA19194", "Yoruba112 NA19194 NA19192 NA19193 1 0");
        hapMapTranslate.put("NA19192", "Yoruba112 NA19192 0 0 1 0");
        hapMapTranslate.put("NA19193", "Yoruba112 NA19193 0 0 2 0");
        hapMapTranslate.put("NA19240", "Yoruba117 NA19240 NA19239 NA19238 2 0");
        hapMapTranslate.put("NA19239", "Yoruba117 NA19239 0 0 1 0");
        hapMapTranslate.put("NA19238", "Yoruba117 NA19238 0 0 2 0");

        // data entered for the additional perlegen samples
        hapMapTranslate.put("NA10844", "NA10844 NA10844 0 0 2 0");
        hapMapTranslate.put("NA17134", "NA17134 NA17134 0 0 2 0");
        hapMapTranslate.put("NA17115", "NA17115 NA17115 0 0 1 0");
        hapMapTranslate.put("NA12560", "NA12560 NA12560 0 0 1 0");
        hapMapTranslate.put("NA17137", "NA17137 NA17137 0 0 2 0");
        hapMapTranslate.put("NA17747", "NA17747 NA17747 0 0 2 0");
        hapMapTranslate.put("NA12547", "NA12547 NA12547 0 0 1 0");
        hapMapTranslate.put("NA17138", "NA17138 NA17138 0 0 2 0");
        hapMapTranslate.put("NA17116", "NA17116 NA17116 0 0 2 0");
        hapMapTranslate.put("NA17753", "NA17753 NA17753 0 0 1 0");
        hapMapTranslate.put("NA17102", "NA17102 NA17102 0 0 1 0");
        hapMapTranslate.put("NA10858", "NA10858 NA10858 0 0 1 0");
        hapMapTranslate.put("NA17106", "NA17106 NA17106 0 0 1 0");
        hapMapTranslate.put("NA17114", "NA17114 NA17114 0 0 1 0");
        hapMapTranslate.put("NA12548", "NA12548 NA12548 0 0 2 0");
        hapMapTranslate.put("NA17109", "NA17109 NA17109 0 0 1 0");
        hapMapTranslate.put("NA17740", "NA17740 NA17740 0 0 2 0");
        hapMapTranslate.put("NA17139", "NA17139 NA17139 0 0 2 0");
        hapMapTranslate.put("NA07349", "NA07349 NA07349 0 0 1 0");
        hapMapTranslate.put("NA17761", "NA17761 NA17761 0 0 2 0");
        hapMapTranslate.put("NA10853", "NA10853 NA10853 0 0 2 0");
        hapMapTranslate.put("NA17737", "NA17737 NA17737 0 0 1 0");
        hapMapTranslate.put("NA17744", "NA17744 NA17744 0 0 2 0");
        hapMapTranslate.put("NA10845", "NA10845 NA10845 0 0 1 0");
        hapMapTranslate.put("NA17140", "NA17140 NA17140 0 0 2 0");
        hapMapTranslate.put("NA17105", "NA17105 NA17105 0 0 1 0");
        hapMapTranslate.put("NA17752", "NA17752 NA17752 0 0 2 0");
        hapMapTranslate.put("NA17746", "NA17746 NA17746 0 0 2 0");
        hapMapTranslate.put("NA17135", "NA17135 NA17135 0 0 2 0");
        hapMapTranslate.put("NA17742", "NA17742 NA17742 0 0 1 0");
        hapMapTranslate.put("NA17104", "NA17104 NA17104 0 0 1 0");
        hapMapTranslate.put("NA17741", "NA17741 NA17741 0 0 2 0");
        hapMapTranslate.put("NA17738", "NA17738 NA17738 0 0 2 0");
        hapMapTranslate.put("NA17201", "NA17201 NA17201 0 0 1 0");
        hapMapTranslate.put("NA17745", "NA17745 NA17745 0 0 2 0");
        hapMapTranslate.put("NA17749", "NA17749 NA17749 0 0 1 0");
        hapMapTranslate.put("NA17133", "NA17133 NA17133 0 0 2 0");
        hapMapTranslate.put("NA10842", "NA10842 NA10842 0 0 1 0");
        hapMapTranslate.put("NA17736", "NA17736 NA17736 0 0 1 0");
        hapMapTranslate.put("NA17107", "NA17107 NA17107 0 0 1 0");
        hapMapTranslate.put("NA10852", "NA10852 NA10852 0 0 2 0");
        hapMapTranslate.put("NA17756", "NA17756 NA17756 0 0 2 0");
        hapMapTranslate.put("NA17735", "NA17735 NA17735 0 0 2 0");
        hapMapTranslate.put("NA10848", "NA10848 NA10848 0 0 1 0");
        hapMapTranslate.put("NA10850", "NA10850 NA10850 0 0 2 0");
        hapMapTranslate.put("NA17110", "NA17110 NA17110 0 0 2 0");
        hapMapTranslate.put("NA17111", "NA17111 NA17111 0 0 1 0");
        hapMapTranslate.put("NA17136", "NA17136 NA17136 0 0 2 0");
        hapMapTranslate.put("NA17755", "NA17755 NA17755 0 0 1 0");
        hapMapTranslate.put("NA06990", "NA06990 NA06990 0 0 2 0");
        hapMapTranslate.put("NA17733", "NA17733 NA17733 0 0 2 0");
        hapMapTranslate.put("NA17103", "NA17103 NA17103 0 0 1 0");
        hapMapTranslate.put("NA17739", "NA17739 NA17739 0 0 2 0");
        hapMapTranslate.put("NA17108", "NA17108 NA17108 0 0 1 0");
        hapMapTranslate.put("NA17759", "NA17759 NA17759 0 0 1 0");
        hapMapTranslate.put("NA17112", "NA17112 NA17112 0 0 2 0");
        hapMapTranslate.put("NA17113", "NA17113 NA17113 0 0 2 0");
        hapMapTranslate.put("NA10843", "NA10843 NA10843 0 0 2 0");
        hapMapTranslate.put("NA17743", "NA17743 NA17743 0 0 1 0");
        hapMapTranslate.put("NA17757", "NA17757 NA17757 0 0 2 0");
        hapMapTranslate.put("NA17754", "NA17754 NA17754 0 0 2 0");
        hapMapTranslate.put("NA17734", "NA17734 NA17734 0 0 2 0");

        //HAPMAP 3
        hapMapTranslate.put("NA19625", "2357 NA19625 0 0 2 0");
        hapMapTranslate.put("NA19702", "2367 NA19702 NA19700 NA19701 1 0");
        hapMapTranslate.put("NA19700", "2367 NA19700 0 0 1 0");
        hapMapTranslate.put("NA19701", "2367 NA19701 0 0 2 0");
        hapMapTranslate.put("NA19705", "2368 NA19705 NA19703 NA19704 1 0");
        hapMapTranslate.put("NA19703", "2368 NA19703 0 0 1 0");
        hapMapTranslate.put("NA19704", "2368 NA19704 0 0 2 0");
        hapMapTranslate.put("NA19708", "2369 NA19708 0 NA19707 2 0");
        hapMapTranslate.put("NA19707", "2369 NA19707 0 0 2 0");
        hapMapTranslate.put("NA19711", "2371 NA19711 0 0 1 0");
        hapMapTranslate.put("NA19712", "2371 NA19712 0 0 2 0");
        hapMapTranslate.put("NA19828", "2418 NA19828 NA19818 NA19819 1 0");
        hapMapTranslate.put("NA19818", "2418 NA19818 0 0 1 0");
        hapMapTranslate.put("NA19819", "2418 NA19819 0 0 2 0");
        hapMapTranslate.put("NA19836", "2424 NA19836 NA19834 NA19835 2 0");
        hapMapTranslate.put("NA19834", "2424 NA19834 0 0 1 0");
        hapMapTranslate.put("NA19835", "2424 NA19835 0 0 2 0");
        hapMapTranslate.put("NA19902", "2425 NA19902 NA19900 NA19901 2 0");
        hapMapTranslate.put("NA19900", "2425 NA19900 0 0 1 0");
        hapMapTranslate.put("NA19901", "2425 NA19901 0 0 2 0");
        hapMapTranslate.put("NA19904", "2426 NA19904 0 0 1 0");
        hapMapTranslate.put("NA19919", "2427 NA19919 NA19908 NA19909 1 0");
        hapMapTranslate.put("NA19908", "2427 NA19908 0 0 1 0");
        hapMapTranslate.put("NA19909", "2427 NA19909 0 0 2 0");
        hapMapTranslate.put("NA19915", "2430 NA19915 0 NA19914 1 0");
        hapMapTranslate.put("NA19914", "2430 NA19914 0 0 2 0");
        hapMapTranslate.put("NA19918", "2431 NA19918 NA19916 NA19917 1 0");
        hapMapTranslate.put("NA19916", "2431 NA19916 0 0 1 0");
        hapMapTranslate.put("NA19917", "2431 NA19917 0 0 2 0");
        hapMapTranslate.put("NA20129", "2433 NA20129 NA19920 NA19921 2 0");
        hapMapTranslate.put("NA19920", "2433 NA19920 0 0 1 0");
        hapMapTranslate.put("NA19921", "2433 NA19921 0 0 2 0");
        hapMapTranslate.put("NA19983", "2436 NA19983 NA19982 NA19713 2 0");
        hapMapTranslate.put("NA19982", "2436 NA19982 0 0 1 0");
        hapMapTranslate.put("NA19713", "2436 NA19713 0 0 2 0");
        hapMapTranslate.put("NA19714", "2437 NA19714 0 NA19985 2 0");
        hapMapTranslate.put("NA19985", "2437 NA19985 0 0 2 0");
        hapMapTranslate.put("NA20128", "2446 NA20128 NA20126 NA20127 2 0");
        hapMapTranslate.put("NA20126", "2446 NA20126 0 0 1 0");
        hapMapTranslate.put("NA20127", "2446 NA20127 0 0 2 0");
        hapMapTranslate.put("NA20277", "2466 NA20277 0 NA20276 2 0");
        hapMapTranslate.put("NA20276", "2466 NA20276 0 0 2 0");
        hapMapTranslate.put("NA20279", "2467 NA20279 NA20278 0 1 0");
        hapMapTranslate.put("NA20278", "2467 NA20278 0 0 1 0");
        hapMapTranslate.put("NA20284", "2469 NA20284 NA20281 NA20282 1 0");
        hapMapTranslate.put("NA20281", "2469 NA20281 0 0 1 0");
        hapMapTranslate.put("NA20282", "2469 NA20282 0 0 2 0");
        hapMapTranslate.put("NA20288", "2470 NA20288 0 NA20287 1 0");
        hapMapTranslate.put("NA20287", "2470 NA20287 0 0 2 0");
        hapMapTranslate.put("NA20290", "2471 NA20290 0 NA20289 2 0");
        hapMapTranslate.put("NA20289", "2471 NA20289 0 0 2 0");
        hapMapTranslate.put("NA20292", "2472 NA20292 NA20291 0 2 0");
        hapMapTranslate.put("NA20291", "2472 NA20291 0 0 1 0");
        hapMapTranslate.put("NA20295", "2474 NA20295 0 NA20294 1 0");
        hapMapTranslate.put("NA20294", "2474 NA20294 0 0 2 0");
        hapMapTranslate.put("NA20297", "2475 NA20297 0 NA20296 1 0");
        hapMapTranslate.put("NA20296", "2475 NA20296 0 0 2 0");
        hapMapTranslate.put("NA20300", "2476 NA20300 0 NA20299 2 0");
        hapMapTranslate.put("NA20299", "2476 NA20299 0 0 2 0");
        hapMapTranslate.put("NA20302", "2477 NA20302 0 NA20301 1 0");
        hapMapTranslate.put("NA20301", "2477 NA20301 0 0 2 0");
        hapMapTranslate.put("NA20316", "2479 NA20316 0 NA20314 1 0");
        hapMapTranslate.put("NA20314", "2479 NA20314 0 0 2 0");
        hapMapTranslate.put("NA20319", "2480 NA20319 0 NA20317 2 0");
        hapMapTranslate.put("NA20317", "2480 NA20317 0 0 2 0");
        hapMapTranslate.put("NA20322", "2481 NA20322 0 0 2 0");
        hapMapTranslate.put("NA20333", "2483 NA20333 0 NA20332 2 0");
        hapMapTranslate.put("NA20332", "2483 NA20332 0 0 2 0");
        hapMapTranslate.put("NA20335", "2484 NA20335 0 NA20334 1 0");
        hapMapTranslate.put("NA20334", "2484 NA20334 0 0 2 0");
        hapMapTranslate.put("NA20337", "2485 NA20337 0 NA20336 2 0");
        hapMapTranslate.put("NA20336", "2485 NA20336 0 0 2 0");
        hapMapTranslate.put("NA20340", "2487 NA20340 0 0 1 0");
        hapMapTranslate.put("NA20341", "2487 NA20341 0 0 2 0");
        hapMapTranslate.put("NA20343", "2488 NA20343 NA20342 0 1 0");
        hapMapTranslate.put("NA20342", "2488 NA20342 0 0 1 0");
        hapMapTranslate.put("NA20345", "2489 NA20345 0 NA20344 1 0");
        hapMapTranslate.put("NA20344", "2489 NA20344 0 0 2 0");
        hapMapTranslate.put("NA20347", "2490 NA20347 NA20346 0 1 0");
        hapMapTranslate.put("NA20346", "2490 NA20346 0 0 1 0");
        hapMapTranslate.put("NA20348", "2491 NA20348 0 0 1 0");
        hapMapTranslate.put("NA20350", "2492 NA20350 NA20349 0 1 0");
        hapMapTranslate.put("NA20349", "2492 NA20349 0 0 1 0");
        hapMapTranslate.put("NA20358", "2494 NA20358 NA20356 NA20357 1 0");
        hapMapTranslate.put("NA20356", "2494 NA20356 0 0 1 0");
        hapMapTranslate.put("NA20357", "2494 NA20357 0 0 2 0");
        hapMapTranslate.put("NA20360", "2495 NA20360 0 NA20359 1 0");
        hapMapTranslate.put("NA20359", "2495 NA20359 0 0 2 0");
        hapMapTranslate.put("NA20364", "2496 NA20364 0 NA20363 2 0");
        hapMapTranslate.put("NA20363", "2496 NA20363 0 0 2 0");
        hapMapTranslate.put("NA10846", "1334 NA10846 NA12144 NA12145 1 0");
        hapMapTranslate.put("NA12146", "1334 NA12146 0 0 1 0");
        hapMapTranslate.put("NA12239", "1334 NA12239 0 0 2 0");
        hapMapTranslate.put("NA10847", "1334 NA10847 NA12146 NA12239 2 0");
        hapMapTranslate.put("NA12144", "1334 NA12144 0 0 1 0");
        hapMapTranslate.put("NA12145", "1334 NA12145 0 0 2 0");
        hapMapTranslate.put("NA07029", "1340 NA07029 NA06994 NA07000 1 0");
        hapMapTranslate.put("NA07022", "1340 NA07022 0 0 1 0");
        hapMapTranslate.put("NA07056", "1340 NA07056 0 0 2 0");
        hapMapTranslate.put("NA07019", "1340 NA07019 NA07022 NA07056 2 0");
        hapMapTranslate.put("NA06994", "1340 NA06994 0 0 1 0");
        hapMapTranslate.put("NA07000", "1340 NA07000 0 0 2 0");
        hapMapTranslate.put("NA07048", "1341 NA07048 NA07034 NA07055 1 0");
        hapMapTranslate.put("NA06993", "1341 NA06993 0 0 1 0");
        hapMapTranslate.put("NA06985", "1341 NA06985 0 0 2 0");
        hapMapTranslate.put("NA06991", "1341 NA06991 NA06993 NA06985 2 0");
        hapMapTranslate.put("NA07034", "1341 NA07034 0 0 1 0");
        hapMapTranslate.put("NA07055", "1341 NA07055 0 0 2 0");
        hapMapTranslate.put("NA10851", "1344 NA10851 NA12056 NA12057 1 0");
        hapMapTranslate.put("NA12056", "1344 NA12056 0 0 1 0");
        hapMapTranslate.put("NA12057", "1344 NA12057 0 0 2 0");
        hapMapTranslate.put("NA07357", "1345 NA07357 0 0 1 0");
        hapMapTranslate.put("NA07345", "1345 NA07345 0 0 2 0");
        hapMapTranslate.put("NA07348", "1345 NA07348 NA07357 NA07345 2 0");
        hapMapTranslate.put("NA10857", "1346 NA10857 NA12043 NA12044 1 0");
        hapMapTranslate.put("NA12043", "1346 NA12043 0 0 1 0");
        hapMapTranslate.put("NA12044", "1346 NA12044 0 0 2 0");
        hapMapTranslate.put("NA11881", "1347 NA11881 0 0 1 0");
        hapMapTranslate.put("NA11882", "1347 NA11882 0 0 2 0");
        hapMapTranslate.put("NA10859", "1347 NA10859 NA11881 NA11882 2 0");
        hapMapTranslate.put("NA11839", "1349 NA11839 0 0 1 0");
        hapMapTranslate.put("NA11840", "1349 NA11840 0 0 2 0");
        hapMapTranslate.put("NA10854", "1349 NA10854 NA11839 NA11840 2 0");
        hapMapTranslate.put("NA10856", "1350 NA10856 NA11829 NA11830 1 0");
        hapMapTranslate.put("NA11831", "1350 NA11831 0 0 1 0");
        hapMapTranslate.put("NA11832", "1350 NA11832 0 0 2 0");
        hapMapTranslate.put("NA10855", "1350 NA10855 NA11831 NA11832 2 0");
        hapMapTranslate.put("NA11829", "1350 NA11829 0 0 1 0");
        hapMapTranslate.put("NA11830", "1350 NA11830 0 0 2 0");
        hapMapTranslate.put("NA12707", "1358 NA12707 NA12716 NA12717 1 0");
        hapMapTranslate.put("NA12716", "1358 NA12716 0 0 1 0");
        hapMapTranslate.put("NA12717", "1358 NA12717 0 0 2 0");
        hapMapTranslate.put("NA10860", "1362 NA10860 NA11992 NA11993 1 0");
        hapMapTranslate.put("NA11994", "1362 NA11994 0 0 1 0");
        hapMapTranslate.put("NA11995", "1362 NA11995 0 0 2 0");
        hapMapTranslate.put("NA10861", "1362 NA10861 NA11994 NA11995 2 0");
        hapMapTranslate.put("NA11992", "1362 NA11992 0 0 1 0");
        hapMapTranslate.put("NA11993", "1362 NA11993 0 0 2 0");
        hapMapTranslate.put("NA12264", "1375 NA12264 0 0 1 0");
        hapMapTranslate.put("NA12234", "1375 NA12234 0 0 2 0");
        hapMapTranslate.put("NA10863", "1375 NA10863 NA12264 NA12234 2 0");
        hapMapTranslate.put("NA10830", "1408 NA10830 NA12154 NA12236 1 0");
        hapMapTranslate.put("NA12155", "1408 NA12155 0 0 1 0");
        hapMapTranslate.put("NA12156", "1408 NA12156 0 0 2 0");
        hapMapTranslate.put("NA10831", "1408 NA10831 NA12155 NA12156 2 0");
        hapMapTranslate.put("NA12154", "1408 NA12154 0 0 1 0");
        hapMapTranslate.put("NA12236", "1408 NA12236 0 0 2 0");
        hapMapTranslate.put("NA10835", "1416 NA10835 NA12248 NA12249 1 0");
        hapMapTranslate.put("NA12248", "1416 NA12248 0 0 1 0");
        hapMapTranslate.put("NA12249", "1416 NA12249 0 0 2 0");
        hapMapTranslate.put("NA10838", "1420 NA10838 NA12003 NA12004 1 0");
        hapMapTranslate.put("NA12005", "1420 NA12005 0 0 1 0");
        hapMapTranslate.put("NA12006", "1420 NA12006 0 0 2 0");
        hapMapTranslate.put("NA10839", "1420 NA10839 NA12005 NA12006 2 0");
        hapMapTranslate.put("NA12003", "1420 NA12003 0 0 1 0");
        hapMapTranslate.put("NA12004", "1420 NA12004 0 0 2 0");
        hapMapTranslate.put("NA12750", "1444 NA12750 0 0 1 0");
        hapMapTranslate.put("NA12751", "1444 NA12751 0 0 2 0");
        hapMapTranslate.put("NA12740", "1444 NA12740 NA12750 NA12751 2 0");
        hapMapTranslate.put("NA12752", "1447 NA12752 NA12760 NA12761 1 0");
        hapMapTranslate.put("NA12762", "1447 NA12762 0 0 1 0");
        hapMapTranslate.put("NA12763", "1447 NA12763 0 0 2 0");
        hapMapTranslate.put("NA12753", "1447 NA12753 NA12762 NA12763 2 0");
        hapMapTranslate.put("NA12760", "1447 NA12760 0 0 1 0");
        hapMapTranslate.put("NA12761", "1447 NA12761 0 0 2 0");
        hapMapTranslate.put("NA12801", "1454 NA12801 NA12812 NA12813 1 0");
        hapMapTranslate.put("NA12814", "1454 NA12814 0 0 1 0");
        hapMapTranslate.put("NA12815", "1454 NA12815 0 0 2 0");
        hapMapTranslate.put("NA12802", "1454 NA12802 NA12814 NA12815 2 0");
        hapMapTranslate.put("NA12812", "1454 NA12812 0 0 1 0");
        hapMapTranslate.put("NA12813", "1454 NA12813 0 0 2 0");
        hapMapTranslate.put("NA12864", "1459 NA12864 NA12872 NA12873 1 0");
        hapMapTranslate.put("NA12874", "1459 NA12874 0 0 1 0");
        hapMapTranslate.put("NA12875", "1459 NA12875 0 0 2 0");
        hapMapTranslate.put("NA12865", "1459 NA12865 NA12874 NA12875 2 0");
        hapMapTranslate.put("NA12872", "1459 NA12872 0 0 1 0");
        hapMapTranslate.put("NA12873", "1459 NA12873 0 0 2 0");
        hapMapTranslate.put("NA12891", "1463 NA12891 0 0 1 0");
        hapMapTranslate.put("NA12892", "1463 NA12892 0 0 2 0");
        hapMapTranslate.put("NA12878", "1463 NA12878 NA12891 NA12892 2 0");
        hapMapTranslate.put("NA12329", "1328 NA12329 NA06984 NA06989 2 0");
        hapMapTranslate.put("NA06984", "1328 NA06984 0 0 1 0");
        hapMapTranslate.put("NA06989", "1328 NA06989 0 0 2 0");
        hapMapTranslate.put("NA12335", "1330 NA12335 NA12340 NA12341 1 0");
        hapMapTranslate.put("NA12342", "1330 NA12342 0 0 1 0");
        hapMapTranslate.put("NA12343", "1330 NA12343 0 0 2 0");
        hapMapTranslate.put("NA12336", "1330 NA12336 NA12342 NA12343 2 0");
        hapMapTranslate.put("NA12340", "1330 NA12340 0 0 1 0");
        hapMapTranslate.put("NA12341", "1330 NA12341 0 0 2 0");
        hapMapTranslate.put("NA12058", "1344 NA12058 0 0 2 0");
        hapMapTranslate.put("NA10850", "1344 NA10850 0 NA12058 2 0");
        hapMapTranslate.put("NA07349", "1345 NA07349 NA07347 NA07346 1 0");
        hapMapTranslate.put("NA07347", "1345 NA07347 0 0 1 0");
        hapMapTranslate.put("NA07346", "1345 NA07346 0 0 2 0");
        hapMapTranslate.put("NA12045", "1346 NA12045 0 0 1 0");
        hapMapTranslate.put("NA10852", "1346 NA10852 NA12045 0 2 0");
        hapMapTranslate.put("NA10853", "1349 NA10853 NA11843 0 1 0");
        hapMapTranslate.put("NA11843", "1349 NA11843 0 0 1 0");
        hapMapTranslate.put("NA12375", "1353 NA12375 0 NA12383 1 0");
        hapMapTranslate.put("NA12546", "1353 NA12546 0 0 1 0");
        hapMapTranslate.put("NA12489", "1353 NA12489 0 0 2 0");
        hapMapTranslate.put("NA12376", "1353 NA12376 NA12546 NA12489 2 0");
        hapMapTranslate.put("NA12383", "1353 NA12383 0 0 2 0");
        hapMapTranslate.put("NA12399", "1354 NA12399 0 0 1 0");
        hapMapTranslate.put("NA12400", "1354 NA12400 0 0 2 0");
        hapMapTranslate.put("NA12386", "1354 NA12386 NA12399 NA12400 2 0");
        hapMapTranslate.put("NA12485", "1355 NA12485 NA12413 NA12414 1 0");
        hapMapTranslate.put("NA12413", "1355 NA12413 0 0 1 0");
        hapMapTranslate.put("NA12414", "1355 NA12414 0 0 2 0");
        hapMapTranslate.put("NA12718", "1358 NA12718 0 0 2 0");
        hapMapTranslate.put("NA12708", "1358 NA12708 0 NA12718 2 0");
        hapMapTranslate.put("NA10865", "1377 NA10865 NA11891 NA11892 1 0");
        hapMapTranslate.put("NA11893", "1377 NA11893 0 0 1 0");
        hapMapTranslate.put("NA11894", "1377 NA11894 0 0 2 0");
        hapMapTranslate.put("NA10864", "1377 NA10864 NA11893 NA11894 2 0");
        hapMapTranslate.put("NA11891", "1377 NA11891 0 0 1 0");
        hapMapTranslate.put("NA11892", "1377 NA11892 0 0 2 0");
        hapMapTranslate.put("NA10837", "1418 NA10837 NA12272 NA12273 1 0");
        hapMapTranslate.put("NA12274", "1418 NA12274 0 0 1 0");
        hapMapTranslate.put("NA12275", "1418 NA12275 0 0 2 0");
        hapMapTranslate.put("NA10836", "1418 NA10836 NA12274 NA12275 2 0");
        hapMapTranslate.put("NA12272", "1418 NA12272 0 0 1 0");
        hapMapTranslate.put("NA12273", "1418 NA12273 0 0 2 0");
        hapMapTranslate.put("NA12286", "1421 NA12286 0 0 1 0");
        hapMapTranslate.put("NA12287", "1421 NA12287 0 0 2 0");
        hapMapTranslate.put("NA10840", "1421 NA10840 NA12286 NA12287 2 0");
        hapMapTranslate.put("NA12282", "1421 NA12282 0 0 1 0");
        hapMapTranslate.put("NA12283", "1421 NA12283 0 0 2 0");
        hapMapTranslate.put("NA10842", "1423 NA10842 NA11917 NA11918 1 0");
        hapMapTranslate.put("NA11919", "1423 NA11919 0 0 1 0");
        hapMapTranslate.put("NA11920", "1423 NA11920 0 0 2 0");
        hapMapTranslate.put("NA10843", "1423 NA10843 NA11919 NA11920 2 0");
        hapMapTranslate.put("NA11917", "1423 NA11917 0 0 1 0");
        hapMapTranslate.put("NA11918", "1423 NA11918 0 0 2 0");
        hapMapTranslate.put("NA10845", "1424 NA10845 NA11930 NA11931 1 0");
        hapMapTranslate.put("NA11930", "1424 NA11930 0 0 1 0");
        hapMapTranslate.put("NA11931", "1424 NA11931 0 0 2 0");
        hapMapTranslate.put("NA12739", "1444 NA12739 NA12748 NA12749 1 0");
        hapMapTranslate.put("NA12748", "1444 NA12748 0 0 1 0");
        hapMapTranslate.put("NA12749", "1444 NA12749 0 0 2 0");
        hapMapTranslate.put("NA12766", "1451 NA12766 NA12775 NA12776 1 0");
        hapMapTranslate.put("NA12777", "1451 NA12777 0 0 1 0");
        hapMapTranslate.put("NA12778", "1451 NA12778 0 0 2 0");
        hapMapTranslate.put("NA12767", "1451 NA12767 NA12777 NA12778 2 0");
        hapMapTranslate.put("NA12775", "1451 NA12775 0 0 1 0");
        hapMapTranslate.put("NA12776", "1451 NA12776 0 0 2 0");
        hapMapTranslate.put("NA12817", "1456 NA12817 NA12827 NA12828 1 0");
        hapMapTranslate.put("NA12829", "1456 NA12829 0 0 1 0");
        hapMapTranslate.put("NA12830", "1456 NA12830 0 0 2 0");
        hapMapTranslate.put("NA12818", "1456 NA12818 NA12829 NA12830 2 0");
        hapMapTranslate.put("NA12827", "1456 NA12827 0 0 1 0");
        hapMapTranslate.put("NA12828", "1456 NA12828 0 0 2 0");
        hapMapTranslate.put("NA12842", "1458 NA12842 0 0 1 0");
        hapMapTranslate.put("NA12843", "1458 NA12843 0 0 2 0");
        hapMapTranslate.put("NA12832", "1458 NA12832 NA12842 NA12843 2 0");
        hapMapTranslate.put("NA12877", "1463 NA12877 NA12889 NA12890 1 0");
        hapMapTranslate.put("NA12889", "1463 NA12889 0 0 1 0");
        hapMapTranslate.put("NA12890", "1463 NA12890 0 0 2 0");
        hapMapTranslate.put("NA12344", "13281 NA12344 NA12347 NA12348 1 0");
        hapMapTranslate.put("NA12347", "13281 NA12347 0 0 1 0");
        hapMapTranslate.put("NA12348", "13281 NA12348 0 0 2 0");
        hapMapTranslate.put("NA06995", "13291 NA06995 NA07435 NA07037 1 0");
        hapMapTranslate.put("NA06986", "13291 NA06986 0 0 1 0");
        hapMapTranslate.put("NA07045", "13291 NA07045 0 0 2 0");
        hapMapTranslate.put("NA06997", "13291 NA06997 NA06986 NA07045 2 0");
        hapMapTranslate.put("NA07435", "13291 NA07435 0 0 1 0");
        hapMapTranslate.put("NA07037", "13291 NA07037 0 0 2 0");
        hapMapTranslate.put("NA07051", "13292 NA07051 0 0 1 0");
        hapMapTranslate.put("NA07031", "13292 NA07031 0 0 2 0");
        hapMapTranslate.put("NA07014", "13292 NA07014 NA07051 NA07031 2 0");
        hapMapTranslate.put("NA17962", "NA17962 NA17962 0 0 2 0");
        hapMapTranslate.put("NA17965", "NA17965 NA17965 0 0 1 0");
        hapMapTranslate.put("NA17966", "NA17966 NA17966 0 0 2 0");
        hapMapTranslate.put("NA17967", "NA17967 NA17967 0 0 1 0");
        hapMapTranslate.put("NA17968", "NA17968 NA17968 0 0 2 0");
        hapMapTranslate.put("NA17969", "NA17969 NA17969 0 0 1 0");
        hapMapTranslate.put("NA17970", "NA17970 NA17970 0 0 2 0");
        hapMapTranslate.put("NA17971", "NA17971 NA17971 0 0 2 0");
        hapMapTranslate.put("NA17972", "NA17972 NA17972 0 0 1 0");
        hapMapTranslate.put("NA17973", "NA17973 NA17973 0 0 1 0");
        hapMapTranslate.put("NA17974", "NA17974 NA17974 0 0 1 0");
        hapMapTranslate.put("NA17975", "NA17975 NA17975 0 0 1 0");
        hapMapTranslate.put("NA17976", "NA17976 NA17976 0 0 1 0");
        hapMapTranslate.put("NA17977", "NA17977 NA17977 0 0 2 0");
        hapMapTranslate.put("NA17979", "NA17979 NA17979 0 0 1 0");
        hapMapTranslate.put("NA17980", "NA17980 NA17980 0 0 1 0");
        hapMapTranslate.put("NA17981", "NA17981 NA17981 0 0 2 0");
        hapMapTranslate.put("NA17982", "NA17982 NA17982 0 0 2 0");
        hapMapTranslate.put("NA17983", "NA17983 NA17983 0 0 1 0");
        hapMapTranslate.put("NA17986", "NA17986 NA17986 0 0 1 0");
        hapMapTranslate.put("NA17987", "NA17987 NA17987 0 0 2 0");
        hapMapTranslate.put("NA17988", "NA17988 NA17988 0 0 2 0");
        hapMapTranslate.put("NA17989", "NA17989 NA17989 0 0 1 0");
        hapMapTranslate.put("NA17990", "NA17990 NA17990 0 0 2 0");
        hapMapTranslate.put("NA17992", "NA17992 NA17992 0 0 1 0");
        hapMapTranslate.put("NA17993", "NA17993 NA17993 0 0 2 0");
        hapMapTranslate.put("NA17995", "NA17995 NA17995 0 0 2 0");
        hapMapTranslate.put("NA17996", "NA17996 NA17996 0 0 2 0");
        hapMapTranslate.put("NA17997", "NA17997 NA17997 0 0 1 0");
        hapMapTranslate.put("NA17998", "NA17998 NA17998 0 0 2 0");
        hapMapTranslate.put("NA17999", "NA17999 NA17999 0 0 1 0");
        hapMapTranslate.put("NA18101", "NA18101 NA18101 0 0 2 0");
        hapMapTranslate.put("NA18102", "NA18102 NA18102 0 0 1 0");
        hapMapTranslate.put("NA18103", "NA18103 NA18103 0 0 1 0");
        hapMapTranslate.put("NA18105", "NA18105 NA18105 0 0 2 0");
        hapMapTranslate.put("NA18106", "NA18106 NA18106 0 0 1 0");
        hapMapTranslate.put("NA18107", "NA18107 NA18107 0 0 2 0");
        hapMapTranslate.put("NA18108", "NA18108 NA18108 0 0 2 0");
        hapMapTranslate.put("NA18109", "NA18109 NA18109 0 0 2 0");
        hapMapTranslate.put("NA18112", "NA18112 NA18112 0 0 2 0");
        hapMapTranslate.put("NA18114", "NA18114 NA18114 0 0 1 0");
        hapMapTranslate.put("NA18117", "NA18117 NA18117 0 0 1 0");
        hapMapTranslate.put("NA18118", "NA18118 NA18118 0 0 2 0");
        hapMapTranslate.put("NA18120", "NA18120 NA18120 0 0 1 0");
        hapMapTranslate.put("NA18122", "NA18122 NA18122 0 0 1 0");
        hapMapTranslate.put("NA18124", "NA18124 NA18124 0 0 1 0");
        hapMapTranslate.put("NA18125", "NA18125 NA18125 0 0 1 0");
        hapMapTranslate.put("NA18127", "NA18127 NA18127 0 0 1 0");
        hapMapTranslate.put("NA18128", "NA18128 NA18128 0 0 2 0");
        hapMapTranslate.put("NA18129", "NA18129 NA18129 0 0 2 0");
        hapMapTranslate.put("NA18131", "NA18131 NA18131 0 0 2 0");
        hapMapTranslate.put("NA18132", "NA18132 NA18132 0 0 1 0");
        hapMapTranslate.put("NA18133", "NA18133 NA18133 0 0 1 0");
        hapMapTranslate.put("NA18134", "NA18134 NA18134 0 0 2 0");
        hapMapTranslate.put("NA18135", "NA18135 NA18135 0 0 2 0");
        hapMapTranslate.put("NA18136", "NA18136 NA18136 0 0 1 0");
        hapMapTranslate.put("NA18138", "NA18138 NA18138 0 0 1 0");
        hapMapTranslate.put("NA18139", "NA18139 NA18139 0 0 2 0");
        hapMapTranslate.put("NA18140", "NA18140 NA18140 0 0 2 0");
        hapMapTranslate.put("NA18141", "NA18141 NA18141 0 0 1 0");
        hapMapTranslate.put("NA18143", "NA18143 NA18143 0 0 1 0");
        hapMapTranslate.put("NA18144", "NA18144 NA18144 0 0 2 0");
        hapMapTranslate.put("NA18146", "NA18146 NA18146 0 0 2 0");
        hapMapTranslate.put("NA18147", "NA18147 NA18147 0 0 1 0");
        hapMapTranslate.put("NA18148", "NA18148 NA18148 0 0 2 0");
        hapMapTranslate.put("NA18149", "NA18149 NA18149 0 0 1 0");
        hapMapTranslate.put("NA18150", "NA18150 NA18150 0 0 2 0");
        hapMapTranslate.put("NA18151", "NA18151 NA18151 0 0 2 0");
        hapMapTranslate.put("NA18152", "NA18152 NA18152 0 0 1 0");
        hapMapTranslate.put("NA18153", "NA18153 NA18153 0 0 2 0");
        hapMapTranslate.put("NA18154", "NA18154 NA18154 0 0 2 0");
        hapMapTranslate.put("NA18155", "NA18155 NA18155 0 0 1 0");
        hapMapTranslate.put("NA18156", "NA18156 NA18156 0 0 1 0");
        hapMapTranslate.put("NA18157", "NA18157 NA18157 0 0 2 0");
        hapMapTranslate.put("NA18158", "NA18158 NA18158 0 0 1 0");
        hapMapTranslate.put("NA18159", "NA18159 NA18159 0 0 2 0");
        hapMapTranslate.put("NA18160", "NA18160 NA18160 0 0 1 0");
        hapMapTranslate.put("NA18161", "NA18161 NA18161 0 0 2 0");
        hapMapTranslate.put("NA18162", "NA18162 NA18162 0 0 2 0");
        hapMapTranslate.put("NA18163", "NA18163 NA18163 0 0 1 0");
        hapMapTranslate.put("NA18166", "NA18166 NA18166 0 0 1 0");
        hapMapTranslate.put("NA18670", "NA18670 NA18670 0 0 2 0");
        hapMapTranslate.put("NA18674", "NA18674 NA18674 0 0 1 0");
        hapMapTranslate.put("NA18682", "NA18682 NA18682 0 0 1 0");
        hapMapTranslate.put("NA18685", "NA18685 NA18685 0 0 1 0");
        hapMapTranslate.put("NA18689", "NA18689 NA18689 0 0 1 0");
        hapMapTranslate.put("NA18694", "NA18694 NA18694 0 0 2 0");
        hapMapTranslate.put("NA18696", "NA18696 NA18696 0 0 2 0");
        hapMapTranslate.put("NA18702", "NA18702 NA18702 0 0 2 0");
        hapMapTranslate.put("NA18704", "NA18704 NA18704 0 0 2 0");
        hapMapTranslate.put("NA17963", "NA17963 NA17963 0 0 2 0");
        hapMapTranslate.put("NA17991", "NA17991 NA17991 0 0 2 0");
        hapMapTranslate.put("NA17994", "NA17994 NA17994 0 0 1 0");
        hapMapTranslate.put("NA18110", "NA18110 NA18110 0 0 2 0");
        hapMapTranslate.put("NA18111", "NA18111 NA18111 0 0 2 0");
        hapMapTranslate.put("NA18115", "NA18115 NA18115 0 0 2 0");
        hapMapTranslate.put("NA18130", "NA18130 NA18130 0 0 1 0");
        hapMapTranslate.put("NA18673", "NA18673 NA18673 0 0 1 0");
        hapMapTranslate.put("NA18691", "NA18691 NA18691 0 0 1 0");
        hapMapTranslate.put("NA18693", "NA18693 NA18693 0 0 1 0");
        hapMapTranslate.put("NA20845", "NA20845 NA20845 0 0 1 0");
        hapMapTranslate.put("NA20846", "NA20846 NA20846 0 0 1 0");
        hapMapTranslate.put("NA20847", "NA20847 NA20847 0 0 2 0");
        hapMapTranslate.put("NA20849", "NA20849 NA20849 0 0 2 0");
        hapMapTranslate.put("NA20850", "NA20850 NA20850 0 0 1 0");
        hapMapTranslate.put("NA20851", "NA20851 NA20851 0 0 2 0");
        hapMapTranslate.put("NA20852", "NA20852 NA20852 0 0 1 0");
        hapMapTranslate.put("NA20853", "NA20853 NA20853 0 0 2 0");
        hapMapTranslate.put("NA20854", "NA20854 NA20854 0 0 2 0");
        hapMapTranslate.put("NA20856", "NA20856 NA20856 0 0 2 0");
        hapMapTranslate.put("NA20858", "NA20858 NA20858 0 0 1 0");
        hapMapTranslate.put("NA20859", "NA20859 NA20859 0 0 2 0");
        hapMapTranslate.put("NA20861", "NA20861 NA20861 0 0 1 0");
        hapMapTranslate.put("NA20862", "NA20862 NA20862 0 0 2 0");
        hapMapTranslate.put("NA20866", "NA20866 NA20866 0 0 1 0");
        hapMapTranslate.put("NA20869", "NA20869 NA20869 0 0 2 0");
        hapMapTranslate.put("NA20870", "NA20870 NA20870 0 0 1 0");
        hapMapTranslate.put("NA20871", "NA20871 NA20871 0 0 1 0");
        hapMapTranslate.put("NA20872", "NA20872 NA20872 0 0 2 0");
        hapMapTranslate.put("NA20873", "NA20873 NA20873 0 0 1 0");
        hapMapTranslate.put("NA20874", "NA20874 NA20874 0 0 2 0");
        hapMapTranslate.put("NA20875", "NA20875 NA20875 0 0 2 0");
        hapMapTranslate.put("NA20876", "NA20876 NA20876 0 0 2 0");
        hapMapTranslate.put("NA20877", "NA20877 NA20877 0 0 2 0");
        hapMapTranslate.put("NA20879", "NA20879 NA20879 0 0 2 0");
        hapMapTranslate.put("NA20881", "NA20881 NA20881 0 0 2 0");
        hapMapTranslate.put("NA20882", "NA20882 NA20882 0 0 2 0");
        hapMapTranslate.put("NA20883", "NA20883 NA20883 0 0 1 0");
        hapMapTranslate.put("NA20884", "NA20884 NA20884 0 0 1 0");
        hapMapTranslate.put("NA20885", "NA20885 NA20885 0 0 1 0");
        hapMapTranslate.put("NA20886", "NA20886 NA20886 0 0 2 0");
        hapMapTranslate.put("NA20887", "NA20887 NA20887 0 0 1 0");
        hapMapTranslate.put("NA20888", "NA20888 NA20888 0 0 2 0");
        hapMapTranslate.put("NA20889", "NA20889 NA20889 0 0 1 0");
        hapMapTranslate.put("NA20890", "NA20890 NA20890 0 0 1 0");
        hapMapTranslate.put("NA20891", "NA20891 NA20891 0 0 1 0");
        hapMapTranslate.put("NA20892", "NA20892 NA20892 0 0 2 0");
        hapMapTranslate.put("NA20894", "NA20894 NA20894 0 0 2 0");
        hapMapTranslate.put("NA20895", "NA20895 NA20895 0 0 1 0");
        hapMapTranslate.put("NA20896", "NA20896 NA20896 0 0 2 0");
        hapMapTranslate.put("NA20897", "NA20897 NA20897 0 0 1 0");
        hapMapTranslate.put("NA20898", "NA20898 NA20898 0 0 1 0");
        hapMapTranslate.put("NA20899", "NA20899 NA20899 0 0 2 0");
        hapMapTranslate.put("NA20900", "NA20900 NA20900 0 0 2 0");
        hapMapTranslate.put("NA20901", "NA20901 NA20901 0 0 1 0");
        hapMapTranslate.put("NA20902", "NA20902 NA20902 0 0 2 0");
        hapMapTranslate.put("NA20903", "NA20903 NA20903 0 0 1 0");
        hapMapTranslate.put("NA20904", "NA20904 NA20904 0 0 1 0");
        hapMapTranslate.put("NA20906", "NA20906 NA20906 0 0 2 0");
        hapMapTranslate.put("NA20907", "NA20907 NA20907 0 0 2 0");
        hapMapTranslate.put("NA20908", "NA20908 NA20908 0 0 2 0");
        hapMapTranslate.put("NA20909", "NA20909 NA20909 0 0 1 0");
        hapMapTranslate.put("NA20910", "NA20910 NA20910 0 0 2 0");
        hapMapTranslate.put("NA20911", "NA20911 NA20911 0 0 1 0");
        hapMapTranslate.put("NA21086", "NA21086 NA21086 0 0 2 0");
        hapMapTranslate.put("NA21088", "NA21088 NA21088 0 0 2 0");
        hapMapTranslate.put("NA21089", "NA21089 NA21089 0 0 2 0");
        hapMapTranslate.put("NA21090", "NA21090 NA21090 0 0 1 0");
        hapMapTranslate.put("NA21091", "NA21091 NA21091 0 0 1 0");
        hapMapTranslate.put("NA21092", "NA21092 NA21092 0 0 1 0");
        hapMapTranslate.put("NA21094", "NA21094 NA21094 0 0 1 0");
        hapMapTranslate.put("NA21097", "NA21097 NA21097 0 0 2 0");
        hapMapTranslate.put("NA21098", "NA21098 NA21098 0 0 1 0");
        hapMapTranslate.put("NA21099", "NA21099 NA21099 0 0 1 0");
        hapMapTranslate.put("NA21100", "NA21100 NA21100 0 0 1 0");
        hapMapTranslate.put("NA21101", "NA21101 NA21101 0 0 2 0");
        hapMapTranslate.put("NA21102", "NA21102 NA21102 0 0 2 0");
        hapMapTranslate.put("NA21103", "NA21103 NA21103 0 0 2 0");
        hapMapTranslate.put("NA21104", "NA21104 NA21104 0 0 1 0");
        hapMapTranslate.put("NA21105", "NA21105 NA21105 0 0 1 0");
        hapMapTranslate.put("NA21106", "NA21106 NA21106 0 0 2 0");
        hapMapTranslate.put("NA21107", "NA21107 NA21107 0 0 1 0");
        hapMapTranslate.put("NA21108", "NA21108 NA21108 0 0 2 0");
        hapMapTranslate.put("NA21109", "NA21109 NA21109 0 0 1 0");
        hapMapTranslate.put("NA21111", "NA21111 NA21111 0 0 1 0");
        hapMapTranslate.put("NA21112", "NA21112 NA21112 0 0 1 0");
        hapMapTranslate.put("NA21113", "NA21113 NA21113 0 0 1 0");
        hapMapTranslate.put("NA21115", "NA21115 NA21115 0 0 1 0");
        hapMapTranslate.put("NA21116", "NA21116 NA21116 0 0 1 0");
        hapMapTranslate.put("NA21117", "NA21117 NA21117 0 0 1 0");
        hapMapTranslate.put("NA21118", "NA21118 NA21118 0 0 1 0");
        hapMapTranslate.put("NA21119", "NA21119 NA21119 0 0 1 0");
        hapMapTranslate.put("NA21121", "NA21121 NA21121 0 0 2 0");
        hapMapTranslate.put("NA21123", "NA21123 NA21123 0 0 1 0");
        hapMapTranslate.put("NA21125", "NA21125 NA21125 0 0 2 0");
        hapMapTranslate.put("NA21137", "NA21137 NA21137 0 0 2 0");
        hapMapTranslate.put("NA21141", "NA21141 NA21141 0 0 2 0");
        hapMapTranslate.put("NA21142", "NA21142 NA21142 0 0 2 0");
        hapMapTranslate.put("NA21143", "NA21143 NA21143 0 0 2 0");
        hapMapTranslate.put("NA21144", "NA21144 NA21144 0 0 2 0");
        hapMapTranslate.put("NA20878", "NA20878 NA20878 0 0 2 0");
        hapMapTranslate.put("NA20893", "NA20893 NA20893 0 0 2 0");
        hapMapTranslate.put("NA21110", "NA21110 NA21110 0 0 2 0");
        hapMapTranslate.put("NA21120", "NA21120 NA21120 0 0 2 0");
        hapMapTranslate.put("NA21122", "NA21122 NA21122 0 0 2 0");
        hapMapTranslate.put("NA21127", "NA21127 NA21127 0 0 1 0");
        hapMapTranslate.put("NA21128", "NA21128 NA21128 0 0 1 0");
        hapMapTranslate.put("NA21130", "NA21130 NA21130 0 0 1 0");
        hapMapTranslate.put("NA21133", "NA21133 NA21133 0 0 1 0");
        hapMapTranslate.put("NA21135", "NA21135 NA21135 0 0 1 0");
        hapMapTranslate.put("NA18555", "NA18555 NA18555 0 0 2 0");
        hapMapTranslate.put("NA18524", "NA18524 NA18524 0 0 1 0");
        hapMapTranslate.put("NA18526", "NA18526 NA18526 0 0 2 0");
        hapMapTranslate.put("NA18529", "NA18529 NA18529 0 0 2 0");
        hapMapTranslate.put("NA18532", "NA18532 NA18532 0 0 2 0");
        hapMapTranslate.put("NA18537", "NA18537 NA18537 0 0 2 0");
        hapMapTranslate.put("NA18540", "NA18540 NA18540 0 0 2 0");
        hapMapTranslate.put("NA18542", "NA18542 NA18542 0 0 2 0");
        hapMapTranslate.put("NA18545", "NA18545 NA18545 0 0 2 0");
        hapMapTranslate.put("NA18547", "NA18547 NA18547 0 0 2 0");
        hapMapTranslate.put("NA18550", "NA18550 NA18550 0 0 2 0");
        hapMapTranslate.put("NA18552", "NA18552 NA18552 0 0 2 0");
        hapMapTranslate.put("NA18558", "NA18558 NA18558 0 0 1 0");
        hapMapTranslate.put("NA18561", "NA18561 NA18561 0 0 1 0");
        hapMapTranslate.put("NA18562", "NA18562 NA18562 0 0 1 0");
        hapMapTranslate.put("NA18563", "NA18563 NA18563 0 0 1 0");
        hapMapTranslate.put("NA18564", "NA18564 NA18564 0 0 2 0");
        hapMapTranslate.put("NA18566", "NA18566 NA18566 0 0 2 0");
        hapMapTranslate.put("NA18570", "NA18570 NA18570 0 0 2 0");
        hapMapTranslate.put("NA18571", "NA18571 NA18571 0 0 2 0");
        hapMapTranslate.put("NA18572", "NA18572 NA18572 0 0 1 0");
        hapMapTranslate.put("NA18573", "NA18573 NA18573 0 0 2 0");
        hapMapTranslate.put("NA18576", "NA18576 NA18576 0 0 2 0");
        hapMapTranslate.put("NA18577", "NA18577 NA18577 0 0 2 0");
        hapMapTranslate.put("NA18579", "NA18579 NA18579 0 0 2 0");
        hapMapTranslate.put("NA18582", "NA18582 NA18582 0 0 2 0");
        hapMapTranslate.put("NA18592", "NA18592 NA18592 0 0 2 0");
        hapMapTranslate.put("NA18593", "NA18593 NA18593 0 0 2 0");
        hapMapTranslate.put("NA18594", "NA18594 NA18594 0 0 2 0");
        hapMapTranslate.put("NA18603", "NA18603 NA18603 0 0 1 0");
        hapMapTranslate.put("NA18605", "NA18605 NA18605 0 0 1 0");
        hapMapTranslate.put("NA18608", "NA18608 NA18608 0 0 1 0");
        hapMapTranslate.put("NA18609", "NA18609 NA18609 0 0 1 0");
        hapMapTranslate.put("NA18611", "NA18611 NA18611 0 0 1 0");
        hapMapTranslate.put("NA18612", "NA18612 NA18612 0 0 1 0");
        hapMapTranslate.put("NA18620", "NA18620 NA18620 0 0 1 0");
        hapMapTranslate.put("NA18621", "NA18621 NA18621 0 0 1 0");
        hapMapTranslate.put("NA18622", "NA18622 NA18622 0 0 1 0");
        hapMapTranslate.put("NA18623", "NA18623 NA18623 0 0 1 0");
        hapMapTranslate.put("NA18624", "NA18624 NA18624 0 0 1 0");
        hapMapTranslate.put("NA18632", "NA18632 NA18632 0 0 1 0");
        hapMapTranslate.put("NA18633", "NA18633 NA18633 0 0 1 0");
        hapMapTranslate.put("NA18635", "NA18635 NA18635 0 0 1 0");
        hapMapTranslate.put("NA18636", "NA18636 NA18636 0 0 1 0");
        hapMapTranslate.put("NA18637", "NA18637 NA18637 0 0 1 0");
        hapMapTranslate.put("NA18530", "NA18530 NA18530 0 0 1 0");
        hapMapTranslate.put("NA18534", "NA18534 NA18534 0 0 1 0");
        hapMapTranslate.put("NA18536", "NA18536 NA18536 0 0 1 0");
        hapMapTranslate.put("NA18543", "NA18543 NA18543 0 0 1 0");
        hapMapTranslate.put("NA18544", "NA18544 NA18544 0 0 1 0");
        hapMapTranslate.put("NA18546", "NA18546 NA18546 0 0 1 0");
        hapMapTranslate.put("NA18548", "NA18548 NA18548 0 0 1 0");
        hapMapTranslate.put("NA18549", "NA18549 NA18549 0 0 1 0");
        hapMapTranslate.put("NA18557", "NA18557 NA18557 0 0 1 0");
        hapMapTranslate.put("NA18559", "NA18559 NA18559 0 0 1 0");
        hapMapTranslate.put("NA18595", "NA18595 NA18595 0 0 2 0");
        hapMapTranslate.put("NA18596", "NA18596 NA18596 0 0 2 0");
        hapMapTranslate.put("NA18597", "NA18597 NA18597 0 0 2 0");
        hapMapTranslate.put("NA18599", "NA18599 NA18599 0 0 2 0");
        hapMapTranslate.put("NA18602", "NA18602 NA18602 0 0 2 0");
        hapMapTranslate.put("NA18606", "NA18606 NA18606 0 0 1 0");
        hapMapTranslate.put("NA18610", "NA18610 NA18610 0 0 2 0");
        hapMapTranslate.put("NA18613", "NA18613 NA18613 0 0 1 0");
        hapMapTranslate.put("NA18614", "NA18614 NA18614 0 0 2 0");
        hapMapTranslate.put("NA18615", "NA18615 NA18615 0 0 2 0");
        hapMapTranslate.put("NA18616", "NA18616 NA18616 0 0 2 0");
        hapMapTranslate.put("NA18617", "NA18617 NA18617 0 0 2 0");
        hapMapTranslate.put("NA18618", "NA18618 NA18618 0 0 2 0");
        hapMapTranslate.put("NA18619", "NA18619 NA18619 0 0 2 0");
        hapMapTranslate.put("NA18625", "NA18625 NA18625 0 0 2 0");
        hapMapTranslate.put("NA18626", "NA18626 NA18626 0 0 2 0");
        hapMapTranslate.put("NA18627", "NA18627 NA18627 0 0 2 0");
        hapMapTranslate.put("NA18628", "NA18628 NA18628 0 0 2 0");
        hapMapTranslate.put("NA18630", "NA18630 NA18630 0 0 2 0");
        hapMapTranslate.put("NA18631", "NA18631 NA18631 0 0 2 0");
        hapMapTranslate.put("NA18634", "NA18634 NA18634 0 0 2 0");
        hapMapTranslate.put("NA18638", "NA18638 NA18638 0 0 1 0");
        hapMapTranslate.put("NA18639", "NA18639 NA18639 0 0 1 0");
        hapMapTranslate.put("NA18640", "NA18640 NA18640 0 0 2 0");
        hapMapTranslate.put("NA18641", "NA18641 NA18641 0 0 2 0");
        hapMapTranslate.put("NA18642", "NA18642 NA18642 0 0 2 0");
        hapMapTranslate.put("NA18643", "NA18643 NA18643 0 0 1 0");
        hapMapTranslate.put("NA18645", "NA18645 NA18645 0 0 1 0");
        hapMapTranslate.put("NA18647", "NA18647 NA18647 0 0 1 0");
        hapMapTranslate.put("NA18740", "NA18740 NA18740 0 0 1 0");
        hapMapTranslate.put("NA18745", "NA18745 NA18745 0 0 1 0");
        hapMapTranslate.put("NA18747", "NA18747 NA18747 0 0 1 0");
        hapMapTranslate.put("NA18748", "NA18748 NA18748 0 0 1 0");
        hapMapTranslate.put("NA18749", "NA18749 NA18749 0 0 1 0");
        hapMapTranslate.put("NA18757", "NA18757 NA18757 0 0 1 0");
        hapMapTranslate.put("NA18956", "NA18956 NA18956 0 0 2 0");
        hapMapTranslate.put("NA18940", "NA18940 NA18940 0 0 1 0");
        hapMapTranslate.put("NA18942", "NA18942 NA18942 0 0 2 0");
        hapMapTranslate.put("NA18943", "NA18943 NA18943 0 0 1 0");
        hapMapTranslate.put("NA18944", "NA18944 NA18944 0 0 1 0");
        hapMapTranslate.put("NA18945", "NA18945 NA18945 0 0 1 0");
        hapMapTranslate.put("NA18947", "NA18947 NA18947 0 0 2 0");
        hapMapTranslate.put("NA18948", "NA18948 NA18948 0 0 1 0");
        hapMapTranslate.put("NA18949", "NA18949 NA18949 0 0 2 0");
        hapMapTranslate.put("NA18951", "NA18951 NA18951 0 0 2 0");
        hapMapTranslate.put("NA18952", "NA18952 NA18952 0 0 1 0");
        hapMapTranslate.put("NA18953", "NA18953 NA18953 0 0 1 0");
        hapMapTranslate.put("NA18959", "NA18959 NA18959 0 0 1 0");
        hapMapTranslate.put("NA18960", "NA18960 NA18960 0 0 1 0");
        hapMapTranslate.put("NA18961", "NA18961 NA18961 0 0 1 0");
        hapMapTranslate.put("NA18964", "NA18964 NA18964 0 0 2 0");
        hapMapTranslate.put("NA18965", "NA18965 NA18965 0 0 1 0");
        hapMapTranslate.put("NA18966", "NA18966 NA18966 0 0 1 0");
        hapMapTranslate.put("NA18967", "NA18967 NA18967 0 0 1 0");
        hapMapTranslate.put("NA18968", "NA18968 NA18968 0 0 2 0");
        hapMapTranslate.put("NA18969", "NA18969 NA18969 0 0 2 0");
        hapMapTranslate.put("NA18970", "NA18970 NA18970 0 0 1 0");
        hapMapTranslate.put("NA18971", "NA18971 NA18971 0 0 1 0");
        hapMapTranslate.put("NA18972", "NA18972 NA18972 0 0 2 0");
        hapMapTranslate.put("NA18973", "NA18973 NA18973 0 0 2 0");
        hapMapTranslate.put("NA18974", "NA18974 NA18974 0 0 1 0");
        hapMapTranslate.put("NA18975", "NA18975 NA18975 0 0 2 0");
        hapMapTranslate.put("NA18976", "NA18976 NA18976 0 0 2 0");
        hapMapTranslate.put("NA18978", "NA18978 NA18978 0 0 2 0");
        hapMapTranslate.put("NA18980", "NA18980 NA18980 0 0 2 0");
        hapMapTranslate.put("NA18981", "NA18981 NA18981 0 0 2 0");
        hapMapTranslate.put("NA18987", "NA18987 NA18987 0 0 2 0");
        hapMapTranslate.put("NA18990", "NA18990 NA18990 0 0 1 0");
        hapMapTranslate.put("NA18991", "NA18991 NA18991 0 0 2 0");
        hapMapTranslate.put("NA18992", "NA18992 NA18992 0 0 2 0");
        hapMapTranslate.put("NA18994", "NA18994 NA18994 0 0 1 0");
        hapMapTranslate.put("NA18995", "NA18995 NA18995 0 0 1 0");
        hapMapTranslate.put("NA18997", "NA18997 NA18997 0 0 2 0");
        hapMapTranslate.put("NA18998", "NA18998 NA18998 0 0 2 0");
        hapMapTranslate.put("NA18999", "NA18999 NA18999 0 0 2 0");
        hapMapTranslate.put("NA19000", "NA19000 NA19000 0 0 1 0");
        hapMapTranslate.put("NA19003", "NA19003 NA19003 0 0 2 0");
        hapMapTranslate.put("NA19005", "NA19005 NA19005 0 0 1 0");
        hapMapTranslate.put("NA19007", "NA19007 NA19007 0 0 1 0");
        hapMapTranslate.put("NA19012", "NA19012 NA19012 0 0 1 0");
        hapMapTranslate.put("NA18939", "NA18939 NA18939 0 0 2 0");
        hapMapTranslate.put("NA18941", "NA18941 NA18941 0 0 2 0");
        hapMapTranslate.put("NA18946", "NA18946 NA18946 0 0 2 0");
        hapMapTranslate.put("NA18954", "NA18954 NA18954 0 0 2 0");
        hapMapTranslate.put("NA18955", "NA18955 NA18955 0 0 1 0");
        hapMapTranslate.put("NA18957", "NA18957 NA18957 0 0 2 0");
        hapMapTranslate.put("NA18962", "NA18962 NA18962 0 0 1 0");
        hapMapTranslate.put("NA18963", "NA18963 NA18963 0 0 2 0");
        hapMapTranslate.put("NA18977", "NA18977 NA18977 0 0 1 0");
        hapMapTranslate.put("NA18979", "NA18979 NA18979 0 0 2 0");
        hapMapTranslate.put("NA18993", "NA18993 NA18993 0 0 2 0");
        hapMapTranslate.put("NA19001", "NA19001 NA19001 0 0 2 0");
        hapMapTranslate.put("NA19002", "NA19002 NA19002 0 0 2 0");
        hapMapTranslate.put("NA19009", "NA19009 NA19009 0 0 1 0");
        hapMapTranslate.put("NA19010", "NA19010 NA19010 0 0 2 0");
        hapMapTranslate.put("NA19054", "NA19054 NA19054 0 0 2 0");
        hapMapTranslate.put("NA19055", "NA19055 NA19055 0 0 1 0");
        hapMapTranslate.put("NA19056", "NA19056 NA19056 0 0 1 0");
        hapMapTranslate.put("NA19057", "NA19057 NA19057 0 0 2 0");
        hapMapTranslate.put("NA19058", "NA19058 NA19058 0 0 1 0");
        hapMapTranslate.put("NA19059", "NA19059 NA19059 0 0 2 0");
        hapMapTranslate.put("NA19060", "NA19060 NA19060 0 0 1 0");
        hapMapTranslate.put("NA19062", "NA19062 NA19062 0 0 1 0");
        hapMapTranslate.put("NA19063", "NA19063 NA19063 0 0 1 0");
        hapMapTranslate.put("NA19064", "NA19064 NA19064 0 0 2 0");
        hapMapTranslate.put("NA19065", "NA19065 NA19065 0 0 2 0");
        hapMapTranslate.put("NA19066", "NA19066 NA19066 0 0 1 0");
        hapMapTranslate.put("NA19067", "NA19067 NA19067 0 0 1 0");
        hapMapTranslate.put("NA19068", "NA19068 NA19068 0 0 1 0");
        hapMapTranslate.put("NA19070", "NA19070 NA19070 0 0 1 0");
        hapMapTranslate.put("NA19072", "NA19072 NA19072 0 0 1 0");
        hapMapTranslate.put("NA19074", "NA19074 NA19074 0 0 2 0");
        hapMapTranslate.put("NA19075", "NA19075 NA19075 0 0 1 0");
        hapMapTranslate.put("NA19076", "NA19076 NA19076 0 0 1 0");
        hapMapTranslate.put("NA19077", "NA19077 NA19077 0 0 2 0");
        hapMapTranslate.put("NA19078", "NA19078 NA19078 0 0 2 0");
        hapMapTranslate.put("NA19079", "NA19079 NA19079 0 0 1 0");
        hapMapTranslate.put("NA19080", "NA19080 NA19080 0 0 2 0");
        hapMapTranslate.put("NA19081", "NA19081 NA19081 0 0 2 0");
        hapMapTranslate.put("NA19082", "NA19082 NA19082 0 0 1 0");
        hapMapTranslate.put("NA19083", "NA19083 NA19083 0 0 1 0");
        hapMapTranslate.put("NA19084", "NA19084 NA19084 0 0 2 0");
        hapMapTranslate.put("NA19085", "NA19085 NA19085 0 0 1 0");
        hapMapTranslate.put("NA19086", "NA19086 NA19086 0 0 1 0");
        hapMapTranslate.put("NA19087", "NA19087 NA19087 0 0 2 0");
        hapMapTranslate.put("NA19088", "NA19088 NA19088 0 0 1 0");
        hapMapTranslate.put("NA19027", "NA19027 NA19027 0 0 1 0");
        hapMapTranslate.put("NA19028", "NA19028 NA19028 0 0 1 0");
        hapMapTranslate.put("NA19031", "NA19031 NA19031 0 0 1 0");
        hapMapTranslate.put("NA19035", "NA19035 NA19035 0 0 1 0");
        hapMapTranslate.put("NA19036", "NA19036 NA19036 0 0 2 0");
        hapMapTranslate.put("NA19038", "NA19038 NA19038 0 0 2 0");
        hapMapTranslate.put("NA19041", "NA19041 NA19041 0 0 1 0");
        hapMapTranslate.put("NA19044", "NA19044 NA19044 0 0 1 0");
        hapMapTranslate.put("NA19046", "NA19046 NA19046 0 0 1 0");
        hapMapTranslate.put("NA19307", "NA19307 NA19307 0 0 1 0");
        hapMapTranslate.put("NA19308", "NA19308 NA19308 0 0 1 0");
        hapMapTranslate.put("NA19309", "NA19309 NA19309 0 0 1 0");
        hapMapTranslate.put("NA19310", "NA19310 NA19310 0 0 2 0");
        hapMapTranslate.put("NA19311", "NA19311 NA19311 0 0 1 0");
        hapMapTranslate.put("NA19313", "NA19313 NA19313 0 0 2 0");
        hapMapTranslate.put("NA19314", "NA19314 NA19314 0 0 2 0");
        hapMapTranslate.put("NA19315", "NA19315 NA19315 0 0 2 0");
        hapMapTranslate.put("NA19316", "NA19316 NA19316 0 0 2 0");
        hapMapTranslate.put("NA19317", "NA19317 NA19317 0 0 1 0");
        hapMapTranslate.put("NA19318", "NA19318 NA19318 0 0 1 0");
        hapMapTranslate.put("NA19319", "NA19319 NA19319 0 0 1 0");
        hapMapTranslate.put("NA19321", "NA19321 NA19321 0 0 2 0");
        hapMapTranslate.put("NA19324", "NA19324 NA19324 0 0 2 0");
        hapMapTranslate.put("NA19327", "NA19327 NA19327 0 0 2 0");
        hapMapTranslate.put("NA19328", "NA19328 NA19328 0 0 2 0");
        hapMapTranslate.put("NA19332", "NA19332 NA19332 0 0 2 0");
        hapMapTranslate.put("NA19334", "NA19334 NA19334 0 0 1 0");
        hapMapTranslate.put("NA19346", "NA19346 NA19346 0 0 1 0");
        hapMapTranslate.put("NA19347", "NA19347 NA19347 0 0 1 0");
        hapMapTranslate.put("NA19350", "NA19350 NA19350 0 0 1 0");
        hapMapTranslate.put("NA19352", "NA19352 NA19352 0 0 1 0");
        hapMapTranslate.put("NA19359", "NA19359 NA19359 0 0 1 0");
        hapMapTranslate.put("NA19360", "NA19360 NA19360 0 0 1 0");
        hapMapTranslate.put("NA19371", "NA19371 NA19371 0 0 1 0");
        hapMapTranslate.put("NA19372", "NA19372 NA19372 0 0 1 0");
        hapMapTranslate.put("NA19373", "NA19373 NA19373 0 0 1 0");
        hapMapTranslate.put("NA19374", "NA19374 NA19374 0 0 1 0");
        hapMapTranslate.put("NA19375", "NA19375 NA19375 0 0 1 0");
        hapMapTranslate.put("NA19376", "NA19376 NA19376 0 0 1 0");
        hapMapTranslate.put("NA19377", "NA19377 NA19377 0 0 2 0");
        hapMapTranslate.put("NA19379", "NA19379 NA19379 0 0 2 0");
        hapMapTranslate.put("NA19380", "NA19380 NA19380 0 0 1 0");
        hapMapTranslate.put("NA19381", "NA19381 NA19381 0 0 2 0");
        hapMapTranslate.put("NA19382", "NA19382 NA19382 0 0 1 0");
        hapMapTranslate.put("NA19383", "NA19383 NA19383 0 0 1 0");
        hapMapTranslate.put("NA19384", "NA19384 NA19384 0 0 1 0");
        hapMapTranslate.put("NA19385", "NA19385 NA19385 0 0 1 0");
        hapMapTranslate.put("NA19390", "NA19390 NA19390 0 0 2 0");
        hapMapTranslate.put("NA19391", "NA19391 NA19391 0 0 2 0");
        hapMapTranslate.put("NA19393", "NA19393 NA19393 0 0 1 0");
        hapMapTranslate.put("NA19394", "NA19394 NA19394 0 0 1 0");
        hapMapTranslate.put("NA19396", "NA19396 NA19396 0 0 2 0");
        hapMapTranslate.put("NA19397", "NA19397 NA19397 0 0 1 0");
        hapMapTranslate.put("NA19398", "NA19398 NA19398 0 0 2 0");
        hapMapTranslate.put("NA19399", "NA19399 NA19399 0 0 2 0");
        hapMapTranslate.put("NA19403", "NA19403 NA19403 0 0 2 0");
        hapMapTranslate.put("NA19404", "NA19404 NA19404 0 0 2 0");
        hapMapTranslate.put("NA19428", "NA19428 NA19428 0 0 1 0");
        hapMapTranslate.put("NA19429", "NA19429 NA19429 0 0 1 0");
        hapMapTranslate.put("NA19430", "NA19430 NA19430 0 0 1 0");
        hapMapTranslate.put("NA19431", "NA19431 NA19431 0 0 2 0");
        hapMapTranslate.put("NA19434", "NA19434 NA19434 0 0 2 0");
        hapMapTranslate.put("NA19435", "NA19435 NA19435 0 0 2 0");
        hapMapTranslate.put("NA19436", "NA19436 NA19436 0 0 2 0");
        hapMapTranslate.put("NA19437", "NA19437 NA19437 0 0 2 0");
        hapMapTranslate.put("NA19438", "NA19438 NA19438 0 0 2 0");
        hapMapTranslate.put("NA19439", "NA19439 NA19439 0 0 2 0");
        hapMapTranslate.put("NA19440", "NA19440 NA19440 0 0 2 0");
        hapMapTranslate.put("NA19443", "NA19443 NA19443 0 0 1 0");
        hapMapTranslate.put("NA19444", "NA19444 NA19444 0 0 1 0");
        hapMapTranslate.put("NA19445", "NA19445 NA19445 0 0 2 0");
        hapMapTranslate.put("NA19446", "NA19446 NA19446 0 0 2 0");
        hapMapTranslate.put("NA19448", "NA19448 NA19448 0 0 1 0");
        hapMapTranslate.put("NA19449", "NA19449 NA19449 0 0 2 0");
        hapMapTranslate.put("NA19451", "NA19451 NA19451 0 0 1 0");
        hapMapTranslate.put("NA19452", "NA19452 NA19452 0 0 1 0");
        hapMapTranslate.put("NA19455", "NA19455 NA19455 0 0 1 0");
        hapMapTranslate.put("NA19456", "NA19456 NA19456 0 0 2 0");
        hapMapTranslate.put("NA19457", "NA19457 NA19457 0 0 2 0");
        hapMapTranslate.put("NA19462", "NA19462 NA19462 0 0 2 0");
        hapMapTranslate.put("NA19463", "NA19463 NA19463 0 0 2 0");
        hapMapTranslate.put("NA19466", "NA19466 NA19466 0 0 1 0");
        hapMapTranslate.put("NA19467", "NA19467 NA19467 0 0 2 0");
        hapMapTranslate.put("NA19468", "NA19468 NA19468 0 0 2 0");
        hapMapTranslate.put("NA19469", "NA19469 NA19469 0 0 2 0");
        hapMapTranslate.put("NA19470", "NA19470 NA19470 0 0 2 0");
        hapMapTranslate.put("NA19471", "NA19471 NA19471 0 0 2 0");
        hapMapTranslate.put("NA19472", "NA19472 NA19472 0 0 2 0");
        hapMapTranslate.put("NA19473", "NA19473 NA19473 0 0 2 0");
        hapMapTranslate.put("NA19474", "NA19474 NA19474 0 0 2 0");
        hapMapTranslate.put("NA19020", "NA19020 NA19020 0 0 1 0");
        hapMapTranslate.put("NA19312", "NA19312 NA19312 0 0 1 0");
        hapMapTranslate.put("NA19331", "NA19331 NA19331 0 0 1 0");
        hapMapTranslate.put("NA19338", "NA19338 NA19338 0 0 2 0");
        hapMapTranslate.put("NA19351", "NA19351 NA19351 0 0 2 0");
        hapMapTranslate.put("NA19355", "NA19355 NA19355 0 0 2 0");
        hapMapTranslate.put("NA19395", "NA19395 NA19395 0 0 2 0");
        hapMapTranslate.put("NA19401", "NA19401 NA19401 0 0 2 0");
        hapMapTranslate.put("NA19453", "NA19453 NA19453 0 0 1 0");
        hapMapTranslate.put("NA19461", "NA19461 NA19461 0 0 1 0");
        hapMapTranslate.put("NA21302", "2563 NA21302 NA21301 NA21303 2 0");
        hapMapTranslate.put("NA21301", "2563 NA21301 0 0 1 0");
        hapMapTranslate.put("NA21303", "2563 NA21303 0 0 2 0");
        hapMapTranslate.put("NA21309", "2565 NA21309 NA21307 NA21308 2 0");
        hapMapTranslate.put("NA21307", "2565 NA21307 0 0 1 0");
        hapMapTranslate.put("NA21308", "2565 NA21308 0 0 2 0");
        hapMapTranslate.put("NA21310", "2566 NA21310 NA21311 NA21363 1 0");
        hapMapTranslate.put("NA21311", "2566 NA21311 0 0 1 0");
        hapMapTranslate.put("NA21363", "2566 NA21363 0 0 2 0");
        hapMapTranslate.put("NA21313", "2567 NA21313 NA21312 NA21362 1 0");
        hapMapTranslate.put("NA21312", "2567 NA21312 0 0 1 0");
        hapMapTranslate.put("NA21362", "2567 NA21362 0 0 2 0");
        hapMapTranslate.put("NA21317", "2569 NA21317 NA21316 NA21580 1 0");
        hapMapTranslate.put("NA21316", "2569 NA21316 0 0 1 0");
        hapMapTranslate.put("NA21580", "2569 NA21580 0 0 2 0");
        hapMapTranslate.put("NA21366", "2575 NA21366 NA21344 NA21365 1 0");
        hapMapTranslate.put("NA21344", "2575 NA21344 0 0 1 0");
        hapMapTranslate.put("NA21365", "2575 NA21365 0 0 2 0");
        hapMapTranslate.put("NA21361", "2579 NA21361 NA21359 NA21360 2 0");
        hapMapTranslate.put("NA21359", "2579 NA21359 0 0 1 0");
        hapMapTranslate.put("NA21360", "2579 NA21360 0 0 2 0");
        hapMapTranslate.put("NA21383", "2583 NA21383 NA21381 NA21382 1 0");
        hapMapTranslate.put("NA21381", "2583 NA21381 0 0 1 0");
        hapMapTranslate.put("NA21382", "2583 NA21382 0 0 2 0");
        hapMapTranslate.put("NA21386", "2584 NA21386 NA21384 NA21385 2 0");
        hapMapTranslate.put("NA21384", "2584 NA21384 0 0 1 0");
        hapMapTranslate.put("NA21385", "2584 NA21385 0 0 2 0");
        hapMapTranslate.put("NA21389", "2585 NA21389 NA21387 NA21388 1 0");
        hapMapTranslate.put("NA21387", "2585 NA21387 0 0 1 0");
        hapMapTranslate.put("NA21388", "2585 NA21388 0 0 2 0");
        hapMapTranslate.put("NA21581", "2586 NA21581 NA21390 NA21391 1 0");
        hapMapTranslate.put("NA21390", "2586 NA21390 0 0 1 0");
        hapMapTranslate.put("NA21391", "2586 NA21391 0 0 2 0");
        hapMapTranslate.put("NA21401", "2587 NA21401 NA21399 NA21400 1 0");
        hapMapTranslate.put("NA21399", "2587 NA21399 0 0 1 0");
        hapMapTranslate.put("NA21400", "2587 NA21400 0 0 2 0");
        hapMapTranslate.put("NA21404", "2588 NA21404 NA21402 NA21403 2 0");
        hapMapTranslate.put("NA21402", "2588 NA21402 0 0 1 0");
        hapMapTranslate.put("NA21403", "2588 NA21403 0 0 2 0");
        hapMapTranslate.put("NA21425", "2596 NA21425 NA21423 NA21424 2 0");
        hapMapTranslate.put("NA21423", "2596 NA21423 0 0 1 0");
        hapMapTranslate.put("NA21424", "2596 NA21424 0 0 2 0");
        hapMapTranslate.put("NA21439", "2602 NA21439 NA21447 NA21438 1 0");
        hapMapTranslate.put("NA21447", "2602 NA21447 0 0 1 0");
        hapMapTranslate.put("NA21438", "2602 NA21438 0 0 2 0");
        hapMapTranslate.put("NA21442", "2603 NA21442 NA21440 NA21441 1 0");
        hapMapTranslate.put("NA21440", "2603 NA21440 0 0 1 0");
        hapMapTranslate.put("NA21441", "2603 NA21441 0 0 2 0");
        hapMapTranslate.put("NA21455", "2608 NA21455 NA21453 NA21454 2 0");
        hapMapTranslate.put("NA21453", "2608 NA21453 0 0 1 0");
        hapMapTranslate.put("NA21454", "2608 NA21454 0 0 2 0");
        hapMapTranslate.put("NA21477", "2614 NA21477 NA21475 NA21476 1 0");
        hapMapTranslate.put("NA21475", "2614 NA21475 0 0 1 0");
        hapMapTranslate.put("NA21476", "2614 NA21476 0 0 2 0");
        hapMapTranslate.put("NA21480", "2615 NA21480 NA21478 NA21479 2 0");
        hapMapTranslate.put("NA21478", "2615 NA21478 0 0 1 0");
        hapMapTranslate.put("NA21479", "2615 NA21479 0 0 2 0");
        hapMapTranslate.put("NA21487", "2618 NA21487 NA21485 NA21486 2 0");
        hapMapTranslate.put("NA21485", "2618 NA21485 0 0 1 0");
        hapMapTranslate.put("NA21486", "2618 NA21486 0 0 2 0");
        hapMapTranslate.put("NA21490", "2619 NA21490 NA21488 NA21489 1 0");
        hapMapTranslate.put("NA21488", "2619 NA21488 0 0 1 0");
        hapMapTranslate.put("NA21489", "2619 NA21489 0 0 2 0");
        hapMapTranslate.put("NA21494", "2621 NA21494 NA21522 NA21493 2 0");
        hapMapTranslate.put("NA21522", "2621 NA21522 0 0 1 0");
        hapMapTranslate.put("NA21493", "2621 NA21493 0 0 2 0");
        hapMapTranslate.put("NA21525", "2633 NA21525 NA21523 NA21524 1 0");
        hapMapTranslate.put("NA21523", "2633 NA21523 0 0 1 0");
        hapMapTranslate.put("NA21524", "2633 NA21524 0 0 2 0");
        hapMapTranslate.put("NA21527", "2634 NA21527 NA21583 NA21526 1 0");
        hapMapTranslate.put("NA21583", "2634 NA21583 0 0 1 0");
        hapMapTranslate.put("NA21526", "2634 NA21526 0 0 2 0");
        hapMapTranslate.put("NA21514", "2637 NA21514 NA21512 NA21513 1 0");
        hapMapTranslate.put("NA21512", "2637 NA21512 0 0 1 0");
        hapMapTranslate.put("NA21513", "2637 NA21513 0 0 2 0");
        hapMapTranslate.put("NA21601", "2664 NA21601 NA21599 NA21600 2 0");
        hapMapTranslate.put("NA21599", "2664 NA21599 0 0 1 0");
        hapMapTranslate.put("NA21600", "2664 NA21600 0 0 2 0");
        hapMapTranslate.put("NA21608", "2666 NA21608 NA21614 NA21615 1 0");
        hapMapTranslate.put("NA21614", "2666 NA21614 0 0 1 0");
        hapMapTranslate.put("NA21615", "2666 NA21615 0 0 2 0");
        hapMapTranslate.put("NA21636", "2674 NA21636 NA21634 NA21635 2 0");
        hapMapTranslate.put("NA21634", "2674 NA21634 0 0 1 0");
        hapMapTranslate.put("NA21635", "2674 NA21635 0 0 2 0");
        hapMapTranslate.put("NA21648", "2677 NA21648 NA21647 NA21686 1 0");
        hapMapTranslate.put("NA21647", "2677 NA21647 0 0 1 0");
        hapMapTranslate.put("NA21686", "2677 NA21686 0 0 2 0");
        hapMapTranslate.put("NA21718", "2699 NA21718 NA21716 NA21717 1 0");
        hapMapTranslate.put("NA21716", "2699 NA21716 0 0 1 0");
        hapMapTranslate.put("NA21717", "2699 NA21717 0 0 2 0");
        hapMapTranslate.put("NA21295", "2560 NA21295 0 0 1 0");
        hapMapTranslate.put("NA21333", "2560 NA21333 0 0 2 0");
        hapMapTranslate.put("NA21297", "2561 NA21297 0 0 2 0");
        hapMapTranslate.put("NA21300", "2562 NA21300 0 0 2 0");
        hapMapTranslate.put("NA21306", "2564 NA21306 0 0 2 0");
        hapMapTranslate.put("NA21314", "2568 NA21314 0 0 1 0");
        hapMapTranslate.put("NA21364", "2568 NA21364 0 0 2 0");
        hapMapTranslate.put("NA21318", "2570 NA21318 0 0 1 0");
        hapMapTranslate.put("NA21685", "2571 NA21685 0 0 1 0");
        hapMapTranslate.put("NA21320", "2571 NA21320 0 0 2 0");
        hapMapTranslate.put("NA21336", "2572 NA21336 0 0 2 0");
        hapMapTranslate.put("NA21339", "2573 NA21339 0 0 2 0");
        hapMapTranslate.put("NA21352", "2576 NA21352 0 0 1 0");
        hapMapTranslate.put("NA21353", "2576 NA21353 0 0 2 0");
        hapMapTranslate.put("NA21355", "2577 NA21355 0 0 1 0");
        hapMapTranslate.put("NA21356", "2578 NA21356 0 0 1 0");
        hapMapTranslate.put("NA21357", "2578 NA21357 0 0 2 0");
        hapMapTranslate.put("NA21367", "2580 NA21367 0 0 1 0");
        hapMapTranslate.put("NA21368", "2580 NA21368 0 0 2 0");
        hapMapTranslate.put("NA21370", "2581 NA21370 0 0 1 0");
        hapMapTranslate.put("NA21371", "2581 NA21371 0 0 2 0");
        hapMapTranslate.put("NA21378", "2582 NA21378 0 0 1 0");
        hapMapTranslate.put("NA21379", "2582 NA21379 0 0 2 0");
        hapMapTranslate.put("NA21405", "2589 NA21405 0 0 1 0");
        hapMapTranslate.put("NA21408", "2590 NA21408 0 0 1 0");
        hapMapTranslate.put("NA21414", "2593 NA21414 0 0 1 0");
        hapMapTranslate.put("NA21415", "2593 NA21415 0 0 2 0");
        hapMapTranslate.put("NA21417", "2594 NA21417 0 0 1 0");
        hapMapTranslate.put("NA21418", "2594 NA21418 0 0 2 0");
        hapMapTranslate.put("NA21420", "2595 NA21420 0 0 1 0");
        hapMapTranslate.put("NA21421", "2595 NA21421 0 0 2 0");
        hapMapTranslate.put("NA21434", "2600 NA21434 0 0 2 0");
        hapMapTranslate.put("NA21435", "2601 NA21435 0 0 1 0");
        hapMapTranslate.put("NA21436", "2601 NA21436 0 0 2 0");
        hapMapTranslate.put("NA21443", "2604 NA21443 0 0 1 0");
        hapMapTranslate.put("NA21448", "2606 NA21448 0 0 1 0");
        hapMapTranslate.put("NA21451", "2607 NA21451 0 0 2 0");
        hapMapTranslate.put("NA21457", "2609 NA21457 0 0 2 0");
        hapMapTranslate.put("NA21519", "2613 NA21519 0 0 1 0");
        hapMapTranslate.put("NA21473", "2613 NA21473 0 0 2 0");
        hapMapTranslate.put("NA21582", "2620 NA21582 0 0 1 0");
        hapMapTranslate.put("NA21491", "2620 NA21491 0 0 2 0");
        hapMapTranslate.put("NA21520", "2629 NA21520 0 0 1 0");
        hapMapTranslate.put("NA21521", "2632 NA21521 0 0 1 0");
        hapMapTranslate.put("NA21528", "2635 NA21528 0 0 1 0");
        hapMapTranslate.put("NA21529", "2635 NA21529 0 0 2 0");
        hapMapTranslate.put("NA21509", "2636 NA21509 0 0 1 0");
        hapMapTranslate.put("NA21510", "2636 NA21510 0 0 2 0");
        hapMapTranslate.put("NA21613", "2638 NA21613 0 0 2 0");
        hapMapTranslate.put("NA21517", "2639 NA21517 0 0 2 0");
        hapMapTranslate.put("NA21573", "2653 NA21573 0 0 1 0");
        hapMapTranslate.put("NA21574", "2653 NA21574 0 0 2 0");
        hapMapTranslate.put("NA21575", "2654 NA21575 0 0 1 0");
        hapMapTranslate.put("NA21576", "2654 NA21576 0 0 2 0");
        hapMapTranslate.put("NA21577", "2655 NA21577 0 0 1 0");
        hapMapTranslate.put("NA21578", "2655 NA21578 0 0 2 0");
        hapMapTranslate.put("NA21587", "2657 NA21587 0 0 1 0");
        hapMapTranslate.put("NA21596", "2663 NA21596 0 0 1 0");
        hapMapTranslate.put("NA21597", "2663 NA21597 0 0 2 0");
        hapMapTranslate.put("NA21616", "2667 NA21616 0 0 1 0");
        hapMapTranslate.put("NA21617", "2667 NA21617 0 0 2 0");
        hapMapTranslate.put("NA21619", "2668 NA21619 0 0 1 0");
        hapMapTranslate.put("NA21620", "2668 NA21620 0 0 2 0");
        hapMapTranslate.put("NA21611", "2670 NA21611 0 0 2 0");
        hapMapTranslate.put("NA21631", "2673 NA21631 0 0 1 0");
        hapMapTranslate.put("NA21632", "2673 NA21632 0 0 2 0");
        hapMapTranslate.put("NA21649", "2678 NA21649 0 0 1 0");
        hapMapTranslate.put("NA21650", "2678 NA21650 0 0 2 0");
        hapMapTranslate.put("NA21678", "2689 NA21678 0 0 1 0");
        hapMapTranslate.put("NA21682", "2690 NA21682 0 0 1 0");
        hapMapTranslate.put("NA21683", "2690 NA21683 0 0 2 0");
        hapMapTranslate.put("NA21689", "2691 NA21689 0 0 1 0");
        hapMapTranslate.put("NA21693", "2692 NA21693 0 0 2 0");
        hapMapTranslate.put("NA21719", "2700 NA21719 0 0 1 0");
        hapMapTranslate.put("NA21722", "2701 NA21722 0 0 1 0");
        hapMapTranslate.put("NA21723", "2701 NA21723 0 0 2 0");
        hapMapTranslate.put("NA21733", "NA21733 NA21733 0 0 2 0");
        hapMapTranslate.put("NA21768", "NA21768 NA21768 0 0 2 0");
        hapMapTranslate.put("NA21774", "NA21774 NA21774 0 0 2 0");
        hapMapTranslate.put("NA21776", "NA21776 NA21776 0 0 2 0");
        hapMapTranslate.put("NA21784", "NA21784 NA21784 0 0 2 0");
        hapMapTranslate.put("NA21825", "NA21825 NA21825 0 0 2 0");
        hapMapTranslate.put("NA21826", "NA21826 NA21826 0 0 2 0");
        hapMapTranslate.put("NA21738", "NA21738 NA21738 0 0 1 0");
        hapMapTranslate.put("NA21739", "NA21739 NA21739 0 0 1 0");
        hapMapTranslate.put("NA21740", "NA21740 NA21740 0 0 1 0");
        hapMapTranslate.put("NA21741", "NA21741 NA21741 0 0 1 0");
        hapMapTranslate.put("NA21742", "NA21742 NA21742 0 0 1 0");
        hapMapTranslate.put("NA21743", "NA21743 NA21743 0 0 1 0");
        hapMapTranslate.put("NA21744", "NA21744 NA21744 0 0 1 0");
        hapMapTranslate.put("NA19650", "M001 NA19650 NA19649 NA19648 1 0");
        hapMapTranslate.put("NA19649", "M001 NA19649 0 0 1 0");
        hapMapTranslate.put("NA19648", "M001 NA19648 0 0 2 0");
        hapMapTranslate.put("NA19671", "M002 NA19671 NA19670 NA19669 2 0");
        hapMapTranslate.put("NA19670", "M002 NA19670 0 0 1 0");
        hapMapTranslate.put("NA19669", "M002 NA19669 0 0 2 0");
        hapMapTranslate.put("NA19677", "M004 NA19677 NA19676 NA19675 2 0");
        hapMapTranslate.put("NA19676", "M004 NA19676 0 0 1 0");
        hapMapTranslate.put("NA19675", "M004 NA19675 0 0 2 0");
        hapMapTranslate.put("NA19653", "M005 NA19653 NA19652 NA19651 2 0");
        hapMapTranslate.put("NA19652", "M005 NA19652 0 0 1 0");
        hapMapTranslate.put("NA19651", "M005 NA19651 0 0 2 0");
        hapMapTranslate.put("NA19656", "M006 NA19656 NA19655 NA19654 2 0");
        hapMapTranslate.put("NA19655", "M006 NA19655 0 0 1 0");
        hapMapTranslate.put("NA19654", "M006 NA19654 0 0 2 0");
        hapMapTranslate.put("NA19659", "M007 NA19659 NA19658 NA19657 2 0");
        hapMapTranslate.put("NA19658", "M007 NA19658 0 0 1 0");
        hapMapTranslate.put("NA19657", "M007 NA19657 0 0 2 0");
        hapMapTranslate.put("NA19662", "M008 NA19662 NA19661 NA19660 2 0");
        hapMapTranslate.put("NA19661", "M008 NA19661 0 0 1 0");
        hapMapTranslate.put("NA19660", "M008 NA19660 0 0 2 0");
        hapMapTranslate.put("NA19680", "M009 NA19680 NA19679 NA19678 2 0");
        hapMapTranslate.put("NA19679", "M009 NA19679 0 0 1 0");
        hapMapTranslate.put("NA19678", "M009 NA19678 0 0 2 0");
        hapMapTranslate.put("NA19683", "M010 NA19683 NA19682 NA19681 2 0");
        hapMapTranslate.put("NA19682", "M010 NA19682 0 0 1 0");
        hapMapTranslate.put("NA19681", "M010 NA19681 0 0 2 0");
        hapMapTranslate.put("NA19686", "M011 NA19686 NA19685 NA19684 2 0");
        hapMapTranslate.put("NA19685", "M011 NA19685 0 0 1 0");
        hapMapTranslate.put("NA19684", "M011 NA19684 0 0 2 0");
        hapMapTranslate.put("NA19665", "M012 NA19665 NA19664 NA19663 2 0");
        hapMapTranslate.put("NA19664", "M012 NA19664 0 0 1 0");
        hapMapTranslate.put("NA19663", "M012 NA19663 0 0 2 0");
        hapMapTranslate.put("NA19718", "M014 NA19718 NA19717 NA19716 2 0");
        hapMapTranslate.put("NA19717", "M014 NA19717 0 0 1 0");
        hapMapTranslate.put("NA19716", "M014 NA19716 0 0 2 0");
        hapMapTranslate.put("NA19721", "M015 NA19721 NA19720 NA19719 2 0");
        hapMapTranslate.put("NA19720", "M015 NA19720 0 0 1 0");
        hapMapTranslate.put("NA19719", "M015 NA19719 0 0 2 0");
        hapMapTranslate.put("NA19724", "M016 NA19724 NA19723 NA19722 1 0");
        hapMapTranslate.put("NA19723", "M016 NA19723 0 0 1 0");
        hapMapTranslate.put("NA19722", "M016 NA19722 0 0 2 0");
        hapMapTranslate.put("NA19727", "M017 NA19727 NA19726 NA19725 1 0");
        hapMapTranslate.put("NA19726", "M017 NA19726 0 0 1 0");
        hapMapTranslate.put("NA19725", "M017 NA19725 0 0 2 0");
        hapMapTranslate.put("NA19730", "M018 NA19730 NA19729 NA19728 2 0");
        hapMapTranslate.put("NA19729", "M018 NA19729 0 0 1 0");
        hapMapTranslate.put("NA19728", "M018 NA19728 0 0 2 0");
        hapMapTranslate.put("NA19733", "M019 NA19733 NA19732 NA19731 2 0");
        hapMapTranslate.put("NA19732", "M019 NA19732 0 0 1 0");
        hapMapTranslate.put("NA19731", "M019 NA19731 0 0 2 0");
        hapMapTranslate.put("NA19748", "M023 NA19748 NA19747 NA19746 2 0");
        hapMapTranslate.put("NA19747", "M023 NA19747 0 0 1 0");
        hapMapTranslate.put("NA19746", "M023 NA19746 0 0 2 0");
        hapMapTranslate.put("NA19751", "M024 NA19751 NA19750 NA19749 1 0");
        hapMapTranslate.put("NA19750", "M024 NA19750 0 0 1 0");
        hapMapTranslate.put("NA19749", "M024 NA19749 0 0 2 0");
        hapMapTranslate.put("NA19757", "M026 NA19757 NA19756 NA19755 1 0");
        hapMapTranslate.put("NA19756", "M026 NA19756 0 0 1 0");
        hapMapTranslate.put("NA19755", "M026 NA19755 0 0 2 0");
        hapMapTranslate.put("NA19760", "M027 NA19760 NA19759 NA19758 2 0");
        hapMapTranslate.put("NA19759", "M027 NA19759 0 0 1 0");
        hapMapTranslate.put("NA19758", "M027 NA19758 0 0 2 0");
        hapMapTranslate.put("NA19763", "M028 NA19763 NA19762 NA19761 2 0");
        hapMapTranslate.put("NA19762", "M028 NA19762 0 0 1 0");
        hapMapTranslate.put("NA19761", "M028 NA19761 0 0 2 0");
        hapMapTranslate.put("NA19772", "M031 NA19772 NA19771 NA19770 1 0");
        hapMapTranslate.put("NA19771", "M031 NA19771 0 0 1 0");
        hapMapTranslate.put("NA19770", "M031 NA19770 0 0 2 0");
        hapMapTranslate.put("NA19787", "M032 NA19787 NA19786 NA19785 1 0");
        hapMapTranslate.put("NA19786", "M032 NA19786 0 0 1 0");
        hapMapTranslate.put("NA19785", "M032 NA19785 0 0 2 0");
        hapMapTranslate.put("NA19775", "M033 NA19775 NA19774 NA19773 2 0");
        hapMapTranslate.put("NA19774", "M033 NA19774 0 0 1 0");
        hapMapTranslate.put("NA19773", "M033 NA19773 0 0 2 0");
        hapMapTranslate.put("NA19778", "M034 NA19778 NA19777 NA19776 1 0");
        hapMapTranslate.put("NA19777", "M034 NA19777 0 0 1 0");
        hapMapTranslate.put("NA19776", "M034 NA19776 0 0 2 0");
        hapMapTranslate.put("NA19781", "M035 NA19781 NA19780 NA19779 2 0");
        hapMapTranslate.put("NA19780", "M035 NA19780 0 0 1 0");
        hapMapTranslate.put("NA19779", "M035 NA19779 0 0 2 0");
        hapMapTranslate.put("NA19784", "M036 NA19784 NA19783 NA19782 1 0");
        hapMapTranslate.put("NA19783", "M036 NA19783 0 0 1 0");
        hapMapTranslate.put("NA19782", "M036 NA19782 0 0 2 0");
        hapMapTranslate.put("NA19790", "M037 NA19790 NA19789 NA19788 2 0");
        hapMapTranslate.put("NA19789", "M037 NA19789 0 0 1 0");
        hapMapTranslate.put("NA19788", "M037 NA19788 0 0 2 0");
        hapMapTranslate.put("NA19796", "M039 NA19796 NA19795 NA19794 1 0");
        hapMapTranslate.put("NA19795", "M039 NA19795 0 0 1 0");
        hapMapTranslate.put("NA19794", "M039 NA19794 0 0 2 0");
        hapMapTranslate.put("NA20502", "NA20502 NA20502 0 0 2 0");
        hapMapTranslate.put("NA20504", "NA20504 NA20504 0 0 2 0");
        hapMapTranslate.put("NA20505", "NA20505 NA20505 0 0 2 0");
        hapMapTranslate.put("NA20506", "NA20506 NA20506 0 0 2 0");
        hapMapTranslate.put("NA20508", "NA20508 NA20508 0 0 2 0");
        hapMapTranslate.put("NA20509", "NA20509 NA20509 0 0 1 0");
        hapMapTranslate.put("NA20510", "NA20510 NA20510 0 0 1 0");
        hapMapTranslate.put("NA20512", "NA20512 NA20512 0 0 1 0");
        hapMapTranslate.put("NA20515", "NA20515 NA20515 0 0 1 0");
        hapMapTranslate.put("NA20516", "NA20516 NA20516 0 0 1 0");
        hapMapTranslate.put("NA20517", "NA20517 NA20517 0 0 2 0");
        hapMapTranslate.put("NA20518", "NA20518 NA20518 0 0 1 0");
        hapMapTranslate.put("NA20519", "NA20519 NA20519 0 0 1 0");
        hapMapTranslate.put("NA20520", "NA20520 NA20520 0 0 1 0");
        hapMapTranslate.put("NA20521", "NA20521 NA20521 0 0 1 0");
        hapMapTranslate.put("NA20522", "NA20522 NA20522 0 0 2 0");
        hapMapTranslate.put("NA20524", "NA20524 NA20524 0 0 1 0");
        hapMapTranslate.put("NA20525", "NA20525 NA20525 0 0 1 0");
        hapMapTranslate.put("NA20527", "NA20527 NA20527 0 0 1 0");
        hapMapTranslate.put("NA20528", "NA20528 NA20528 0 0 1 0");
        hapMapTranslate.put("NA20529", "NA20529 NA20529 0 0 2 0");
        hapMapTranslate.put("NA20530", "NA20530 NA20530 0 0 2 0");
        hapMapTranslate.put("NA20531", "NA20531 NA20531 0 0 2 0");
        hapMapTranslate.put("NA20534", "NA20534 NA20534 0 0 1 0");
        hapMapTranslate.put("NA20535", "NA20535 NA20535 0 0 2 0");
        hapMapTranslate.put("NA20538", "NA20538 NA20538 0 0 1 0");
        hapMapTranslate.put("NA20539", "NA20539 NA20539 0 0 1 0");
        hapMapTranslate.put("NA20540", "NA20540 NA20540 0 0 2 0");
        hapMapTranslate.put("NA20541", "NA20541 NA20541 0 0 2 0");
        hapMapTranslate.put("NA20542", "NA20542 NA20542 0 0 2 0");
        hapMapTranslate.put("NA20543", "NA20543 NA20543 0 0 1 0");
        hapMapTranslate.put("NA20544", "NA20544 NA20544 0 0 1 0");
        hapMapTranslate.put("NA20581", "NA20581 NA20581 0 0 1 0");
        hapMapTranslate.put("NA20582", "NA20582 NA20582 0 0 2 0");
        hapMapTranslate.put("NA20585", "NA20585 NA20585 0 0 2 0");
        hapMapTranslate.put("NA20586", "NA20586 NA20586 0 0 1 0");
        hapMapTranslate.put("NA20588", "NA20588 NA20588 0 0 1 0");
        hapMapTranslate.put("NA20589", "NA20589 NA20589 0 0 2 0");
        hapMapTranslate.put("NA20752", "NA20752 NA20752 0 0 1 0");
        hapMapTranslate.put("NA20753", "NA20753 NA20753 0 0 2 0");
        hapMapTranslate.put("NA20754", "NA20754 NA20754 0 0 1 0");
        hapMapTranslate.put("NA20755", "NA20755 NA20755 0 0 1 0");
        hapMapTranslate.put("NA20756", "NA20756 NA20756 0 0 2 0");
        hapMapTranslate.put("NA20757", "NA20757 NA20757 0 0 2 0");
        hapMapTranslate.put("NA20758", "NA20758 NA20758 0 0 1 0");
        hapMapTranslate.put("NA20759", "NA20759 NA20759 0 0 1 0");
        hapMapTranslate.put("NA20760", "NA20760 NA20760 0 0 2 0");
        hapMapTranslate.put("NA20761", "NA20761 NA20761 0 0 2 0");
        hapMapTranslate.put("NA20765", "NA20765 NA20765 0 0 1 0");
        hapMapTranslate.put("NA20766", "NA20766 NA20766 0 0 2 0");
        hapMapTranslate.put("NA20768", "NA20768 NA20768 0 0 2 0");
        hapMapTranslate.put("NA20769", "NA20769 NA20769 0 0 2 0");
        hapMapTranslate.put("NA20770", "NA20770 NA20770 0 0 1 0");
        hapMapTranslate.put("NA20771", "NA20771 NA20771 0 0 2 0");
        hapMapTranslate.put("NA20772", "NA20772 NA20772 0 0 2 0");
        hapMapTranslate.put("NA20773", "NA20773 NA20773 0 0 2 0");
        hapMapTranslate.put("NA20774", "NA20774 NA20774 0 0 2 0");
        hapMapTranslate.put("NA20775", "NA20775 NA20775 0 0 2 0");
        hapMapTranslate.put("NA20778", "NA20778 NA20778 0 0 1 0");
        hapMapTranslate.put("NA20783", "NA20783 NA20783 0 0 1 0");
        hapMapTranslate.put("NA20785", "NA20785 NA20785 0 0 1 0");
        hapMapTranslate.put("NA20786", "NA20786 NA20786 0 0 2 0");
        hapMapTranslate.put("NA20787", "NA20787 NA20787 0 0 1 0");
        hapMapTranslate.put("NA20790", "NA20790 NA20790 0 0 2 0");
        hapMapTranslate.put("NA20792", "NA20792 NA20792 0 0 1 0");
        hapMapTranslate.put("NA20795", "NA20795 NA20795 0 0 2 0");
        hapMapTranslate.put("NA20796", "NA20796 NA20796 0 0 1 0");
        hapMapTranslate.put("NA20797", "NA20797 NA20797 0 0 2 0");
        hapMapTranslate.put("NA20798", "NA20798 NA20798 0 0 1 0");
        hapMapTranslate.put("NA20799", "NA20799 NA20799 0 0 2 0");
        hapMapTranslate.put("NA20800", "NA20800 NA20800 0 0 2 0");
        hapMapTranslate.put("NA20801", "NA20801 NA20801 0 0 1 0");
        hapMapTranslate.put("NA20802", "NA20802 NA20802 0 0 2 0");
        hapMapTranslate.put("NA20803", "NA20803 NA20803 0 0 1 0");
        hapMapTranslate.put("NA20804", "NA20804 NA20804 0 0 2 0");
        hapMapTranslate.put("NA20805", "NA20805 NA20805 0 0 1 0");
        hapMapTranslate.put("NA20806", "NA20806 NA20806 0 0 1 0");
        hapMapTranslate.put("NA20807", "NA20807 NA20807 0 0 2 0");
        hapMapTranslate.put("NA20808", "NA20808 NA20808 0 0 2 0");
        hapMapTranslate.put("NA20809", "NA20809 NA20809 0 0 1 0");
        hapMapTranslate.put("NA20810", "NA20810 NA20810 0 0 1 0");
        hapMapTranslate.put("NA20811", "NA20811 NA20811 0 0 1 0");
        hapMapTranslate.put("NA20812", "NA20812 NA20812 0 0 1 0");
        hapMapTranslate.put("NA20813", "NA20813 NA20813 0 0 2 0");
        hapMapTranslate.put("NA20815", "NA20815 NA20815 0 0 1 0");
        hapMapTranslate.put("NA20816", "NA20816 NA20816 0 0 1 0");
        hapMapTranslate.put("NA20818", "NA20818 NA20818 0 0 2 0");
        hapMapTranslate.put("NA20819", "NA20819 NA20819 0 0 2 0");
        hapMapTranslate.put("NA20826", "NA20826 NA20826 0 0 2 0");
        hapMapTranslate.put("NA20828", "NA20828 NA20828 0 0 2 0");
        hapMapTranslate.put("NA20503", "NA20503 NA20503 0 0 2 0");
        hapMapTranslate.put("NA20507", "NA20507 NA20507 0 0 2 0");
        hapMapTranslate.put("NA20513", "NA20513 NA20513 0 0 1 0");
        hapMapTranslate.put("NA20514", "NA20514 NA20514 0 0 2 0");
        hapMapTranslate.put("NA20526", "NA20526 NA20526 0 0 2 0");
        hapMapTranslate.put("NA20532", "NA20532 NA20532 0 0 1 0");
        hapMapTranslate.put("NA20533", "NA20533 NA20533 0 0 2 0");
        hapMapTranslate.put("NA20536", "NA20536 NA20536 0 0 1 0");
        hapMapTranslate.put("NA20537", "NA20537 NA20537 0 0 1 0");
        hapMapTranslate.put("NA20814", "NA20814 NA20814 0 0 1 0");
        hapMapTranslate.put("NA18500", "Y004 NA18500 NA18501 NA18502 1 0");
        hapMapTranslate.put("NA18501", "Y004 NA18501 0 0 1 0");
        hapMapTranslate.put("NA18502", "Y004 NA18502 0 0 2 0");
        hapMapTranslate.put("NA18503", "Y005 NA18503 NA18504 NA18505 1 0");
        hapMapTranslate.put("NA18504", "Y005 NA18504 0 0 1 0");
        hapMapTranslate.put("NA18505", "Y005 NA18505 0 0 2 0");
        hapMapTranslate.put("NA18506", "Y009 NA18506 NA18507 NA18508 1 0");
        hapMapTranslate.put("NA18507", "Y009 NA18507 0 0 1 0");
        hapMapTranslate.put("NA18508", "Y009 NA18508 0 0 2 0");
        hapMapTranslate.put("NA18860", "Y012 NA18860 NA18859 NA18858 1 0");
        hapMapTranslate.put("NA18859", "Y012 NA18859 0 0 1 0");
        hapMapTranslate.put("NA18858", "Y012 NA18858 0 0 2 0");
        hapMapTranslate.put("NA18515", "Y013 NA18515 NA18516 NA18517 1 0");
        hapMapTranslate.put("NA18516", "Y013 NA18516 0 0 1 0");
        hapMapTranslate.put("NA18517", "Y013 NA18517 0 0 2 0");
        hapMapTranslate.put("NA18521", "Y016 NA18521 NA18522 NA18523 1 0");
        hapMapTranslate.put("NA18522", "Y016 NA18522 0 0 1 0");
        hapMapTranslate.put("NA18523", "Y016 NA18523 0 0 2 0");
        hapMapTranslate.put("NA18872", "Y017 NA18872 NA18871 NA18870 1 0");
        hapMapTranslate.put("NA18871", "Y017 NA18871 0 0 1 0");
        hapMapTranslate.put("NA18870", "Y017 NA18870 0 0 2 0");
        hapMapTranslate.put("NA18854", "Y018 NA18854 NA18853 NA18852 1 0");
        hapMapTranslate.put("NA18853", "Y018 NA18853 0 0 1 0");
        hapMapTranslate.put("NA18852", "Y018 NA18852 0 0 2 0");
        hapMapTranslate.put("NA18857", "Y023 NA18857 NA18856 NA18855 1 0");
        hapMapTranslate.put("NA18856", "Y023 NA18856 0 0 1 0");
        hapMapTranslate.put("NA18855", "Y023 NA18855 0 0 2 0");
        hapMapTranslate.put("NA18863", "Y024 NA18863 NA18862 NA18861 1 0");
        hapMapTranslate.put("NA18862", "Y024 NA18862 0 0 1 0");
        hapMapTranslate.put("NA18861", "Y024 NA18861 0 0 2 0");
        hapMapTranslate.put("NA18914", "Y028 NA18914 NA18913 NA18912 1 0");
        hapMapTranslate.put("NA18913", "Y028 NA18913 0 0 1 0");
        hapMapTranslate.put("NA18912", "Y028 NA18912 0 0 2 0");
        hapMapTranslate.put("NA19094", "Y040 NA19094 NA19092 NA19093 2 0");
        hapMapTranslate.put("NA19092", "Y040 NA19092 0 0 1 0");
        hapMapTranslate.put("NA19093", "Y040 NA19093 0 0 2 0");
        hapMapTranslate.put("NA19103", "Y042 NA19103 NA19101 NA19102 1 0");
        hapMapTranslate.put("NA19101", "Y042 NA19101 0 0 1 0");
        hapMapTranslate.put("NA19102", "Y042 NA19102 0 0 2 0");
        hapMapTranslate.put("NA19139", "Y043 NA19139 NA19138 NA19137 1 0");
        hapMapTranslate.put("NA19138", "Y043 NA19138 0 0 1 0");
        hapMapTranslate.put("NA19137", "Y043 NA19137 0 0 2 0");
        hapMapTranslate.put("NA19202", "Y045 NA19202 NA19200 NA19201 2 0");
        hapMapTranslate.put("NA19200", "Y045 NA19200 0 0 1 0");
        hapMapTranslate.put("NA19201", "Y045 NA19201 0 0 2 0");
        hapMapTranslate.put("NA19173", "Y047 NA19173 NA19171 NA19172 1 0");
        hapMapTranslate.put("NA19171", "Y047 NA19171 0 0 1 0");
        hapMapTranslate.put("NA19172", "Y047 NA19172 0 0 2 0");
        hapMapTranslate.put("NA19205", "Y048 NA19205 NA19203 NA19204 1 0");
        hapMapTranslate.put("NA19203", "Y048 NA19203 0 0 1 0");
        hapMapTranslate.put("NA19204", "Y048 NA19204 0 0 2 0");
        hapMapTranslate.put("NA19211", "Y050 NA19211 NA19210 NA19209 1 0");
        hapMapTranslate.put("NA19210", "Y050 NA19210 0 0 1 0");
        hapMapTranslate.put("NA19209", "Y050 NA19209 0 0 2 0");
        hapMapTranslate.put("NA19208", "Y051 NA19208 NA19207 NA19206 1 0");
        hapMapTranslate.put("NA19207", "Y051 NA19207 0 0 1 0");
        hapMapTranslate.put("NA19206", "Y051 NA19206 0 0 2 0");
        hapMapTranslate.put("NA19161", "Y056 NA19161 NA19160 NA19159 1 0");
        hapMapTranslate.put("NA19160", "Y056 NA19160 0 0 1 0");
        hapMapTranslate.put("NA19159", "Y056 NA19159 0 0 2 0");
        hapMapTranslate.put("NA19221", "Y058 NA19221 NA19223 NA19222 2 0");
        hapMapTranslate.put("NA19223", "Y058 NA19223 0 0 1 0");
        hapMapTranslate.put("NA19222", "Y058 NA19222 0 0 2 0");
        hapMapTranslate.put("NA19120", "Y060 NA19120 NA19119 NA19116 1 0");
        hapMapTranslate.put("NA19119", "Y060 NA19119 0 0 1 0");
        hapMapTranslate.put("NA19116", "Y060 NA19116 0 0 2 0");
        hapMapTranslate.put("NA19142", "Y071 NA19142 NA19141 NA19140 1 0");
        hapMapTranslate.put("NA19141", "Y071 NA19141 0 0 1 0");
        hapMapTranslate.put("NA19140", "Y071 NA19140 0 0 2 0");
        hapMapTranslate.put("NA19154", "Y072 NA19154 NA19153 NA19152 1 0");
        hapMapTranslate.put("NA19153", "Y072 NA19153 0 0 1 0");
        hapMapTranslate.put("NA19152", "Y072 NA19152 0 0 2 0");
        hapMapTranslate.put("NA19145", "Y074 NA19145 NA19144 NA19143 1 0");
        hapMapTranslate.put("NA19144", "Y074 NA19144 0 0 1 0");
        hapMapTranslate.put("NA19143", "Y074 NA19143 0 0 2 0");
        hapMapTranslate.put("NA19129", "Y077 NA19129 NA19128 NA19127 2 0");
        hapMapTranslate.put("NA19128", "Y077 NA19128 0 0 1 0");
        hapMapTranslate.put("NA19127", "Y077 NA19127 0 0 2 0");
        hapMapTranslate.put("NA19132", "Y101 NA19132 NA19130 NA19131 2 0");
        hapMapTranslate.put("NA19130", "Y101 NA19130 0 0 1 0");
        hapMapTranslate.put("NA19131", "Y101 NA19131 0 0 2 0");
        hapMapTranslate.put("NA19100", "Y105 NA19100 NA19098 NA19099 2 0");
        hapMapTranslate.put("NA19098", "Y105 NA19098 0 0 1 0");
        hapMapTranslate.put("NA19099", "Y105 NA19099 0 0 2 0");
        hapMapTranslate.put("NA19194", "Y112 NA19194 NA19192 NA19193 1 0");
        hapMapTranslate.put("NA19192", "Y112 NA19192 0 0 1 0");
        hapMapTranslate.put("NA19193", "Y112 NA19193 0 0 2 0");
        hapMapTranslate.put("NA19240", "Y117 NA19240 NA19239 NA19238 2 0");
        hapMapTranslate.put("NA19239", "Y117 NA19239 0 0 1 0");
        hapMapTranslate.put("NA19238", "Y117 NA19238 0 0 2 0");
        hapMapTranslate.put("NA18484", "Y001 NA18484 NA18486 NA18488 2 0");
        hapMapTranslate.put("NA18486", "Y001 NA18486 0 0 1 0");
        hapMapTranslate.put("NA18488", "Y001 NA18488 0 0 2 0");
        hapMapTranslate.put("NA18485", "Y002 NA18485 NA18487 NA18489 1 0");
        hapMapTranslate.put("NA18487", "Y002 NA18487 0 0 1 0");
        hapMapTranslate.put("NA18489", "Y002 NA18489 0 0 2 0");
        hapMapTranslate.put("NA18497", "Y003 NA18497 NA18498 NA18499 1 0");
        hapMapTranslate.put("NA18498", "Y003 NA18498 0 0 1 0");
        hapMapTranslate.put("NA18499", "Y003 NA18499 0 0 2 0");
        hapMapTranslate.put("NA19109", "Y006 NA19109 NA19107 NA19108 2 0");
        hapMapTranslate.put("NA19107", "Y006 NA19107 0 0 1 0");
        hapMapTranslate.put("NA19108", "Y006 NA19108 0 0 2 0");
        hapMapTranslate.put("NA18869", "Y007 NA18869 NA18868 NA18867 1 0");
        hapMapTranslate.put("NA18868", "Y007 NA18868 0 0 1 0");
        hapMapTranslate.put("NA18867", "Y007 NA18867 0 0 2 0");
        hapMapTranslate.put("NA18509", "Y010 NA18509 0 NA18511 1 0");
        hapMapTranslate.put("NA18511", "Y010 NA18511 0 0 2 0");
        hapMapTranslate.put("NA18510", "Y010 NA18510 0 0 1 0");
        hapMapTranslate.put("NA18518", "Y014 NA18518 NA18519 NA18520 2 0");
        hapMapTranslate.put("NA18519", "Y014 NA18519 0 0 1 0");
        hapMapTranslate.put("NA18520", "Y014 NA18520 0 0 2 0");
        hapMapTranslate.put("NA18875", "Y019 NA18875 NA18874 NA18873 2 0");
        hapMapTranslate.put("NA18874", "Y019 NA18874 0 0 1 0");
        hapMapTranslate.put("NA18873", "Y019 NA18873 0 0 2 0");
        hapMapTranslate.put("NA19252", "Y025 NA19252 0 NA18907 1 0");
        hapMapTranslate.put("NA18907", "Y025 NA18907 0 0 2 0");
        hapMapTranslate.put("NA18908", "Y025 NA18908 0 0 1 0");
        hapMapTranslate.put("NA18911", "Y027 NA18911 NA18910 NA18909 1 0");
        hapMapTranslate.put("NA18910", "Y027 NA18910 0 0 1 0");
        hapMapTranslate.put("NA18909", "Y027 NA18909 0 0 2 0");
        hapMapTranslate.put("NA18930", "Y030 NA18930 NA18917 NA18916 2 0");
        hapMapTranslate.put("NA18917", "Y030 NA18917 0 0 1 0");
        hapMapTranslate.put("NA18916", "Y030 NA18916 0 0 2 0");
        hapMapTranslate.put("NA18925", "Y033 NA18925 NA18923 NA18924 1 0");
        hapMapTranslate.put("NA18923", "Y033 NA18923 0 0 1 0");
        hapMapTranslate.put("NA18924", "Y033 NA18924 0 0 2 0");
        hapMapTranslate.put("NA19199", "Y035 NA19199 NA19198 NA19197 2 0");
        hapMapTranslate.put("NA19198", "Y035 NA19198 0 0 1 0");
        hapMapTranslate.put("NA19197", "Y035 NA19197 0 0 2 0");
        hapMapTranslate.put("NA18935", "Y036 NA18935 NA18934 NA18933 1 0");
        hapMapTranslate.put("NA18934", "Y036 NA18934 0 0 1 0");
        hapMapTranslate.put("NA18933", "Y036 NA18933 0 0 2 0");
        hapMapTranslate.put("NA19180", "Y038 NA19180 NA19178 NA19179 2 0");
        hapMapTranslate.put("NA19178", "Y038 NA19178 0 0 1 0");
        hapMapTranslate.put("NA19179", "Y038 NA19179 0 0 2 0");
        hapMapTranslate.put("NA19186", "Y039 NA19186 NA19184 NA19185 1 0");
        hapMapTranslate.put("NA19184", "Y039 NA19184 0 0 1 0");
        hapMapTranslate.put("NA19185", "Y039 NA19185 0 0 2 0");
        hapMapTranslate.put("NA19097", "Y041 NA19097 NA19096 NA19095 2 0");
        hapMapTranslate.put("NA19096", "Y041 NA19096 0 0 1 0");
        hapMapTranslate.put("NA19095", "Y041 NA19095 0 0 2 0");
        hapMapTranslate.put("NA19177", "Y044 NA19177 NA19175 NA19176 1 0");
        hapMapTranslate.put("NA19175", "Y044 NA19175 0 0 1 0");
        hapMapTranslate.put("NA19176", "Y044 NA19176 0 0 2 0");
        hapMapTranslate.put("NA19183", "Y052 NA19183 NA19181 NA19182 2 0");
        hapMapTranslate.put("NA19181", "Y052 NA19181 0 0 1 0");
        hapMapTranslate.put("NA19182", "Y052 NA19182 0 0 2 0");
        hapMapTranslate.put("NA19224", "Y057 NA19224 NA19226 NA19225 1 0");
        hapMapTranslate.put("NA19226", "Y057 NA19226 0 0 1 0");
        hapMapTranslate.put("NA19225", "Y057 NA19225 0 0 2 0");
        hapMapTranslate.put("NA19123", "Y061 NA19123 NA19121 NA19122 1 0");
        hapMapTranslate.put("NA19121", "Y061 NA19121 0 0 1 0");
        hapMapTranslate.put("NA19122", "Y061 NA19122 0 0 2 0");
        hapMapTranslate.put("NA19151", "Y073 NA19151 NA19150 NA19149 2 0");
        hapMapTranslate.put("NA19150", "Y073 NA19150 0 0 1 0");
        hapMapTranslate.put("NA19149", "Y073 NA19149 0 0 2 0");
        hapMapTranslate.put("NA19148", "Y075 NA19148 NA19146 NA19147 2 0");
        hapMapTranslate.put("NA19146", "Y075 NA19146 0 0 1 0");
        hapMapTranslate.put("NA19147", "Y075 NA19147 0 0 2 0");
        hapMapTranslate.put("NA19115", "Y079 NA19115 NA19113 NA19114 2 0");
        hapMapTranslate.put("NA19113", "Y079 NA19113 0 0 1 0");
        hapMapTranslate.put("NA19114", "Y079 NA19114 0 0 2 0");
        hapMapTranslate.put("NA19258", "Y092 NA19258 NA19256 NA19257 1 0");
        hapMapTranslate.put("NA19256", "Y092 NA19256 0 0 1 0");
        hapMapTranslate.put("NA19257", "Y092 NA19257 0 0 2 0");
        hapMapTranslate.put("NA19174", "Y100 NA19174 NA19117 NA19118 1 0");
        hapMapTranslate.put("NA19117", "Y100 NA19117 0 0 1 0");
        hapMapTranslate.put("NA19118", "Y100 NA19118 0 0 2 0");
        hapMapTranslate.put("NA19215", "Y110 NA19215 NA19213 NA19214 2 0");
        hapMapTranslate.put("NA19213", "Y110 NA19213 0 0 1 0");
        hapMapTranslate.put("NA19214", "Y110 NA19214 0 0 2 0");
        hapMapTranslate.put("NA19191", "Y111 NA19191 NA19189 NA19190 1 0");
        hapMapTranslate.put("NA19189", "Y111 NA19189 0 0 1 0");
        hapMapTranslate.put("NA19190", "Y111 NA19190 0 0 2 0");
        hapMapTranslate.put("NA19237", "Y116 NA19237 NA19236 NA19235 2 0");
        hapMapTranslate.put("NA19236", "Y116 NA19236 0 0 1 0");
        hapMapTranslate.put("NA19235", "Y116 NA19235 0 0 2 0");
        hapMapTranslate.put("NA19249", "Y120 NA19249 NA19248 NA19247 1 0");
        hapMapTranslate.put("NA19248", "Y120 NA19248 0 0 1 0");
        hapMapTranslate.put("NA19247", "Y120 NA19247 0 0 2 0");

    }

    /**
     * gets the allIndividuals Vector
     */
    public Vector getAllIndividuals() {
        return allIndividuals;
    }

    public Vector getUnusedIndividuals() {
        HashSet used = new HashSet(getUnrelatedIndividuals());
        HashSet all = new HashSet(getAllIndividuals());
        all.removeAll(used);
        return new Vector(all);
    }

    public Vector getUnrelatedIndividuals() {
        return unrelatedIndividuals;
    }

    /**
     * @return enumeration containing a list of familyID's in the families hashtable
     */
    public Enumeration getFamList() {
        return this.families.keys();
    }

    /**
     * @param familyID id of desired family
     * @return Family identified by familyID in families hashtable
     */
    public Family getFamily(String familyID) {
        return (Family) this.families.get(familyID);
    }

    /**
     * @return the number of Family objects in the families hashtable
     */
    public int getNumFamilies() {
        return this.families.size();
    }

    /**
     * this method iterates through each family in Hashtable families and adds up
     * the number of individuals in total across all families
     *
     * @return the total number of individuals in all the family objects in the families hashtable
     */
    public int getNumIndividuals() {
        Enumeration famEnum = this.families.elements();
        int total = 0;
        while (famEnum.hasMoreElements()) {
            Family fam = (Family) famEnum.nextElement();
            total += fam.getNumMembers();
        }
        return total;
    }

    /**
     * finds the first individual in the first family and returns the number of markers for that individual
     *
     * @return the number of markers
     */
    public int getNumMarkers() {
        Enumeration famList = this.families.elements();
        int numMarkers = 0;
        while (famList.hasMoreElements()) {
            Family fam = (Family) famList.nextElement();
            Enumeration indList = fam.getMemberList();
            Individual ind = null;
            while (indList.hasMoreElements()) {
                try {
                    ind = fam.getMember((String) indList.nextElement());
                } catch (PedFileException pfe) {
                }
                numMarkers = ind.getNumMarkers();
                if (numMarkers > 0) {
                    return numMarkers;
                }
            }
        }
        return 0;
    }

    /**
     * takes in a pedigree file in the form of a vector of strings and parses it.
     * data is stored in families in the member hashtable families
     */
    public void parseLinkage(Vector pedigrees) throws PedFileException {
        int colNum = -1;
        boolean withOptionalColumn = false;
        int numMarkers = 0;
        boolean genoError = false;
        int numLines = pedigrees.size();
        if (numLines == 0) {
            throw new PedFileException("Data format error: empty file");
        }
        Individual ind;
        this.allIndividuals = new Vector();

        for (int k = 0; k < numLines; k++) {
            StringTokenizer tokenizer = new StringTokenizer((String) pedigrees.get(k), "\n\t\" \"");
            int numTokens = tokenizer.countTokens();

            //reading the first line
            if (colNum < 1) {
                //only check column number count for the first nonblank line
                colNum = numTokens;
                if (colNum % 2 == 1) {
                    withOptionalColumn = true;
                    numMarkers = (numTokens - 7) / 2;
                } else {
                    numMarkers = (numTokens - 6) / 2;
                }
            }
            if (colNum != numTokens) {
                //this line has a different number of columns
                //should send some sort of error message
                throw new PedFileException("Column number mismatch in pedfile. line " + (k + 1));
            }

            try {
                ind = new Individual(numMarkers, false);
            } catch (NegativeArraySizeException neg) {
                throw new PedFileException("File formatting error.");
            }
            if (numTokens < 6) {
                throw new PedFileException("Incorrect number of fields on line " + (k + 1));
            }

            if (tokenizer.hasMoreTokens()) {

                ind.setFamilyID(new String(tokenizer.nextToken().trim()));
                ind.setIndividualID(new String(tokenizer.nextToken().trim()));
                ind.setDadID(new String(tokenizer.nextToken().trim()));
                ind.setMomID(new String(tokenizer.nextToken().trim()));
                try {
                    ind.setGender(Integer.parseInt(tokenizer.nextToken().trim()));
                    ind.setAffectedStatus(Integer.parseInt(tokenizer.nextToken().trim()));
                    if (withOptionalColumn) {
                        ind.setLiability(Integer.parseInt(tokenizer.nextToken().trim()));
                    }
                } catch (NumberFormatException nfe) {
                    throw new PedFileException("Pedfile error: invalid gender or affected status on line " + (k + 1));
                }

                byte genotype1;
                byte genotype2;
                if (!tokenizer.hasMoreTokens()) {
                    throw new PedFileException("Pedfile error: no marker genotypes specified.");
                }
                while (tokenizer.hasMoreTokens()) {
                    try {
                        String alleleA = tokenizer.nextToken();
                        String alleleB = tokenizer.nextToken();
                        int[] checker1, checker2;
                        checker1 = checkGenotype(alleleA);
                        checker2 = checkGenotype(alleleB);
                        if (checker1[1] != checker2[1]) {
                            genoError = !genoError;
                        }

                        if (genoError) {
                            throw new PedFileException("File input error on line " + (k + 1) + ", marker " + (ind.getNumMarkers() + 1) +
                                    ".\nFor any marker, an individual's genotype must be only letters or only numbers.");
                        }

                        if (checker1[0] < 0 || checker1[0] > 4 || checker2[0] < 0 || checker2[0] > 4) {
                            throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k + 1)
                                    + ".\n all genotypes must be 0-4 or A/C/G/T.");
                        }
                        genotype1 = (byte) checker1[0];
                        genotype2 = (byte) checker2[0];
                        ind.addMarker(genotype1, genotype2);
                    } catch (NumberFormatException nfe) {
                        throw new PedFileException("Pedigree file input error: invalid genotype on line " + (k + 1));
                    }
                }

                //check if the family exists already in the Hashtable
                Family fam = (Family) this.families.get(ind.getFamilyID());
                if (fam == null) {
                    //it doesnt exist, so create a new Family object
                    fam = new Family(ind.getFamilyID());
                }

                if (fam.getMembers().containsKey(ind.getIndividualID())) {
                    throw new PedFileException("Individual " + ind.getIndividualID() + " in family " + ind.getFamilyID() + " appears more than once.");
                }

                fam.addMember(ind);
                this.families.put(ind.getFamilyID(), fam);
                this.allIndividuals.add(ind);

            }
        }

        //now we check if anyone has a reference to a parent who isnt in the file, and if so, we remove the reference
        for (int i = 0; i < allIndividuals.size(); i++) {
            Individual currentInd = (Individual) allIndividuals.get(i);
            Hashtable curFam = ((Family) (families.get(currentInd.getFamilyID()))).getMembers();
            if (!currentInd.getDadID().equals("0") && !(curFam.containsKey(currentInd.getDadID()))) {
                currentInd.setDadID("0");
                bogusParents = true;
            }
            if (!currentInd.getMomID().equals("0") && !(curFam.containsKey(currentInd.getMomID()))) {
                currentInd.setMomID("0");
                bogusParents = true;
            }
        }
    }

    public void parseHapMap(Vector lines, Vector hapsData) throws PedFileException {
        int colNum = -1;
        int numLines = lines.size();
        if (numLines < 2) {
            throw new PedFileException("Hapmap data format error: empty file");
        }
        if (hapsData != null) {
            String indName;
            for (int i = 0; i < hapsData.size(); i++) {
                StringTokenizer hd = new StringTokenizer((String) hapsData.get(i));
                if (hd.countTokens() < 6) {
                    throw new PedFileException("Hapmap data format error: pedigree data on line " + (i + 1) + ".");
                }
                if (hd.countTokens() > 7) {
                    throw new PedFileException("Hapmap data format error: pedigree data on line " + (i + 1) + ".");
                }
                hd.nextToken();
                indName = hd.nextToken();
                hapMapTranslate.put(indName, (String) hapsData.get(i));
            }
        }
        Individual ind;

        this.allIndividuals = new Vector();

        //enumerate indivs
        StringTokenizer st = new StringTokenizer((String) lines.get(0), "\n\t\" \"");
        int numMetaColumns = 0;
        boolean doneMeta = false;
        boolean genoErrorB = false;
        while (!doneMeta && st.hasMoreTokens()) {
            String thisfield = st.nextToken();
            numMetaColumns++;
            //first indiv ID will be a string beginning with "NA"
            if (thisfield.startsWith("NA")) {
                doneMeta = true;
            }
        }
        numMetaColumns--;

        st = new StringTokenizer((String) lines.get(0), "\n\t\" \"");
        for (int i = 0; i < numMetaColumns; i++) {
            st.nextToken();
        }
        Vector namesIncludingDups = new Vector();
        StringTokenizer dt;
        while (st.hasMoreTokens()) {
            //todo: sort out how this used to work. now it's counting the header line so we subtract 1
            ind = new Individual(numLines - 1, false);

            String name = st.nextToken();
            namesIncludingDups.add(name);
            if (name.endsWith("dup")) {
                //skip dups (i.e. don't add 'em to ind array)
                continue;
            }
            String details = (String) hapMapTranslate.get(name);
            if (details == null) {
                throw new PedFileException("Hapmap data format error: " + name);
            }
            dt = new StringTokenizer(details, "\n\t\" \"");
            ind.setFamilyID(dt.nextToken().trim());
            ind.setIndividualID(dt.nextToken().trim());
            ind.setDadID(dt.nextToken().trim());
            ind.setMomID(dt.nextToken().trim());
            try {
                ind.setGender(Integer.parseInt(dt.nextToken().trim()));
                ind.setAffectedStatus(Integer.parseInt(dt.nextToken().trim()));
            } catch (NumberFormatException nfe) {
                throw new PedFileException("File error: invalid gender or affected status for indiv " + name);
            }

            //check if the family exists already in the Hashtable
            Family fam = (Family) this.families.get(ind.getFamilyID());
            if (fam == null) {
                //it doesnt exist, so create a new Family object
                fam = new Family(ind.getFamilyID());
            }
            fam.addMember(ind);
            this.families.put(ind.getFamilyID(), fam);
            this.allIndividuals.add(ind);
        }

        //start at k=1 to skip header which we just processed above.
        hminfo = new String[numLines - 1][];
        for (int k = 1; k < numLines; k++) {
            StringTokenizer tokenizer = new StringTokenizer((String) lines.get(k));
            //reading the first line
            if (colNum < 0) {
                //only check column number count for the first line
                colNum = tokenizer.countTokens();
            }
            if (colNum != tokenizer.countTokens()) {
                //this line has a different number of columns
                //should send some sort of error message
                //TODO: add something which stores number of markers for all lines and checks that they're consistent
                throw new PedFileException("Line number mismatch in input file. line " + (k + 1));
            }

            if (tokenizer.hasMoreTokens()) {
                hminfo[k - 1] = new String[2];
                for (int skip = 0; skip < numMetaColumns; skip++) {
                    //meta-data crap
                    String s;

                    try {
                        s = tokenizer.nextToken().trim();
                    } catch (NoSuchElementException nse) {
                        throw new PedFileException("Data format error on line " + (k + 1) + ": " + (String) lines.get(k));
                    }
                    //get marker name, chrom and pos
                    if (skip == 0) {
                        hminfo[k - 1][0] = s;
                    }
                    if (skip == 2) {
                        String dc = Chromosome.getDataChrom();
                        if (dc != null && !dc.equals("none")) {
                            if (!dc.equalsIgnoreCase(s)) {
                                throw new PedFileException("Hapmap file format error on line " + (k + 1) +
                                        ":\n The file appears to contain multiple chromosomes:" +
                                        "\n" + dc + ", " + s);
                            }
                        } else {
                            Chromosome.setDataChrom(s);
                        }
                    }
                    if (skip == 3) {
                        hminfo[k - 1][1] = s;
                    }
                    if (skip == 5) {
                        Chromosome.setDataBuild(s);
                    }
                }
                int index = 0;
                int indexIncludingDups = -1;
                while (tokenizer.hasMoreTokens()) {
                    String alleles = tokenizer.nextToken();

                    indexIncludingDups++;
                    //we've skipped the dups in the ind array, so we skip their genotypes
                    if (((String) namesIncludingDups.elementAt(indexIncludingDups)).endsWith("dup")) {
                        continue;
                    }

                    ind = (Individual) allIndividuals.elementAt(index);
                    int[] checker1, checker2;
                    try {
                        checker1 = checkGenotype(alleles.substring(0, 1));
                        checker2 = checkGenotype(alleles.substring(1, 2));
                    } catch (NumberFormatException nfe) {
                        throw new PedFileException("Invalid genotype on individual " + ind.getIndividualID() + ".");
                    }
                    if (checker1[1] != checker2[1]) {
                        genoErrorB = !genoErrorB;
                    }
                    byte allele1 = (byte) checker1[0];
                    byte allele2 = (byte) checker2[0];
                    ind.addMarker(allele1, allele2);
                    if (genoErrorB) {
                        throw new PedFileException("File input error: individual " + ind.getIndividualID() + ", marker "
                                + this.hminfo[ind.getNumMarkers() - 1][0] + ".\nFor any marker, an individual's genotype must be only letters or only numbers.");
                    }
                    index++;
                }
            }
        }
    }

    public void parseHapMapPhase(String[] info) throws IOException, PedFileException {
        if (info[3].equals("")) {
            Chromosome.setDataChrom("none");
        } else {
            Chromosome.setDataChrom("chr" + info[3]);
        }
        Chromosome.setDataBuild("ncbi_b35");
        //TODO Add in code to get the build number out of the file and not explicitly
        
        Vector sampleData = new Vector();
        Vector legendData = new Vector();
        Vector legendMarkers = new Vector();
        Vector legendPositions = new Vector();
        Individual ind = null;
        byte[] byteDataT = new byte[0];
        byte[] byteDataU = new byte[0];
        this.allIndividuals = new Vector();

        InputStream phaseStream, sampleStream, legendStream;
        String phaseName, sampleName, legendName;

        //SAMPLE FILE CREAION
        //TODO Incorporate fileConnection asap
        try {
            URL sampleURL = new URL(info[1]);
            sampleName = sampleURL.getFile();
            sampleStream = sampleURL.openStream();
        } catch (MalformedURLException mfe) {
            File sampleFile = new File(info[1]);
            if (sampleFile.length() < 1) {
                throw new PedFileException("Sample file is empty or non-existent: " + sampleFile.getName());
            }
            sampleName = sampleFile.getName();
            sampleStream = new FileInputStream(sampleFile);
        } catch (IOException ioe) {
            throw new PedFileException("Could not connect to " + info[1]);
        }

        //read in the individual ids data.
        try {
            BufferedReader sampleBuffReader;
            if (Options.getGzip()) {
                GZIPInputStream sampleInputStream = new GZIPInputStream(sampleStream);
                sampleBuffReader = new BufferedReader(new InputStreamReader(sampleInputStream));
            } else {
                sampleBuffReader = new BufferedReader(new InputStreamReader(sampleStream));
            }
            String sampleLine;
            while ((sampleLine = sampleBuffReader.readLine()) != null) {
                StringTokenizer sampleTokenizer = new StringTokenizer(sampleLine);
                sampleData.add(sampleTokenizer.nextToken());
            }
        } catch (NoSuchElementException nse) {
            throw new PedFileException("File format error in " + sampleName);
        }

        try {
            URL legendURL = new URL(info[2]);
            legendName = legendURL.getFile();
            legendStream = legendURL.openStream();
        } catch (MalformedURLException mfe) {
            File legendFile = new File(info[2]);
            if (legendFile.length() < 1) {
                throw new PedFileException("Legend file is empty or non-existent: " + legendFile.getName());
            }
            legendName = legendFile.getName();
            legendStream = new FileInputStream(legendFile);
        } catch (IOException ioe) {
            throw new PedFileException("Could not connect to " + info[2]);
        }

        //read in the legend data
        try {
            BufferedReader legendBuffReader;
            if (Options.getGzip()) {
                GZIPInputStream legendInputStream = new GZIPInputStream(legendStream);
                legendBuffReader = new BufferedReader(new InputStreamReader(legendInputStream));
            } else {
                legendBuffReader = new BufferedReader(new InputStreamReader(legendStream));
            }
            String legendLine;
            String zero, one;
            while ((legendLine = legendBuffReader.readLine()) != null) {
                StringTokenizer legendSt = new StringTokenizer(legendLine);
                String markerid = legendSt.nextToken();
                if (markerid.equalsIgnoreCase("rs") || markerid.equalsIgnoreCase("marker")) { //skip header
                    continue;
                }
                legendMarkers.add(markerid);
                legendPositions.add(legendSt.nextToken());
                byte[] legendBytes = new byte[2];
                zero = legendSt.nextToken();
                one = legendSt.nextToken();

                if (zero.equalsIgnoreCase("A")) {
                    legendBytes[0] = 1;
                } else if (zero.equalsIgnoreCase("C")) {
                    legendBytes[0] = 2;
                } else if (zero.equalsIgnoreCase("G")) {
                    legendBytes[0] = 3;
                } else if (zero.equalsIgnoreCase("T")) {
                    legendBytes[0] = 4;
                } else {
                    throw new PedFileException("Invalid allele: " + zero);
                }

                if (one.equalsIgnoreCase("A")) {
                    legendBytes[1] = 1;
                } else if (one.equalsIgnoreCase("C")) {
                    legendBytes[1] = 2;
                } else if (one.equalsIgnoreCase("G")) {
                    legendBytes[1] = 3;
                } else if (one.equalsIgnoreCase("T")) {
                    legendBytes[1] = 4;
                } else {
                    throw new PedFileException("Invalid allele: " + one);
                }

                legendData.add(legendBytes);
            }

            hminfo = new String[legendPositions.size()][2];

            for (int i = 0; i < legendPositions.size(); i++) {
                //marker name.
                hminfo[i][0] = (String) legendMarkers.get(i);
                //marker position.
                hminfo[i][1] = (String) legendPositions.get(i);
            }
        } catch (NoSuchElementException nse) {
            throw new PedFileException("File format error in " + legendName);
        }


        try {
            URL phaseURL = new URL(info[0]);
            phaseName = phaseURL.getFile();
            phaseStream = phaseURL.openStream();
        } catch (MalformedURLException mfe) {
            File phaseFile = new File(info[0]);
            if (phaseFile.length() < 1) {
                throw new PedFileException("Genotypes file is empty or non-existent: " + phaseFile.getName());
            }
            phaseName = phaseFile.getName();
            phaseStream = new FileInputStream(phaseFile);
        } catch (IOException ioe) {
            throw new PedFileException("Could not connect to " + info[0]);
        }

        //read in the phased data.
        try {
            BufferedReader phasedBuffReader;
            if (Options.getGzip()) {
                GZIPInputStream phasedInputStream = new GZIPInputStream(phaseStream);
                phasedBuffReader = new BufferedReader(new InputStreamReader(phasedInputStream));
            } else {
                phasedBuffReader = new BufferedReader(new InputStreamReader(phaseStream));
            }
            String phasedLine;
            int columns = 0;
            String token;
            boolean even = false;
            int iterator = 0;
            while ((phasedLine = phasedBuffReader.readLine()) != null) {
                StringTokenizer phasedSt = new StringTokenizer(phasedLine);
                columns = phasedSt.countTokens();
                if (even) {
                    iterator++;
                } else {   //Only set up a new individual every 2 lines.
                    ind = new Individual(columns, true);
                    try {
                        ind.setIndividualID((String) sampleData.get(iterator));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new PedFileException("File error: Sample file is missing individual IDs");
                    }
                    if (columns != legendData.size()) {
                        throw new PedFileException("File error: invalid number of markers on Individual " + ind.getIndividualID());
                    }
                    String details = (String) hapMapTranslate.get(ind.getIndividualID());
                    //exception in case of wierd compression combos in input files
                    if (details == null) {
                        throw new PedFileException("File format error in " + sampleName);
                    }
                    StringTokenizer dt = new StringTokenizer(details, "\n\t\" \"");
                    ind.setFamilyID(dt.nextToken().trim());
                    //skip individualID since we already have it.
                    dt.nextToken();
                    ind.setDadID(dt.nextToken());
                    ind.setMomID(dt.nextToken());
                    try {
                        ind.setGender(Integer.parseInt(dt.nextToken().trim()));
                        ind.setAffectedStatus(Integer.parseInt(dt.nextToken().trim()));
                    } catch (NumberFormatException nfe) {
                        throw new PedFileException("File error: invalid gender or affected status for indiv " + ind.getIndividualID());
                    }

                    //check if the family exists already in the Hashtable
                    Family fam = (Family) this.families.get(ind.getFamilyID());
                    if (fam == null) {
                        //it doesnt exist, so create a new Family object
                        fam = new Family(ind.getFamilyID());
                    }
                    fam.addMember(ind);
                    this.families.put(ind.getFamilyID(), fam);
                    this.allIndividuals.add(ind);
                }

                int index = 0;
                if (!even) {
                    byteDataT = new byte[columns];
                } else {
                    byteDataU = new byte[columns];
                }
                while (phasedSt.hasMoreTokens()) {
                    token = phasedSt.nextToken();
                    if (!even) {
                        if (token.equalsIgnoreCase("0")) {
                            byteDataT[index] = ((byte[]) legendData.get(index))[0];
                        } else if (token.equalsIgnoreCase("1")) {
                            byteDataT[index] = ((byte[]) legendData.get(index))[1];
                        } else {
                            throw new PedFileException("File format error in " + phaseName);
                        }
                    } else {
                        if (token.equalsIgnoreCase("0")) {
                            byteDataU[index] = ((byte[]) legendData.get(index))[0];
                        } else if (token.equalsIgnoreCase("1")) {
                            byteDataU[index] = ((byte[]) legendData.get(index))[1];
                        } else
                        if (Chromosome.getDataChrom().equalsIgnoreCase("chrx") && ind.getGender() == Individual.MALE && token.equalsIgnoreCase("-")) {
                            //X male
                        } else {
                            throw new PedFileException("File format error in " + phaseName);
                        }
                    }
                    index++;
                }
                if (even) {
                    if (ind.getGender() == Individual.MALE && Chromosome.getDataChrom().equalsIgnoreCase("chrx")) {
                        for (int i = 0; i < columns; i++) {
                            ind.addMarker(byteDataT[i], byteDataT[i]);
                        }
                    } else {
                        for (int i = 0; i < columns; i++) {
                            ind.addMarker(byteDataT[i], byteDataU[i]);
                        }
                    }
                }
                even = !even;
            }
        } catch (NoSuchElementException nse) {
            throw new PedFileException("File format error in " + phaseName);
        }
    }

    public void parseSinglePhaseFile(String[] info) throws IOException, PedFileException {

        String targetChrom = "chr" + info[3];
        Chromosome.setDataChrom(targetChrom);
        Vector legendMarkers = new Vector();
        Vector legendPositions = new Vector();
        Vector hmpVector = new Vector();
        Individual ind = null;
        byte[] byteDataT = new byte[0];
        byte[] byteDataU = new byte[0];
        this.allIndividuals = new Vector();

        boolean pseudoChecked = false;
        boolean infoDone = false;
        boolean hminfoDone = false;

        String fileLocation = info[0];
        BufferedReader hmpBuffReader;

        try {

            fileConnection singlePhaseFile = new fileConnection(fileLocation);
            hmpBuffReader = singlePhaseFile.getBufferedReader();
            
            String hmpLine;
            char token;
            int columns;
            while ((hmpLine = hmpBuffReader.readLine()) != null) {
                if (hmpLine.startsWith("---")) {
                    //continue;
                } else if (hmpLine.startsWith("pop:")) {
                    //continue;
                } else if (hmpLine.startsWith("build:")) {
                    StringTokenizer buildSt = new StringTokenizer(hmpLine);
                    buildSt.nextToken();
                    String build = buildSt.nextToken();
                    Chromosome.setDataBuild(build);

                } else if (hmpLine.startsWith("hapmap_release:")) {
                    //continue;
                } else if (hmpLine.startsWith("filters:")) {
                    //continue;
                } else if (hmpLine.startsWith("start:")) {
                    //continue;
                } else if (hmpLine.startsWith("stop:")) {
                    //continue;
                } else if (hmpLine.startsWith("snps:")) {
                    //continue;
                } else if (hmpLine.startsWith("phased_haplotypes:")) {
                    infoDone = true;
                } else if (hmpLine.startsWith("No")) {
                    throw new PedFileException(hmpLine);
                } else if (hmpLine.startsWith("Too many")) {
                    throw new PedFileException(hmpLine);
                } else if (!infoDone) {
                    StringTokenizer posSt = new StringTokenizer(hmpLine, " \t:-");
                    //posSt.nextToken(); //skip the -
                    legendMarkers.add(posSt.nextToken());
                    legendPositions.add(posSt.nextToken());
                } else if (infoDone) {
                    if (!hminfoDone) {
                        hminfo = new String[legendPositions.size()][2];
                        for (int i = 0; i < legendPositions.size(); i++) {
                            //marker name.
                            hminfo[i][0] = (String) legendMarkers.get(i);
                            //marker position.
                            hminfo[i][1] = (String) legendPositions.get(i);
                        }
                        hminfoDone = true;
                    }
                    hmpVector.add(hmpLine);
                }
            }

            Vector GoodStrands = new Vector();
            boolean strandWaiting = false;
            boolean foundNStrand = false;

            for (int i = 0; i < hmpVector.size(); i++) {

                //GRAB THE SET OF CHROMS
                StringTokenizer dataSt = new StringTokenizer((String) hmpVector.get(i));
                dataSt.nextToken(); //skip the -

                //ID
                String newid = dataSt.nextToken();  //individual ID with _c1/_c2
                StringTokenizer filter = new StringTokenizer(newid, "_:");
                String indivID = filter.nextToken();
                String strandID = filter.nextToken();

                //DEAL WITH NEW IDs
                if (!strandID.startsWith("c")) {
                    if (hapMapTranslate.containsKey(indivID)) {
                        String currentValue = (String) hapMapTranslate.get(indivID);
                        indivID = indivID + "_" + strandID;
                        hapMapTranslate.put(indivID, currentValue);
                    }

                    strandID = filter.nextToken();
                }

                String currAlleles = dataSt.nextToken();

                if (strandID.equals("c1")) {
                    //CHECK IF WE CAPTURED A STRAND FOR N TYPE PROCESSING
                    if (GoodStrands.size() > 1) {
                        strandWaiting = false;
                    } else {
                        strandWaiting = false;
                    }

                    //CHECK THE NEXT LINE, MAKE SURE IT IS NOT ALL N's
                    StringTokenizer nextStrand = new StringTokenizer((String) hmpVector.get(i + 1), ":- \t");
                    String nextID = nextStrand.nextToken();
                    String tmpAlleles = nextStrand.nextToken();
                    int nCount = 0;

                    for (int j = 0; j < tmpAlleles.length(); j++) {
                        char allele = tmpAlleles.charAt(j);
                        if (allele == 'N') {
                            nCount++;
                        }
                    }

                    if (nCount / tmpAlleles.length() == 1) {
                        foundNStrand = true;
                        if (!strandWaiting) {
                            GoodStrands.add(nextID);
                            GoodStrands.add(currAlleles);
                        }
                    } else {
                        foundNStrand = false;
                    }
                }

                //SET ALLELE DATA FOR N STRANDS
                if (foundNStrand) {
                    if (strandWaiting) {
                        if (strandID.equals("c2")) {

                            currAlleles = (String) GoodStrands.get(1);
                            GoodStrands.removeAllElements();
                            foundNStrand = false;
                        }
                    }
                }

                //CHECK SEX
                columns = currAlleles.length();
                if (strandID.equals("c1")) {   //Only set up a new individual on c1.
                    ind = new Individual(columns, true);
                    ind.setIndividualID(indivID);
                    if (columns != legendMarkers.size()) {
                        throw new PedFileException("File error: invalid number of markers on Individual " + ind.getIndividualID());
                    }

                    String details;
                    //GO THROUGH SAVED DATA
                    if (hapMapTranslate.containsKey(ind.getIndividualID())) {
                        details = (String) hapMapTranslate.get(ind.getIndividualID());
                    } else {
                        continue;
                    }

                    //TOKENIZE AND GO THROUGH LINES
                    StringTokenizer dt = new StringTokenizer(details, "\n\t\" \"");
                    ind.setFamilyID(dt.nextToken().trim());
                    //skip individualID since we already have it.
                    dt.nextToken();
                    ind.setDadID(dt.nextToken());
                    ind.setMomID(dt.nextToken());

                    try {
                        ind.setGender(Integer.parseInt(dt.nextToken().trim()));
                        ind.setAffectedStatus(Integer.parseInt(dt.nextToken().trim()));
                    } catch (NumberFormatException nfe) {
                        throw new PedFileException("File error: invalid gender or affected status for indiv " + ind.getIndividualID());
                    }
                    if (!pseudoChecked) {
                        if (ind.getGender() == Individual.MALE) {
                            pseudoChecked = true;
                            if (Chromosome.getDataChrom().equalsIgnoreCase("chrx")) {
                                StringTokenizer checkSt = new StringTokenizer((String) hmpVector.get(i + 1), ":- \t");
                                String checkNewid = checkSt.nextToken();
                                checkSt.nextToken(); //alleles
                                StringTokenizer checkFilter = new StringTokenizer(checkNewid, "_");
                                checkFilter.nextToken();
                                String checkStrand = checkFilter.nextToken();
                                if (checkStrand.equals("c2")) {
                                    Chromosome.setDataChrom("chrp");
                                }
                            }
                        }
                    }

                    //check if the family exists already in the Hashtable
                    Family fam = (Family) this.families.get(ind.getFamilyID());
                    if (fam == null) {
                        //it doesnt exist, so create a new Family object
                        fam = new Family(ind.getFamilyID());
                    }
                    fam.addMember(ind);
                    this.families.put(ind.getFamilyID(), fam);
                    this.allIndividuals.add(ind);
                }

                //PARSE ALLELES
                int index = 0;
                if (strandID.equals("c1")) {
                    byteDataT = new byte[columns];
                } else {
                    byteDataU = new byte[columns];
                }

                for (int k = 0; k < columns; k++) {
                    token = currAlleles.charAt(k);
                    if (strandID.equals("c1")) {
                        if (token == 'A') {
                            byteDataT[index] = 1;
                        } else if (token == 'C') {
                            byteDataT[index] = 2;
                        } else if (token == 'G') {
                            byteDataT[index] = 3;
                        } else if (token == 'T') {
                            byteDataT[index] = 4;
                        } else if (token == 'N') {
                            //Unknowns are assigned an N by Hapmap now on missing parents
                            byteDataU[index] = 0;
                        } else {
                            throw new PedFileException("Invalid Allele: " + token);
                        }
                    } else {
                        if (token == 'A') {
                            byteDataU[index] = 1;
                        } else if (token == 'C') {
                            byteDataU[index] = 2;
                        } else if (token == 'G') {
                            byteDataU[index] = 3;
                        } else if (token == 'T') {
                            byteDataU[index] = 4;
                        } else if (token == '-') {
                            /*if (!(Chromosome.getDataChrom().equalsIgnoreCase("chrx"))){
                                throw new PedFileException("Missing allele on non X-chromosome data");
                            }else{
                                byteDataU[index] = byteDataT[index];
                            }*/
                            throw new PedFileException("Haploview does not currently support regions encompassing both\n"
                                    + "pseudoautosomal and non-pseudoautosomal markers.");
                        } else if (token == 'N') {
                            //Unknowns are assigned an N by Hapmap now on missing parents
                            byteDataU[index] = 0;
                        } else {

                            throw new PedFileException("File format error.");
                        }
                    }
                    index++;
                }

                if (strandID.equals("c2")) {
                    for (int j = 0; j < columns; j++) {

                        ind.addMarker(byteDataT[j], byteDataU[j]);

                    }
                } else if (strandID.equals("c1") && (ind.getGender() == Individual.MALE) &&
                        (Chromosome.getDataChrom().equalsIgnoreCase("chrx"))) {
                    for (int j = 0; j < columns; j++) {
                        ind.addMarker(byteDataT[j], byteDataT[j]);
                    }
                }
            }
        } catch (IOException io) {
            throw new IOException("Could not connect to HapMap database.");
        }
    }

    public void parsePhasedDownload(String[] info) throws IOException, PedFileException {

        byte[] byteDataT = new byte[0];
        byte[] byteDataU = new byte[0];
        this.allIndividuals = new Vector();
        String targetChrom = "chr" + info[4];
        Chromosome.setDataChrom(targetChrom);
        Vector legendMarkers = new Vector();
        Vector legendPositions = new Vector();
        Vector hmpVector = new Vector();
        Individual ind = null;
        String panelChoice = info[1];
        String phaseChoice = "";
        String dataRelease = Chromosome.getDataRelease();
        String output = info[6];
        String urlHmp = "";
        boolean pseudoChecked = false;
        boolean infoDone = false;
        boolean hminfoDone = false;
        long startPos;
        if (info[2].equals("0")) {
            startPos = 1;
        } else {
            startPos = (Integer.parseInt(info[2])) * 1000;
        }
        long stopPos = (Integer.parseInt(info[3])) * 1000;

        if (dataRelease.equals("hapmap_phaseI")) {
            Chromosome.setDataBuild("ncbi_b34");
            phaseChoice = "I";
        } else if (dataRelease.equals("hapmap21_B35")) {
            Chromosome.setDataBuild("ncbi_b35");
            phaseChoice = "II";
        } else if (dataRelease.equals("hapmap22_B36")) {
            Chromosome.setDataBuild("ncbi_b36");
            phaseChoice = "III";
        } else if (dataRelease.equals("hapmap24_B36")) {
            Chromosome.setDataBuild("ncbi_b36");
            phaseChoice = "III";
        } else {
            Chromosome.setDataBuild("ncbi_b36");
        }

        if (dataRelease.equals("hapmap27_B36") || dataRelease.equals("hapmap3r2_B36")) {
            //HAPMAP 3
            urlHmp = "http://www.hapmap.org/cgi-perl/phased_hapmap3?chr=" + targetChrom + "&pop=" + panelChoice +
                    "&start=" + startPos + "&stop=" + stopPos + "&ds=r2" + "&out=" + output;
        } else {
            //HAPMAP 2
            if (panelChoice.equals("CHB+JPT")) {
                panelChoice = "JC";
            }
            urlHmp = "http://www.hapmap.org/cgi-perl/phased?chr=" + targetChrom + "&pop=" + panelChoice +
                    "&start=" + startPos + "&stop=" + stopPos + "&ds=p" + phaseChoice + "&out=" + output + "&filter=cons+"
                    + panelChoice.toLowerCase();
        }

        String hmpLine;
        char token;
        int columns;
        try {
            fileConnection phaseHmpFile = new fileConnection(urlHmp);
            BufferedReader hmpBuffReader = phaseHmpFile.getBufferedReader();

            while ((hmpLine = hmpBuffReader.readLine()) != null) {
                if (hmpLine.startsWith("---")) {
                    //continue;
                } else if (hmpLine.startsWith("pop:")) {
                    //continue;
                } else if (hmpLine.startsWith("build:")) {
                    StringTokenizer buildSt = new StringTokenizer(hmpLine);
                    buildSt.nextToken();
                    Chromosome.setDataBuild(new String(buildSt.nextToken()));
                } else if (hmpLine.startsWith("hapmap_release:")) {
                    //continue;
                } else if (hmpLine.startsWith("filters:")) {
                    //continue;
                } else if (hmpLine.startsWith("start:")) {
                    //continue;
                } else if (hmpLine.startsWith("stop:")) {
                    //continue;
                } else if (hmpLine.startsWith("snps:")) {
                    //continue;
                } else if (hmpLine.startsWith("phased_haplotypes:")) {
                    infoDone = true;
                } else if (hmpLine.startsWith("No")) {
                    throw new PedFileException(hmpLine);
                } else if (hmpLine.startsWith("Too many")) {
                    throw new PedFileException(hmpLine);
                } else if (!infoDone) {
                    StringTokenizer posSt = new StringTokenizer(hmpLine, " \t:-");
                    //posSt.nextToken(); //skip the -
                    legendMarkers.add(posSt.nextToken());
                    legendPositions.add(posSt.nextToken());
                } else if (infoDone) {
                    if (!hminfoDone) {
                        hminfo = new String[legendPositions.size()][2];
                        for (int i = 0; i < legendPositions.size(); i++) {
                            //marker name.
                            hminfo[i][0] = (String) legendMarkers.get(i);
                            //marker position.
                            hminfo[i][1] = (String) legendPositions.get(i);
                        }
                        hminfoDone = true;
                    }
                    hmpVector.add(hmpLine);
                }
            }

            Vector GoodStrands = new Vector();
            boolean strandWaiting = false;
            boolean foundNStrand = false;

            for (int i = 0; i < hmpVector.size(); i++) {

                //GRAB THE SET OF CHROMS
                StringTokenizer dataSt = new StringTokenizer((String) hmpVector.get(i));
                dataSt.nextToken(); //skip the -

                //ID
                String newid = dataSt.nextToken();  //individual ID with _c1/_c2
                StringTokenizer filter = new StringTokenizer(newid, "_:");
                String indivID = filter.nextToken();
                String strandID = filter.nextToken();

                //DEAL WITH NEW IDs
                if (!strandID.startsWith("c")) {
                    if (hapMapTranslate.containsKey(indivID)) {
                        String currentValue = (String) hapMapTranslate.get(indivID);
                        indivID = indivID + "_" + strandID;
                        hapMapTranslate.put(indivID, currentValue);
                    }

                    strandID = filter.nextToken();
                }

                String currAlleles = dataSt.nextToken();

                if (strandID.equals("c1")) {
                    //CHECK IF WE CAPTURED A STRAND FOR N TYPE PROCESSING
                    if (GoodStrands.size() > 1) {
                        strandWaiting = true;
                    } else {
                        strandWaiting = false;
                    }

                    //CHECK THE NEXT LINE, MAKE SURE IT IS NOT ALL N's
                    StringTokenizer nextStrand = new StringTokenizer((String) hmpVector.get(i + 1), ":- \t");
                    String nextID = nextStrand.nextToken();
                    String tmpAlleles = nextStrand.nextToken();
                    int nCount = 0;

                    for (int j = 0; j < tmpAlleles.length(); j++) {
                        char allele = tmpAlleles.charAt(j);
                        if (allele == 'N') {
                            nCount++;
                        }
                    }

                    if (nCount / tmpAlleles.length() == 1) {
                        foundNStrand = true;
                        if (!strandWaiting) {
                            GoodStrands.add(nextID);
                            GoodStrands.add(currAlleles);
                        }
                    } else {

                        foundNStrand = false;

                    }
                }

                //SET ALLELE DATA FOR N STRANDS
                if (foundNStrand) {
                    if (strandWaiting) {
                        if (strandID.equals("c2")) {

                            currAlleles = (String) GoodStrands.get(1);
                            GoodStrands.removeAllElements();
                            foundNStrand = false;
                        }
                    }
                }

                //CHECK SEX
                columns = currAlleles.length();
                if (strandID.equals("c1")) {   //Only set up a new individual on c1.
                    ind = new Individual(columns, true);
                    ind.setIndividualID(indivID);
                    if (columns != legendMarkers.size()) {
                        throw new PedFileException("File error: invalid number of markers on Individual " + ind.getIndividualID());
                    }

                    String details;
                    //GO THROUGH SAVED DATA
                    if (hapMapTranslate.containsKey(ind.getIndividualID())) {
                        details = (String) hapMapTranslate.get(ind.getIndividualID());
                    } else {
                        continue;
                    }

                    //TOKENIZE AND GO THROUGH LINES
                    StringTokenizer dt = new StringTokenizer(details, "\n\t\" \"");
                    ind.setFamilyID(dt.nextToken().trim());
                    //skip individualID since we already have it.
                    dt.nextToken();
                    ind.setDadID(dt.nextToken());
                    ind.setMomID(dt.nextToken());

                    try {
                        ind.setGender(Integer.parseInt(dt.nextToken().trim()));
                        ind.setAffectedStatus(Integer.parseInt(dt.nextToken().trim()));
                    } catch (NumberFormatException nfe) {
                        throw new PedFileException("File error: invalid gender or affected status for indiv " + ind.getIndividualID());
                    }
                    if (!pseudoChecked) {
                        if (ind.getGender() == Individual.MALE) {
                            pseudoChecked = true;
                            if (Chromosome.getDataChrom().equalsIgnoreCase("chrx")) {
                                StringTokenizer checkSt = new StringTokenizer((String) hmpVector.get(i + 1), ":- \t");
                                String checkNewid = checkSt.nextToken();
                                checkSt.nextToken(); //alleles
                                StringTokenizer checkFilter = new StringTokenizer(checkNewid, "_");
                                checkFilter.nextToken();
                                String checkStrand = checkFilter.nextToken();
                                if (checkStrand.equals("c2")) {
                                    Chromosome.setDataChrom("chrp");
                                }
                            }
                        }
                    }

                    //check if the family exists already in the Hashtable
                    Family fam = (Family) this.families.get(ind.getFamilyID());
                    if (fam == null) {
                        //it doesnt exist, so create a new Family object
                        fam = new Family(ind.getFamilyID());
                    }
                    fam.addMember(ind);
                    this.families.put(ind.getFamilyID(), fam);
                    this.allIndividuals.add(ind);
                }

                //PARSE ALLELES
                int index = 0;
                if (strandID.equals("c1")) {
                    byteDataT = new byte[columns];
                } else {
                    byteDataU = new byte[columns];
                }

                for (int k = 0; k < columns; k++) {
                    token = currAlleles.charAt(k);
                    if (strandID.equals("c1")) {
                        if (token == 'A') {
                            byteDataT[index] = 1;
                        } else if (token == 'C') {
                            byteDataT[index] = 2;
                        } else if (token == 'G') {
                            byteDataT[index] = 3;
                        } else if (token == 'T') {
                            byteDataT[index] = 4;
                        } else if (token == 'N') {
                            //Unknowns are assigned an N by Hapmap now on missing parents
                            byteDataU[index] = 0;
                        } else {
                            throw new PedFileException("Invalid Allele: " + token);
                        }
                    } else {
                        if (token == 'A') {
                            byteDataU[index] = 1;
                        } else if (token == 'C') {
                            byteDataU[index] = 2;
                        } else if (token == 'G') {
                            byteDataU[index] = 3;
                        } else if (token == 'T') {
                            byteDataU[index] = 4;
                        } else if (token == '-') {
                            /*if (!(Chromosome.getDataChrom().equalsIgnoreCase("chrx"))){
                                throw new PedFileException("Missing allele on non X-chromosome data");
                            }else{
                                byteDataU[index] = byteDataT[index];
                            }*/
                            throw new PedFileException("Haploview does not currently support regions encompassing both\n"
                                    + "pseudoautosomal and non-pseudoautosomal markers.");
                        } else if (token == 'N') {
                            //Unknowns are assigned an N by Hapmap now on missing parents
                            byteDataU[index] = 0;
                        } else {

                            throw new PedFileException("File format error.");
                        }
                    }
                    index++;
                }

                if (strandID.equals("c2")) {
                    for (int j = 0; j < columns; j++) {

                        ind.addMarker(byteDataT[j], byteDataU[j]);

                    }
                } else if (strandID.equals("c1") && (ind.getGender() == Individual.MALE) &&
                        (Chromosome.getDataChrom().equalsIgnoreCase("chrx"))) {
                    for (int j = 0; j < columns; j++) {
                        ind.addMarker(byteDataT[j], byteDataT[j]);
                    }
                }
            }
        } catch (IOException io) {
            throw new IOException("Could not connect to HapMap database.");
        }
    }

/*    public void parseFastPhase(String[] info) throws IOException, PedFileException{
        if (info[3].equals("")){
            Chromosome.setDataChrom("none");
        }else{
            Chromosome.setDataChrom("chr" + info[3]);
        }
        Chromosome.setDataBuild("ncbi_b35");
        BufferedReader reader;
        InputStream inStream;

        try {
            URL inURL = new URL(info[0]);
            inStream = inURL.openStream();
        }catch (MalformedURLException mfe){
            File inFile = new File(info[0]);
            if (inFile.length() < 1){
                throw new PedFileException("Genotype file is empty or non-existent: " + inFile.getName());
            }
            inStream = new FileInputStream(inFile);
        }catch (IOException ioe){
            throw new PedFileException("Could not connect to " + info[0]);
        }

        if (Options.getGzip()){
            GZIPInputStream sampleInputStream = new GZIPInputStream(inStream);
            reader = new BufferedReader(new InputStreamReader(sampleInputStream));
        }else{
            reader = new BufferedReader(new InputStreamReader(inStream));
        }
        this.allIndividuals = new Vector();
        //TODO: put fastPHASE parsing code here
*//*        byte[] byteDataT = new byte[0];
        byte[] byteDataU = new byte[0];
        char token;
        int numMarkers;
        int lineNumber = 0;
        String line;
        Individual ind = null;
        while((line = reader.readLine())!=null){
            StringTokenizer st = new StringTokenizer(line);
            if (st.countTokens() != 5){
                throw new PedFileException("Invalid file formatting on line " + lineNumber+1);
            }
            String markers = new String(st.nextToken());
            st.nextToken(); //marker numbering
            int gender = Integer.parseInt(st.nextToken());
            String id = st.nextToken();
            char strand = st.nextToken().charAt(0); // T or U
            numMarkers = markers.length();
            if (strand == 'T'){
                ind = new Individual(numMarkers, true);
                ind.setGender(gender);
                ind.setIndividualID(id);
                ind.setFamilyID("Bender");
                ind.setDadID("0");
                ind.setMomID("0");
                byteDataT = new byte[numMarkers];

                //check if the family exists already in the Hashtable
                Family fam = (Family)this.families.get(ind.getFamilyID());
                if(fam == null){
                    //it doesnt exist, so create a new Family object
                    fam = new Family(ind.getFamilyID());
                }
                fam.addMember(ind);
                this.families.put(ind.getFamilyID(),fam);
                this.allIndividuals.add(ind);
            }else{
                byteDataU = new byte[numMarkers];
            }

            int index = 0;
            for (int i = 0; i < numMarkers; i++){
                token = markers.charAt(i);
                if (strand == 'T'){
                    if (token == '1'){
                        byteDataT[index] = 1;
                    }else if (token == '2'){
                        byteDataT[index] = 2;
                    }else if (token == '3'){
                        byteDataT[index] = 3;
                    }else if (token == '4'){
                        byteDataT[index] = 4;
                    }else {
                        throw new PedFileException("Invalid Allele: " + token);
                    }
                }else{
                    if (token == '1'){
                        byteDataU[index] = 1;
                    }else if (token == '2'){
                        byteDataU[index] = 2;
                    }else if (token == '3'){
                        byteDataU[index] = 3;
                    }else if (token == '4'){
                        byteDataU[index] = 4;
                    }else {
                        throw new PedFileException("Invalid Allele: " + token);
                    }
                }
                index++;
            }

            if (strand == 'U'){
                for(int j=0; j < numMarkers; j++){
                    ind.addMarker(byteDataT[j], byteDataU[j]);
                }
            }
            lineNumber++;
        }*//*
    }*/

    public void parseHapsFile(Vector individs) throws PedFileException {
        //This method is used to parse haps files which now go through similar processing to ped files.
        String currentLine;
        byte[] genos = new byte[0];
        String ped, indiv;
        int numLines = individs.size();
        if (numLines == 0) {
            throw new PedFileException("Data format error: empty file");
        }

        Individual ind = null;
        this.allIndividuals = new Vector();
        int lineCount = 0;
        int numTokens = 0;
        Vector chromA = new Vector();
        Vector chromB = new Vector();
        boolean hapsEven = false;
        boolean hapsError = false;

        for (int i = 0; i < numLines; i++) {
            lineCount++;
            currentLine = (individs.get(i)).toString();
            if (currentLine.length() == 0) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(currentLine);
//first two tokens are expected to be ped, indiv
            if (st.countTokens() > 2) {
                ped = st.nextToken();
                indiv = st.nextToken();
            } else {
                throw new PedFileException("Genotype file error:\nLine " + lineCount +
                        " appears to have fewer than 3 columns.");
            }
            if (hapsEven) {
                ind = new Individual(st.countTokens(), false);
                ind.setFamilyID(ped);
                ind.setIndividualID(indiv);
                ind.setDadID("");
                ind.setMomID("");
                ind.setGender(0);
                ind.setAffectedStatus(0);
            }

            //all other tokens are loaded into a vector (they should all be genotypes)
            genos = new byte[st.countTokens()];

            int q = 0;

            if (numTokens == 0) {
                numTokens = st.countTokens();
            }
            if (numTokens != st.countTokens()) {
                throw new PedFileException("Genotype file error:\nLine " + lineCount +
                        " appears to have an incorrect number of entries");
            }
            //Allowed for A/C/G/T input in Haps files.
            while (st.hasMoreTokens()) {
                String thisGenotype = (String) st.nextElement();
                if (!hapsEven) {
                    chromA.add(thisGenotype);
                } else {
                    chromB.add(thisGenotype);
                }
                if (thisGenotype.equalsIgnoreCase("h")) {
                    genos[q] = 9;
                } else if (thisGenotype.equalsIgnoreCase("A")) {
                    genos[q] = 1;
                } else if (thisGenotype.equalsIgnoreCase("C")) {
                    genos[q] = 2;
                } else if (thisGenotype.equalsIgnoreCase("G")) {
                    genos[q] = 3;
                } else if (thisGenotype.equalsIgnoreCase("T")) {
                    genos[q] = 4;
                } else {
                    try {
                        genos[q] = Byte.parseByte(thisGenotype);
                    } catch (NumberFormatException nfe) {
                        throw new PedFileException("Genotype file input error:\ngenotype value \""
                                + thisGenotype + "\" on line " + lineCount + " not allowed.");
                    }
                }
                //Allele values other then 0-4 or 9 generate exceptions.
                if ((genos[q] < 0 || genos[q] > 4) && (genos[q] != 9)) {
                    throw new PedFileException("Genotype file input error:\ngenotype value \"" + genos[q] +
                            "\" on line " + lineCount + " not allowed.");
                }
                q++;
            }

            if (hapsEven) {
                for (int m = 0; m < chromA.size(); m++) {
                    if (((String) chromA.get(m)).equalsIgnoreCase("h")) {
                        chromA.set(m, "9");
                    } else if (((String) chromA.get(m)).equalsIgnoreCase("A")) {
                        chromA.set(m, "1");
                        hapsError = !hapsError;
                    } else if (((String) chromA.get(m)).equalsIgnoreCase("C")) {
                        chromA.set(m, "2");
                        hapsError = !hapsError;
                    } else if (((String) chromA.get(m)).equalsIgnoreCase("G")) {
                        chromA.set(m, "3");
                        hapsError = !hapsError;
                    } else if (((String) chromA.get(m)).equalsIgnoreCase("T")) {
                        chromA.set(m, "4");
                        hapsError = !hapsError;
                    }
                    if (((String) chromB.get(m)).equalsIgnoreCase("h")) {
                        chromB.set(m, "9");
                    } else if (((String) chromB.get(m)).equalsIgnoreCase("A")) {
                        chromB.set(m, "1");
                        hapsError = !hapsError;
                    } else if (((String) chromB.get(m)).equalsIgnoreCase("C")) {
                        chromB.set(m, "2");
                        hapsError = !hapsError;
                    } else if (((String) chromB.get(m)).equalsIgnoreCase("G")) {
                        chromB.set(m, "3");
                        hapsError = !hapsError;
                    } else if (((String) chromB.get(m)).equalsIgnoreCase("T")) {
                        chromB.set(m, "4");
                        hapsError = !hapsError;
                    }
                    if (hapsError) {
                        throw new PedFileException("File input error: Individual " + ind.getFamilyID() + " strand " + ind.getIndividualID() + ", marker " + (m + 1) +
                                ".\nFor any marker, an individual's genotype must be only letters or only numbers.");
                    }
                    byte allele1 = Byte.parseByte(chromA.get(m).toString());
                    byte allele2 = Byte.parseByte(chromB.get(m).toString());
                    ind.addMarker(allele1, allele2);
                }
                //check if the family exists already in the Hashtable
                Family fam = (Family) this.families.get(ind.getFamilyID());
                if (fam == null) {
                    //it doesnt exist, so create a new Family object
                    fam = new Family(ind.getFamilyID());
                }

                if (fam.getMembers().containsKey(ind.getIndividualID())) {
                    throw new PedFileException("Individual " + ind.getIndividualID() + " in family " + ind.getFamilyID() + " appears more than once.");
                }

                fam.addMember(ind);
                this.families.put(ind.getFamilyID(), fam);
                this.allIndividuals.add(ind);
                chromA = new Vector();
                chromB = new Vector();
            }
            hapsEven = !hapsEven;
        }
        if (hapsEven) {
            //we're missing a line here
            throw new PedFileException("Genotype file appears to have an odd number of lines.\n" +
                    "Each individual is required to have two chromosomes");
        }
    }

    public int[] checkGenotype(String allele) throws PedFileException {
        //This method cleans up the genotype checking process for hap map and ped files & allows for both numerical and alphabetical input.
        int[] genotype = new int[2];

        if (allele.equalsIgnoreCase("N")) {
            genotype[0] = 0;
        } else if (allele.equalsIgnoreCase("A")) {
            genotype[0] = 1;
        } else if (allele.equalsIgnoreCase("C")) {
            genotype[0] = 2;
        } else if (allele.equalsIgnoreCase("G")) {
            genotype[0] = 3;
        } else if (allele.equalsIgnoreCase("T")) {
            genotype[0] = 4;
        } else {
            genotype[0] = Integer.parseInt(allele.trim());
            genotype[1] = 1;
        }
        return genotype;
    }

    public Vector check() throws PedFileException {
        //before we perform the check we want to prune out individuals with too much missing data
        //or trios which contain individuals with too much missing data

        Iterator fitr = families.values().iterator();
        Vector useable = new Vector();
        while (fitr.hasNext()) {
            Family curFam = (Family) fitr.next();
            Enumeration indIDEnum = curFam.getMemberList();
            Vector victor = new Vector();
            while (indIDEnum.hasMoreElements()) {
                victor.add(curFam.getMember((String) indIDEnum.nextElement()));
            }

            PedParser pp = new PedParser();
            try {
                SimpleGraph sg = pp.buildGraph(victor, Options.getMissingThreshold());
                Vector indStrings = pp.parsePed(sg);
                if (indStrings != null) {
                    Iterator sitr = indStrings.iterator();
                    while (sitr.hasNext()) {
                        useable.add(curFam.getMember((String) sitr.next()));
                    }
                }
            } catch (PedigreeException pe) {
                String pem = pe.getMessage();
                if (pem.indexOf("one parent") != -1) {
                    indIDEnum = curFam.getMemberList();
                    while (indIDEnum.hasMoreElements()) {
                        curFam.getMember((String) indIDEnum.nextElement()).setReasonImAxed(pem);
                    }
                } else {
                    throw new PedFileException(pem + "\nin family " + curFam.getFamilyName());
                }
            }
        }

        unrelatedIndividuals = useable;

        Vector indList = (Vector) allIndividuals.clone();
        Individual currentInd;
        Family currentFamily;

        //deal with individuals who are missing too much data
        for (int x = 0; x < indList.size(); x++) {
            currentInd = (Individual) indList.elementAt(x);
            currentFamily = getFamily(currentInd.getFamilyID());

            if (currentInd.getGenoPC() < 1 - Options.getMissingThreshold()) {
                allIndividuals.removeElement(currentInd);
                axedPeople.add(currentInd);
                currentInd.setReasonImAxed("% Genotypes: " + new Double(currentInd.getGenoPC() * 100).intValue());
                currentFamily.removeMember(currentInd.getIndividualID());
                if (currentFamily.getNumMembers() == 0) {
                    //if everyone in a family is gone, we remove it from the list
                    families.remove(currentInd.getFamilyID());
                }
            } else if (!useable.contains(currentInd)) {
                axedPeople.add(currentInd);
                if (currentInd.getReasonImAxed() == null) {
                    currentInd.setReasonImAxed("Not a member of maximum unrelated subset.");
                }
            }
        }
        if (useable.size() == 0) {
            //todo: this should be more specific about the problems.
            throw new PedFileException("File contains zero valid individuals.");
        }

        setMendelsExist(false);
        CheckData cd = new CheckData(this);
        Vector results = cd.check();
        this.results = results;
        return results;
    }

    public String[][] getHMInfo() {
        return hminfo;
    }

    public Vector getResults() {
        return results;
    }

    public void setResults(Vector res) {
        results = res;
    }

    public Vector getAxedPeople() {
        return axedPeople;
    }

    public boolean isBogusParents() {
        return bogusParents;
    }

    public Vector getTableData() {
        Vector tableData = new Vector();
        int numResults = results.size();
        markerRatings = new int[numResults];
        dups = new int[numResults];
        for (int i = 0; i < numResults; i++) {
            Vector tempVect = new Vector();
            MarkerResult currentResult = (MarkerResult) results.get(i);
            tempVect.add(new Integer(i + 1));
            if (Chromosome.getUnfilteredMarker(0).getName() != null) {
                tempVect.add(Chromosome.getUnfilteredMarker(i).getDisplayName());
                tempVect.add(new Long(Chromosome.getUnfilteredMarker(i).getPosition()));
            }
            tempVect.add(new Double(currentResult.getObsHet()));
            tempVect.add(new Double(currentResult.getPredHet()));
            tempVect.add(new Double(currentResult.getHWpvalue()));
            tempVect.add(new Double(currentResult.getGenoPercent()));
            tempVect.add(new Integer(currentResult.getFamTrioNum()));
            tempVect.add(new Integer(currentResult.getMendErrNum()));
            tempVect.add(new Double(currentResult.getMAF()));
            tempVect.add(currentResult.getMajorAllele() + ":" + currentResult.getMinorAllele());

            int dupStatus = Chromosome.getUnfilteredMarker(i).getDupStatus();
            if ((currentResult.getRating() > 0 && dupStatus != 2) ||
                    isWhiteListed(Chromosome.getUnfilteredMarker(i))) {
                tempVect.add(new Boolean(true));
            } else {
                tempVect.add(new Boolean(false));
            }

            //these values are never displayed, just kept for bookkeeping
            markerRatings[i] = currentResult.getRating();
            dups[i] = dupStatus;

            tableData.add(tempVect.clone());
        }

        return tableData;
    }

    public int[] getMarkerRatings() {
        return markerRatings;
    }

    public int[] getDups() {
        return dups;
    }

    public Vector getColumnNames() {
        Vector c = new Vector();
        c = new Vector();
        c.add("#");
        if (Chromosome.getUnfilteredMarker(0).getName() != null) {
            c.add("Name");
            c.add("Position");
        }
        c.add("ObsHET");
        c.add("PredHET");
        c.add("HWpval");
        c.add("%Geno");
        c.add("FamTrio");
        c.add("MendErr");
        c.add("MAF");
        c.add("Alleles");
        c.add("Rating");
        return c;
    }

    public void saveCheckDataToText(File outfile) throws IOException {
        FileWriter checkWriter = null;
        if (outfile != null) {
            checkWriter = new FileWriter(outfile);
        } else {
            throw new IOException("Error saving checkdata to file.");
        }

        Vector names = getColumnNames();
        int numCols = names.size();
        StringBuffer header = new StringBuffer();
        for (int i = 0; i < numCols; i++) {
            header.append(names.get(i)).append("\t");
        }
        header.append("\n");
        checkWriter.write(header.toString());

        Vector tableData = getTableData();
        for (int i = 0; i < tableData.size(); i++) {
            StringBuffer sb = new StringBuffer();
            Vector row = (Vector) tableData.get(i);
//don't print the true/false vals in last column
            for (int j = 0; j < numCols - 1; j++) {
                sb.append(row.get(j)).append("\t");
            }
            //print BAD if last column is false
            if (((Boolean) row.get(numCols - 1)).booleanValue()) {
                sb.append("\n");
            } else {
                sb.append("BAD\n");
            }
            checkWriter.write(sb.toString());
        }

        checkWriter.close();
    }

    public void setWhiteList(HashSet whiteListedCustomMarkers) {
        whitelist = whiteListedCustomMarkers;
    }

    public boolean isWhiteListed(SNP snp) {
        return whitelist.contains(snp);
    }

    public Vector getHaploidHets() {
        return haploidHets;
    }

    public void addHaploidHet(String haploid) {
        if (haploidHets != null) {
            haploidHets.add(haploid);
        } else {
            haploidHets = new Vector();
            haploidHets.add(haploid);
        }
    }

    public boolean getMendelsExist() {
        return mendels;
    }

    public void setMendelsExist(boolean mendel) {
        mendels = mendel;
    }
}


