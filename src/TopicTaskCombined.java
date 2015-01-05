import java.io.*;
import java.util.*;

import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;


public class TopicTaskCombined // most of this code is from GetMatrixForCF class -- its an extension of it
{
	public static ArrayList<Task> taskList;
	public static HashMap<String, User> users2;
	public static BuildLDAModel lda;
	public static TensorBuilding tensorClass;
	public static int numUsers, numTasks, numTopics;
	
	public static void main(String args[]) throws IOException, ClassNotFoundException
	{
		taskList = new ArrayList<Task>();
		populateTaskList();
		loadUserData();
		System.out.println(users2.size()+" users loaded.");
		populateUserQueriesInternally();
		populateUserBoWFromSelfQueries();
		// now we have users in place & tasks in the task list
		// now work on LDA
		//printFileForLDA();//NOTE: uncomment when needed
		// use when u need to build LDA again; otherwise just load pre-built LDA model
		/*
		lda = new BuildLDAModel();
		saveLDAModel();
		*/
		loadLDAModel();
		
		numUsers = users2.size();
		numTopics = 10;
		numTasks = taskList.size();
		
		performLDAInference();
		System.exit(0);
		// now we have performed topic modeling & we have with us the lda model, the user details and the tasks
		// now we just need to construct the Tensor
		tensorClass = new TensorBuilding(numTopics, numTasks, numUsers, users2, taskList);
		System.out.println("Starting with populating tensor...");
		tensorClass.populateTensor();
		System.out.println("Done with populating tensor...");
		tensorClass.printTensorToFile();
		//saveTensor();
		//loadTensor();
		System.exit(0);
		calculatePrintUserTaskMatrix();
	}
	
	public static void loadTensor() throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream("src/data/DS/Tensor");
        ObjectInputStream ois = new ObjectInputStream(fis);
        tensorClass.tensor = (double[][][]) ois.readObject();
        ois.close();
	}
	
	public static void saveTensor() throws IOException
	{
		FileOutputStream fos = new FileOutputStream("src/data/DS/Tensor");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(tensorClass.tensor);
		fos.close();
	}
	
	public static void performLDAInference() throws IOException
	{
		// Additionally, we also save the topical representations of users: user profiles based on LDA Topic Models
		FileWriter fstream = new FileWriter("src/data/toUse/userRep_LDA.txt");
		BufferedWriter out = new BufferedWriter(fstream);
		
		InstanceList testing = new InstanceList(lda.instances.getPipe());
		Iterator<User> itr = users2.values().iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			String userString = "";
			Iterator<String> itr1 = u.getSelfTrain().values().iterator();
			while(itr1.hasNext())
			{
				String query = itr1.next();
				userString += query+" ";
			}
			
			testing.addThruPipe(new Instance(userString, null, "test instance", null));
		}
		TopicInferencer inferencer = lda.model.getInferencer();
		itr = users2.values().iterator();
		int c=0;
		while(itr.hasNext())
		{
			User u = itr.next();
			double[] testProb = inferencer.getSampledDistribution(testing.get(c), numTopics, 1, 5);
			c++;
			//out.write(u.userID+"\n");
			for(int j=0;j<numTopics;j++)
			{
				System.out.print(testProb[j]+" ");
				out.write(testProb[j]+"\t");
				u.LDAtopicDistribution[j] = testProb[j];
			}
			out.write("\n");
			System.out.println();
		}
		out.close();
	}
	
	
	public static void printFileForLDA() throws IOException//LDA file build using just the training data
	{
		
		FileWriter fstream1 = new FileWriter("src/data/toUse/LDAInput");
		BufferedWriter out1 = new BufferedWriter(fstream1);
		int c=0;
		Iterator<User> itr = users2.values().iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			//System.out.println(u.queries.size()+"_"+u.getSelfQueries().size());
			//NOTE: Instead of printing from selfQueries hashmap, we will use the arraylist of queries per user - so that duplicate queries dont get removed
			System.out.println("Writing to file "+u.getSelfTrain().size()+"queries for user "+u.userID);
			c+= u.getSelfTrain().size();
			Iterator<String> itr1 = u.getSelfTrain().values().iterator();
			/*Iterator<String> itr1 = u.queries.iterator();//NOTE: ideally it should be getQueries but some fault in getQueries method, see User class for details
			c+=u.queries.size();*/
			while(itr1.hasNext())
			{
				String query = itr1.next();
				out1.write(query);
				out1.write("\n");
			}
		}
		out1.close();
		System.out.println("Check: the LDAInput file should have a total of "+c+" lines/queries");
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
	
	public static void saveLDAModel() throws IOException
	{
		FileOutputStream fos = new FileOutputStream("src/data/DS/LDAModel");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(lda);
		fos.close();
	}
	
	public static void loadLDAModel() throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream("src/data/DS/LDAModel");
        ObjectInputStream ois = new ObjectInputStream(fis);
        lda = (BuildLDAModel) ois.readObject();
        ois.close();
	}
}
