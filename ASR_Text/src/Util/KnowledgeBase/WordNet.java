package Util.KnowledgeBase;

import java.io.*;
import java.net.*;
import java.util.*;


import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.WordnetStemmer;

public class WordNet {
	
	final private String Path = "D:\\Adapt\\Data\\KnowledgeBase\\WordNet2.1\\dict";
	private IDictionary WordNetDict;
	private WordnetStemmer WordNetStemmer;
	private Inflector Inflct;
	
	public WordNet() throws Exception
	{
		URL url = new URL("file", null, Path);
		WordNetDict = new Dictionary(url);
		WordNetDict.open();
		WordNetStemmer = new WordnetStemmer(WordNetDict);
		Inflct = new Inflector();
	}
	
	/** Query **/
	public List<IWord> queryWordID(String queryWord, POS pos) throws Exception
	{
		List<IWord> iWordList = new ArrayList<IWord>();
		IIndexWord idxWord;
		List<IWordID> wordIDList;
		
		try
		{
			idxWord = WordNetDict.getIndexWord(wordNetize(queryWord, pos), pos);
			wordIDList = idxWord.getWordIDs();	
		}
		catch(NullPointerException npe)
		{
			return iWordList;
		}
		
		for(IWordID wordID : wordIDList)
		{
			IWord word = WordNetDict.getWord(wordID);
			iWordList.add(word);
		}
		
		return iWordList;
	}
	
	public List<String> queryGloss(String queryWord, POS pos) throws Exception
	{
		List<String> glossList = new ArrayList<String>();
		List<IWord> iWordList = queryWordID(queryWord, pos);
		for(IWord word : iWordList)
		{
			glossList.add(word.getSynset().getGloss());
		}
		return glossList;
	}
	
	public List<String> queryLemma(String queryWord, POS pos) throws Exception
	{
		List<String> lemmaList = new ArrayList<String>();
		List<IWord> iWordList = queryWordID(queryWord, pos);
		for(IWord word : iWordList)
		{
			String lemma = word.getLemma();
			if(!lemmaList.contains(lemma))
				lemmaList.add(lemma);
		}
		return lemmaList;
	}
	
	public List<IWord> querySynIDs(String queryWord, POS pos) throws Exception
	{
		String toQuery = wordNetize(queryWord, pos);
		List<IWord> idList = new ArrayList<IWord>();
		
		if(toQuery == null)
			return idList;
		
		IIndexWord idxWord = WordNetDict.getIndexWord(toQuery, pos);
		List<IWordID> wordIDList = idxWord.getWordIDs();		
		for(IWordID wordID : wordIDList)
		{
			IWord word = WordNetDict.getWord(wordID);
			ISynset synset = word.getSynset();
			for(IWord synonym : synset.getWords())
			{
				if(!idList.contains(synonym))
					idList.add(synonym);
			}
		}
		
		return idList;
	}
	
	public List<String> querySynonyms(String queryWord, POS pos) throws Exception
	{
		List<String> synList = new ArrayList<String>();
		List<IWord> idList = querySynIDs(queryWord, pos);
		for(IWord word : idList)
		{
			String lemma = word.getLemma();
			if(!synList.contains(lemma))
				synList.add(lemma);
		}
		return synList;
	}
	
	public List<String> querySynonymsNoPos(String queryWord) throws Exception
	{
		POS [] posArray = {POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB};
		List<String> synList = new ArrayList<String>();
		
		for(int i=0; i<4; ++i)
		{
			try
			{
				List<String> tmpList = querySynonyms(queryWord, posArray[i]);
				for(String syn : tmpList)
					if(!synList.contains(syn))
						synList.add(syn);
			}catch(NullPointerException npe)
			{
				continue;
			}
		}
		
		return synList;
	}
	
