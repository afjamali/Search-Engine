import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * This class creates/loads inverted index and saves index into a .csv file (uses stopwords and stemming algorithm)
 */
public class Database {

    static Map<String, HashMap<String, Term>> dictionary = new HashMap<String, HashMap<String, Term>>(); //additional info for each term, contains doc frequency and total occurrence across all docs
    static Map<String, HashMap<Integer, Document>> postings = new TreeMap<String, HashMap<Integer, Document>>(); //contains postings. For each term, contains key: doc id followed by value: posting
    static Map<String, List<String>> docsInDomain = new HashMap<String, List<String>>(); //total number of files in each directory
    public static List<String> stopWordsofwordnet = Arrays.asList( // a list of stop words
            "without", "see", "unless", "due", "also", "must", "might", "like", "]", "[", "}", "{", "<", ">", "?", "\"", "\\", "/", ")", "(", "will", "may", "can", "much", "every",
            "the", "in", "other", "this", "the", "many", "any", "an", "or", "for", "in", "an", "an ", "is", "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren’t",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can’t", "cannot", "could",
            "couldn’t", "did", "didn’t", "do", "does", "doesn’t", "doing", "don’t", "down", "during", "each", "few", "for", "from", "further", "had", "hadn’t", "has", "hasn’t", "have", "haven’t", "having",
            "he", "he’d", "he’ll", "he’s", "her", "here", "here’s", "hers", "herself", "him", "himself", "his", "how", "how’s", "i ", " i", "i’d", "i’ll", "i’m", "i’ve", "if", "in", "into", "is",
            "isn’t", "it", "it’s", "its", "itself", "let’s", "me", "more", "most", "mustn’t", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "ought", "our", "ours", "ourselves",
            "out", "over", "own", "same", "shan’t", "she", "she’d", "she’ll", "she’s", "should", "shouldn’t", "so", "some", "such", "than",
            "that", "that’s", "their", "theirs", "them", "themselves", "then", "there", "there’s", "these", "they", "they’d", "they’ll", "they’re", "they’ve",
            "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn’t", "we", "we’d", "we’ll", "we’re", "we’ve",
            "were", "weren’t", "what", "what’s", "when", "when’s", "where", "where’s", "which", "while", "who", "who’s", "whom",
            "why", "why’s", "with", "won’t", "would", "wouldn’t", "you", "you’d", "you’ll", "you’re", "you’ve", "your", "yours", "yourself", "yourselves",
            "Without", "See", "Unless", "Due", "Also", "Must", "Might", "Like", "Will", "May", "Can", "Much", "Every", "The", "In", "Other", "This", "The", "Many", "Any", "An", "Or", "For",
            "In", "An", "An ", "Is", "A", "About", "Above", "After", "Again", "Against", "All", "Am", "An", "And", "Any", "Are", "Aren’t", "As", "At", "Be", "Because", "Been", "Before", "Being",
            "Below", "Between", "Both", "But", "By", "Can’t", "Cannot", "Could", "Couldn’t", "Did", "Didn’t", "Do", "Does", "Doesn’t", "Doing", "Don’t", "Down", "During", "Each", "Few", "For", "From",
            "Further", "Had", "Hadn’t", "Has", "Hasn’t", "Have", "Haven’t", "Having",
            "He", "He’d", "He’ll", "He’s", "Her", "Here", "Here’s", "Hers", "Herself", "Him", "Himself", "His", "How", "How’s", "I ", " I", "I’d", "I’ll", "I’m", "I’ve", "If", "In", "Into", "Is",
            "Isn’t", "It", "It’s", "Its", "Itself", "Let’s", "Me", "More", "Most", "Mustn’t", "My", "Myself", "No", "Nor", "Not", "Of", "Off", "On", "Once", "Only", "Ought", "Our", "Ours", "Ourselves",
            "Out", "Over", "Own", "Same", "Shan’t", "She", "She’d", "She’ll", "She’s", "Should", "Shouldn’t", "So", "Some", "Such", "Than",
            "That", "That’s", "Their", "Theirs", "Them", "Themselves", "Then", "There", "There’s", "These", "They", "They’d", "They’ll", "They’re", "They’ve",
            "This", "Those", "Through", "To", "Too", "Under", "Until", "Up", "Very", "Was", "Wasn’t", "We", "We’d", "We’ll", "We’re", "We’ve",
            "Were", "Weren’t", "What", "What’s", "When", "When’s", "Where", "Where’s", "Which", "While", "Who", "Who’s", "Whom",
            "Why", "Why’s", "With", "Won’t", "Would", "Wouldn’t", "You", "You’d", "You’ll", "You’re", "You’ve", "Your", "Yours", "Yourself", "Yourselves"
    );

