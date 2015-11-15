package SceneList_Generate;

/**
 * This class grab scenes from corpus.
 * Input: path of corpus, path of raw_scene.txt
 */

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SceneGrab
{
	
	private static final String CONTEXT_REGEX = "([Cc][Uu][Tt][Ss]?\\s*[Tt][Oo][:.]?\\s*)+"+
			"|[Ss][Cc][Ee][Nn][Ee][:.]\\b?"+
			"|\\W[Ii][Nn][Tt][:.]\\b?"+
			"|\\W[Ee][Xx][Tt][:.]\\b?"+
			"|[F][Aa][Dd][Ee][Ss]?\\s*[Ii][Nn][:.]?"+
			"|\\([Oo][Pp][Ee][Nn][Ss]?[:.]?"+
			"|[F][Ll][Aa][Ss][Hh]\\s*[Cc][Uu][Tt][:.]";
	private static final String ILLEGAL_CHAR_REGEX = "[^a-zA-Z,'\\s]";
	
	public static void Implementation(String inputPath, String outputPath) throws Exception
	{
		File folder = new File(inputPath);
	   	BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
	   	int cnt = 0;
	   	for(final File fileEntry: folder.listFiles())
	   	{
	   		cnt++;
	   		System.out.print(cnt);
	   		System.out.println(". Processing file: "+fileEntry.getName());
	   		GrabScene(fileEntry,bw);
	   	}
	   	bw.close();
	}
	
	private static void GrabScene(File inputFile, BufferedWriter outputWriter) throws Exception
	{
		BufferedReader inputReader = new BufferedReader(new FileReader(inputFile));
		   String line = null;
		   Boolean jump = false;
		   int cnt = 0;
		   Pattern contextPattern = Pattern.compile(CONTEXT_REGEX);
		   
		   while((line = inputReader.readLine())!=null)
		   {
			   line = " " + line;
			   Matcher contextMatcher = contextPattern.matcher(line);
			   if(contextMatcher.find())
			   {
				   cnt++;
				   
				   line = line.replace(contextMatcher.group(),"").toLowerCase().trim();
				   line = line.replaceAll(ILLEGAL_CHAR_REGEX, "");
				   line = line.replaceAll("\\s+"," ");
				   line = line.toLowerCase().trim();
				   if(line.equals(""))
				   {
					   jump = true;
					   continue;
				   }
				   else
				   {   
					    if(jump)
					    {
					    	jump = false;
					    }
					    outputWriter.append(line);
				        outputWriter.newLine();
				   }
			   }
			   else if(jump)
			   {
				   line = line.replaceAll(ILLEGAL_CHAR_REGEX, "");
				   line = line.replaceAll("\\s+"," ");
				   line = line.toLowerCase().trim();
				   if(line.equals(""))
				   {
					   continue;
				   }
				   else
				   {
					   jump = false;
					   outputWriter.append(line);
				       outputWriter.newLine();
				   }
			   }
		   }
		   
		   System.out.print(cnt);
		   System.out.println(" Scene(s) found.");
	}
	
}