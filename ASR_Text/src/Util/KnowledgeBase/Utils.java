package Util.KnowledgeBase;

import java.io.*;
import java.lang.Runtime;
import java.util.*;
import java.util.Map.Entry;

//import eventvocabulary.EventFormalization;


public class Utils {

	static public void bwWriteLine(BufferedWriter bw, String toWrite) throws IOException
	{
		bw.write(toWrite + "\r\n");
		bw.flush();
	}	
	
	static public void printMemUsage() throws Exception
	{
		long free = Runtime.getRuntime().freeMemory()/(1024*1024);
		long max = Runtime.getRuntime().maxMemory()/(1024*1024); 
		
		System.out.println("max " + max + "M ,used " + (max-free) + "M");
	}
	
	static public String removeSpaces(String str)
	{
		String [] splitted = str.split(" ");
		String removed = "";
		for(int i = 0; i < splitted.length; ++i)
			removed += splitted[i];
		return removed;
	}
	
	static public void doSample(String toSampleUri, String sampleUri, int count) throws Exception
	{
		File toSampleFile = new File(toSampleUri);
		File sampleFile = new File(sampleUri);
		BufferedReader toSampleBr = new BufferedReader(new FileReader(toSampleFile));
		BufferedWriter sampleBw = new BufferedWriter(new FileWriter(sampleFile));
		List<String> allList = new ArrayList<String> ();
		HashSet<Integer> sampleIdxSet = new HashSet<Integer> ();
		String line = null;
		
		while((line = toSampleBr.readLine()) != null)
			allList.add(line);
		
		int ceiling = allList.size();
		Random rn = new Random();
		for(int i=0; i<count; ++i)
		{
			int rand = (rn.nextInt() & 0x7fffffff) % ceiling;
			while(sampleIdxSet.contains(rand))
				rand = (rn.nextInt() & 0x7fffffff) % ceiling;
			sampleIdxSet.add(rand);
		}
		
		for(Iterator<Integer> itr = sampleIdxSet.iterator(); itr.hasNext();)
			bwWriteLine(sampleBw, allList.get(itr.next()));
		
		toSampleBr.close();
		sampleBw.close();
	}
	
	static public boolean extactlyContains(String str, String [] subs)
	{
		String [] excpt = {"in", "the", "a", "an", "or", "and", "of", "that", "not", "no", "do", "very",
							"up", "down", "on", "under", "with", "by", "for"};
		List<String> excptList = Arrays.asList(excpt);
		
		String [] splitted = str.toLowerCase().split("[ \\+,.]+");
		for(int i=0; i<splitted.length; ++i)
			for(int j=0; j<subs.length; ++j)
			{
				if(excptList.contains(subs[j]))
					continue;
				
				if(splitted[i].equals(subs[j]))
				{
					return true;
				}
			}
		return false;
	}
	
	static public String reverseWords(String str, String splitter)
	{
		String [] splitted = str.toLowerCase().split("[ \\+]+");
		String reversed = splitted[splitted.length-1];
		for(int i=splitted.length-2; i>=0; --i)
			reversed += (splitter + splitted[i]);
		return reversed;
	}

	static public HashSet<String> readRaw(String rawUri, HashSet<String> conceptSet) throws Exception
	{
		File file = new File(rawUri);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		
		while((line = br.readLine()) != null)
		{
			line = line.replace('_', ' '); 
			if(!conceptSet.contains(line))
				conceptSet.add(line);
		}
		
		br.close();
		return conceptSet;
	}
	