    /**
     * If the index.csv file is empty, it will initialize a new inverted index, otherwise
     * it will load stored index.
     */
    public  static void run(){
        File index = new File("index.csv");
        try {
            List<String> allLines = Files.readAllLines(Paths.get(index.toString()), Charset.defaultCharset());

            if(allLines.size() <= 1 || allLines.get(1).isEmpty()){
                initializeIndex();
            }
            else {
                loadIndex(allLines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method initializes the index using the stopwords list and the snowball stemmer.
     * The method searches the directory and for each term does the stemming and calls the addToDictionary method.
     * upon completion, stores the index to the index.csv file for subsequent sessions.
     * @throws IOException file not found
     */
    private static void initializeIndex() throws IOException {

        SnowballStemmer stemmer = new englishStemmer();
        File folder = new File("docs");
        File[] domains = folder.listFiles(); //collect all the directories
        Reader reader;
        int id = 1; //initial document id

        for (File d : domains) { //for each directory
            File[] files = d.listFiles();
            for (File f : files) { //for each file in directory
                String path = f.toString();
                String fileName = path.substring(path.indexOf("\\") + 1, path.indexOf("."));
                reader = new InputStreamReader(new FileInputStream(f));
                reader = new BufferedReader(reader);

                StringBuffer input = new StringBuffer(); //each character is appended to input until it makes up a word

                int repeat = 1; //stem once?
//            if (args.length > 4) {
//                repeat = Integer.parseInt(args[4]);
//            }

                Object[] emptyArgs = new Object[0];
                int lastWord = 0;
                int character;
                while ((character = reader.read()) != -1 || lastWord < 1) { //read in each integer
                    char ch = (char) character; //get character form integer
                    if (character == -1) { //added new code: if last character in file, read last word (default code skipped last word)
                        lastWord++;
                        ch = ' ';
                    }
                    if (Character.isWhitespace(ch)) {
                        if (input.length() > 0) {
                            boolean stopwrd = false;
                            if (stopWordsofwordnet.contains(input.toString())) { //check if it's a stopword
                                stopwrd = true;
                            }
                            if (!stopwrd) {
                                stemmer.setCurrent(input.toString().replaceAll("[^a-zA-Z ]", "")); //set stem word
                                for (int j = repeat; j != 0; j--) {
                                    stemmer.stem(); //stem word
                                }
                                String current = stemmer.getCurrent();
                                if (!current.equals("")){
                                    addToDictionary(current, fileName, id); //add word to index
                                }
                            }
                            input.delete(0, input.length()); //delete temp word, and go to next iteration
                        }
                    } else {
                        input.append(Character.toLowerCase(ch)); //append character
                    }
                }
                id++; //add 1 to doc id for nex file
            }
        }

        saveIndex(); //save index to index.csv

    }

    /**
     * Saves index to index.csv for subsequent sessions.
     * @throws FileNotFoundException
     */
    private static void saveIndex() throws FileNotFoundException {

        // Output stats
        PrintWriter writer = new PrintWriter("index.csv");
        writer.println("Term" + ", " + "Document Frequency" + ", " + "# of Occurrences" + ", " + "Document ID" + ", " + "Term Frequency" +
        ", " + "File Name");
        for(String w : postings.keySet()) {
            for (int x : postings.get(w).keySet()) {
                String domain = postings.get(w).get(x).getDocName().substring(0, postings.get(w).get(x).getDocName().indexOf("\\"));
                writer.print(w);
                writer.print(", " + dictionary.get(w).get(domain).getDocFrequency());
                writer.print(", " + dictionary.get(w).get(domain).getOccurances());
                writer.print(", " + x);
                writer.print(", " + postings.get(w).get(x).getTermFrequency());
                writer.print(", " + postings.get(w).get(x).getDocName());
                writer.println();
            }
        }
        writer.close();
    }

    /**
     * This method adds the current word to the dictionary, including term frequencies and doc frequencies.
     *
     * In addition it adds each domain (directory) to the docsInDomain variable. this is used
     * for tf x idf calculation where you would need total number of docs.
     *
     * Two dictionaries are used, dictionary and postings. dictionary contains additional information. Postings
     * contains the inverted index.     *
     * @param current the current term after stemming
     * @param fileName the domain followed by the file name (e.g. animal\doc1.txt)
     * @param id the document id
     * @see Document
     * @see Term
     */
    private static void addToDictionary(String current, String fileName, int id) {

        String domain = fileName.substring(0, fileName.indexOf("\\")); //extract domain

        if (!docsInDomain.containsKey(domain)){
            docsInDomain.put(domain, new ArrayList<String>());
        }
        if (!docsInDomain.get(domain).contains(fileName)){
            docsInDomain.get(domain).add(fileName);
        }

        if (!postings.containsKey(current)) {
            postings.put(current, new HashMap<Integer, Document>());
            dictionary.put(current, new HashMap<String, Term>());
        }
        if(!dictionary.get(current).containsKey(domain)){
            dictionary.get(current).put(domain, new Term());
        }
        if (!postings.get(current).containsKey(id)) {
            postings.get(current).put(id, new Document(fileName, id));
            dictionary.get(current).get(domain).setDocFrequency(dictionary.get(current).get(domain).getDocFrequency() + 1);
        }
        postings.get(current).get(id).setTermFrequency(postings.get(current).get(id).getTermFrequency() + 1);
        dictionary.get(current).get(domain).setOccurances(dictionary.get(current).get(domain).getOccurances() + 1);
    }

    /**
     * This method loads the index from the index.csv file to the appropriate dictionaries.
     * @param allLines list of lines from index.csv
     * @throws IOException
     */
    private static void loadIndex(List<String> allLines) throws IOException {
        allLines.remove(0);
        for (String line : allLines){
            String[] fieldsArray = line.split("\\,");

            String term = fieldsArray[0].trim();
            int docFreq = Integer.parseInt(fieldsArray[1].trim());
            int numOccur = Integer.parseInt(fieldsArray[2].trim());
            int id = Integer.parseInt(fieldsArray[3].trim());
            double termFreq = Double.parseDouble(fieldsArray[4].trim());
            String fileName = fieldsArray[5].trim();
            String domain = fileName.substring(0, fileName.indexOf("\\"));

            if (!docsInDomain.containsKey(domain)){
                docsInDomain.put(domain, new ArrayList<String>());
            }
            if (!docsInDomain.get(domain).contains(fileName)){
                docsInDomain.get(domain).add(fileName);
            }

            if (!postings.containsKey(term)) {
                postings.put(term, new HashMap<Integer, Document>());
                dictionary.put(term, new HashMap<String, Term>());
            }
            if(!dictionary.get(term).containsKey(domain)){
                dictionary.get(term).put(domain, new Term());
            }
            if (!postings.get(term).containsKey(id)) {
                postings.get(term).put(id, new Document(fileName, id));
                dictionary.get(term).get(domain).setDocFrequency(docFreq);
            }
            postings.get(term).get(id).setTermFrequency(termFreq);
            dictionary.get(term).get(domain).setOccurances(numOccur);
        }
    }
}
