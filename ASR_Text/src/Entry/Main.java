package Entry;

import java.util.List;
import java.io.*;

import EventList_Generate.*;
import SceneEventMap.SceneEventMap;
import SceneList_Generate.*;
import Util.KnowledgeBase.Inflector;
import Util.KnowledgeBase.Test;
import Util.KnowledgeBase.WordNet;
import Util.Paragraph.ParagraphUtil;

public class Main
{
	public static void main(String[] args)throws Exception
	{
		//SceneTop.Implementation();
		//Test.Implementation();
		//SceneEventMap.Implementation();
		//EventFilter.GroupSeed();
		//EventFilter.Implement();
		//WordNetSeed.Implement();
		//SoundRangersSeed.Implement();
		Demo();
 	}
	
	public static void Demo()throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader("data/debug/test.txt"));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = br.readLine())!=null)
		{
			sb.append(line);
			sb.append(System.getProperty("line.separator"));
		}
		line = sb.toString();
		
		line = ParagraphUtil.conversationFilter(line);
		System.out.println(line);
	}
	
	
}