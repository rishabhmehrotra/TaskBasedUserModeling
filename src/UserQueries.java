import java.io.*;
import java.util.*;

public class UserQueries {

	public static void main(String[] args) throws IOException{
		
		/*userFeatures();
		System.exit(0);
		
		combineSortedUsers();
		System.exit(0);
		*/
		String filename = "src/data/AOL1.txt";
		FileWriter fstream = new FileWriter("src/data/UserQueries");
		BufferedWriter out = new BufferedWriter(fstream);
		
		HashMap<String, ArrayList<String>> users = new HashMap<String, ArrayList<String>>();
		ArrayList<User> userList = new ArrayList<User>();
		
		
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
					if(userID.compareTo(prevUserID) == 0)
					{
						ArrayList<String> queries = users.get(userID);
						queries.add(parts[1]);
						users.put(userID, queries);
					}
					else
					{
						if(prevUserID.length()>0)
						{
							//System.out.println("No of queries in previous user: "+prevUserID+"__"+users.get(prevUserID).size());
							//out.write(prevUserID+"\t"+users.get(prevUserID).size());
							//out.write("\n");
							User u = new User(prevUserID, users.get(prevUserID));
							userList.add(u);
							//System.out.println("Added user to userList: "+u.userID+" "+u.numQ);
						}
						prevUserID = userID;
						ArrayList<String> queries = new ArrayList<String>();
						queries.add(parts[1]);
						users.put(userID, queries);
					}
					line = br.readLine();
				} catch(Exception e) {e.printStackTrace();}
				
			}
			System.out.println("Done with AOL"+(count+1)+".txt & no of users right now: "+c);
		}
		
		System.out.println("The total no of users: "+users.size()+"_"+userList.size());
		System.out.println("Total no of queries scanned: "+c);
		System.exit(0);
		//sort the users based on the number of queries for each user
		Collections.sort(userList, new Comparator<User>()  
			{
			public int compare(User u1, User u2) {
				if(u1.numQ < u2.numQ) return 1;
				else if(u1.numQ > u2.numQ) return -1;
				return 0;
			}
		});
		
		Iterator<User> itr = userList.iterator();
		while(itr.hasNext())
		{
			User u = itr.next();
			out.write(u.userID+"\t"+u.numQ);
			out.write("\n");
		}
		out.close();
	}

	private static void userFeatures() throws IOException {
		BufferedReader br1 = new BufferedReader(new FileReader("src/data/userSim_truncated.3000"));
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/3000userFeatures"));
		FileWriter fstream1 = new FileWriter("src/data/3000userFeaturesFinal");
		BufferedWriter out1 = new BufferedWriter(fstream1);
		String line1 = br1.readLine();
		String line2 = br2.readLine();
		int cc=0;
		while(line1!=null && line2!=null)
		{
			cc++;
			String parts1[] = line1.split("\t");
			String parts2[] = line2.split("\t");
			String userID = parts1[0];
			out1.write(userID+"\t");
			int c=0;
			for(int i=0;i<parts2.length;i++)
			{
				out1.write(parts2[i]+"\t");
				c++;
			}
			System.out.println(c+"_"+cc);
			out1.write("\n");
			line1 = br1.readLine();
			line2 = br2.readLine();
			if(line1 == null || line2==null) System.out.println("---\n"+line1+"\n"+line2+"\n---");
		}
		out1.close();
	}

	private static void combineSortedUsers() throws IOException {
		ArrayList<User> ulist = new ArrayList<User>();
		BufferedReader br;
		String line = "";
		int c=0;
		br = new BufferedReader(new FileReader("src/data/UserQueries1098"));
		line = br.readLine();
		while(line!=null)
		{
			String parts[] = line.split("\t");
			User u = new User(parts[0], Integer.parseInt(parts[1]));
			ulist.add(u);
			c++;
			line = br.readLine();
		}
		br = new BufferedReader(new FileReader("src/data/UserQueries765"));
		line = br.readLine();
		while(line!=null)
		{
			String parts[] = line.split("\t");
			User u = new User(parts[0], Integer.parseInt(parts[1]));
			ulist.add(u);
			c++;
			line = br.readLine();
		}
		br = new BufferedReader(new FileReader("src/data/UserQueries4321"));
		line = br.readLine();
		while(line!=null)
		{
			String parts[] = line.split("\t");
			User u = new User(parts[0], Integer.parseInt(parts[1]));
			ulist.add(u);
			c++;
			line = br.readLine();
		}
		
		
		Collections.sort(ulist, new Comparator<User>()  
				{
				public int compare(User u1, User u2) {
					if(u1.numQ < u2.numQ) return 1;
					else if(u1.numQ > u2.numQ) return -1;
					return 0;
				}
			});
		
		FileWriter fstream = new FileWriter("src/data/sortedUsers");
		BufferedWriter out = new BufferedWriter(fstream);
		Iterator<User> itr = ulist.iterator();
		int cc=0, c2=0;
		while(itr.hasNext())
		{
			cc++;
			User u = itr.next();
			if(u.numQ>=1000) c2++;
			out.write(u.userID+"\t"+u.numQ);
			out.write("\n");
		}
		out.close();
		System.out.println("Written to file info for "+c+"__"+cc+" users");
		System.out.println("Users with >=1000 queries: "+c2);
	}

}