	public List<IWord> querySynIDsOfSense(IWord iWord) throws Exception
	{
		List<IWord> idList = new ArrayList<IWord>();
		ISynset synset = iWord.getSynset();
		for(IWord synonym : synset.getWords())
		{
			if(!idList.contains(synonym))
				idList.add(synonym);
		}
		return idList;
	} 
	
	public List<String> querySynonymsOfSense(IWord iWord) throws Exception
	{
		List<String> synList = new ArrayList<String>();
		List<IWord> idList = querySynIDsOfSense(iWord);
		for(IWord word : idList)
		{
			String lemma = word.getLemma();
			if(!synList.contains(lemma))
				synList.add(lemma);
		}
		return synList;
	}
	
	public List<IWord> queryRelatedWordIDs(String queryWord, POS pos, IPointer relationPtr, List<IWord> relatedIDList, int itr) throws Exception
	{
		String toQuery = wordNetize(queryWord, pos);
		IIndexWord idxWord;
		List<IWordID> wordIDList;
		
		if(toQuery == null)
			return relatedIDList;
		
		--itr;
		try
		{
			idxWord = WordNetDict.getIndexWord(queryWord, pos);
			wordIDList = idxWord.getWordIDs();	
		}
		catch(Exception npe)
		{
			return relatedIDList;
		}
		
		for(IWordID wordID : wordIDList)
		{
			IWord word = WordNetDict.getWord(wordID);
			ISynset synset = word.getSynset ();
			
			List<ISynsetID> relatedSynIDList = synset.getRelatedSynsets(relationPtr);
			for(ISynsetID rsId : relatedSynIDList)
			{
				List<IWord> relatedSynList = WordNetDict.getSynset(rsId).getWords();
				for(IWord relatedWord : relatedSynList)
				{
					if(!relatedIDList.contains(relatedWord))
						relatedIDList.add(relatedWord);
					
					// iteration
					if(itr>0)
						relatedIDList = queryRelatedWordIDsOfSense(relatedWord, relationPtr, relatedIDList, itr);
				}
			}
		}
		
		return relatedIDList;
	}
	
	public List<IWord> queryRelatedWordIDsNoPos(String queryWord, IPointer relationPtr, int itr) throws Exception
	{
		List<IWord> relatedIDList = new ArrayList<IWord>();
		relatedIDList = queryRelatedWordIDs(queryWord, POS.NOUN, relationPtr, relatedIDList, itr);
		relatedIDList = queryRelatedWordIDs(queryWord, POS.VERB, relationPtr, relatedIDList, itr);
		return relatedIDList;
	}
	
	public List<String> queryRelatedWordsNoPos(String queryWord, IPointer relationPtr, int itr) throws Exception
	{
		List<String> relatedList = new ArrayList<String>();
		relatedList = queryRelatedWords(queryWord, POS.NOUN, relationPtr, relatedList, itr);
		relatedList = queryRelatedWords(queryWord, POS.VERB, relationPtr, relatedList, itr);
		return relatedList;
	}
	
	public List<String> queryRelatedWords(String queryWord, POS pos, IPointer relationPtr, List<String> relatedList, int itr) throws Exception
	{
		List<IWord> relatedIDList = new ArrayList<IWord>();
		relatedIDList = queryRelatedWordIDs(queryWord, pos, relationPtr, relatedIDList, itr);
		for(IWord relatedIW : relatedIDList)
		{
			String lemma = relatedIW.getLemma();
			if(!relatedList.contains(lemma))
				relatedList.add(lemma);
		}
		return relatedList;
	}
	
	public List<IWord> queryRelatedWordIDsOfSense(IWord iWord, IPointer relationPtr, List<IWord> relatedIDList, int itr)
	{
		ISynset synset = iWord.getSynset();
		--itr;
		
		List<ISynsetID> relatedSynIDList = synset.getRelatedSynsets(relationPtr);
		for(ISynsetID rsId : relatedSynIDList)
		{
			List<IWord> relatedSynList = WordNetDict.getSynset(rsId).getWords();
			for(IWord relatedWord : relatedSynList)
			{
				if(!relatedIDList.contains(relatedWord))
					relatedIDList.add(relatedWord);
				
				// iteration
				if(itr>0)
				{
					//System.out.println(relatedWord.getLemma() + "\t" + itr);
					relatedIDList = queryRelatedWordIDsOfSense(relatedWord, relationPtr, relatedIDList, itr);
				}
			}
		}
		
		return relatedIDList;
	}
	