	static public HashSet<String> readFormedSet(String formedUri, boolean splitByTab) throws Exception
	{
		File file = new File(formedUri);
		BufferedReader br = new BufferedReader(new FileReader(file));
		HashSet<String> conceptSet = new  HashSet<String>();
		String line = null;
		boolean newGrp = true;
		
		while((line = br.readLine()) != null)
		{
			if(newGrp)
			{
				newGrp = false;
				continue;
			}
			
			if(line.length()==0)
			{
				newGrp = true;
				continue;
			}
			
			if(splitByTab)
			{
				String [] splitted = line.split("\t");
				for(int i=0; i<splitted.length; ++i)
				{
					String concept = splitted[i].replace('+', ' ');
					if(!conceptSet.contains(concept))
						conceptSet.add(concept);
				}
			}
			else
			{
				String concept = line.replace(' ', '+');
				if(!conceptSet.contains(concept))
					conceptSet.add(concept);
			}
		}
		
		br.close();
		return conceptSet;
	}
	
	static public HashMap<String, List<String>> readFormed(String formedUri) throws Exception
	{
		File file = new File(formedUri);
		BufferedReader br = new BufferedReader(new FileReader(file));
		HashMap<String, List<String>> map = new HashMap<String,List<String>>();
		List<String> eventList = new ArrayList<String>();
		String line = null, header = null;
		boolean newGrp = true;
		
		while((line = br.readLine()) != null)
		{
			if(newGrp)
			{
				eventList = new ArrayList<String>();
				header = line;
				newGrp = false;
				continue;
			}
			
			if(line.length()==0)
			{
				map.put(header, eventList);
				newGrp = true;
				continue;
			}
			
			String [] splitted = line.split("\t");
			for(int i=0; i<splitted.length; ++i)
				eventList.add(splitted[i].replace('+', ' '));
		}
		map.put(header, eventList);
		
		br.close();
		return map;
	}
	
//	static public void mergeFormed(String uri1, String uri2, String mergedUri, EventFormalization efml) throws Exception
//	{
//		HashMap<String, List<String>> map1 = readFormed(uri1);
//		HashMap<String, List<String>> map2 = readFormed(uri2);
//		HashSet<String> eventSet = new HashSet<String>();
//		
//		for(Iterator<Map.Entry<String, List<String>>> itr = map1.entrySet().iterator(); itr.hasNext();)
//		{
//			Map.Entry<String, List<String>> entry = itr.next();
//			List<String> tmpList = entry.getValue();
//			for(String event : tmpList)
//				if(!eventSet.contains(event))
//					eventSet.add(event);
//		}
//		
//		for(Iterator<Map.Entry<String, List<String>>> itr = map2.entrySet().iterator(); itr.hasNext();)
//		{
//			Map.Entry<String, List<String>> entry = itr.next();
//			List<String> tmpList = entry.getValue();
//			for(String event : tmpList)
//				if(!eventSet.contains(event))
//					eventSet.add(event);
//		}
//		
//		efml.formalize(null, eventSet, mergedUri);
//	}
	
	static public void quickSort(String [] strArray)
	{
		quickSort(strArray, 0, strArray.length-1);
	}
	
	static private void quickSort(String [] strArray, int low, int high)
	{
		if(low>=high)
			return;
		int mid = divide(strArray, low, high);
		quickSort(strArray, low, mid-1);
		quickSort(strArray, mid+1, high);
	}
	
	static private int divide(String [] strArray, int low, int high)
	{
		String tmp = strArray[low];
		while(low < high)
		{
			while(low < high && strArray[high].compareTo(tmp)>0) --high;
			strArray[low] = strArray[high];
			//System.out.println(high);
			while(low < high && strArray[low].compareTo(tmp)<=0) ++low;
			strArray[high] = strArray[low];
		}
		strArray[low] = tmp;
		return low;
	}
	
	static public String lxgraphicalKey(String [] strArray)
	{
		quickSort(strArray);
		String lxKey = "";
		for(int i=0; i<strArray.length; ++i)
			lxKey += (strArray[i] + " ");
		return lxKey.trim();
	}
	
	static public ArrayList<Map.Entry<String, Integer>> sortMapByValue(HashMap<String, Integer> map) {          
        ArrayList<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());    
        Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() 
        {      
            @Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
            	return (o2.getValue() - o1.getValue());
			}      
        });       
        return entryList;    
    }
}
