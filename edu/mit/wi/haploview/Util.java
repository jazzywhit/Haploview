package edu.mit.wi.haploview;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;

public abstract class Util {

    public static String formatPValue(double pval){
         DecimalFormat df;
        //java truly sucks for simply restricting the number of sigfigs but still
        //using scientific notation when appropriate
        if (pval < 0.0001){
            df = new DecimalFormat("0.0000E0", new DecimalFormatSymbols(Locale.US));
        }else{
            df = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
        }
        String formattedNumber =  df.format(pval, new StringBuffer(), new FieldPosition(NumberFormat.INTEGER_FIELD)).toString();
        return formattedNumber;
    }
}