	public List<String> queryRelatedWordsOfSenseK(String queryWord, POS pos, int k, IPointer relationPtr, int itrRemains)
	{
		List<String> relatedList = new ArrayList<String>();
		List<IWord> idList = new ArrayList<IWord>();
		
		IIndexWord idxWord = WordNetDict.getIndexWord(queryWord, pos);
		List<IWordID> wordIDList = idxWord.getWordIDs();		
		IWord word = WordNetDict.getWord(wordIDList.get(k));

		idList = queryRelatedWordIDsOfSense(word, relationPtr, idList, itrRemains);
		for(IWord relatedIW : idList)
		{
			String lemma = relatedIW.getLemma();
			if(!relatedList.contains(lemma))
				relatedList.add(lemma);
		}
		return relatedList;
	}
	
	public List<String> queryRelatedWordsOfSenses(HashMap<String, Integer> wordKMap, POS pos, IPointer relationPtr, int itrRemains)
	{
		List<String> relatedList = new ArrayList<String>();
		for(Iterator<Map.Entry<String, Integer>> itr = wordKMap.entrySet().iterator(); itr.hasNext();)
		{
			Map.Entry<String, Integer> entry = itr.next();
			List<String> tmpList = queryRelatedWordsOfSenseK(entry.getKey(), pos, entry.getValue(), relationPtr, itrRemains);
			for(String related : tmpList)
				if(!relatedList.contains(related))
					relatedList.add(related);
		}
		return relatedList;
	}
	
	public List<IWord> queryDerivRelatedID(String queryWord, POS pos, List<IWord> iwdList) throws Exception
	{
		IIndexWord idxWord;
		List<IWordID> wordIDList = new ArrayList<IWordID>();
		String toQuery = wordNetize(queryWord, pos);
		
		if(toQuery == null)
			return iwdList;
		
		try
		{
			idxWord = WordNetDict.getIndexWord(queryWord, pos);
			wordIDList = idxWord.getWordIDs();	
		}
		catch(Exception npe)
		{
			return iwdList;
		}
		
		for(IWordID wordID : wordIDList)
		{
			IWord word = WordNetDict.getWord(wordID);
			List<IWordID> relatedIDList = word.getRelatedWords(Pointer.DERIVATIONALLY_RELATED);
			for(IWordID relatedID : relatedIDList)
			{
				IWord related = WordNetDict.getWord(relatedID);
				if(!iwdList.contains(related))
					iwdList.add(related);
			}
		}
		
		return iwdList;
	}
	
	public List<String> queryDerivRelatedWords(String queryWord) throws Exception
	{
		List<IWord> relatedIDList = new ArrayList<IWord>();
		List<String> relatedList = new ArrayList<String>();
		
		relatedIDList = queryDerivRelatedID(queryWord, POS.NOUN, relatedIDList);
		relatedIDList = queryDerivRelatedID(queryWord, POS.VERB, relatedIDList);
		relatedIDList = queryDerivRelatedID(queryWord, POS.ADJECTIVE, relatedIDList);
		relatedIDList = queryDerivRelatedID(queryWord, POS.ADVERB, relatedIDList);
		
		for(IWord relatedIW : relatedIDList)
		{
			String lemma = relatedIW.getLemma();
			if(!relatedList.contains(lemma))
				relatedList.add(lemma);
		}
		return relatedList;
	}
	
