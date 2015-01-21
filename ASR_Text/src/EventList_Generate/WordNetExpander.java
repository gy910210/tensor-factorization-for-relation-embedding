package EventList_Generate;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.Pointer;
import Util.KnowledgeBase.*;

public class WordNetExpander {
	private HashMap<String, List<String>> HeadEventMap;
	private HashMap<IWord, Integer> HyperCntMap;
	private HashMap<IWord, List<IWord>> HyperHypoMap;
	private HashMap<IWord, Integer> HyperWeightMap;
	private HashMap<IWord, Integer> HypoCntMap;
	private WordNet WN; 
	
	public WordNetExpander() throws Exception {
		WN = new WordNet();
		HyperCntMap = new HashMap<IWord, Integer>();
		HyperHypoMap = new HashMap<IWord, List<IWord>>();
	}
	
	public void expandOnce(int weighThresh, double proportThresh, String formUri) throws Exception
	{
		HashMap<IWord, Integer> hyperCntMap = new HashMap<IWord, Integer>();
		HypoCntMap = new HashMap<IWord, Integer>();
		HyperWeightMap = new HashMap<IWord, Integer>();
		HeadEventMap = Utils.readFormed(formUri);
		
		for(Iterator<Map.Entry<String, List<String>>> itr0 = HeadEventMap.entrySet().iterator(); itr0.hasNext();)
		{
			Map.Entry<String, List<String>> entry = itr0.next();
			List<IWord> hyperList = WN.queryRelatedWordIDsNoPos(entry.getKey(), Pointer.HYPERNYM, 1);
			for(IWord hypernym : hyperList)
				if(hyperCntMap.containsKey(hypernym))
				{
					hyperCntMap.put(hypernym, hyperCntMap.get(hypernym) + 1);
					HyperWeightMap.put(hypernym, HyperWeightMap.get(hypernym) + HeadEventMap.get(entry.getKey()).size());
				}
				else
				{
					hyperCntMap.put(hypernym, 1);
					HyperWeightMap.put(hypernym, HeadEventMap.get(entry.getKey()).size());
				}
		}
		
		// filter hypernyms according to 2 rules
		for(Iterator<Map.Entry<IWord, Integer>> itr1 = HyperWeightMap.entrySet().iterator(); itr1.hasNext();)
		{
			Entry<IWord, Integer> entry = itr1.next();
			IWord hyper = entry.getKey();
			int weight = entry.getValue();
			int sharedCnt = hyperCntMap.get(hyper);
			
			List<IWord> hypoList = new ArrayList<IWord>();
			if(!HyperHypoMap.containsKey(hyper))
			{
				hypoList = WN.queryRelatedWordIDsOfSense(hyper, Pointer.HYPONYM, hypoList, 1);
			}
			else
				hypoList = HyperHypoMap.get(hyper);
			double proport = (double)(sharedCnt)/hypoList.size();
			// rule 1: highly shared by currrent events
			// rule 2: a high proportion of its hyponyms are in current vocabulary
			if((weight >= weighThresh && proport >= proportThresh) || proport >= proportThresh+0.2)
			{
				if(!HyperHypoMap.containsKey(hyper))
					HyperHypoMap.put(hyper, hypoList);
				for(IWord hypoID : hypoList)
					if(HypoCntMap.containsKey(hypoID))
						HypoCntMap.put(hypoID, HypoCntMap.get(hypoID) + 1);
					else
						HypoCntMap.put(hypoID, 1);
			}
			
			// collect new hypernyms in this round into our global map
			HyperCntMap.put(hyper, sharedCnt);
		}
	}
	
	/** Utils **/
	public void seeHypos(String hypoUri) throws Exception
	{
		File hypoFile = new File(hypoUri);
		BufferedWriter hypoBw = new BufferedWriter(new FileWriter(hypoFile));
		HashMap<String, Integer> hypoCntMap = new HashMap<String, Integer>();
		
		// solve multiple senses of a same term
		for(Iterator<Map.Entry<IWord, Integer>> itr = HypoCntMap.entrySet().iterator(); itr.hasNext();)
		{
			Entry<IWord, Integer> entry = itr.next();
			String hypo = entry.getKey().getLemma();
			if(hypoCntMap.containsKey(hypo))
				hypoCntMap.put(hypo, hypoCntMap.get(hypo)+entry.getValue());
			else
				hypoCntMap.put(hypo, entry.getValue());
		}
		
		// print
		for(Iterator<Map.Entry<String, Integer>> itr = hypoCntMap.entrySet().iterator(); itr.hasNext();)
		{
			Entry<String, Integer> entry = itr.next();
			String hypo = entry.getKey();
			if(!HeadEventMap.containsKey(hypo))
				Utils.bwWriteLine(hypoBw, hypo/* + "\t" + hypoCntMap.get(hypo)*/);
		}
		
		hypoBw.close();
	}

	public void seeSharedHyper(String hyperUri) throws Exception
	{
		HashMap<String, Integer> hyperWeightMap = new HashMap<String, Integer>();
		HashMap<String, Double> hyperPropMap = new HashMap<String, Double>();
		
		// solve multiple senses of a same term
		for(Iterator<Map.Entry<IWord, List<IWord>>> itr0 = HyperHypoMap.entrySet().iterator(); itr0.hasNext();)
		{
			Entry<IWord, List<IWord>> entry = itr0.next();
			IWord hyperID = entry.getKey();
			String hyper = hyperID.getLemma();
			double proport = (double)(HyperCntMap.get(hyperID))/HyperHypoMap.get(hyperID).size();
			if(hyperWeightMap.containsKey(hyper))
			{
				hyperWeightMap.put(hyper, hyperWeightMap.get(hyper) + HyperWeightMap.get(hyperID));
				if(proport > hyperPropMap.get(hyper))
					hyperPropMap.put(hyper, proport);
			}
			else
			{
				hyperWeightMap.put(hyper, HyperWeightMap.get(hyperID));
				hyperPropMap.put(hyper, proport);
			}
		}
		
		// print
		File hyperFile = new File(hyperUri);
		BufferedWriter hyperBw = new BufferedWriter(new FileWriter(hyperFile));
		for(Iterator<Map.Entry<String, Integer>> itr1 = hyperWeightMap.entrySet().iterator(); itr1.hasNext();)
		{
			Entry<String, Integer> entry = itr1.next();
			String hyper = entry.getKey();
			Utils.bwWriteLine(hyperBw, hyper + "\t" + hyperWeightMap.get(hyper) + "\t" + hyperPropMap.get(hyper));
		}
		
		hyperBw.close();
	}
}
