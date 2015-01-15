package SceneEventMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

import Util.Paragraph.Paragraph;

import com.google.common.collect.Lists;

public class SceneEventMap
{
	private static String SCENE_PATH = "data/debug/scene.txt";
	private static String CORPUS_PATH = "data/debug/corpus/";
	private static String MATRIX_PATH = "data/distribution/scene_event_dis.txt";
	private static String EVENT_PATH = "data/debug/event.txt";
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
	
	public static void Implementation()throws Exception
	{
	    LoadScene();
	    LoadEvent();
//	    Demo();
	    TraverseCorpus();
	}
	
	private static void LoadScene()throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(SCENE_PATH));
		String line = null;
		int cnt = 0;
		while((line = br.readLine()) != null)
		{
			sceneSet.put(line, cnt++);
		}
		br.close();
	}
	
	private static void LoadEvent() throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(EVENT_PATH));
		String line = null;
		String tmp = null;
		int cnt = 0;
		while((line = br.readLine()) != null)
		{
		//	tmp = line.split(">>")[1];
			tmp = line;
			eventSet.put(tmp,cnt++);
		}
		br.close();
	}
	
	private static void Demo()throws Exception
	{
		for(Entry<String, Integer> e : eventSet.entrySet())
		{
			System.out.println(e.getKey()+"--"+e.getValue());
		}
	}
	
	private static void TraverseCorpus()throws Exception
	{
		File folder = new File(CORPUS_PATH);
	  // 	BufferedWriter bw = new BufferedWriter(new FileWriter(MATRIX_PATH));
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
		 String scene_name;
		 Pattern pattern = Pattern.compile(CONTEXT_REGEX);
	     Matcher matcher = pattern.matcher(script);
	     
	     List<String> contextList = Lists.newArrayList(Arrays.asList(pattern.split(script)));
	       
	     for (String context : contextList) {	
	    	 System.out.println(context);
		     System.out.println("------");
	        
	         context = context.trim();
	         if(context.length() == 0)
	         {
	            continue;
	         }
	                
	         String[] tmp_st = context.split("\r\n");
	         scene_name = Find_Scene(tmp_st[0]);
	         Context t = new Context(context,scene_name, sceneSet.get(scene_name),eventSet.keySet());
	         t.Write_Info();
	    }
	}
	
	
	private static String Find_Scene(String scene_line)
	{
		String tmp = "-1";
		Pattern contextPattern = Pattern.compile(CONTEXT_REGEX);

	    scene_line = " " + scene_line;
		Matcher contextMatcher = contextPattern.matcher(scene_line);
		if(contextMatcher.find())
		{ 
			scene_line = scene_line.replace(contextMatcher.group(),"").toLowerCase().trim();
		}
			
		scene_line = scene_line.replaceAll(ILLEGAL_CHAR_REGEX, "");
		scene_line = scene_line.replaceAll("\\s+"," ");
		scene_line = scene_line.toLowerCase().trim();
			   	
		if(!scene_line.equals(""))
		{
		   tmp = scene_line;
		}

		return tmp;
	}
}