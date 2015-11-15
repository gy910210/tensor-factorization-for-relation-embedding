package Util.KnowledgeBase;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.jwi.item.POS;


public class Formalization {
	private WordNet WN;
	private CoreNLP CNLP;
	private HashMap<String, Double> HeadScoreMap;
	private HashMap<String, List<String>> HeadConMap;
	private HashMap<String, List<String>> ConceptHeadMap;
	private String [] Prefix, Suffix, NoScoreArray;
	
	public Formalization(String [] p, String [] s, String [] n) throws Exception
	{
		WN = new WordNet();
		CNLP = new CoreNLP();
		Prefix = p;
		Suffix = s;
		NoScoreArray = n;
	}
	
	public HashSet<String> lemmatize(HashSet<String> rawSet) throws Exception
	{
		HashSet<String> conceptSet = new HashSet<String>();
		HashMap<String, String> lxgraphicalMap = new HashMap<String, String>();
		
		for(Iterator<String> itr = rawSet.iterator(); itr.hasNext(); )
		{
			String rawCon = itr.next();
			
			String [] splitted = new String [1];
			// 1. Split by comma and TAB
			if(rawCon.contains(","))
				splitted = rawCon.split(",");
			else if(rawCon.contains("\t"))
				splitted = rawCon.split("\t");
			else
				splitted[0] = rawCon;
			
			for(int j=0; j<splitted.length; ++j)
			{
				// 2. Remove those with other punctuations
				Pattern p = Pattern.compile("[a-zA-Z /-_/:]+");
		        Matcher m = p.matcher(splitted[j]);
		        if(m.matches())
		        {
		        	// 3. Lemmatize: lower case, unify to singular unless neccessary
		        	String lemmed = WN.lemmatize(splitted[j]);
		        	if(lemmed != null)
		        	{
		        		// 4. Trim prefix and suffix words
		        		String trimmed = Filter.trimSuffix(Filter.trimPrefix(lemmed, Prefix), Suffix);
		        		
		        		// 5. Merge "shutting car door", "door shut car", "car door shut"
		        		String [] components = trimmed.split(" ");
		        		if(!trimmed.contains(" ") || !WN.inWordNet(trimmed))
		        			// 6. shutting -> shut
		        			for(int k=0; k<components.length; ++k)
		        				if(WN.isVBG(components[k]))
			        			{
			        				String oldCompK = components[k];
			        				components[k] = WN.wordNetize(oldCompK, POS.VERB);
			        				trimmed = trimmed.replace(oldCompK, components[k]);
			        			}
		        		
		        		String lxgraphKey = Utils.lxgraphicalKey(components);
		        		//System.out.println(lxgraphKey);
		        		
		        		if(trimmed.length() > 0 && !conceptSet.contains(trimmed))
		        			if(!lxgraphicalMap.containsKey(lxgraphKey))
			        		{
			        			lxgraphicalMap.put(lxgraphKey, trimmed);
			        			conceptSet.add(trimmed);
			        		}
			        		else
			        			if(WN.wordNetize(components[0], POS.VERB)!=null)
			        			{
			        				conceptSet.remove(lxgraphicalMap.get(lxgraphKey));
			        				conceptSet.add(trimmed);
			        				lxgraphicalMap.put(lxgraphKey, trimmed);
			        			}
		        	}
		        }
			}
		}
		
		return conceptSet;
	}
	
