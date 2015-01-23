package SceneEventMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import Util.KnowledgeBase.Inflector;
import Util.KnowledgeBase.WordNet;
import Util.Paragraph.Paragraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SceneEventMap
{
	private static String SCENE_PATH = "data/scene/scene_list.txt";
	private static String CORPUS_PATH = "data/corpus/";
	private static String MATRIX_PATH = "data/distribution/scene_event_dis.txt";
	private static String EVENT_PATH = "data/event/Merged/merged_seed.txt";
	private static String PARA_PATH = "data/paragraph/";
	private static final String CONTEXT_REGEX = "([Cc][Uu][Tt][Ss]?\\s*[Tt][Oo][:.]?\\s*)+"+
			"|[Ss][Cc][Ee][Nn][Ee][:.]\\b?"+
			"|\\W[Ii][Nn][Tt][:.]\\b?"+
			"|\\W[Ee][Xx][Tt][:.]\\b?"+
			"|[F][Aa][Dd][Ee][Ss]?\\s*[Ii][Nn][:.]?"+
			"|\\([Oo][Pp][Ee][Nn][Ss]?[:.]?"+
			"|[F][Ll][Aa][Ss][Hh]\\s*[Cc][Uu][Tt][:.]";
	
	private static final String ILLEGAL_CHAR_REGEX = "[^a-zA-Z,'\\s]";
	
	private static HashMap<String, Integer> sceneSet = new HashMap<String, Integer>();
	private static HashMap<String, Integer> eventSet = new HashMap<String, Integer>();
	private static List<List<Integer>> sceneEventMap = new ArrayList<List<Integer>>();
	private static WordNet wn;
	private static int sceneNum,eventNum;
	private static List<Integer> eventDist = new ArrayList<Integer>();
	private static List<String> eventList = new ArrayList<String>();
	
	public static void Implementation()throws Exception
	{
		 wn = new WordNet();
		 LoadScene();
		 LoadEvent();
		
		 //Demo();
       TraverseCorpus();
	}
	
	private static void LoadScene()throws Exception
	{  
		BufferedReader br = new BufferedReader(new FileReader(SCENE_PATH));
		String line = null;
		int cnt = 0;
		while((line = br.readLine()) != null)
		{
			if(line.length() >= 0)
				sceneSet.put(line, cnt++);
		}
		sceneNum = sceneSet.size();
		br.close();
	}
	
	private static void LoadEvent() throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(EVENT_PATH));
		String line = null;
		String tmp = null;
		String[] st = null;
		int cnt = 0;
		while((line = br.readLine()) != null)
		{
		//	tmp = line.split(">>")[1];
			tmp = line;
			eventSet.put(tmp,cnt++);
			eventList.add(tmp);
		}
		eventNum = eventSet.size();
		br.close();
	}
	
	private static void Demo()throws Exception
	{
		String tmp,line,scene_name = null;
		Integer scene_number;
		
		File folder = new File(PARA_PATH);	
		for(final File fileEntry: folder.listFiles())
	   	{
			int cnt = 0;
	   		tmp = fileEntry.getName();
	   		scene_name = tmp.split("\\.")[0];
	   		//scene_number = Integer.parseInt(tmp.split("\\.")[0]);
	   		BufferedReader br = new BufferedReader(new FileReader(PARA_PATH+tmp));
	   		while((line = br.readLine())!=null)
			{
				cnt++;
			}
	   		System.out.println(scene_name+"--"+Integer.toString(cnt));
	   	    br.close();
	   	}
		

		
	}
	

	// Take all the texts in the corpus to StringBuilder sb
	private static void TraverseCorpus()throws Exception
	{
		File folder = new File(CORPUS_PATH);
	   	
	   	int cnt = 0;
	   	StringBuilder sb = new StringBuilder();
	   	String line = null;
	   	
	   	for(final File fileEntry: folder.listFiles())
	   	{
	   		cnt++;
	   		System.out.print(cnt);
	   		System.out.println(". Processing file: "+fileEntry.getName());
	   		BufferedReader br = new BufferedReader(new FileReader(new File(CORPUS_PATH+fileEntry.getName())));
	   		while((line = br.readLine())!=null)
	   		{
	   			sb.append(line);
	   			sb.append(System.getProperty("line.separator"));
	   		}
	   		br.close();
	   	}
        Script_Split(sb.toString());
	}
	
	
	private static void Script_Split(String script)throws Exception
	{
		 int cnt_unrec = 0;
		 int cnt_show = 0;
		 String scene_name;
		 Pattern pattern = Pattern.compile(CONTEXT_REGEX);

	     List<String> contextList = Lists.newArrayList(Arrays.asList(pattern.split(script)));
	     System.out.print(contextList.size());
	     System.out.println(" contexts found.");
	     
	     // Record the SE map
	     HashMap<String, List<Integer>> recordSEMap = new HashMap<String, List<Integer>>();
	     List<Integer> tmp_eventList = new ArrayList<Integer>();
	     List<Integer> tmp_eventList2 = new ArrayList<Integer>();
	     
	     for(int i = 0; i < eventSet.size(); ++i)
	     {
	    	 tmp_eventList.add(0); 
	    	 tmp_eventList2.add(0);
	    	 eventDist.add(0);
	     }
	     
	     for(String sceneName : sceneSet.keySet())
	    	 recordSEMap.put(sceneName,tmp_eventList);
	     
	     for (String context : contextList) {	
	    	 
	         System.out.print(cnt_show++);
	         System.out.println("th Context.");
	         context = context.trim();
	         if(context.length() == 0)
	         {
	            continue;
	         }
	                
	        String[] tmp_st = context.split("\r\n");
	        scene_name = Find_Scene(tmp_st[0]);
	        
	        
	        if(scene_name.equals("-1"))
	        {
	        	 cnt_unrec++;
	        	 continue;
	        }
	        else{
	        	tmp_eventList = recordSEMap.get(scene_name);
	        	tmp_eventList2 = GenerateEventDist(context);
	        	eventDist = addList(tmp_eventList,tmp_eventList2);
	        	recordSEMap.put(scene_name, eventDist);
	        }
	    }
	    System.out.println(cnt_unrec +" scene(s) not recognized!");
	    
	    for(String st : recordSEMap.keySet())
	    {
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(PARA_PATH + st +".txt"));
	    	eventDist = recordSEMap.get(st);
	    	for(int i = 0; i < sceneSet.size(); ++i)
	    	{
	    		if(eventDist.get(i)!=0){
	    			bw.append(eventList.get(i)+"\t=\t");
		    		bw.append(eventDist.get(i).toString());
		    		bw.newLine();
	    		}
	    		
	    	}
	    	bw.close();
	    }
	}
	
	// Output the name of the scene given a descriptive scene line
	private static String Find_Scene(String scene_line)
	{
		String tmp = "-1";	
		scene_line = scene_line.replaceAll(ILLEGAL_CHAR_REGEX, "");
		scene_line = scene_line.replaceAll("\\s+"," ");
		scene_line = scene_line.toLowerCase().trim();
			   	
		if(scene_line.equals(""))
		{
			System.out.println("Empty line!");
			return tmp;
		}
		
		for(String st:sceneSet.keySet())
		{
			if(scene_line.contains(st))
			{
				tmp = st;
			}
		}
		System.out.println(tmp);
		return tmp;
	}
	
	
	private static List<Integer> GenerateEventDist(String para) throws Exception
	{
		List<Integer> rst = new ArrayList<Integer>();
		
		for(int i = 0; i < eventNum ; ++i)
			rst.add(0);
		
		String tmp = null;
		for (String word : para.split("\\s+")) {
	    	 tmp = word.toLowerCase().replaceAll(ILLEGAL_CHAR_REGEX, "");
	    	 tmp = wn.lemmatize(tmp);

	         if(eventSet.keySet().contains(tmp))
	 		 {  
	        	 rst.set(eventSet.get(tmp), rst.get(eventSet.get(tmp))+1);
	         }
	    }
		return rst;
	}
	
	//Add elements in two strings
	private static List<Integer> addList(List<Integer> a, List<Integer> b)
	{
		int size_a = a.size();
		int size_b = b.size();
		if(size_a != size_b)
		{	
			System.out.println("Dimension does not match!");
		    return null;
		}
		 
		List<Integer> rst = new ArrayList<Integer>();
		for(int i = 0; i < size_a; ++i)
		{
			rst.add(a.get(i) + b.get(i));
		}
		
		return rst;
	}

	private static void ConvertToMatrix() throws Exception
	{
		List<Integer> tmp = new ArrayList<Integer>();
		
		for(int i = 0; i < sceneSet.size(); ++i)
		{
			for(int j = 0; j < eventSet.size() ; ++j)
			{
				tmp.add(0);
			}
			
			sceneEventMap.add(tmp);
			
		}
		
		File folder = new File(PARA_PATH);	
		BufferedWriter bw = new BufferedWriter(new FileWriter(MATRIX_PATH));
	    
		String tmp_st = null;
		Integer scene_number = -1;
		Integer event_number = -1;
		Integer event_time = 0;
	   	
	   	for(final File fileEntry: folder.listFiles())
	   	{
	   		tmp_st = fileEntry.getName();
	   		scene_number = Integer.parseInt(tmp_st.split("\\.")[0]);
	   		if(scene_number != -1){
	   			System.out.println(scene_number);
	   			sceneEventMap.set(scene_number, Distribution(fileEntry));
	   		}
	   		
	   	}
	   	
	   	for(int i = 0; i < sceneSet.size(); ++i)
	   	{
	   		tmp = sceneEventMap.get(i);
	   		for(int j = 0; j < eventSet.size(); ++j)
	   		{
	   			bw.append(Integer.toString(tmp.get(j)));
	   			bw.append("\t");
	   		}
	   		bw.newLine();
	   	}
		
	   	bw.close();
	}
	
	private static List<Integer> Distribution(File file) throws Exception
	{
		List<Integer> rst = new ArrayList<Integer>();
		for(int i = 0; i < eventSet.size(); ++i)
		{
			rst.add(0);
		}
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line, eventName = null;
		Integer times = 0;
		
		while((line = br.readLine())!=null)
		{
			eventName = line.split("=")[0];
			
			times = Integer.parseInt(line.split("=")[1]);
			rst.set(eventSet.get(eventName),times);
		}
		
		return rst;
	}
}