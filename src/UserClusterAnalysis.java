import java.io.*;
import java.util.*;

import output.ValidationWriter;
import input.Dataset;
import input.InputReader;
import validationIndices.CIndex;
import validationIndices.GoodmanKruskal;
import algorithms.Algorithms;
import algorithms.KMeans;
import distance.EuclideanDistance;

public class UserClusterAnalysis {

	/* Cluster Analysis: In this class we run the experiments to compare the user representations based on the cluster analysis of the representations learnt
	 * the intuition being that good representations will result in better user clusters
	 * we measure the cluster performance based on internal cluster validation techniques
	 */

	public static HashMap<String, ArrayList<Double>> uBoW;
	public static HashMap<String, ArrayList<Double>> uLDA;//userRep_LDA
	public static HashMap<String, ArrayList<Double>> uTask;//users.txt
	public static HashMap<String, ArrayList<Double>> uTT;//gen2.txt

	public static void main(String[] args) throws IOException {
		uBoW = new HashMap<String, ArrayList<Double>>();
		uLDA = new HashMap<String, ArrayList<Double>>();
		uTask = new HashMap<String, ArrayList<Double>>();
		uTT = new HashMap<String, ArrayList<Double>>();

		int initialSetupRequired = 2;
		if(initialSetupRequired == 1)
		{
			loadUserRepresentations();
			populateFilesForClustering();
		}
		else
		{
			int nC = 10;
			for(int i=1;i<=10;i++)
			{
				cluster("src/data/toUse/cluster-LDA.txt", nC);
				cluster("src/data/toUse/cluster-Task.txt", nC);
				cluster("src/data/toUse/cluster-TT.txt", nC);
				nC += 10;
			}
		}
	}



	@SuppressWarnings("unused")
	public static void cluster(String filename, int nClusters)
	{
		System.out.println("\n\n\n------------------------------"+filename);
		/*
		InputReader inputReader = new InputReader();
        //this.testset =inputReader.readFromfile("C:\\Users\\Markus\\Documents\\Masterarbeit\\Workspace\\Clusterer\\src\\90.valid");
        Dataset dataSet =inputReader.readFromfile("src/data/toUse/cluster-Task.txt");
        Leader leaderClusterer = new Leader();
        //optimize parameters for leader
        List <Float> distances = new ArrayList<Float>((dataSet.size()* (dataSet.size()-1))/2);
        int iterationCount = 0;
        EuclideanDistance eDist = new EuclideanDistance();
        for (int i = 0; i < dataSet.size(); i++){
                for (int j = i+1; j < dataSet.size(); j++) {
                         float dist = eDist.calculate(dataSet.get(i).getFeatures(), dataSet.get(j).getFeatures());
                         distances.add(dist);
                         iterationCount++;
                }
        }
        float maxdist = Collections.max(distances); 
        System.out.println("Maxdistance between elements is" + maxdist);
        CIndex cIndex = new CIndex();
        GoodmanKruskal kruski = new GoodmanKruskal();
        List <Float> indexvalues = new ArrayList();
        for (int i = 2; i <=  maxdist; i++  ){
        //      System.out.println("Iteration " +i+" of "+ maxdist);
                leaderClusterer.setEpsilon(i);
                leaderClusterer.doClustering(dataSet);
                float index = cIndex.calculateIndex(dataSet);
                indexvalues.add(index);
                System.out.println ("The C index for epsilon = "+i + " is: "+index +" and there are "+ dataSet.getClustermap().size() + "clusters");

        }
        System.out.println("The best value for epsilon is " + Collections.min(indexvalues));
		 */
		String inputFileName,outputFileName;
		int numOfClusters;

		inputFileName = filename;
		outputFileName = "src/data/toUse/cluster-Task_OUTPUT.txt";
		numOfClusters = 10;

		Dataset dataset = InputReader.readFromfile(inputFileName);

		KMeans kmeans = new KMeans(new EuclideanDistance(),numOfClusters);
		kmeans.doClustering(dataset);
		InputReader.writeDatasetToFile(outputFileName , dataset);
		Map<String,String> params = new HashMap<String,String>();
		params.put(ValidationWriter.KMEANS_K_LABEL,String.valueOf(numOfClusters));
		ValidationWriter.printValidationIndices("KMEANS", params, dataset);
		ValidationWriter.writeToCSV("kmeansResults.csv", Algorithms.KMeans, dataset, params);
		ValidationWriter.writeValidationIndice(outputFileName, "KMeans", params, dataset);
	}

