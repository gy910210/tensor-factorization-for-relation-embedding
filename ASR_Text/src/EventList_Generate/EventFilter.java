package EventList_Generate;

import java.util.HashSet;
import java.io.*;

public class EventFilter
{
	private final static String SEED_PATH_1 = "data/event/findSounds.seed";
	private final static String SEED_PATH_2 = "data/event/mediaCollege.seed";
//	private final static String SEED_PATH_1 = "data/event/test.txt";
//	private final static String SEED_PATH_2 = "data/event/test2.txt";
	private final static String SEED_PATH_3 = "data/event/soundRangers.seed";
	private final static String MERGED_PATH = "data/event/merged_seed.txt";
	
	private static String ILLEGAL_REGEX =  "[^a-zA-Z,'\\s]|sound\\s+effect[s]?|sound[s]?";
	private static HashSet<String> eventSet = new HashSet<String>();
	
	
	
	public static void GroupSeed() throws Exception
	{
		BufferedReader br1 = new BufferedReader(new FileReader(SEED_PATH_1));
		String line = null;
		String[] st = null;
		
		while((line = br1.readLine())!=null)
		{
			line = line.toLowerCase().replaceAll(ILLEGAL_REGEX," ").trim();
			if(line.contains(","))
			{
				st = line.split(",");
				for(String s : st)
				{
					s = s.trim();
					eventSet.add(s);
				}
			}
			else{
				eventSet.add(line);
			}
			
		}
		br1.close();
		
		
		BufferedReader br2 = new BufferedReader(new FileReader(SEED_PATH_2));
		while((line = br2.readLine())!=null)
		{
			line = line.toLowerCase().replaceAll(ILLEGAL_REGEX," ").trim();
			if(line.contains(","))
			{
				st = line.split(",");
				for(String s : st)
				{
					s = s.trim();
					eventSet.add(s);
				}
			}
			else{
				eventSet.add(line);
			}
			
		}
		br2.close();
		
		BufferedReader br3 = new BufferedReader(new FileReader(SEED_PATH_3));
		while((line = br3.readLine())!=null)
		{
			line = line.toLowerCase().replaceAll(ILLEGAL_REGEX," ").trim();
			if(line.contains(","))
			{
				st = line.split(",");
				for(String s : st)
				{
					s = s.trim();
					eventSet.add(s);
				}
			}
			else{
				eventSet.add(line);
			}
			
		}
		br3.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(MERGED_PATH));
		for(String st1 : eventSet)
		{
			bw.append(st1);
			bw.newLine();
		}
		
		bw.close();
		
	}
}