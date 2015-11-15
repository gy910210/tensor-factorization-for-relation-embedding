package EventList_Generate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import Util.KnowledgeBase.*;

public class WordNetSeed {
	final private static String TmpDir = "data/event/tmp/";
	
	public static void Implement() throws Exception
	{
		WordNet wn = new WordNet();
		File rawFile = new File(TmpDir + "wordNet.seed.raw");
		BufferedWriter rawBw = new BufferedWriter(new FileWriter(rawFile, true));
		
		List<String> soundHypoList = wn.queryRelatedWordsOfSenseK("sound", POS.NOUN, 3, Pointer.HYPONYM, 1);
		List<String> noiseHypoList = wn.queryRelatedWordsOfSenseK("noise", POS.NOUN, 0, Pointer.HYPONYM, 1);
		for(String soundHypo : soundHypoList)
			Utils.bwWriteLine(rawBw, soundHypo);
		for(String noiseHypo : noiseHypoList)
			Utils.bwWriteLine(rawBw, noiseHypo);
		rawBw.close();
		
		Filter.trimDuplicate(TmpDir + "wordNet.seed.raw", TmpDir + "wordNet.seed");
	}
}