	/** Lemmatize **/
	public String lemmatize(String toLem) throws Exception
	{
		String [] splited = toLem.trim().split("[ ]+");
		String lemmed = "";
		
		for(int i=0; i<splited.length; ++i)
		{
			String wordNetized = wordNetize(splited[i], POS.NOUN);
			if(wordNetized == null)
				wordNetized = wordNetize(splited[i], POS.VERB);
			if(wordNetized == null)
				wordNetized = wordNetize(splited[i], POS.ADJECTIVE);
			if(wordNetized == null)
				wordNetized = wordNetize(splited[i], POS.ADVERB);
			if(wordNetized == null)
				wordNetized = splited[i].toLowerCase();
			
			lemmed += (wordNetized + " ");
		}
		
		return lemmed.trim();
	}
	
	public boolean inWordNet(String word) throws Exception
	{
		String wordNetized = wordNetize(word, POS.NOUN);
		if(wordNetized == null)
			wordNetized = wordNetize(word, POS.VERB);
		if(wordNetized == null)
			wordNetized = wordNetize(word, POS.ADJECTIVE);
		if(wordNetized == null)
			wordNetized = wordNetize(word, POS.ADVERB);
		if(wordNetized == null)
			return false;
		return true;
	}
	
	public boolean isVBG(String word) throws Exception
	{
		if(!word.endsWith("ing"))
			return false;
		if(word.length()<5)
			return false;
		
		// 1. not verbs in wordnet -> not VBG
		List<String> stemmedVList = stemWord(word, POS.VERB);
		if(stemmedVList.size() == 0)
			return false;
		// 2. verbs that themselves end with -ing -> not VBG
		else
			for(String stemmed : stemmedVList)
				if(stemmed.equals(word))
					return false;
		
		List<String> stemmedNList = stemWord(word, POS.NOUN);
		if(stemmedNList.size() == 0)
		{
			// 3. adjectives -> not VBG
			List<String> stemmedAList = stemWord(word, POS.ADJECTIVE);
			if(stemmedAList.size()>0)
				return false;
			// 4. not nouns (only V-ing) in wordnet -> VBG
			else
				return true;
		}
		else
		{			
			for(String stemmed : stemmedNList)
				if(stemmed.equals(word))
				{
					List<String> glossNList = queryGloss(word, POS.NOUN);
					List<String> glossVList = queryGloss(word, POS.VERB);
					//System.err.println(word + "\t" + glossNList.size() +"\t" + glossVList.size());
					// 5. much more likely to be verbs -> VBG
					if(glossNList.size() <= glossVList.size()/3)
						return true;
					for(String gloss : glossNList)
					{
						//System.err.println(word + " : " + gloss);
						// 6. salient gloss of VBG -> VBG
						// 6.1 ...state of..., ...act of..., ...sound of..., ...process...
						if(gloss.contains("state of") || gloss.contains("act of") || gloss.contains("action of") || gloss.contains("work of") || gloss.contains("process")
								|| gloss.contains("pitch") || gloss.contains("sound") || gloss.contains("noise") || gloss.contains("loud"))
							return true;
						else
						{
							if(gloss.startsWith("("))
								gloss = gloss.substring(gloss.indexOf(')')+1).trim();
							
							String [] splitted = gloss.split(" ");
							// 6.2 described by another VBG
							if(splitted[0].endsWith("ing") && !splitted[0].equals("something") && !splitted[0].equals("being"))
								return true;
						}
					}
				}
		}
		
		return false;
	}
	