	public void formalize(String rawUri, HashSet<String> rawSet, String formUri) throws Exception
	{
		File formFile = new File(formUri);
		BufferedWriter formBw = new BufferedWriter(new FileWriter(formFile));
		HashSet<String> conceptSet = new HashSet<String>();
		List<String> singWordList = new ArrayList<String>();
		String line = null;
		HeadScoreMap = new HashMap<String, Double>();
		HeadConMap = new HashMap<String, List<String>>();
		ConceptHeadMap = new HashMap<String, List<String>>();
		
		if(rawUri!=null && rawSet==null)
		{
			File rawFile = new File(rawUri);
			BufferedReader rawBr = new BufferedReader(new FileReader(rawFile));
			while((line = rawBr.readLine()) != null)
				if(!conceptSet.contains(line))
					conceptSet.add(line);
			rawBr.close();
			
			conceptSet = lemmatize(conceptSet);
		}
		else if(rawSet!=null)
			conceptSet = lemmatize(rawSet);
			
		System.out.println("Lemmed :)");
		for(Iterator<String> itr = conceptSet.iterator(); itr.hasNext(); )
		{
			String rawCon = itr.next();
			
			if(rawCon.length()==0)
				continue;
			
			if(rawCon.contains("_"))
				rawCon = rawCon.replace("_", " ");
			
			// 1. single-word concept
			if(!rawCon.contains(" "))
			{
				// cov: VBG -> V
				if(WN.isVBG(rawCon))
					rawCon = WN.wordNetize(rawCon, POS.VERB);
				addToMaps(rawCon, rawCon, 1);
				continue;
			}
			
			// 2. with :
			String beforeColon = null;
			if(rawCon.contains(":"))
				beforeColon = rawCon.substring(0, rawCon.indexOf(':')).trim();
				
			// 3. a multi-word concept cannot be divided
			if(WN.selfHeader(rawCon))
				addToMaps(rawCon, rawCon, 1.2);
			
			// core nlp deep parse
			String dependencies = CNLP.deepParse(rawCon);
			String trunkDep = null;
			dependencies = dependencies.substring(1, dependencies.length()-1);
			if(dependencies.contains("["))
			{
				Matcher m1 = Pattern.compile("\\[.*\\]").matcher(dependencies);
				String branch = null;
				if(m1.find())
					branch = m1.group();
				trunkDep = dependencies.replace(branch, "%");
			}
			else
				trunkDep = dependencies;
			
			String [] cnlpParsed = new String [4];
			for(int i=0; i<cnlpParsed.length; ++i) 
				cnlpParsed[i]=null;
			cnlpParsed[0] = trunkDep.substring(0, trunkDep.indexOf('/'));
			if(trunkDep.contains("nn:"))
			{
				Matcher matcher = Pattern.compile("nn:(.*?)/").matcher(trunkDep);  
		        if (matcher.find()) {  
		        	cnlpParsed[1] = matcher.group(1);
		        }  
			}
			if(trunkDep.contains("conj:"))
			{
				Matcher matcher = Pattern.compile("conj:(.*?)/").matcher(trunkDep);  
		        if (matcher.find()) {  
		        	cnlpParsed[2] = matcher.group(1);
		        }  
			}
			if(trunkDep.contains("npadvmod:"))
			{
				Matcher matcher = Pattern.compile("npadvmod:(.*?)/").matcher(trunkDep);  
		        if (matcher.find()) {  
		        	cnlpParsed[3] = matcher.group(1);
		        }  
			}
			
			String [] splitted = rawCon.split("[ :]+");
			for(int i=0; i<splitted.length; ++i)
			{
				// 2. with :
				if(beforeColon!=null)
					if(!beforeColon.contains(splitted[i]))
						continue;
				
				// 4. Skip words that should cannot be headers
				int k;
				for(k=0; k<NoScoreArray.length; ++k)
					if(splitted[i].equals(NoScoreArray[k]))
						break;
				if(k<NoScoreArray.length)
					continue;
				
				String potentHeader = null;
				
				// cov: VBG -> V
				if(WN.isVBG(splitted[i]))
					potentHeader = WN.wordNetize(splitted[i], POS.VERB);
				else
					potentHeader = splitted[i];
				
				// 5. part of this concept is likely to be its header 
				if(WN.isHeader(rawCon, splitted[i]))
				{
					addToMaps(rawCon, potentHeader, 1.2);
					continue;
				}
				
				// 6. part of this concept is not likely to be its header (likely to be its modifier)
				if(WN.isModif(splitted[i]))
				{
					addToMaps(rawCon, potentHeader, 0.6);
					continue;
				}
				
				// 7. header, nn, conj and npadvmod parsed by coreNLP
				boolean isBoosted = false;
				for(k=0; k<cnlpParsed.length; ++k)
					if(cnlpParsed[k]!=null && splitted[i].equals(cnlpParsed))
					{
						addToMaps(rawCon, potentHeader, 1.2);
						isBoosted = true;
						break;
					}
				
				// 8. not in above cases
				if(!isBoosted)
					addToMaps(rawCon, potentHeader, 1);
			}
		}
		
		// 9. settle the maps
		// 9.1 carwash -> car wash
		for(Iterator<Map.Entry<String, Double>> itr = HeadScoreMap.entrySet().iterator(); itr.hasNext();)
		{
			Map.Entry<String, Double> entry = itr.next();
			String header = entry.getKey();
			if(!header.contains(" "))
			{
				singWordList.add(header);
			}
		}
		
		List<String> toRmHeadList = new ArrayList<String>();
		List<String> toRmConList = new ArrayList<String>();
		String newWord = null;
		for(String toSolveWord : singWordList)
			for(String word : singWordList)
			{
				if((newWord = WN.isCombinedWord(toSolveWord, word)) != null)
				{
					transTo(toSolveWord, newWord, word);
					if(!toSolveWord.equals(newWord))
					{
						if(!toRmHeadList.contains(toSolveWord))
							toRmHeadList.add(toSolveWord);
					}
				}
			}
		for(String toRemove : toRmHeadList)
			removeFrom(toRemove);
		/*
		// 9.3 slam door, door slam -> slam door
		for(Iterator<Map.Entry<String, List<String>>> itr = ConceptHeadMap.entrySet().iterator(); itr.hasNext();)
		{
			Map.Entry<String, List<String>> entry = itr.next();
			String concept = entry.getKey();
			
			if(concept.contains(" "))
			{
				String reversed = Utils.reverseWords(concept, " ");
				if(ConceptHeadMap.containsKey(reversed) && !toRmConList.contains(reversed))
					if(!toRmConList.contains(concept))
						toRmConList.add(reversed);
			}
		}
		for(String toRmCon : toRmConList)
			removeConcept(toRmCon);
		*/
		int cnt = 0;
		for(Iterator<Map.Entry<String, List<String>>> itr = HeadConMap.entrySet().iterator(); itr.hasNext();)
		{
			Map.Entry<String, List<String>> entry = itr.next();
			String header = entry.getKey();
			List<String> conList = entry.getValue();
			boolean needPrint = false;
			
			// Do not print groups who are too weak
			double currScore = HeadScoreMap.get(header);
			if(currScore <= 2)
			{
				for(String concept : conList)
				{
					boolean np = true;
					for(String hyper : ConceptHeadMap.get(concept))
						if(HeadScoreMap.get(hyper) > currScore)
								np = false;
					if(np)
						needPrint = true;
					
					if(needPrint)
						break;
				}
			}
			else
				needPrint = true;
			
			if(!needPrint)
			{
				//System.out.println(header);
				continue;
			}
			
			Utils.bwWriteLine(formBw, header);
			
			HashSet<String> printedSet = new HashSet<String>();
			for(String concept : conList)
				// 9.2 door bell, doorbell -> one concept
				if(concept.contains(" "))
				{
					String noSpace = concept.replace(" ", "");
					if(conList.contains(noSpace))
					{
						Utils.bwWriteLine(formBw, noSpace + "\t" + concept);
						printedSet.add(noSpace);
					}
					else
						Utils.bwWriteLine(formBw, concept);
				}
			for(String concept : conList)
				if(!concept.contains(" ") && !printedSet.contains(concept))
					Utils.bwWriteLine(formBw, concept);
			
			Utils.bwWriteLine(formBw, "");
		}
		
		formBw.close();
	}
	
