package edu.mit.wi.pedfile;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;
//import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: Jul 15, 2003
 * Time: 10:24:22 AM
 * To change this template use Options | File Templates.
 */
public class Test {
    public static void main(String[] args){
        try {
            String line;
            Vector lines = new Vector();
            BufferedReader reader = new BufferedReader(new FileReader("/home/jules/clean.ped"));
            while((line=reader.readLine())!=null){
                lines.add(line);
            }
            PedFile myFile = new PedFile();
            myFile.parse(lines);

            /*Enumeration famList = myFile.getFamList();
            while(famList.hasMoreElements()){
               Family fam = myFile.getFamily((String)famList.nextElement());
               Enumeration members = fam.getMemberList();
                while(members.hasMoreElements()){
                    Individual ind = fam.getMember((String)members.nextElement());
                    System.out.println(fam.getFamilyName() + "\t" + ind.getIndividualID());
                }
            } */

	        myFile.check();


        }
        catch(IOException e){
            System.out.println(e);
        }


    }
}
