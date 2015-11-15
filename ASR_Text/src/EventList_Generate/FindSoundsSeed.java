package EventList_Generate;

import java.net.URL;
import java.util.HashSet;
import java.io.*;

import Util.KnowledgeBase.*;

public class FindSoundsSeed {
	final private static String TmpDir = "data/event/tmp/";
	final private static String CategoryUrlStr = "http://www.findsounds.com/types.html"; 
	private static HashSet<String> fsEventSet;
	
	public static void Implement()throws Exception
	{
		fsEventSet = new HashSet<String>();
		URL categoryUrl = new URL(CategoryUrlStr);
		InputStream categoryIs = categoryUrl.openStream();
		BufferedReader categoryBr = new BufferedReader(new InputStreamReader(categoryIs,"UTF-8"));
		File rawFile = new File(TmpDir + "findSounds.seed.raw");
		BufferedWriter rawBw = new BufferedWriter(new FileWriter(rawFile, false));
		boolean flag = true;
		String line = "";
		while((line = categoryBr.readLine()) != null){
			
			if(line.contains("TV and Movies"))
				flag = false;
			
			if(line.contains("Vehicles"))
				flag = true;
			
			
			if(line.contains("</A>") && flag)
			{	
				Utils.bwWriteLine(rawBw, line.substring(line.indexOf(">")+1, line.indexOf("</A>")));
			}
		}
		categoryIs.close();
		categoryBr.close();
		rawBw.close();
		
		Filter.trimDuplicate(TmpDir + "findSounds.seed.raw", TmpDir + "findSounds.seed");
		
	}
	
}
