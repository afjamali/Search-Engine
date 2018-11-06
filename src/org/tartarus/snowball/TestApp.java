
package org.tartarus.snowball;

import java.lang.reflect.Method;
import java.io.Reader;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;

public class TestApp {

    public static String[] stopWordsofwordnet = {
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
    };

    private static void usage() {
        System.err.println("Usage: TestApp <algorithm> <input file> [-o <output file>]");
    }

    public static void main(String[] args) throws Throwable {
        if (args.length < 2) {
            usage();
            return;
        }

        Class stemClass = Class.forName("org.tartarus.snowball.ext." +
                args[0] + "Stemmer");
        SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();

        Reader reader;
        reader = new InputStreamReader(new FileInputStream(args[1]));
        reader = new BufferedReader(reader);

        StringBuffer input = new StringBuffer();

        OutputStream outstream;

        if (args.length > 2) {
            if (args.length >= 4 && args[2].equals("-o")) {
                outstream = new FileOutputStream(args[3]);
            } else {
                usage();
                return;
            }
        } else {
            outstream = System.out;
        }
        Writer output = new OutputStreamWriter(outstream);
        output = new BufferedWriter(output);

        int repeat = 1;
        if (args.length > 4) {
            repeat = Integer.parseInt(args[4]);
        }

        Object[] emptyArgs = new Object[0];
        int lastWord = 0;
        int character;
        while ((character = reader.read()) != -1 || lastWord < 1) {
            char ch = (char) character;
            if (character == -1) {
                lastWord++;
                ch = ' ';
            }
            if (Character.isWhitespace(ch)) {
                if (input.length() > 0) {
                    boolean stopwrd = false;
                    for (int i = 0; i < stopWordsofwordnet.length; i++) {
                        if (stopWordsofwordnet[i].contains(input.toString())) {
                            stopwrd = true;
                            break;
                        }
                    }
                    if (!stopwrd) {
                        stemmer.setCurrent(input.toString().replaceAll("[^a-zA-Z ]", ""));
                        for (int j = repeat; j != 0; j--) {
                            stemmer.stem();
                        }
                        output.write(stemmer.getCurrent());
                        output.write('\n');
                    }
                    input.delete(0, input.length());
                }
            } else {
                input.append(Character.toLowerCase(ch));
            }
        }
        output.flush();
    }
}
