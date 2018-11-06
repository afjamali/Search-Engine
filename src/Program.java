import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * This class is used to compute tf x idf weights and cosine similarity depending
 * on the domain. tf x idf are calculated each time user searches since the domain can change
 * each time.
 *
 * This class is also used for GUI components, including mouse and action listeners.
 * The results are displayed to the user.
 *
 * includes a no arg constructor. Two listeners are added, Action listener and Mouse listener.
 * The core functionality is in the Action listener, when the user clicks the Search button.
 * The query is produced, the collection of relevant documents is obtained, then calculated
 * using tf x idf weights, and measured using cosine similarity and finally displayed back to user.
 */
public class Program {
    private JTextField searchTextField;
    private JButton searchButton;
    private JList resultList;
    private JLabel nameLabel;
    private JPanel mainPanel;
    private JComboBox domainComboBox;
    private JLabel selectDomainLabel;
    static Map<String, Query> query;
    final DecimalFormat df = new DecimalFormat("#.###"); //decimal format (3 decimal places)

    /**
     * This method initialized the user interface.
     * @param args not used
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Program");
        frame.setContentPane(new Program().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * This is the no arg constructor. Two listeners are added, Action listener and Mouse listener.
     * The core functionality is in the Action listener, when the user clicks the Search button.
     * The query is produced, the collection of relevant documents is obtained, then calculated
     * using tf x idf weights, and measured using cosine similarity and finally displayed back to user.
     * The user has the option to open the files based on the results.
     */
    public Program() {

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                produceQueryDictionary(searchTextField.getText()); //produce the query, including term frequencies
                Set<Integer> docCollection = getDocCollection(); //retrieve relevant documents based on domain
                // Calculate TF x IDF
                Map<String, HashMap<Integer, Document>> tf_x_idf = calculateTFxIDF(docCollection);
                displayResult(calculateCosineSim(tf_x_idf, docCollection)); //calculate cosine similarity and display results.
            }
        });

        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                JList theList = (JList) mouseEvent.getSource();
                if (mouseEvent.getClickCount() == 2) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        Object o = theList.getModel().getElementAt(index);
                        try {
                            Desktop.getDesktop().open(new File("docs\\" + o.toString() + ".txt")); //open selected file from the results
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * This method displays the search results to the user.
     * @param hashMap the document id followed by the cosine similarity. Sorted by sim in descending order
     */
    private void displayResult(HashMap<Integer, Double> hashMap) {
        List<Integer> tracker = new ArrayList<Integer>();
        DefaultListModel listModel = new DefaultListModel();

        if (hashMap.isEmpty()){
            listModel.addElement("No documents found...");
        }
        else {
            for (int id : hashMap.keySet()){
                for (String term : query.keySet()){
                    if(Database.postings.get(term).containsKey(id)){
                        if(!tracker.contains(id)) {
                            listModel.addElement(Database.postings.get(term).get(id).getDocName());
                            tracker.add(id);
                        }
                    }
                }
            }
        }

        resultList.setModel(listModel);

    }

    /**
     * This method collects all relevant document id's based on the domain chosen by the user.
     * If no domain is chosen, it will treat the collection as all the files in all directories.
     * @return document id's
     */
    private Set<Integer> getDocCollection() {

        Set<Integer> docCollection = new HashSet<Integer>(); //initialize collection variable
        String domain = domainComboBox.getSelectedItem().toString(); //the domain
        List<String> queryList = new ArrayList<String>(query.keySet()); //copy of query. For purposes of modification to the original query

        //if domain is not set, collect all files containing the queries
        if (domain.contains("not set...")) {
            for (String term : queryList) {
                //if term is not in the index, check if a term similar to the index term exists!!!!!!
                if (!Database.postings.containsKey(term)) {
                    for (String t : Database.dictionary.keySet()){
                        if (term.contains(t)) {
                            docCollection.addAll(Database.postings.get(t).keySet()); //add all id's that contain the term
                            query.put(t, new Query(term)); //add modified term to query
                            query.get(t).setTermFrequency(query.get(term).getTermFrequency());
                        }
                    }
                    query.remove(term); //delete non-existent term
                }
                else
                    docCollection.addAll(Database.postings.get(term).keySet()); //add all id's that contain the term
            }
        } else {
            for (String term : queryList) { //if domain is chosen by user, only collect id's relevant to the chosen domain
                if(!Database.postings.containsKey(term)) { //if term is not in the index, check if a term similar to the index term exists!!!!!!
                    for (String t : Database.dictionary.keySet()){
                        if (term.contains(t)) {
                            for (int id : Database.postings.get(t).keySet()) {
                                if (Database.postings.get(t).get(id).getDocName().contains(domain)) { //is the term relevant to the domain?
                                    docCollection.add(id); //add relevant id's by domain
                                }
                                query.put(t, new Query(term)); //add modified term to query
                                query.get(t).setTermFrequency(query.get(term).getTermFrequency());
                            }
                        }
                    }
                    query.remove(term); //delete non-existent term
                }
                else {
                    for (int id : Database.postings.get(term).keySet()) {
                        if (Database.postings.get(term).get(id).getDocName().contains(domain)) {
                            docCollection.add(id); //add relevant id's by domain
                        }
                    }
                }
            }
        }
        return docCollection;
    }

    /**
     * Calculate the cosine similarities between the queries and the corresponding relevant documents.
     * @param weightedTerms tf x idf
     * @param docCollection relevant documents (id's)
     * @return HashMap of document id followed by sim. Sorted by similarity in descending order
     */
    private HashMap calculateCosineSim(Map<String, HashMap<Integer, Document>> weightedTerms, Set<Integer> docCollection) {
        // declare variables
        Map<Integer, Double> norm = new HashMap<Integer, Double>();
        Map<Integer, Double> dot = new HashMap<Integer, Double>();
        Map<Integer, Double> sim = new HashMap<Integer, Double>();
        double sumPower = 0;

        //calculate the sum of all the queries squared
        for (String term : query.keySet()) {
            sumPower += Math.pow(query.get(term).getTermFrequency(), 2);
        }
        double sqrt = Math.sqrt(sumPower); // get square root of the sum of queries squared
        norm.put(0, Double.valueOf(df.format(sqrt))); //add to variable, assign index 0 for queries

        //calculate the sum of all the terms squared for each document
        for (int id : docCollection) {
            sumPower = 0;
            for (String term : Database.postings.keySet()) {
                if (Database.postings.get(term).containsKey(id)) {
                    sumPower += Math.pow(weightedTerms.get(term).get(id).getTermFrequency(), 2);
                }
            }
            sqrt = Math.sqrt(sumPower); // get square root of the sum of terms squared
            norm.put(id, Double.valueOf(df.format(sqrt))); //add to variable, retain id of doc measured
        }
        //get dot product of query and terms > 0 frequency
        for (int id : docCollection) {
            double sumDot = 0;
            for (String term : query.keySet()) {
                if (Database.postings.get(term).containsKey(id)) {
                    sumDot += (query.get(term).getTermFrequency() * weightedTerms.get(term).get(id).getTermFrequency());
                }
            }
            dot.put(id, Double.valueOf(df.format(sumDot)));
        }

        //***********calculate similarity and add to dictionary***************
        for (int id : docCollection){
            sim.put(id, dot.get(id) / (norm.get(0) * norm.get(id)));
        }
        return sortByValues((HashMap) sim);
    }

    /**
     * This method calculates tf x idf weights for the terms
     * @param docCollection the collection of relevant documents
     * @return weighted terms
     */
    private Map<String, HashMap<Integer, Document>> calculateTFxIDF(Set<Integer> docCollection) {
        String domain = domainComboBox.getSelectedItem().toString(); //get domain
        Map<String, HashMap<Integer, Document>> weightedTerms = new TreeMap<String, HashMap<Integer, Document>>();
        double totalDocs = 0;

        //get total numbsr of documents in the collection depending on domain chosen by user
        if(domain.contains("not set...")){
            for (String d : Database.docsInDomain.keySet()){
                totalDocs += Database.docsInDomain.get(d).size();
            }
        }
        else {
            totalDocs = Database.docsInDomain.get(domain).size();
        }

        // perform tf x idf calculation
        for (int id : docCollection) { //for each document in vector
            for (String term : Database.postings.keySet()) {
                if (Database.postings.get(term).containsKey(id)) { //if document contains document

                    if(!weightedTerms.containsKey(term))
                        weightedTerms.put(term, new HashMap<Integer, Document>());

                    Document d = Database.postings.get(term).get(id);
                    double docFreq = 0;

                    //get document frequency based on domain
                    if (domain.contains("not set...")) {
                        for (String dom : Database.dictionary.get(term).keySet()) {
                            docFreq += Database.dictionary.get(term).get(dom).getDocFrequency();
                        }
                    } else {
                        if (Database.dictionary.get(term).containsKey(domain))
                            docFreq = Database.dictionary.get(term).get(domain).getDocFrequency();
                    }
                    weightedTerms.get(term).put(id, new Document(d.getDocName(), id));

                    //*********** calculate tf x idf ***********
                    weightedTerms.get(term).get(id).setTermFrequency(d.getTermFrequency() * (Math.log(totalDocs / docFreq) / Math.log(2)));

                    if(query.containsKey(term)){
                        query.get(term).setTermFrequency(query.get(term).getTermFrequency() * (Math.log(totalDocs / docFreq) / Math.log(2)));
                    }
                }
            }
        }
        return weightedTerms;
    }

    /**
     * This method stems and produces the query dictionary that contains term frequencies.
     * @param text user input (query)
     */
    private void produceQueryDictionary(String text) {

        String[] fieldsArray = text.split("[\\s\\.,]+"); //split query to a list
        query = new TreeMap<String, Query>(); //new instance of query

        SnowballStemmer stemmer = new englishStemmer();
        String current = "";

        for (String term : fieldsArray) {
            // check if is a stopword
            if (Database.stopWordsofwordnet.contains(term.trim())) {
                continue;
            }

            //stem word and add to dictionary
            stemmer.setCurrent(term.trim().replaceAll("[^a-zA-Z ]", ""));
            stemmer.stem();
            current = stemmer.getCurrent();
            if (!query.containsKey(current))
                query.put(current, new Query(term));

            query.get(current).setTermFrequency(query.get(current).getTermFrequency() + 1); //record frequency of word
        }
    }

    /**
     * This method sorts the similarity measures in descending order.
     * @param map sim
     * @return sorted sim
     */
    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here [highest to lowest]
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
}
