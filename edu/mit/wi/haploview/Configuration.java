package edu.mit.wi.haploview;

import java.util.*;
import java.io.*;
/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Aug 25, 2004
 * Time: 10:54:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class Configuration {
    private static Properties config;

    private static void initializeConfig() {
        Properties defaults = new Properties();
        defaults.setProperty("checkForUpdate","true");
        config = new Properties(defaults);
    }


    public static void readConfigFile() {
        if(config == null) {
            initializeConfig();
        }

        String homeDir = System.getProperty("user.home");
        File configFile = new File(homeDir + "/.haploview");
        if(configFile.exists()) {
            BufferedInputStream bis = null;
            try {
                bis = new BufferedInputStream(new FileInputStream(configFile));
                config.load(bis);
                bis.close();
            } catch(IOException ioe) {
                //it doesnt really matter if we cant read the file; we just use the defaults
            }

        }
    }

    public static void writeConfigFile() {
        if(config != null) {
            String homeDir = System.getProperty("user.home");
            File configFile = new File(homeDir + "/.haploview");
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(configFile));
                config.store(bos, "Haploview Configuration file. Automatically generated, do not edit");
            } catch(IOException ioe) {
                //well, something went wrong writing the file, but we dont really care
            }
        }
    }

    public static boolean isCheckForUpdate() {
        if(config == null) {
            initializeConfig();
        }
        return (config.getProperty("checkForUpdate").equals("true"));
    }

    public static void setCheckForUpdate(boolean b) {
        if(config == null) {
            initializeConfig();
        }
        if(b) {
            config.setProperty("checkForUpdate", "true");
        } else {
            config.setProperty("checkForUpdate", "false");
        }

    }
}
