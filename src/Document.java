/**
 * This class contains term frequency, doc id, and doc name.
 */
public class Document {

    private int ID; // Document ID
    private String docName;
    private double termFrequency; // Number of times term appears inside document

    public Document(String doc, int ID) {
        docName = doc;
        this.ID = ID;
    }

    public void setTermFrequency(double termFrequency) {
        this.termFrequency = termFrequency;
    }

    public int getID() {
        return ID;
    }

    public String getDocName(){return docName;}

    public double getTermFrequency() {
        return termFrequency;
    }

}
