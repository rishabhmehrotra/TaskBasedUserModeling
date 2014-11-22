import java.io.*;
import java.util.*;

public class ExtractRelevantUsersFromAOLLog {
	
	public static int THRESHOLD = 500;
	public static ArrayList<User> userList;
	public static HashMap<String, String> usersOverThreshold;// just the userIDs of users above thres no of queries from sortedUsers file
	public static HashMap<String, User> users2;
	public static HashMap<String, ArrayList<String>> users = new HashMap<String, ArrayList<String>>();
	
	public static void main(String[] args) throws IOException
	{
		userList = new ArrayList<User>();
		usersOverThreshold = new HashMap<String, String>();
		users2 = new HashMap<String, User>();
		populateValidUsersMap();
		System.out.println("\nnDone with populateValidUsers\n\n");
		populateUsersAoveThresholdNoOfQueriesFromAOLLog();
		System.out.println("\n\nDone with populating users from AOL logs\n\n");
		//printUserAboveThreshold();
		
		//printFileForLDA();
		//System.exit(0);
		users.clear();
		FileOutputStream fos = new FileOutputStream("src/data/DS/usersAbove500Q");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(users2);
		oos.close();
		System.out.println("Done -"+users2.size()+"- saved!");
	}
	
	public static void printFileForLDA() throws IOException
	{
		
		FileWriter fstream1 = new FileWriter("src/data/toUse/LDAInput");
		BufferedWriter out1 = new BufferedWriter(fstream1);
		
		Iterator<User> itr = users2.values().iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			if(u.queries.size()<10) continue;
			System.out.println("User "+u.userID+" Queries List size: "+u.queries.size());
			u.populateSelfQueries();
			System.out.println("Writing to file "+u.getSelfQueries().size()+"queries for user "+u.userID);
			Iterator<String> itr1 = u.getSelfQueries().values().iterator();
			while(itr1.hasNext())
			{
				String query = itr1.next();
				out1.write(query);
				out1.write("\n");
			}
		}
		out1.close();
	}
	
	public static void printUserAboveThreshold()
	{
		Iterator<User> itr = users2.values().iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			System.out.println("UserID: "+u.userID+" nQ: "+u.numQ);
		}
	}
	
	public static void populateValidUsersMap() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("src/data/sortedUsers"));
		String line = br.readLine();
		while(line!=null)
		{
			String parts[] = line.split("\t");
			String userID = parts[0];
			int nQ = Integer.parseInt(parts[1]);
			if(nQ > THRESHOLD)
			{
				usersOverThreshold.put(userID, userID);
				User u = new User(userID, 0);
				users2.put(userID, u);
			}
			line = br.readLine();
		}
		System.out.println(usersOverThreshold.size()+" users added to the threshold map");
	}
	
	public static void populateUsersAoveThresholdNoOfQueriesFromAOLLog() throws IOException
	{
		String filename = "src/data/AOL/AOL1.txt";
		BufferedReader br;
		br = new BufferedReader(new FileReader(filename));
		String line = br.readLine();
		line = br.readLine();
		int start = 1;
		String prevUserID = "";
		int c=0, count=10;
		while(count>0)
		{
			filename = "src/data/AOL/AOL"+count+".txt";
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
					if(!usersOverThreshold.containsKey(userID)) {line = br.readLine();continue;}
					//if(line.contains("2317930")) System.out.println("-----------------------"+userID);
					
					if(userID.compareTo(prevUserID) == 0)
					{
						ArrayList<String> queries = users.get(userID);
						if(queries == null) queries = new ArrayList<String>();
						queries.add(parts[1]);
						
						User u = users2.get(userID);
						u.setQueries(queries);
						//u.populateSelfQueries();
						//u.populateSelfTestTrainQueries();
						u.setNumQ(queries.size());
						users2.put(userID, u);
						
						users.put(userID, queries);
					}
					else
					{
						/*if(prevUserID.length()>0)
						{
							User u = new User(prevUserID, users.get(prevUserID));
							userList.add(u);
							//System.out.println("Added user to userList: "+u.userID+" "+u.numQ);
						}*/
						prevUserID = userID;
						ArrayList<String> queries = new ArrayList<String>();
						queries.add(parts[1]);
						
						User u = users2.get(userID);
						u.setQueries(queries);
						//u.populateSelfQueries();
						//u.populateSelfTestTrainQueries();
						u.setNumQ(queries.size());
						users2.put(userID, u);
						
						users.put(userID, queries);
					}
					line = br.readLine();
				} catch(Exception e) {e.printStackTrace();}

			}
			System.out.println("Done with AOL"+(count+1)+".txt & no of users right now: "+c+"_"+users.size());
		}

		System.out.println("The total no of users: "+users.size()+"_"+userList.size()+"_"+users2.size());
		System.out.println("Total no of queries scanned: "+c);
	}

}
