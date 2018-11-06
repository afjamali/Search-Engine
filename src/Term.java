/**
 * This class contains document frequency, total number of occurances accross all
 * documents in a collection/domain.
 */
public class Term {

    private int docFrequency; // Number of documents containing the word
    private int occurances; // Total number of appearances in all documents

    public void setDocFrequency(int docFrequency){
        this.docFrequency = docFrequency;
    }

    public void setOccurances(int occurances){
        this.occurances = occurances;
    }

    public int getDocFrequency(){return docFrequency;}

    public int getOccurances(){return occurances;}
}
