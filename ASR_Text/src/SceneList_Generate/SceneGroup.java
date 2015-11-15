package SceneList_Generate;


import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class SceneGroup
{
	public static void Implementation(String inputPath, String outputPath) throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(inputPath));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));
		
		HashMap<String, Integer> sceneMap = new HashMap<String, Integer>(); 
		String line;
		while ((line = br.readLine()) != null) {
		   
		   if (sceneMap.containsKey(line)) 
			   sceneMap.put(line, sceneMap.get(line)+1); 
		   else 
			   sceneMap.put(line, 1); 
		}
		br.close();

        ValueComparator bvc =  new ValueComparator(sceneMap);
        Map<String,Integer> sortedMap = new TreeMap<String,Integer>(bvc);
        sortedMap.putAll(sceneMap);

        for(Entry<String, Integer> e : sortedMap.entrySet())
        {
        	bw.append(e.getKey()+" "+e.getValue());
        	bw.newLine();
        }
   
        bw.close();
	}
}

class ValueComparator implements Comparator<String> {
    Map<String, Integer> base;
    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}