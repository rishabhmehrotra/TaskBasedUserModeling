import java.io.*;
import java.util.*;

public class Collaborative_UserCluster_QueryRecommendation implements Serializable{

	private static final long serialVersionUID = 1L;
	public static HashMap<String, User> users2;
	public static HashMap<String, ArrayList<String>> users = new HashMap<String, ArrayList<String>>();
	public static ArrayList<User> userList = new ArrayList<User>();
	public static ArrayList<User> usersArrayList;
	public static float[][] simU;
	public static HashMap<String, Double> perUserCutOff;
	public static int similarUsersThreshold = 40;
	public static int candidateQueriesCutoff = 40;
	public static int baselineOrNot = 1; // 1 for Task Based, 2 for BoW,
	
	public static HashMap<String, Integer> user2ClusterMap;
	public static HashMap<Integer, ArrayList<User>> clusterUserCollectionMap;
	
	public static ArrayList<Task> taskList;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		int part = 2;
		if(part == 1)
		{
			taskList = new ArrayList<Task>();
			users2 = new HashMap<String, User>();
			populateTaskList();
			
			loadUserData_PMF();
			
			
			//loadUserData_PTF();
			
			//populateUserFeatures();
			System.out.println("No of users: "+users2.size());
			populatePerUserSelfTrainTest();
			//populateUsersFromAOLData();
			
			//populateUserBoWFromFile();
			populateUserBoWFromSelfQueries();

			//checkUserDetails();
			/*FileOutputStream fos = new FileOutputStream("src/data/DS/users2HashMapWIthSelfTrainTestBOW");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(users2);

			fos.close();*/
			System.out.println("End of Part 1");
			//System.exit(0);
		/*}
		else if(part == 2)
		{*/
			loadUserMap();
			
			
			checkPointPause();
		}
		else if(part == 2)
		{
			System.out.println("Starting part 2 now...");
			checkPointResume();
			System.out.println("Loaded userArrayList with no of enteries: "+usersArrayList.size());
			
			populateUser2ClusterMap("src/data/toUse/cluster-Task_OUTPUT.txt");
			System.out.println("Done with populating user2cluster map with no of entries: "+user2ClusterMap.size());
			//populateClusteredUserCollectionMap();
			
			// if users dont belong to the same cluster, then the simU for them is made 0
			// this will ensure that the **userSimCutOff** calculated would be calculated in the right way
			userSimilarity();
			// imp to ensure that the cutoff isnt calculated wrongly, coz users not the cluster shouldn't affect the cutoff
			calculateSimCutOff(similarUsersThreshold);
			
			
			populateCandidatesForEachUser();
			
			calculateScoreForEachCandidateForEachUser();
			//checkStatusOfUsersArrayList();
			
			/*FileOutputStream fos = new FileOutputStream("src/data/usersArrayList");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(usersArrayList);
			fos.close();*/
			System.out.println("End of Part 2");
			//System.exit(0);
		/*}
		else if(part == 3)
		{*/
			evaluateMatchingQueries();
			System.out.println("End of Part 3"+"baselineornot: "+baselineOrNot + " no U:"+ similarUsersThreshold + "  candidateQCutoff: "+candidateQueriesCutoff);
		}
	}
	
	public static void populateClusteredUserCollectionMap()
	{
		clusterUserCollectionMap = new HashMap<Integer, ArrayList<User>>();
		Iterator<String> itr = user2ClusterMap.keySet().iterator();
		while(itr.hasNext())
		{
			String userID = itr.next();
			User u = users2.get(userID);
			int clusterID = user2ClusterMap.get(userID);
			if(clusterUserCollectionMap.containsKey(clusterID))
			{
				ArrayList<User> uList = clusterUserCollectionMap.get(clusterID);
				uList.add(u);
				clusterUserCollectionMap.put(clusterID, uList);
			}
			else
			{
				ArrayList<User> uList = new ArrayList<User>();
				uList.add(u);
				clusterUserCollectionMap.put(clusterID, uList);
			}
		}
	}
	
	public static void populateUser2ClusterMap(String file) throws IOException
	{
		user2ClusterMap = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		while(line!=null)
		{
			String parts[] = line.split(" ");
			String userID = parts[0];
			int clusterID = Integer.parseInt(parts[1]);
			user2ClusterMap.put(userID, new Integer(clusterID));
			line = br.readLine();
		}
		System.out.println("Populated user2Cluster map with "+user2ClusterMap.size()+" enteries");
	}
	
	public static void checkPointResume() throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream("src/data/DS/userArrayList");
        ObjectInputStream ois = new ObjectInputStream(fis);
        usersArrayList = (ArrayList<User>) ois.readObject();
        ois.close();
        
        /*FileInputStream fis1 = new FileInputStream("src/data/DS/simMatrix");
        ObjectInputStream ois1 = new ObjectInputStream(fis1);
        simU = (float[][]) ois1.readObject();
        ois1.close();
        
        FileInputStream fis2 = new FileInputStream("src/data/DS/perUserCutOff");
        ObjectInputStream ois2 = new ObjectInputStream(fis2);
        perUserCutOff = (HashMap<String, Double>) ois2.readObject();
        ois2.close();
        */
	}
	
	public static void checkPointPause() throws IOException
	{
		FileOutputStream fos = new FileOutputStream("src/data/DS/userArrayList");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(usersArrayList);
		fos.close();
		
		/*FileOutputStream fos1 = new FileOutputStream("src/data/DS/simMatrix");
		ObjectOutputStream oos1 = new ObjectOutputStream(fos1);
		oos1.writeObject(simU);
		fos1.close();
		
		FileOutputStream fos2 = new FileOutputStream("src/data/DS/perUserCutOff");
		ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
		oos2.writeObject(perUserCutOff);
		fos2.close();
		*/
		
	}
	
	public static void populatePerUserSelfTrainTest()
	{
		Iterator<User> itr = users2.values().iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			u.populateSelfQueries();
			u.populateSelfTestTrainQueries();
		}
		System.out.println("Done with populatePerUserSelfTrainTest");
	}
	
	public static void loadUserData_PTF() throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream("src/data/DS/usersAbove500Q");
        ObjectInputStream ois = new ObjectInputStream(fis);
        users2 = (HashMap<String, User>) ois.readObject();
        ois.close();
        BufferedReader br1 = new BufferedReader(new FileReader("src/data/toUse/gen2.txt"));
		String line1 = br1.readLine();
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/toUse/userOrder"));
		String line2 = br2.readLine();
		
		while(line1!=null && line2!=null)
		{
			String userID = line2.trim();
			String line = userID+"\t"+line1;
			//User u = new User(userID, line);
			User u = users2.get(userID);
			u.populateUserFeaturesPTF(line);
			users2.put(userID, u);
			line1=br1.readLine();
			line2=br2.readLine();
		}
		System.out.println("No of users: "+users2.size());
	}
	
	public static void loadUserData_PMF() throws IOException, ClassNotFoundException
	{
		FileInputStream fis = new FileInputStream("src/data/DS/usersAbove500Q");
        ObjectInputStream ois = new ObjectInputStream(fis);
        users2 = (HashMap<String, User>) ois.readObject();
        ois.close();
        BufferedReader br1 = new BufferedReader(new FileReader("src/data/toUse/users.txt"));
		String line1 = br1.readLine();
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/toUse/userOrder"));
		String line2 = br2.readLine();
		
		while(line1!=null && line2!=null)
		{
			String userID = line2.trim();
			String line = userID+"\t"+line1;
			//User u = new User(userID, line);
			User u = users2.get(userID);
			u.populateUserFeaturesPMF(line);
			users2.put(userID, u);
			line1=br1.readLine();
			line2=br2.readLine();
		}
		System.out.println("No of users: "+users2.size());
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
			// Uh oh, I MISSED adding this bow back to the user :(
			u.setBow(bow);// added on 14th July 2014.
			System.out.println("Populated bow for user: "+u.userID+" with bow size: "+u.bow.size());
		}
	}
	
	private static void populateUserBoWFromFile() throws IOException {
		BufferedReader br;
		br = new BufferedReader(new FileReader("src/data/3000UserBoW"));
		String line = br.readLine();
		while(line!=null)
		{
			String userID = line.substring(0, line.indexOf(' '));
			int t = line.indexOf(' ');
			line = line.substring(line.indexOf(' '));
			String parts[] = line.split("\t");
			if(parts.length<2) {line = br.readLine();continue;}
			//System.out.println("-----"+userID+" "+parts[0]+" "+parts[1]);
			//String userID = parts[0];
			User u = users2.get(userID);
			HashMap<String, Float> bow = new HashMap<String, Float>();
			for(int i=1;i<parts.length;i+=2)
			{
				if(parts[i]!=null && parts[i+1]!=null)
				{
					String word = parts[i];
					Float f = Float.parseFloat(parts[i+1]);
					bow.put(word, f);
				}
			}
			u.setBow(bow);
			//System.out.println("user "+u.userID+" populated with words: "+u.getBow().size());
			line = br.readLine();
		}
		//System.exit(0);
	}

	public static void evaluateMatchingQueries() throws IOException, ClassNotFoundException
	{
		/*FileInputStream fis = new FileInputStream("src/data/usersArrayList");
        ObjectInputStream ois = new ObjectInputStream(fis);
        usersArrayList = (ArrayList<User>) ois.readObject();
        System.out.println("Loaded the arrayList from memory..."+usersArrayList.size());
        fis.close();
        */
		System.out.println("Starting with evaluateMatchingQueries");
        Iterator<User> itr = usersArrayList.iterator();
        double avgMatch=0;
        int count = 0;
        while(itr.hasNext())
        {
        	User u = itr.next();
        	if(u.numQ<100) continue;
        	count++;
        	// now for this user, sort the candidate queries based on their scores..
        	Collections.sort(u.candidateQList, new Comparator<Query>()  // REVERSE sort the arrayList, so that den of p is easy
        	{

        		public int compare(Query q1, Query q2) {
        			if(q1.score < q2.score) return 1;
        			else if(q1.score > q2.score) return -1;
        			else return 0;
        		}
        	});
        	System.out.println("User: "+u.userID+"  ");
        	int match = 0, max = u.candidateQList.size();
        	for(int i=0;i<candidateQueriesCutoff;i++)
        	{
        		if(i == max) break;
        		Query q = u.candidateQList.get(i);
        		System.out.print(q.query+"_"+q.score+"_______");
        		//if(baselineOrNot == 1) {if(u.selfQueries.containsKey(q.query)) match++;}
        		//else if(baselineOrNot == 2) {if(u.selfTest.containsKey(q.query)) match++;}
        		if(u.selfTest.containsKey(q.query)) match++;
        	}
        	avgMatch+= match;
        	System.out.println("\nMatches in 10:_"+match);
        }
        System.out.println("average no of matches among "+count+" users:_"+avgMatch/count);
	}
	
	public static void checkStatusOfUsersArrayList(){
		Iterator<User> itr = usersArrayList.iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			Iterator<Query> itr1 = u.candidateQList.iterator();
			System.out.println("user: "+u.userID+" no of candidate queries: "+u.candidateQList.size());
		}
	}
	
	private static void calculateSimCutOff(int threshold) {//this function gives theequivalent similarity score for threshold no of user-neighbors
		perUserCutOff = new HashMap<String, Double>();
		int size = usersArrayList.size();
		for(int i=0;i<size;i++)
		{
			String userID = usersArrayList.get(i).userID;
			ArrayList<Float> scores = new ArrayList<Float>();
			for(int j=0;j<size;j++)
			{
				
				User u1 = usersArrayList.get(i);
				User u2 = usersArrayList.get(j);
				int clusterID1 = user2ClusterMap.get(u1.userID);
				int clusterID2 = user2ClusterMap.get(u2.userID);
				if(clusterID1 != clusterID2){continue;}
				
				if(i==j) continue;
				scores.add(simU[i][j]);
			}
			Comparator<Float> comparator = Collections.reverseOrder();
			Collections.sort(scores, comparator);
			//for(int j=0;j<11;j++) System.out.print(scores.get(j)+" ");
			//System.out.println();
			System.out.println("Populatign perUserCutOff: userID:"+userID+" scores size: "+scores.size());
			
			perUserCutOff.put(userID, new Double(scores.get(threshold)));
		}
		//System.exit(0);
		System.out.println("Done with calculateSimCutOff");
	}

	private static void calculateScoreForEachCandidateForEachUser() {// I mean each candidate QUERY for each user
		System.out.println("Starting with score calculation for each candidate");
		int size = usersArrayList.size();
		for(int i=0;i<size;i++)
		{
			User u1 = usersArrayList.get(i);
			u1.candidateQList = new ArrayList<Query>();
			double max = 0;String maxQuery="";
			HashMap<String, Double> candidates = u1.getCandidateQueries();
			Iterator<String> itr = candidates.keySet().iterator();
			while(itr.hasNext())
			{
				String query = itr.next();
				if(query.compareTo("-")==0) continue;
				double score = 0;
				for(int j=0;j<size;j++)
				{
					if(i==j) continue;
					User u2 = usersArrayList.get(j);
					if(u2.getSelfQueries().containsKey(query))
					{
						if(simU[i][j]>=perUserCutOff.get(u1.userID))
							score += simU[i][j];
					}
				}
				candidates.put(query, new Double(score));
				if(max<score) {max = score;maxQuery=query;}
			}
			u1.setCandidateQueries(candidates);
			System.out.println("for user "+u1.userID+" max score: "+max+" for query: "+maxQuery);
			u1.populateCandidateList();//only after the scores have been updated in the hashmap.
		}
		System.out.println("Done with calculateScoreForEachCandidateForEachUser");
	}

	public static void populateCandidatesForEachUser()
	{
		int size = usersArrayList.size();
		for(int i=0;i<size;i++)
		{
			User u1 = usersArrayList.get(i);
			if(u1.userID.compareTo("7486574")==0) continue;
			System.out.print(u1.userID+"--");
			u1.candidateQueries = new HashMap<String, Double>();
			for(int j=0;j<size;j++)
			{
				if(i==j) continue;
				User u2= usersArrayList.get(j);
				
				int clusterID1 = user2ClusterMap.get(u1.userID);
				int clusterID2 = user2ClusterMap.get(u2.userID);
				if(clusterID1 != clusterID2) continue;
				
				if(simU[i][j]>=perUserCutOff.get(u1.userID))
				{
					ArrayList<String> queries = u2.getQueries();
					for(int k =0;k<queries.size();k++)
					{
						String query = queries.get(k);
						if(u1.candidateQueries.containsKey(query));
						else u1.candidateQueries.put(query, new Double(0));
					}
				}
				else continue;
			}
			//System.out.println(i+" User "+u1.userID+" paopulated with candidate queries: "+usersArrayList.get(i).candidateQueries.size());
		}
		System.out.println("Done with populateCandidatesForEachUser");
	}
	
	private static void userSimilarity() {
		int size = usersArrayList.size();
		System.out.println("Size: "+size);
		simU = new float[size][size];
		for(int i=0;i<size;i++)
		{
			for(int j=0;j<size;j++)
			{
				User u1 = usersArrayList.get(i);
				User u2 = usersArrayList.get(j);
				int clusterID1 = user2ClusterMap.get(u1.userID);
				int clusterID2 = user2ClusterMap.get(u2.userID);
				if(clusterID1 != clusterID2){simU[i][j] = 0;continue;}
				
				if(i==j) simU[i][j]=1;
				if(baselineOrNot == 1)
					simU[i][j] = computeSim(usersArrayList.get(i),usersArrayList.get(j));
				else if(baselineOrNot==2)
				{
					float sim = cosSimilarityBetweenFreqMaps(usersArrayList.get(i).getBow(),usersArrayList.get(j).getBow());
					if(sim>=0 && sim<=10) simU[i][j] = sim;
					else sim=0;
				}
				//System.out.print(simU[i][j]+"__");
			}
			//System.out.println();
		}
		System.out.println("Done with populating userSimilarity...");
	}
	
	public static float computeSim(User u1, User u2)
	{
		float sim = 0, dot=0, d1=0,d2=0;
		HashMap<Integer, Double> features1 = u1.getFeatures();
		HashMap<Integer, Double> features2 = u2.getFeatures();
		int numF=features1.size();
		for(int i=1;i<=numF;i++)
		{
			dot += (features1.get(new Integer(i))*features2.get(new Integer(i)));
			d1 +=  (features1.get(new Integer(i))*features1.get(new Integer(i)));
			d2 +=  (features2.get(new Integer(i))*features2.get(new Integer(i)));
		}
		d1 = (float) Math.sqrt(d1);
		d2 = (float) Math.sqrt(d2);
		double denom = d1*d2;
		sim = (float) (dot/denom);
		return sim;
	}

	public static void loadUserMap() throws IOException, ClassNotFoundException
	{
		/*FileInputStream fis = new FileInputStream("src/data/users2HashMap");
        ObjectInputStream ois = new ObjectInputStream(fis);
        users2 = (HashMap<String, User>) ois.readObject();
        ois.close();*/
        System.out.println(users2.size());
        usersArrayList = new ArrayList<User>();
        Iterator<User> itr = users2.values().iterator();
        while(itr.hasNext())
        {
        	User u = itr.next();
        	if(u.numQ > 550 && u.numQ < 600)
        	{
        		u.populateUniqueQueryList();
        		usersArrayList.add(u);
        	}
        }
        System.out.println("No of users added to arrayList: "+usersArrayList.size());
        users2.clear();
        /*
        for(int i=0;i<usersArrayList.size();i++)
        {
        	ArrayList<String> queries1 = usersArrayList.get(i).getQueries();
        	int max=0; String maxUserID="";
        	for(int j=0;j<usersArrayList.size();j++)
        	{
        		if(i==j) continue;
        		ArrayList<String> queries2 = usersArrayList.get(j).getQueries();
        		int c=0;
        		for(int k=0;k<queries1.size();k++)
        		{
        			String query1 = queries1.get(k);
        			if(queries1.get(k).compareTo("")==0 || query1.length()<2) continue;
        			for(int l=0;l<queries2.size();l++)
        			{
        				String query2=queries2.get(l);
        				if(queries2.get(l).compareTo("")==0 || query2.length()<2) continue;
        				if(queries1.get(k).compareTo(queries2.get(l))==0)
        				{
        					c++;
        					//System.out.println(queries1.get(k)+"------"+queries2.get(l));
        				}
        			}
        		}
        		if(max<c) {max=c;maxUserID=usersArrayList.get(j).userID;}
        		//System.out.print(c+"_");
        	}
        	System.out.println("numQ: "+usersArrayList.get(i).getNumQ()+" max: "+max+" for users___"+usersArrayList.get(i).userID+"__with__"+maxUserID);
        }
        */
	}

	private static void checkUserDetails() throws IOException {
		
		System.out.println("\n\nCHECKING USER DETAILS\n\n");
		
		FileWriter fstream2 = new FileWriter("src/data/dump/userAllQ");
		BufferedWriter out2 = new BufferedWriter(fstream2);
		FileWriter fstream3 = new FileWriter("src/data/dump/userSelfQMap-Unique");
		BufferedWriter out3 = new BufferedWriter(fstream3);
		System.out.println("\n\n\n\n\nSize of users2 map: "+users2.size());
		Iterator<User> itr = users2.values().iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			System.out.println("userID: "+u.userID+"  NumQ: "+u.getNumQ()+"  nFeatures: "+u.getFeatures().size()+" numUniqueSelfQ: "+u.getSelfQueries().size());
			ArrayList<String> queries = u.getQueries();
			for(int i=0;i<queries.size();i++)
			{
				out2.write(queries.get(i));
				out2.write("\n");
			}
			out2.write("\n");
			
			Iterator<String> itr1 = u.getSelfQueries().values().iterator();
			while(itr1.hasNext())
			{
				String query = itr1.next();
				out3.write(query);
				out3.write("\n");
			}
		}
		out2.close();
		out3.close();
	}

	private static void populateUsersFromAOLData() throws IOException {
		String filename = "src/data/AOL1.txt";
		BufferedReader br;
		br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		line = br.readLine();
		int start = 1;
		String prevUserID = "";
		int c=0, count=10;
		while(count>0)
		{
			filename = "src/data/AOL"+count+".txt";
			count--;
			br = new BufferedReader(new FileReader(filename));
			line = br.readLine();line = br.readLine();
			while(line!=null)
			{
				try{
					c++;
					//if(c==100) break;
					String parts[] = line.split("\t");
					String userID = "";
					
					
					if(line.length()<1 || parts.length<1) {line = br.readLine();continue;}
					userID = parts[0];
					if(!users2.containsKey(userID)) {line = br.readLine();continue;}
					//if(line.contains("2317930")) System.out.println("-----------------------"+userID);
					if(userID.compareTo(prevUserID) == 0)
					{
						ArrayList<String> queries = users.get(userID);
						queries.add(parts[1]);
						
						User u = users2.get(userID);
						u.setQueries(queries);
						u.populateSelfQueries();
						u.populateSelfTestTrainQueries();
						u.setNumQ(queries.size());
						users2.put(userID, u);
						
						users.put(userID, queries);
					}
					else
					{
						if(prevUserID.length()>0)
						{
							User u = new User(prevUserID, users.get(prevUserID));
							userList.add(u);
							//System.out.println("Added user to userList: "+u.userID+" "+u.numQ);
						}
						prevUserID = userID;
						ArrayList<String> queries = new ArrayList<String>();
						queries.add(parts[1]);
						
						User u = users2.get(userID);
						u.setQueries(queries);
						u.populateSelfQueries();
						u.populateSelfTestTrainQueries();
						u.setNumQ(queries.size());
						users2.put(userID, u);
						
						users.put(userID, queries);
					}
					line = br.readLine();
				} catch(Exception e) {e.printStackTrace();}

			}
			System.out.println("Done with AOL"+(count+1)+".txt & no of users right now: "+c);
		}

		System.out.println("The total no of users: "+users.size()+"_"+userList.size());
		System.out.println("Total no of queries scanned: "+c);
	}

	private static void populateUserFeatures() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("src/data/3000userFeaturesFinal"));
		String line = br.readLine();
		while(line!=null)
		{
			String parts[] = line.split("\t");
			String userID = parts[0].trim();
			User u = new User(userID, line);
			users2.put(userID, u);
			line = br.readLine();
		}
		Iterator<String> itr = users2.keySet().iterator();
		while(itr.hasNext())
		{
			String userID = itr.next();
			System.out.println("_"+userID+"_");
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