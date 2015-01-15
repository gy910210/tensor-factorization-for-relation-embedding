package SceneList_Generate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.*;

import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import Util.KnowledgeBase.*;

public class SceneKB
{
	public static String RAW_PATH;
	public static String FILTERED_PATH;
	
	public static void Implementation_WN(String outputPath) throws Exception
	{   
		HashSet<String> contextSet = new HashSet<String>();
		
		RAW_PATH = outputPath + "raw_scene_list_wn4.txt";
		FILTERED_PATH = outputPath + "filtered_scene_list_wn4.txt";
		
		// WordNet contexts
		WordNet wn = new WordNet();
		HashMap<String, Integer> wordSenseMap = new HashMap<String, Integer>();
		/*wordSenseMap.put("activity", 0);*/
//		wordSenseMap.put("scene", 0);
//		wordSenseMap.put("occasion", 1);
//		wordSenseMap.put("social_event", 0);
//		wordSenseMap.put("construction", 2);
//		wordSenseMap.put("facility", 0);
//		wordSenseMap.put("facility", 3);
//		wordSenseMap.put("vehicle", 0);
//		wordSenseMap.put("organization", 0);
//		wordSenseMap.put("field", 0);
//		wordSenseMap.put("field", 7);
//		wordSenseMap.put("geological_formation", 0);
//		wordSenseMap.put("geological_phenomenon", 0);
//		wordSenseMap.put("workplace", 0);
//		wordSenseMap.put("building", 0);
//		wordSenseMap.put("eating_place", 0);
//		wordSenseMap.put("public_holiday", 0);
//		wordSenseMap.put("public_transport", 0);
//		wordSenseMap.put("group_action", 0);
//		wordSenseMap.put("road", 0);
//		wordSenseMap.put("terminal", 0);
//		wordSenseMap.put("house", 0);
//		wordSenseMap.put("house", 2);
//		wordSenseMap.put("way", 5);
//		wordSenseMap.put("room", 0);
//		wordSenseMap.put("room", 1);
//		wordSenseMap.put("place_of_business", 0);
//		wordSenseMap.put("land_site", 0);
//		wordSenseMap.put("city_district", 0);
//		wordSenseMap.put("rural_area", 0);
//		wordSenseMap.put("land", 2);
//		wordSenseMap.put("social_group", 0);
//		wordSenseMap.put("geographical_point", 0);
		
		List<String> hypoList = wn.queryRelatedWordsOfSenses(wordSenseMap, POS.NOUN, Pointer.HYPONYM, 6);
		File rawFile = new File(RAW_PATH);
		BufferedWriter rawBw = new BufferedWriter(new FileWriter(rawFile, true));
		
		for(String context : hypoList)
		{
			Utils.bwWriteLine(rawBw, context);
			if(!contextSet.contains(context))
				contextSet.add(context);
		}
		rawBw.close();
		
		Filter.trimDuplicate(RAW_PATH, FILTERED_PATH);
	}
	
	public static void Implementation_PB(String outputPath) throws Exception
	{
		HashSet<String> contextSet = new HashSet<String>();
		WordNet wn = new WordNet();
		FILTERED_PATH = outputPath + "filtered_scene_list_pb.txt";
		
		// Probase contexts
		Probase pb = new Probase();
		String [] concepts = {"activity", "occasion", "scene", "scenario", "amenity", "facility", "public transport", "public area",
				"building", "environment", "disaster", "public space", "open area", "social event", "sport", "crowded place", "high-traffic area"};
		HashMap<String, Integer> instFreqMap = new HashMap<String, Integer>();
		
		for(int i=0; i<concepts.length; ++i)
			instFreqMap = pb.findHypo(concepts[i], instFreqMap);
		
		instFreqMap = Filter.filterPbIllegal(instFreqMap);
		instFreqMap = Filter.solvePbOverlap(instFreqMap, wn);
		instFreqMap = Filter.filterPbByFreq(instFreqMap, wn, 50);

		// print
		File solvedFile = new File(FILTERED_PATH);
		BufferedWriter solvedWr = new BufferedWriter(new FileWriter(solvedFile, false));
		for(Iterator<Map.Entry<String, Integer>> itr = instFreqMap.entrySet().iterator(); itr.hasNext();)
		{
			Map.Entry<String, Integer> entry = itr.next();
			String context = entry.getKey();
			Utils.bwWriteLine(solvedWr, context + "\t" + entry.getValue());
			if(!contextSet.contains(context))
				contextSet.add(context);
		}
		solvedWr.close();
	}
}