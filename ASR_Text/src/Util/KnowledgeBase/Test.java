package Util.KnowledgeBase;
import java.io.*;

public class Test
{
	private static final String DEBUG_PATH_IN = "data/debug/test_in.txt";
	private static final String DEBUG_PATH_OUT = "data/debug/test_out.txt";
	
	public static void Implementation() throws Exception{
		//Filter.trimDuplicate(DEBUG_PATH_IN, DEBUG_PATH_OUT);
		BufferedWriter bw = new BufferedWriter(new FileWriter(DEBUG_PATH_OUT));
        for(int i = 0; i < 10; ++i)
        {
        	bw.write(Integer.toString(i));
        	bw.newLine();
     
        }
        bw.close();
   
		
	}
	
	
}