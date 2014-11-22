import java.io.*;
import java.util.*;

public class Task {
	public String taskSTring;
	public HashMap<String, Float> wordMap;
	
	public Task(String line)
	{
		this.wordMap = new HashMap<String, Float>();
		String parts[] = line.split("\t");
		this.taskSTring = parts[1];
		populateWordMap();
	}
	
	public void populateWordMap()
	{
		String parts[] = taskSTring.split(" ");
		for(int i=0;i<parts.length;i++)
		{
			String temp[] = parts[i].split(":");
			String word = temp[0];
			Float d = Float.parseFloat(temp[1]);
			if(word.length()>1)
				this.wordMap.put(word, d);
			//System.out.print(word+"_"+d+"  ");
		}
		//System.out.println("\n WordMap populated with "+this.wordMap.size()+" words");
	}
}
