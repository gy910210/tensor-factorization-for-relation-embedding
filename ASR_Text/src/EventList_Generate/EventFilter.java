package EventList_Generate;

import java.util.HashSet;
import java.io.*;

import Util.KnowledgeBase.Formalization;

public class EventFilter
{
	private static Formalization FML;
	private final static String SEED_PATH_1 = "data/event/toMerge/findSounds.seed";
	private final static String SEED_PATH_2 = "data/event/toMerge/soundRangers.seed";
	private final static String MERGED_PATH = "data/event/Merged/merged_seed.txt";
	
	private final static String [] Prefix = {"sound in", "sound of", "man", "woman", "different", "various"};
	private final static String [] Suffix = {"sound effects", "sound effect", "ambience sound", "ambient sound", "sound", "effect", "sfx", "ambience", "ambient", "ambiance", "noise", "etc"};
	private final static String [] NoScoreArray = {"in", "the", "a", "an", "or", "and", "of", "that", "not", "no", "do", "very",
			"up", "down", "on", "under", "with", "voice", "by", "for", "surround", "out"};
	
	private static HashSet<String> eventSet = new HashSet<String>();
	
	public static void Implement()throws Exception
	{
		GroupSeed();	
	}
	
	public static void Formalize()throws Exception
	{
		FML = new Formalization(Prefix, Suffix, NoScoreArray);
	}
	
	public static HashSet<String> lemmatize(HashSet<String> rawSet) throws Exception
	{
		return FML.lemmatize(rawSet);
	}
	
	public void formalize(String rawUri, HashSet<String> rawSet, String formUri) throws Exception
	{
		FML.formalize(rawUri, rawSet, formUri);
	}
	
	// Group seed events to Merged_seed.txt
	public static void GroupSeed() throws Exception
	{
		BufferedReader br1 = new BufferedReader(new FileReader(SEED_PATH_1));
		String line = null;
		
		while((line = br1.readLine())!=null)
				eventSet.add(line);	
		
		br1.close();
		
		
		BufferedReader br2 = new BufferedReader(new FileReader(SEED_PATH_2));
		while((line = br2.readLine())!=null)		
				eventSet.add(line);
			
		br2.close();
		
		
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(MERGED_PATH));
		for(String st1 : eventSet)
		{
			bw.append(st1);
			bw.newLine();
		}
		
		bw.close();
	}

    
}