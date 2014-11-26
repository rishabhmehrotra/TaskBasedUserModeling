import java.io.*;


public class Misc {

	public static void main(String[] args) throws IOException {
		//misc1();
		misc2();
	}
	
	public static void misc2() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("src/data/toUse/gen2.txt"));
		String line = br.readLine();
		
	}
	
	public static void misc1() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("src/data/toUse/filledTensorForTensorDecomposition"));
		String line = br.readLine();
		
		FileWriter fstream = new FileWriter("src/data/toUse/tensorForMatlab");
		BufferedWriter out = new BufferedWriter(fstream);
		
		for(int i=0;i<8391;i++)
		{
			for(int j=0;j<10;j++)
			{
				for(int k=0;k<1521;k++)
				{
					double d = Double.parseDouble(line);
					out.write(""+(i+1)+","+(j+1)+","+(k+1)+","+(float)d);
					out.write("\n");
					line = br.readLine();
				}
			}
		}
		out.close();
		br.close();
	}

}
