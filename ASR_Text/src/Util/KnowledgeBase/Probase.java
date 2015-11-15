package Util.KnowledgeBase;

import java.io.*;
import java.util.*;


public class Probase{
	final private String ProbaseDir = "F:/Corpus/probase_v47/";
	final private String ConceptDictUri = ProbaseDir + "conceptDict.txt";
	final private String InstanceDictUri =  ProbaseDir + "instanceDict.txt";
	final private String PairUri = ProbaseDir + "probaseMatrix.txt";
	private HashMap<String, Integer> ConceptIdMap;
	private List<String> ConceptList;
	private HashMap<String, Integer> InstanceIdMap;
	private List<String> InstanceList;
	private HashMap<Integer, List<Integer>> ConceptInstanceMap;
	private HashMap<Integer, List<Integer>> InstanceConceptMap;
	private HashMap<List<Integer>, Integer> PairFreqMap;
	
	public Probase() throws Exception
	{
		loadProbase();
	}
	
	private void loadProbase() throws Exception
	{
		File conceptFile = new File(ConceptDictUri);
		File instanceFile = new File(InstanceDictUri);
		File pairFile = new File(PairUri);
		BufferedReader conceptBr = new BufferedReader(new FileReader(conceptFile));
		BufferedReader instanceBr = new BufferedReader(new FileReader(instanceFile));
		BufferedReader pairBr = new BufferedReader(new FileReader(pairFile));
		String line = null;
		ConceptIdMap = new HashMap<String, Integer>();
		ConceptList = new ArrayList<String>();
		InstanceIdMap = new HashMap<String, Integer>();
		InstanceList = new ArrayList<String>();
		ConceptInstanceMap = new HashMap<Integer, List<Integer>>();
		InstanceConceptMap = new HashMap<Integer, List<Integer>>();
		PairFreqMap = new HashMap<List<Integer>, Integer>();
		
		System.out.println("Loading Probase v4.7...");
		
		Utils.printMemUsage();
		while((line = conceptBr.readLine()) != null)
		{
			String [] splitted = line.split("\t");
			Integer id = Integer.parseInt(splitted[1]);
			ConceptIdMap.put(splitted[0], id);
			ConceptList.add(splitted[0]);
		}
		conceptBr.close();
		System.gc();
		Utils.printMemUsage();
		
		while((line = instanceBr.readLine()) != null)
		{
			String [] splitted = line.split("\t");
			Integer id = Integer.parseInt(splitted[1]);
			InstanceIdMap.put(splitted[0], id);
			InstanceList.add(splitted[0]);
		}
		instanceBr.close();
		System.gc();
		Utils.printMemUsage();
		
		int cnt = 0;
		while((line = pairBr.readLine()) != null)
		{
			String [] splitted = line.split("\t");
			Integer conceptId = Integer.parseInt(splitted[0]), instanceId = Integer.parseInt(splitted[1]), freq = Integer.parseInt(splitted[2]);
			if(ConceptInstanceMap.containsKey(conceptId))
			{
				List<Integer> tmpList = ConceptInstanceMap.get(conceptId);
				tmpList.add(instanceId);
				ConceptInstanceMap.put(conceptId, tmpList);
			}
			else
			{
				List<Integer> tmpList = new ArrayList<Integer>();
				tmpList.add(instanceId);
				ConceptInstanceMap.put(conceptId, tmpList);
			}
			
			if(InstanceConceptMap.containsKey(instanceId))
			{
				List<Integer> tmpList = InstanceConceptMap.get(instanceId);
				tmpList.add(conceptId);
				InstanceConceptMap.put(instanceId, tmpList);
			}
			else
			{
				List<Integer> tmpList = new ArrayList<Integer>();
				tmpList.add(conceptId);
				InstanceConceptMap.put(instanceId, tmpList);
			}
			
			Integer [] pair = {conceptId, instanceId};
			PairFreqMap.put(Arrays.asList(pair), freq);
			
			++cnt;
			if(cnt % 1000000 == 0)
			{
				System.gc();
				Utils.printMemUsage();
			}
		}
		pairBr.close();
		System.gc();
		Utils.printMemUsage();
		
		System.out.println("Probase v4.7 loaded:)");
	}
	
	public HashMap<String, Integer> findHypo(String concept, HashMap<String, Integer> hypoFreqMap) throws Exception
	{
		if(ConceptIdMap.containsKey(concept))
		{
			Integer cId = ConceptIdMap.get(concept);
			List<Integer> tmpList = ConceptInstanceMap.get(cId);
			for(Integer iId : tmpList)
			{
				Integer [] pair = {cId, iId};
				String instance = InstanceList.get(iId);
				if(!hypoFreqMap.containsKey(instance))
					hypoFreqMap.put(instance, PairFreqMap.get(Arrays.asList(pair)));
				else
					hypoFreqMap.put(instance, hypoFreqMap.get(instance) + PairFreqMap.get(Arrays.asList(pair)));
			}
		}

		return hypoFreqMap;
	}
	
	public List<String> findHyper(String instance) throws Exception
	{
		List<String> hyperList = new ArrayList<String>();
		if(InstanceIdMap.containsKey(instance))
		{
			Integer iId = InstanceIdMap.get(instance);
			List<Integer> tmpList = InstanceConceptMap.get(iId);
			for(Integer cId : tmpList)
			{
				Integer [] pair = {cId, iId};
				hyperList.add(ConceptList.get(cId));
			}
		}
		return hyperList;
	}
	
	public HashMap<String, Integer> findHyper(String instance, HashMap<String, Integer> hyperFreqMap) throws Exception
	{
		if(InstanceIdMap.containsKey(instance))
		{
			Integer iId = InstanceIdMap.get(instance);
			List<Integer> tmpList = InstanceConceptMap.get(iId);
			for(Integer cId : tmpList)
			{
				Integer [] pair = {cId, iId};
				String concept = ConceptList.get(cId);
				if(!hyperFreqMap.containsKey(concept))
					hyperFreqMap.put(concept, PairFreqMap.get(Arrays.asList(pair)));
				else
					hyperFreqMap.put(concept, hyperFreqMap.get(concept) + PairFreqMap.get(Arrays.asList(pair)));
			}
		}

		return hyperFreqMap;
	}
}
