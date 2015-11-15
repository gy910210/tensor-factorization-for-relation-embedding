package Util.KnowledgeBase;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.*;
import edu.stanford.nlp.util.*;

public class CoreNLP {
	StanfordCoreNLP StCoreNLP;
	
	public CoreNLP() {
		StCoreNLP = new StanfordCoreNLP();
	}
	
	/*public List<String> posTag(String toTag) throws Exception
	{
		List<String> tokenTagList = new ArrayList<String>();
		Annotation annotation = new Annotation(toTag);
	    StCoreNLP.annotate(annotation);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    List<CoreLabel> tokenList = sentences.get(0).get(TokensAnnotation.class);
	    for(CoreLabel token: tokenList) 
	    {
	    	String pos = token.get(PartOfSpeechAnnotation.class);
	    	tokenTagList.add(token.word() + "\t" + pos);
	    }
	    return tokenTagList;
	}*/

	public String deepParse(String toParse) throws Exception
	{
		Annotation annotation = new Annotation(toParse);
	    StCoreNLP.annotate(annotation);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    SemanticGraph dependencies = sentences.get(0).get(BasicDependenciesAnnotation.class);
	    String ret = dependencies.toFormattedString();
	    if(ret.length()==2)
	    {
	    	CoreLabel token = sentences.get(0).get(TokensAnnotation.class).get(0);
	    	ret = "[" + toParse + "/" + token.get(PartOfSpeechAnnotation.class) + "]";
	    }

	    return ret;
	}
}
