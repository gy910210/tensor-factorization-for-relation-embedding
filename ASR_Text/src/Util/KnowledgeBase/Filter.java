package Util.KnowledgeBase;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import edu.mit.jwi.item.POS;

public class Filter {
	
	static public void trimDuplicate(String toTrimUri, String trimmedUri) throws Exception
	{
		File toTrimFile = new File(toTrimUri);
		File trimmedFile = new File(trimmedUri);
		BufferedReader toTrimBr = new BufferedReader(new FileReader(toTrimFile));
		BufferedWriter trimmedBw = new BufferedWriter(new FileWriter(trimmedFile, false));
		HashSet<String> allSet = new HashSet<String> ();
		String line = null;
		
		while((line = toTrimBr.readLine()) != null)
			if(!allSet.contains(line))
			{
				allSet.add(line);
				Utils.bwWriteLine(trimmedBw, line);
			}
		
		toTrimBr.close();
		trimmedBw.close();
	}
	
	static public String trimPrefix(String toTrim, String [] prefix) throws Exception
	{
		if(prefix==null)
			return toTrim;
		for(int i=0; i<prefix.length; ++i)
			if(toTrim.startsWith(prefix[i]))
				return toTrim.substring(prefix[i].length()).trim();
		return toTrim;
	}
	
	static public String trimSuffix(String toTrim, String [] suffix) throws Exception
	{
		if(suffix==null)
			return toTrim;
		for(int i=0; i<suffix.length; ++i)
			if(toTrim.endsWith(suffix[i]))
				return toTrim.substring(0, toTrim.length() - suffix[i].length()).trim();
		return toTrim;
	}
	
	static public HashMap<String, Integer> filterPbIllegal(HashMap<String, Integer> instFreqMap) throws Exception
	{
		List<String> toRemoveList = new ArrayList<String>();
		
		for(Iterator<Map.Entry<String, Integer>> itr = instFreqMap.entrySet().iterator(); itr.hasNext();)
		{
			Map.Entry<String, Integer> entry = itr.next();
			String inst = entry.getKey();
			Pattern p = Pattern.compile("[0-9a-zA-Z ]+");
	        Matcher m = p.matcher(inst);
	        if(!m.matches())
	        	toRemoveList.add(inst);
		}
		
		for(String toRemove : toRemoveList)
			instFreqMap.remove(toRemove);
		
		return instFreqMap;
	}
	
	static public HashMap<String, Integer> solvePbOverlap(HashMap<String, Integer> instFreqMap, WordNet wn) throws Exception
	{
		HashMap<String, Integer> oneWordMap = new HashMap<String, Integer>();
		List<String> toRemoveList = new ArrayList<String>();
		
		// collect one-word concepts
		for(Iterator<Map.Entry<String, Integer>> itr1 = instFreqMap.entrySet().iterator(); itr1.hasNext();)
		{
			Map.Entry<String, Integer> entry = itr1.next();
			String instance = entry.getKey();
			if(!instance.contains(" "))
				oneWordMap.put(wn.singularize(instance), instFreqMap.get(instance));
		}
		
		// solve possible headers
		for(Iterator<Map.Entry<String, Integer>> itr2 = instFreqMap.entrySet().iterator(); itr2.hasNext();)
		{
			Map.Entry<String, Integer> entry = itr2.next();
			String instance = entry.getKey();
			if(instance.contains(" "))
			{
				boolean remove = false;
				String [] splitted = instance.split(" ");
				for(int i=0; i<splitted.length; ++i)
				{
					String part = wn.singularize(splitted[i]);
					if(oneWordMap.containsKey(part))
						if(oneWordMap.get(part) >= 2*instFreqMap.get(instance))
						{
							oneWordMap.put(part, oneWordMap.get(part) + instFreqMap.get(instance));
							remove = true;
						}
				}
				if(remove)
					toRemoveList.add(instance);	
			}
		}
		
		for(Iterator<Map.Entry<String, Integer>> itr3 = oneWordMap.entrySet().iterator(); itr3.hasNext();)
		{
			Map.Entry<String, Integer> entry = itr3.next();
			instFreqMap.put(entry.getKey(), entry.getValue());
		}
		for(String toRemove : toRemoveList)
			if(instFreqMap.containsKey(toRemove))
				instFreqMap.remove(toRemove);
		
		return instFreqMap;
	}
	
	static private HashMap<String, Integer> assistSynonyms(HashMap<String, Integer> instFreqMap, WordNet wn) throws Exception
	{
		HashMap<String, Integer> assistMap = new HashMap<String, Integer>();
		
		for(Iterator<Map.Entry<String, Integer>> itr = instFreqMap.entrySet().iterator(); itr.hasNext();)
		{
			Map.Entry<String, Integer> entry = itr.next();
			String instance = entry.getKey();
			String [] syns = wn.allSyns(instance);
			for(int i=0; i<syns.length; ++i)
			{
				String synonym = syns[i];
				if(synonym.equals(instance))
					continue;
				
				if(instFreqMap.containsKey(synonym))
					if(!assistMap.containsKey(synonym))
						assistMap.put(synonym, entry.getValue());
					else
						assistMap.put(synonym, assistMap.get(synonym) + entry.getValue());
			}
		}
		
		return assistMap;
	}
	
	static public HashMap<String, Integer>  filterPbByFreq(HashMap<String, Integer> instFreqMap, WordNet wn, int threshold) throws Exception
	{
		HashMap<String, Integer> filteredMap = new HashMap<String, Integer>();
		HashMap<String, Integer> assistMap = assistSynonyms(instFreqMap, wn);
		
		for(Iterator<Map.Entry<String, Integer>> itr = instFreqMap.entrySet().iterator(); itr.hasNext();)
		{
			Map.Entry<String, Integer> entry = itr.next();
			String inst =  entry.getKey();
			Integer freq = entry.getValue();
			if(assistMap.containsKey(inst))
				freq += assistMap.get(inst);
			if(freq >= threshold)
				filteredMap.put(inst, freq);
		}
		return filteredMap;
	}
}
