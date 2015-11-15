package SceneList_Generate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class SceneFilter
{
	private static final String TIME_REG = "\\s+[Dd][Aa][Yy]"+
		    "|\\s+[Nn][Ii][Gg][Hh][Tt]"+
		    "|\\s+[Mm][Oo][Rr][Nn][Ii][Nn][Gg]"+
		    "|\\s+[Aa][Ff][Tt][Ee][Rr][Nn][Oo][Oo][Nn]"+
		    "|\\s+[Nn][Oo][Oo][Nn]"+
		    "|\\s+[Ee][Vv][Ee][Nn][Ii][Nn][Gg]";
	private static final String[] forbidden_tags = {"PRP","POS","JJ",".","RB","#","CD","DT"};
	private static String phrase;
	
	
	public static void Implementation(String rawPath, String filteredPath)throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(rawPath));
		BufferedWriter bw = new BufferedWriter(new FileWriter(filteredPath));
		bw.flush();
	   	List<CoreMap> sentences = new ArrayList<CoreMap>();
	   	List<String> phrase_list= new ArrayList<String>();
	   	List<TaggedWord> wordList = Lists.newArrayList();
	   	String line = null;
    	Boolean remove_flag = false;
    	while((line = br.readLine())!=null)
    	{
    		phrase_list.add(line.replaceAll(TIME_REG,""));
    	}
    	
    	Properties properties = new Properties();
        properties.put("annotators", "tokenize,ssplit,pos,lemma,ner"); 
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        List<String> cleaned = new ArrayList<String>();
        
        
        //Clean all the phrases in the phrase list
        for(int i = 0; i < phrase_list.size(); ++i)
        {   
        	remove_flag = false;
        	wordList.clear();
        	sentences.clear();
        	phrase = phrase_list.get(i);
        	phrase = phrase.replaceAll("[^a-zA-Z']", " ").toLowerCase().trim();
        	System.out.println(phrase);
        	if(!phrase.equals(""))
        	{
        	System.out.println("=========================");
        	System.out.println(phrase);
        	phrase = toTitleCase(phrase);
        	Annotation document = new Annotation(phrase);
        	pipeline.annotate(document);
            sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            
            //Assume only one sentence in a phrase
            CoreMap sentence = sentences.get(0);
        	cleaned.clear();
        	
        	//For every token in the sentence
        	for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
   		
                String ner = token.get(NamedEntityTagAnnotation.class);
                String tag = token.get(PartOfSpeechAnnotation.class);
                String word = token.getString(TextAnnotation.class);
                //System.out.println(word+"..."+tag);
                //Find 's, clear the possessive terms
                if(tag.equals("POS") && !remove_flag && !word.equals("'") &&cleaned.size()!=0)
                {
                	
                    cleaned.remove(cleaned.size()-1);
                }
                
                //legal tokens
                remove_flag = false;
                if(!inStringArray(forbidden_tags,tag))// && !ner.equals("LOCATION"))
                {
                	if(!ner.equals("LOCATION"))
                    { 
                		cleaned.add(word);
                    }
                	else
                	{
                		remove_flag = true;
                	}
                }
                else
                {
                	remove_flag = true;
                }

            }

            phrase = "";
            for(String tmp : cleaned)
            {
            	if(!tmp.equals("") && tmp.length() != 1)
            	{
            		phrase = phrase + tmp + " ";
            	}
            }
        	phrase = phrase.toLowerCase().trim();
            if(phrase.length() > 1)
            {
              phrase.replace(".", "");
        	  bw.append(phrase);
        	  bw.newLine();
        	}
            
            System.out.println(phrase);
         }
        }
	}
	 private static Boolean inStringArray(String[] sa, String st)
	    {
	    	Boolean flag = false;
	    	for(String tmp : sa )
	    	{
	    		if(tmp.equals(st))
	    		{
	    			flag = true;
	    			break;
	    		}
	    	}
	    	return flag;
	    }
	    
	    private static String toTitleCase(String s) {
	        
	        StringBuffer sb = new StringBuffer();
	        sb.append(Character.toUpperCase(s.charAt(0)))
	          .append(s.substring(1)).append(" ");
	                
	        return sb.toString().trim();
	    }  
}