import java.util.*;
import java.io.*;

class JNITest {
    public native String runEM(int num_haplos, int num_loci, String[] input_haplos, int num_blocks, int[] block_size);

    public static void main(String[] args) throws IOException{
	System.loadLibrary("haplos");
	Vector inputHaploVector = new Vector();
	
	File resultFile = new File("jnitest.txt");
	BufferedReader resultReader = new BufferedReader(new FileReader(resultFile));
	String currentLine;
	while ((currentLine = resultReader.readLine()) != null){
	    inputHaploVector.add(currentLine);
	}
	resultReader.close();
	String[] input_haplos = (String[])inputHaploVector.toArray(new String[0]);
	
	int[] block_size = {2};
	
	String results = new JNITest().runEM(92, 2, input_haplos, 1, block_size);
	System.out.println(results);
    }
}
