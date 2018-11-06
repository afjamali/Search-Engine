/**
 * This class contains term frequency, original word (before stemming).
 */
public class Query {
    private double termFrequency; // Number of times term appears inside query
    private String origWord;

    public Query(String queryWord) {
        this.origWord = queryWord;
    }

    public void setQueryWord(String queryWord) {
        this.origWord = queryWord;
    }

    public void setTermFrequency(double termFrequency) {
        this.termFrequency = termFrequency;
    }

    public double getTermFrequency() {
        return termFrequency;
    }

    public String getQueryWord() {
        return origWord;
    }
}
