import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.MarginalProbEstimator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class MalletTest {
	private static final String[] INPUT_FILES = {
		//"src/data/3000QueriesDump"
		"src/data/toUse/LDAInput"
	};

	private static final String STOP_WORDS_FILE = "stoplists/en.txt";
	private static final int NUM_TOPICS = 10;
	private static final int NUM_ITERATIONS = 400;
	private static final int NUM_THREADS = 1;

	private static final int OPTIMIZE_INTERVAL = 0;
	private static final int BURN_IN_PERIOD = 0;

	private static final double ALPHA = 50.0;
	private static final double BETA = 0.01;

	public static void main(String[] args) throws IOException {
		System.out.println(NUM_ITERATIONS + " Iterations; " 
				+ NUM_TOPICS + " topics; optimize interval = " + OPTIMIZE_INTERVAL 
				+ "; burn in period = " + BURN_IN_PERIOD
				+ "; initial alpha = " + (new DecimalFormat("##.#####")).format(ALPHA/NUM_TOPICS) + "; "
				+ "initial beta = " + BETA);
		System.out.println("starting...");

		for(int inputIdx = 0; inputIdx < INPUT_FILES.length; inputIdx++) {
			System.out.println("Processing file " + INPUT_FILES[inputIdx]);
			System.out.println("[Topic No.]  [Topic Proportion]  [Alpha]  [Topic Words]");
			List<Pipe> pipeList = new ArrayList<Pipe>();
			pipeList.add(new CharSequenceLowercase());
			pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
			pipeList.add(new TokenSequenceRemoveStopwords(new File(STOP_WORDS_FILE), "UTF-8", false, false, false));
			pipeList.add(new TokenSequence2FeatureSequence());

			InstanceList instances = new InstanceList(new SerialPipes(pipeList));            
			Reader fileReader = new InputStreamReader(new FileInputStream(new File(INPUT_FILES[inputIdx])));
			instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(.*)$"), 1, 0, 0));

			ParallelTopicModel model = new ParallelTopicModel(NUM_TOPICS, ALPHA, BETA);
			model.setRandomSeed(412321);
			model.addInstances(instances);
			model.setNumThreads(NUM_THREADS);
			model.setNumIterations(NUM_ITERATIONS);
			model.setTopicDisplay(NUM_ITERATIONS, 15);
			model.setBurninPeriod(BURN_IN_PERIOD);
			model.setOptimizeInterval(OPTIMIZE_INTERVAL);



			long start = System.currentTimeMillis();
			model.estimate();
			long end = System.currentTimeMillis();
			System.out.println("Topic Modelling finished in " + ((end-start)/1000) + " seconds.");
			//			Object [][]res = model.getTopWords(10);
			//			for(int i=0;i<10;i++)
			//			{
			//				for(int j=0;j<10;j++)
			//				{
			//					System.out.print(res[i][j]+"\t");
			//				}
			//				System.out.println();
			//			}

			double[] alpha = model.getAlpha();
			System.out.print("Optimized Alpha:  ");
			for(int i = 0; i < alpha.length; i++) {
				System.out.print(alpha[i] + ", ");
			}
			System.out.println("\nOptimized Beta:  " + model.getBeta());
			fileReader.close();
			System.out.println("----------------------------------------------------------------------------------------");

		}
	}
}