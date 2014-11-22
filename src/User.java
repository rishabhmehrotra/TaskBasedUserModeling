import java.util.*;
import java.io.*;
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	public String userID;
	public int numQ, numUniqQ;
	public ArrayList<String> queries;
	public ArrayList<String> uniqueQueries;
	public HashMap<Integer, Double> features;
	public String featureString;
	public HashMap<String, Double> candidateQueries;
	public HashMap<String, String> selfQueries;
	public ArrayList<Query> candidateQList;
	public HashMap<String, Float> bow;
	public HashMap<String, String> selfTest;
	public HashMap<String, String> selfTrain;
	public double LDAtopicDistribution[] = new double[10];

	public User(String userID, ArrayList<String> queries)
	{
		this.candidateQueries = new HashMap<String, Double>();
		//this.selfQueries = new HashMap<String, String>();
		this.bow = new HashMap<String, Float>();
		this.userID = userID;
		this.queries = queries;
		this.numQ = queries.size();
		this.numUniqQ = 0;
	}
	
	public User(String userID, int numQ)
	{
		this.candidateQueries = new HashMap<String, Double>();
		//this.selfQueries = new HashMap<String, String>();
		this.bow = new HashMap<String, Float>();
		this.userID = userID;
		this.numQ = numQ;
	}
	
	public User(String userID, String featureString)
	{
		this.candidateQueries = new HashMap<String, Double>();
		//this.selfQueries = new HashMap<String, String>();
		this.bow = new HashMap<String, Float>();
		this.userID = userID;
		this.featureString = featureString;
		this.features = new HashMap<Integer,Double>();
		populateFeaturesMap();
	}
	
	public void populateUserFeatures(String featureString)
	{
		this.candidateQueries = new HashMap<String, Double>();
		//this.selfQueries = new HashMap<String, String>();
		this.bow = new HashMap<String, Float>();
		this.featureString = featureString;
		this.features = new HashMap<Integer,Double>();
		populateFeaturesMap();
	}

	private void populateFeaturesMap() {
		String parts[] = this.featureString.split("\t");
		//start loop from 1 as 0 is the userID
		System.out.print(this.userID+"__");
		for(int i=1;i<parts.length;i++)
		{
			Double d = Double.parseDouble(parts[i]);
			this.features.put(new Integer(i), d);
			System.out.print(d+"__");
		}
		System.out.println();
	}
	
	public HashMap<Integer, Double> getFeatures() {
		return features;
	}

	public void setFeatures(HashMap<Integer, Double> features) {
		this.features = features;
	}
	
	public ArrayList<String> getQueries() {
		//return queries;
		return this.uniqueQueries;//NOTE: This should be just queries
	}

	public void setQueries(ArrayList<String> queries) {
		this.queries = queries;
	}

	public int getNumQ() {
		return numQ;
	}

	public void setNumQ(int numQ) {
		this.numQ = numQ;
	}
	public HashMap<String, Double> getCandidateQueries() {
		return candidateQueries;
	}

	public void setCandidateQueries(HashMap<String, Double> candidateQueries) {
		this.candidateQueries = candidateQueries;
	}

	public void populateSelfQueries() {
		this.selfQueries = new HashMap<String, String>();
		Iterator<String> itr = this.queries.iterator();
		while(itr.hasNext())
		{
			String query = itr.next();
			if(this.selfQueries.containsKey(query));
			else this.selfQueries.put(query, query);//NOTE: Doing this removes query repetitions for each user
		}
		this.numUniqQ = this.selfQueries.size();
	}
	
	public HashMap<String, String> getSelfQueries() {
		return selfQueries;
	}

	public void setSelfQueries(HashMap<String, String> selfQueries) {
		this.selfQueries = selfQueries;
	}

	public void populateCandidateList() {
		this.candidateQList = new ArrayList<Query>();
		Iterator<String> itr = this. candidateQueries.keySet().iterator();
		while(itr.hasNext())
		{
			String query =  itr.next();
			double score = this.candidateQueries.get(query);
			Query q = new Query(query,score);
			this.candidateQList.add(q);
		}
	}
	
	public HashMap<String, Float> getBow() {
		return bow;
	}

	public void setBow(HashMap<String, Float> bow) {
		this.bow = bow;
	}
	
	public HashMap<String, String> getSelfTest() {
		return selfTest;
	}

	public void setSelfTest(HashMap<String, String> selfTest) {
		this.selfTest = selfTest;
	}

	public HashMap<String, String> getSelfTrain() {
		return selfTrain;
	}

	public void setSelfTrain(HashMap<String, String> selfTrain) {
		this.selfTrain = selfTrain;
	}

	public void populateSelfTestTrainQueries() {
		this.selfTest = new HashMap<String, String>();
		this.selfTrain = new HashMap<String, String>();
		int num = this.numUniqQ;
		int c =0;
		Iterator<String> itr = this.selfQueries.keySet().iterator();
		while(itr.hasNext())
		{
			String query = itr.next();
			if(c<(num/5)) this.selfTrain.put(query, query);
			else this.selfTest.put(query, query);
			c++;
		}
	}
	
	public void populateUniqueQueryList()
	{
		this.uniqueQueries = new ArrayList<String>();
		Iterator<String> itr = this.selfQueries.values().iterator();
		while(itr.hasNext())
		{
			String query = itr.next();
			this.uniqueQueries.add(query);
		}
	}
}