	/** Utils **/
	private void addToMaps(String concept, String header, double score)
	{
		if(HeadScoreMap.containsKey(header))
		{
			if(HeadConMap.get(header).contains(concept))
				return;
			HeadScoreMap.put(header, HeadScoreMap.get(header) + score);
			HeadConMap.get(header).add(concept);
		}
		else
		{
			HeadScoreMap.put(header, score);
			List<String> tmpList = new ArrayList<String>();
			tmpList.add(concept);
			HeadConMap.put(header, tmpList);
		}
		
		if(ConceptHeadMap.containsKey(concept))
			if(ConceptHeadMap.get(concept).contains(header))
				return;
			else
				ConceptHeadMap.get(concept).add(header);
		else
		{
			List<String> tmpList = new ArrayList<String>();
			tmpList.add(header);
			ConceptHeadMap.put(concept, tmpList);
		}
	}
	
	private void transTo(String fromHeader, String newFromHeader, String toHeader)
	{
		double fromScore = HeadScoreMap.get(fromHeader);
		HeadScoreMap.put(toHeader, HeadScoreMap.get(toHeader) + fromScore);
		
		List<String> fromConList = HeadConMap.get(fromHeader);
		for(String con : fromConList)
		{
			String newCon = con.replace(fromHeader, newFromHeader);
			if(!HeadConMap.get(toHeader).contains(newCon))
				HeadConMap.get(toHeader).add(newCon);
			else
				HeadScoreMap.put(toHeader, HeadScoreMap.get(toHeader) - 0.6);
			
			if(!ConceptHeadMap.containsKey(newCon))
			{
				List<String> tmpList = new ArrayList<String>();
				tmpList.add(toHeader);
				ConceptHeadMap.put(newCon, tmpList);
			}
			else
				if(!ConceptHeadMap.get(newCon).contains(toHeader))
					ConceptHeadMap.get(newCon).add(toHeader);
		}
	}
	
	private void removeFrom(String fromHeader)
	{
		HeadScoreMap.remove(fromHeader);
		List<String> fromConList = HeadConMap.remove(fromHeader);
		for(String con : fromConList)
			ConceptHeadMap.get(con).remove(fromHeader);
	}
	
	private void removeConcept(String concept)
	{
		List<String> hdList = ConceptHeadMap.remove(concept);
		for(String header : hdList)
		{
			HeadScoreMap.put(header, HeadScoreMap.get(header) - 0.6);
			HeadConMap.get(header).remove(concept);
		}
	}
}