	public boolean isModif(String word) throws Exception
	{
		String [] placeIndicator = {"country", "republic", "colony", "area", "continent", "district", "nation", "monarchy", "island", "locate"};
		String [] personIndicator = {"who", "whose", "he", "his", "she", "her", "mother", "father", "son", "daughter", "husband", "wife", "born", "president", "vice"};
		
		// 1. is a VBG
		if(isVBG(word))
			return true;
		
		// 2. much more likely to be a verb than a noun
		List<String> stemmedVList = stemWord(word, POS.VERB);
		List<String> stemmedNList = stemWord(word, POS.NOUN);
		//System.err.println(word + ":" + stemmedVList.size() + " " + stemmedNList.size());
		if(stemmedVList.size()>0 && stemmedNList.size()==0)
			return true;

		List<String> glossNList = queryGloss(word, POS.NOUN);
		List<String> glossVList = queryGloss(word, POS.VERB);
		//System.err.println(word + ":" + glossVList.size() + " " + glossNList.size());
		if(glossVList.size() >= 5 && glossVList.size() >= glossNList.size() * 3)
			return true;
		
		// 3. much more likely to be an adjective than a noun
		List<String> stemmedAdjList = stemWord(word, POS.ADJECTIVE);
		if(stemmedAdjList.size()>0)
		{
			List<String> glossAdjList = queryGloss(word, POS.ADJECTIVE);
			if(glossAdjList.size() > glossNList.size())
				return true;
		}
		
		// 4. much more likely to be an adverb than a noun
		List<String> stemmedAdvList = stemWord(word, POS.ADVERB);
		if(stemmedAdvList.size()>0)
		{
			List<String> glossAdvList = queryGloss(word, POS.ADVERB);
			if(glossAdvList.size() > glossNList.size())
				return true;
		}
		
		if(glossNList.size() == 0)
			return true;
		int personCnt = 0;
		List<String> lemmaNList = queryLemma(word, POS.NOUN);
		for(int i=0; i<lemmaNList.size(); ++i)
		{
			String lemma = lemmaNList.get(i);
			if(lemma.charAt(0) >= 'A' && lemma.charAt(0) <= 'Z')
			{
				String gloss = glossNList.get(i);
				// 5. is a place
				if(Utils.extactlyContains(gloss, placeIndicator))
					return true;
				// 6. is a person
				else
				{
					if(gloss.contains("\""))
						gloss = gloss.substring(0, gloss.indexOf('\"'));
					if(Utils.extactlyContains(gloss, personIndicator))
						personCnt++;
				}
			}
		}
		if(personCnt>0)
			if(lemmaNList.size() <= 2 || personCnt*2 >= lemmaNList.size())
				return true;
		
		return false;
	}
	
	public boolean isHeader(String word, String potentHeader) throws Exception
	{
		// 1. potentHeader is a synonym of word
		List<String> synList = querySynonymsNoPos(wordNetize(word, POS.NOUN));
		for(String synonym : synList)
			if(synonym.equals(potentHeader))
				return true;
		
		// 2. potentHeader is a hypernym of word
		List<String> hyperList = new ArrayList<String> ();
		hyperList = queryRelatedWords(wordNetize(word, POS.NOUN), POS.NOUN, Pointer.HYPERNYM, hyperList, 5);
		for(String hypernym : hyperList)
		{
			if(hypernym.equals(potentHeader))
				return true;
		}
		
		return false;
	}
	
	public boolean selfHeader(String word) throws Exception
	{
		POS [] pos = {POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB};
		
		String [] splitted = word.split(" ");
		int i;
		
		// 1. only one word
		if(!word.contains(" "))
			return false;
		
		// 2. every word is contained in wordNet
		for(i=0; i<splitted.length; ++i)
		{
			boolean inWN = false;
			for(int j=0; j<pos.length; ++j)
				if(wordNetize(splitted[i], pos[j])!=null)
					inWN = true;
			if(!inWN)
				break;
		}
		if(i<splitted.length)
			return false;
		
		// 3. the phrase itself is also in wordNet
		boolean wordInWN = false;
		for(int j=0; j<pos.length; ++j)
			if(wordNetize(word, pos[j])!=null)
				wordInWN = true;
		if(!wordInWN)
			return false;
		
		// 4. but no isA relation between the phase and its components
		for(i=0; i<splitted.length; ++i)
			if(isHeader(word, splitted[i]))
				break;
		if(i==splitted.length)
			return true;
		else
			return false;
	}
	
