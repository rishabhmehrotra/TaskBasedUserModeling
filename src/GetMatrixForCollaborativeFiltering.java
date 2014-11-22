import java.io.*;
import java.util.*;

public class GetMatrixForCollaborativeFiltering {
	
	public static ArrayList<Task> taskList;
	public static HashMap<String, User> users2;
	
	public static void main(String args[]) throws IOException, ClassNotFoundException
	{
		taskList = new ArrayList<Task>();
		populateTaskList();
		loadUserData();
		System.out.println(users2.size()+" users loaded.");
		populateUserQueriesInternally();
		populateUserBoWFromSelfQueries();
		calculatePrintUserTaskMatrix();
	}
	
	public static void populateUserQueriesInternally()
	{
		Iterator<User> itr = users2.values().iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			u.populateSelfQueries();
			u.populateSelfTestTrainQueries();
			System.out.println("User "+u.userID+" populated with "+u.selfTrain.size()+" train & "+u.selfTest.size()+" test queries; total: "+u.selfQueries.size());
		}
	}
	
	public static void calculatePrintUserTaskMatrix() throws IOException
	{
		FileWriter fstream1 = new FileWriter("src/data/toUse/trainUTask");
		BufferedWriter out1 = new BufferedWriter(fstream1);
		FileWriter fstream2 = new FileWriter("src/data/toUse/validUTask");
		BufferedWriter out2 = new BufferedWriter(fstream2);
		FileWriter fstream3 = new FileWriter("src/data/toUse/userOrder");
		BufferedWriter out3 = new BufferedWriter(fstream3);
		int userID = 1;
		int taskID =1;
		
		int numUsers = users2.size();
		int count = 0;
		
		Iterator<User> itr = users2.values().iterator();
		int uID=0,tID=0;
		while(itr.hasNext())
		{
			User u = itr.next();
			Iterator<Task> itr1 = taskList.iterator();
			uID++;
			tID=0;
			out3.write(u.userID);
			out3.write("\n");
			while(itr1.hasNext())
			{
				tID++;
				Task t = itr1.next();
				float sim = cosSimilarityBetweenFreqMaps(u.getBow(), t.wordMap);
				if(uID < (numUsers*0.7))
				{
					out1.write(uID+" "+tID+" "+sim);
					out1.write("\n");
				}
				else
				{
					out2.write(uID+" "+tID+" "+sim);
					out2.write("\n");
				}
			}
		}

		out1.close();
		out2.close();
		out3.close();
	}
	
	public static void populateUserBoWFromSelfQueries()
	{
		Iterator<User> itr = users2.values().iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			HashMap<String, Float> bow = new HashMap<String, Float>();
			Iterator<String> itr1 = u.getSelfTrain().keySet().iterator();
			while(itr1.hasNext())
			{
				String query = itr1.next();
				String parts[] = query.split(" ");
				for(int i =0;i<parts.length;i++)
				{
					String word = parts[i].trim();
					if(bow.containsKey(word))
					{
						Float f = bow.get(word);
						f = f+1;
						bow.put(word, f);
					}
					else
					{
						bow.put(word, new Float(1.0));
					}
				}
			}
			u.setBow(bow);
		}
	}
	
	public static void loadUserData() throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream("src/data/DS/usersAbove500Q");
        ObjectInputStream ois = new ObjectInputStream(fis);
        users2 = (HashMap<String, User>) ois.readObject();
        ois.close();
	}
	
	public static void populateTaskList() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("src/data/taskVectors.txt"));
		String line = br.readLine();
		while(line!=null)
		{
			Task t = new Task(line);
			taskList.add(t);
			line = br.readLine();
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
	
	public static void printMatrixOLD() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("src/data/userSim_truncated.3000"));
		FileWriter fstream1 = new FileWriter("src/data/3000train");
		BufferedWriter out1 = new BufferedWriter(fstream1);
		FileWriter fstream2 = new FileWriter("src/data/3000valid");
		BufferedWriter out2 = new BufferedWriter(fstream2);
		String line = br.readLine();
		int userID = 1;
		while(line!=null)
		{
			String parts[] = line.split("\t");
			int taskID =1;
			for(int i=1;i<parts.length;i++)
			{
				if(userID<161)
				{
					out1.write(userID+" "+taskID+" "+parts[i]);
					out1.write("\n");
				}
				else
				{
					out2.write(userID+" "+taskID+" "+parts[i]);
					out2.write("\n");
				}
				taskID++;
			}
			userID++;
			System.out.println(parts.length+"_"+taskID+"_"+userID);
			line = br.readLine();
		}
		out1.close();
		out2.close();
	}
}
