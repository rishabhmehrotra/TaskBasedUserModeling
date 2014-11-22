import java.io.Serializable;


public class Query implements Serializable{

	private static final long serialVersionUID = 1L;
	public String query;
	public double score;

	public Query(String query, double score)
	{
		this.query = query;
		this.score = score;
	}
	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
}
