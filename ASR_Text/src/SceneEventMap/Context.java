package SceneEventMap;

import java.util.*;
import java.io.*;

import Util.KnowledgeBase.WordNet;
import Util.Paragraph.*;

import com.google.common.collect.Maps;

import edu.stanford.nlp.ling.TaggedWord;

public class Context
{
	private static final String PARAGRAPH_PATH ="data/debug/paragraph/";
	private static Integer Scene_Num;
	private static String Scene_Name;
	private static Map<String, Integer> Event_Map;
	private static WordNet wn;
	private static String content;
	private static final String ILLEGAL_CHAR_REGEX = "[^a-zA-Z,'\\s]";
	public Context(String script,String scene_name ,Integer scene_num, Set<String> event_voc) throws Exception
	{
		wn = new WordNet();
		content = script;
		Scene_Num = scene_num;
		Scene_Name = scene_name;
		Event_Map = new HashMap<String,Integer>();
		Event_Map = Find_Audible_Events(content,event_voc);

	}
	
	public static void Write_Info()throws Exception
	{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(PARAGRAPH_PATH+Integer.toString(Scene_Num)+".paragraph")));
//		bw.append(Integer.toString(Scene_Num) + " " + Scene_Name);
//		bw.newLine();
//		bw.append("-----------------------");
//		bw.newLine();
		for(Map.Entry<String, Integer> e : Event_Map.entrySet())
		{
			bw.append(e.toString());
			bw.newLine();
		}
		bw.close();
	}
	
	private static Map<String, Integer> Find_Audible_Events(String para,Set<String> event_voc) throws Exception
	{
		Map<String, Integer> wordList = Maps.newHashMap();
		String tmp = null;
	    for (String word : para.split("\\s+")) {
	    	 tmp = word.toLowerCase().replaceAll(ILLEGAL_CHAR_REGEX, "");
	    	 tmp = wn.lemmatize(tmp);

	         //if(In_Audible_Vocabulary(tmp,event_voc)) 
	         if(event_voc.contains(tmp))
	 		 {  
	        	 if (wordList.containsKey(tmp)) {
	                    wordList.put(tmp, wordList.get(tmp.toLowerCase()) + 1);
	                } else {
	                    wordList.put(tmp, 1);
	                }
	              }
	        }
		
		return wordList;
	}
	
	private static boolean In_Audible_Vocabulary(String word, Set<String> event_voc) throws Exception
	{
//		for(String audible_words : event_voc)
//		{
//			List<String> tmp = wn.querySynonymsNoPos(audible_words);
//			if(tmp.contains(word))
//			{
//				return true;
//			}
//		}
		return false;
	}


}