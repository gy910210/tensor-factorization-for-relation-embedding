package EventList_Generate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import Util.KnowledgeBase.*;

public class SoundRangersSeed {
	final private static String TmpDir = "data/event/tmp/";
	final private static String CategoryUrlStr = "http://www.soundrangers.com/index.cfm?category=1&left_cat=1"; 
    private static List<String> Illegal_Type = Arrays.asList("Buttons, Interface and Video Game Sounds",
    		"Creature and Monster Sounds","Door Sound Effects","Imaging Elements Sound Effects",
    		"Sci-Fi, Electronic, Fantasy","Sound Design and Foley",
    		"Voice Prompts","Whoosh Sound Effects","Toy and Game Sound Effects");
	private static String removeRex = "sounds?|sound effects";
    
    public static void Implement() throws Exception{
    	
		URL categoryUrl = new URL(CategoryUrlStr);
		InputStream categoryIs = categoryUrl.openStream();
		BufferedReader categoryBr = new BufferedReader(new InputStreamReader(categoryIs,"UTF-8"));
		File rawFile = new File(TmpDir + "soundRangers.seed.raw");
		BufferedWriter rawBw = new BufferedWriter(new FileWriter(rawFile, false));
		
		boolean flag = false;
		String line = "";
		
		while((line = categoryBr.readLine()) != null){
			
			if(line.contains("</a></span></td>"))
			{
				line = line.substring(line.indexOf(" >") + 2, line.indexOf("</a"));
				if(Illegal_Type.contains(line))
					flag = false;
				else
					flag = true;
			}
			
			if(line.contains("<td width=\"33%\"><img") && flag)
			{
				line = line.substring(line.indexOf("\" >") + 3, line.indexOf("</a>")).trim().toLowerCase();
				if(line.contains("sounds"))
				{
					line = line.replace("sounds","");
				}
				
				if(line.contains("sound effects"))
				{
					line = line.replace("sound effects","");
				}
				
				Utils.bwWriteLine(rawBw, line );
			}
		
		}
		categoryIs.close();
		categoryBr.close();
		rawBw.close();
		
		Filter(TmpDir + "soundRangers.seed.raw",TmpDir + "soundRangers.seed");
	}
    
    private static void Filter(String inputPath, String outputPath) throws Exception
    {
       BufferedReader br = new BufferedReader(new FileReader(inputPath));
       BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
       
       HashSet<String> prefixSet = new HashSet<String>();
       String line="";
       String[] tmp;
       boolean duplicate = false;
       
       // Filter:  1.remove discription 2.remove duplicate 
       while((line = br.readLine()) != null)
       {
    	   if(line.contains(" and "))
    	   {
    		   System.out.println(line);
    		   tmp = line.split(" and ");
    		   System.out.println(tmp[0]);
    		   System.out.println(tmp[1]);
    		   bw.append(tmp[0]);
    		   bw.newLine();
    		   bw.append(tmp[1]);
    		   bw.newLine();
    	   }
    	   else{
    		   
    		   tmp = line.split("\\s+");
    		   if( !prefixSet.contains(tmp[0]))
        	   {  
        		   System.out.print(line+">>>>>>");
        		   prefixSet.add(tmp[0]);
        	       duplicate = false;
        	   }
        	   else
        	   {
        		   duplicate = true;
        	   }
        	   
        	   if(!duplicate)
        	   {
        		   tmp[0] = tmp[0].replace(",", "");
        		   System.out.println(tmp[0]);
        		   bw.append(tmp[0]);
        		   bw.newLine();
        	   }
    	   }
    	   
    	  
       }
       br.close();
       bw.close();
    }
}
