import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class TrainingEntry {

	private String query;
	private String docid;
	private Map<String, Double> features;
	private Double score;
	private String type;

	public TrainingEntry(String query, String docid, Map<String, Double> features, Double score, String type) {
		this.setQuery(query);
		this.setDocid(docid);
		this.setFeatures(features);
		this.setScore(score);
		this.setType(type);
	}

	public Map<String, Double> getFeatures() {
		return features;
	}

	public void setFeatures(Map<String, Double> features) {
		this.features = features;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDocid() {
		return docid;
	}

	public void setDocid(String docid) {
		this.docid = docid;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}


}
