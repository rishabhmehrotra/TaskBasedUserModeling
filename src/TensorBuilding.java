import java.io.*;
import java.util.*;

public class TensorBuilding implements Serializable  {
	
	public int numTopics, numTasks, numUsers;
	public double tensor[][][];
	ArrayList<Task> taskList;
	public HashMap<String, User> users2;
	
	public TensorBuilding(int numTopics, int numTasks, int numUsers, HashMap<String, User> users2, ArrayList<Task> taskList)
	{
		this.numTasks = numTasks;
		this.numTopics = numTopics;
		this.numUsers = numUsers;
		this.users2 = users2;
		this.taskList = taskList;
		tensor  = new double[10000][10][1600];
	}
	
	public void printTensorToFile() throws IOException
	{
		FileWriter fstream = new FileWriter("src/data/toUse/filledTensorForTensorDecomposition");
		BufferedWriter out = new BufferedWriter(fstream);
		for(int i =00;i<numUsers;i++)
		{
			for(int j=0;j<numTopics;j++)
			{
				for(int k=0;k<numTasks;k++)
				{
					out.write(""+this.tensor[i][j][k]);
					out.write("\n");
				}
			}
		}
		out.close();
		System.out.println("Done with writing tensor to file!!");
	}

	public void populateTensor() {
		Iterator<User> itr = users2.values().iterator();
		System.out.println("total no of users: "+users2.size()+" topics: "+numTopics+" tasks: "+numTasks);
		int c=0;
		while(itr.hasNext())
		{
			User u = itr.next();
			for(int i=0;i<numTopics;i++)
			{
				for(int j=0;j<numTasks;j++)
				{
					Task t = this.taskList.get(j);
					tensor[c][i][j] = u.LDAtopicDistribution[i]*cosSimilarityBetweenFreqMaps(u.getBow(), t.wordMap);
				}
			}
			c++;
			if(c%100 == 0) System.out.println("Done for "+c+" users");
		}
	}
	
	public static float cosSimilarityBetweenFreqMaps(HashMap<String, Float> map1, HashMap<String, Float> map2) {
		  float d1 = 0f, d2 = 0f;
		  for (Float v : map1.values())
		   d1 += v * v;
		  for (Float v : map2.values())
		   d2 += v * v;
		  float denominator = (float) (Math.sqrt(d1) * Math.sqrt(d2));
		  float numerator = 0f;
		  if (map1.size() <= map2.size()) {
		   for (String key : map1.keySet()) {
		    numerator += map1.get(key) * getWordFreqFrom(key, map2);
		   }
		  } else {
		   for (String key : map2.keySet()) {
		    numerator += map2.get(key) * getWordFreqFrom(key, map1);
		   }
		  }
		  return numerator / denominator;
	}
	
	public static float getWordFreqFrom(String word, HashMap<String, Float> map) {
		  Float count = map.get(word);
		  if (count == null)
		   return 0f;
		  else
		   return count;
	}
}