	public String isCombinedWord(String combined, String part) throws Exception
	{
		if(combined.equals(part))
			return null;
		
		String rest = null, partBySpace = null;
		if(combined.startsWith(part))
		{
			rest = combined.substring(part.length());
	        partBySpace = part + " " + rest;
		}
		
		if(combined.endsWith(part))
		{
			rest = combined.substring(0, combined.indexOf(part));
	        partBySpace = rest + " " + part;
		}
		
		// restriction 1: both rest and part should be long enough
		if(rest==null || rest.length()<3 || part.length()<3)
			return null;
		
		POS [] pos = {POS.NOUN, POS.VERB, POS.ADJECTIVE, POS.ADVERB};
		int i;
		
		// restriction 2: rest should be contained in wordNet
		for(i=0; i<pos.length; ++i)
			if(wordNetize(rest, pos[i]) != null)
				break;
		if(i==pos.length)
			return null;
		
		// case 1: is a compound word contained in wordNet -> if the partition really makes sense : return it directly; else, null
		boolean partCorrect = true;
		for(i=0; i<pos.length; ++i)
			if(wordNetize(combined, pos[i]) != null)
				{
					if(isHeader(combined, part) || isHeader(combined, rest))
						return combined;
					partCorrect = false;
				}
		if(!partCorrect)
			return null;
		// case 2: otherwise -> partition the word by space
		return partBySpace;
	}
	
	public String partIt(String combined, String part) throws Exception
	{
		if(combined.equals(part))
			return null;
		
		String rest = null, partBySpace = null;
		if(combined.startsWith(part))
		{
			rest = combined.substring(part.length());
	        partBySpace = part + " " + rest;
		}
		
		if(combined.endsWith(part))
		{
			rest = combined.substring(0, combined.indexOf(part));
	        partBySpace = rest + " " + part;
		}
		
		return partBySpace;
	}
	
	public String [] allSyns(String word) throws Exception
	{
		List<String> relatedList = queryDerivRelatedWords(word);
		List<String> synList = querySynonymsNoPos(word);
		String pl = pluralize(word);
		if(!relatedList.contains(word))
			relatedList.add(word);
		if(!relatedList.contains(pl))
			relatedList.add(pl);
		for(String synonym : synList)
			if(!relatedList.contains(synonym))
				relatedList.add(synonym);
		return relatedList.toArray(new String [relatedList.size()]);
	}
	
	/** Utils **/
	public List<String> stemWord(String queryWord, POS pos) throws Exception
	{
		List<String> stemList = new ArrayList<String> ();
		List<String> stemmedList = new ArrayList<String> ();
		
		try
		{
			stemList = WordNetStemmer.findStems(queryWord, pos);
			stemmedList = new ArrayList<String>();
		}catch(Exception e)
		{
			return stemmedList;
		}
		
		for(String stemmed : stemList)
		{
			IIndexWord idxWord = WordNetDict.getIndexWord(stemmed, pos);
			try
			{
				List<IWordID> wordIDList = idxWord.getWordIDs();
				stemmedList.add(stemmed);
			}
			catch(Exception e){ }
		}
		
		return stemmedList;
	}
	
	public String wordNetize(String word, POS pos) throws Exception
	{
		List<String> stemmedList = stemWord(word, pos);
		if(stemmedList.size() == 0)
			return null;
		for(String stemmed : stemmedList)
			if(stemmed.equals(word.toLowerCase()))
				return stemmed;
		return stemmedList.get(0);
	}
	
	public String printGloss(IWord word)
	{
		return word.getSynset().getGloss();
	}
	
	public String singularize(String word) throws Exception
	{
		String sing = Inflct.singularize(word);
		if(word.equals(wordNetize(word, POS.NOUN)))
			return word;
		else
			return sing;
	}
	
	public String pluralize(String word) throws Exception
	{
		String pl = Inflct.pluralize(word);
		if(wordNetize(pl, POS.NOUN)!=null)
			return pl;
		else
			return word;
	}
}