	public static void loadUserRepresentations() throws IOException
	{
		System.out.println("Starting loading user representations...");
		BufferedReader br0 = new BufferedReader(new FileReader("src/data/toUse/userOrder"));
		String line0 = br0.readLine();
		BufferedReader br2 = new BufferedReader(new FileReader("src/data/toUse/userRep_LDA.txt"));
		String line2 = br2.readLine();
		BufferedReader br3 = new BufferedReader(new FileReader("src/data/toUse/users.txt"));
		String line3 = br3.readLine();
		BufferedReader br4 = new BufferedReader(new FileReader("src/data/toUse/gen2.txt"));
		String line4 = br4.readLine();

		while(line0!=null)
		{
			String userID = line0.trim();

			// extract features from lines
			String parts2[] = line2.split("\t");
			String parts3[] = line3.split("\t");
			String parts4[] = line4.split(" ");

			//System.out.println("Extacting features for user:"+userID+" "+parts2.length+"_"+parts3.length+"_"+parts4.length+"_");

			ArrayList<Double> list2 = new ArrayList<Double>();
			ArrayList<Double> list3 = new ArrayList<Double>();
			ArrayList<Double> list4 = new ArrayList<Double>();

			for(int i=0;i<parts2.length;i++)
			{
				double t = Double.parseDouble(parts2[i]);
				//System.out.print(t+"_");
				list2.add(t);
			}
			//System.out.println();
			for(int i=0;i<parts3.length;i++)
			{
				double t = Double.parseDouble(parts3[i]);
				//System.out.print("_"+t+"_");
				list3.add(t);
			}
			//System.out.println();
			for(int i=0;i<parts4.length;i++)
			{
				double t = Double.parseDouble(parts4[i]);
				//System.out.print("_"+t+"_");
				list4.add(t);
			}
			//System.out.println();
			//System.out.println("Extacted features for user:"+userID+" "+list2.size()+" "+list3.size()+" "+list4.size());

			uLDA.put(userID, list2);
			uTask.put(userID, list3);
			uTT.put(userID, list4);

			line0 = br0.readLine();
			line2 = br2.readLine();
			line3 = br3.readLine();
			line4 = br4.readLine();
		}
		br0.close();br2.close();br3.close();br4.close();
		System.out.println("Completed loading user representations with hashmap sizes:"+uLDA.size()+"_"+uTask.size()+"_"+uTT.size());
	}

	public static void populateFilesForClustering() throws IOException
	{
		FileWriter fstream2 = new FileWriter("src/data/toUse/cluster-LDA.txt");
		BufferedWriter out2 = new BufferedWriter(fstream2);
		FileWriter fstream3 = new FileWriter("src/data/toUse/cluster-Task.txt");
		BufferedWriter out3 = new BufferedWriter(fstream3);
		FileWriter fstream4 = new FileWriter("src/data/toUse/cluster-TT.txt");
		BufferedWriter out4 = new BufferedWriter(fstream4);

		Iterator<String> itr = uLDA.keySet().iterator();
		while(itr.hasNext())
		{
			String userID = itr.next();
			ArrayList<Double> al2 = uLDA.get(userID);
			ArrayList<Double> al3 = uTask.get(userID);
			ArrayList<Double> al4 = uTT.get(userID);
			out2.write(userID+" ");
			out3.write(userID+" ");
			out4.write(userID+" ");
			for(int i=0;i<al2.size();i++)
			{
				out2.write(i+1+":"+al2.get(i)+" ");
			}
			for(int i=0;i<al3.size();i++)
			{
				out3.write(i+1+":"+al3.get(i)+" ");
			}
			for(int i=0;i<al4.size();i++)
			{
				out4.write(i+1+":"+al4.get(i)+" ");
			}
			out2.write("\n");
			out3.write("\n");
			out4.write("\n");
		}
		out2.close();out3.close();out4.close();
	}
}
