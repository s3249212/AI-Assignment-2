import java.io.*;
import java.util.*;

public class Bayespam
{
    static double epsilon = 1;
    static double alpha = 1;
    // This defines the two types of messages we have.
    static enum MessageType
    {
        NORMAL, SPAM
    }

    // This a class with two counters (for regular and for spam)
    static class Multiple_Counter
    {
        int counter_spam    = 0;
        int counter_regular = 0;

        // Increase one of the counters by one
        public void incrementCounter(MessageType type)
        {
            if ( type == MessageType.NORMAL ){
                ++counter_regular;
            } else {
                ++counter_spam;
            }
        }
    }

    // Listings of the two subdirectories (regular/ and spam/)
    private static File[] listing_regular = new File[0];
    private static File[] listing_spam = new File[0];

    // A hash table for the vocabulary (word searching is very fast in a hash table)
    private static Hashtable <String, Multiple_Counter> vocab = new Hashtable <String, Multiple_Counter> ();


    // Add a word to the vocabulary
    private static void addWord(String word, MessageType type)
    {
        Multiple_Counter counter = new Multiple_Counter();

        if ( vocab.containsKey(word) ){                  // if word exists already in the vocabulary..
            counter = vocab.get(word);                  // get the counter from the hashtable
        }
        counter.incrementCounter(type);                 // increase the counter appropriately

        vocab.put(word, counter);                       // put the word with its counter into the hashtable
    }

    private static String modified(final String input){
        int cnt=0;
        final StringBuilder builder = new StringBuilder();
        for(final char c : input.toCharArray())
            if(Character.isLetter(c)) {
                builder.append(Character.isLowerCase(c) ? c : Character.toLowerCase(c));
                cnt++;
            }
        if(cnt>=4)
            return builder.toString();
        else
            return null;
    }

    // List the regular and spam messages
    private static void listDirs(File dir_location)
    {
        // List all files in the directory passed
        File[] dir_listing = dir_location.listFiles();

        // Check that there are 2 subdirectories
        if ( dir_listing.length != 2 )
        {
            System.out.println( "- Error: specified directory does not contain two subdirectories.\n" );
            Runtime.getRuntime().exit(0);
        }

        listing_regular = dir_listing[0].listFiles();
        listing_spam    = dir_listing[1].listFiles();
    }


    // Print the current content of the vocabulary
    private static void printVocab()
    {
        Multiple_Counter counter = new Multiple_Counter();

        for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
        {
            String word;

            word = e.nextElement();
            counter  = vocab.get(word);

            System.out.println( word + " | in regular: " + counter.counter_regular +
                    " in spam: "    + counter.counter_spam);
        }
    }


    // Read the words from messages and add them to your vocabulary. The boolean type determines whether the messages are regular or not
    private static void readMessages(MessageType type)
            throws IOException
    {
        File[] messages = new File[0];

        if (type == MessageType.NORMAL){
            messages = listing_regular;
        } else {
            messages = listing_spam;
        }

        for (int i = 0; i < messages.length; ++i)
        {
            FileInputStream i_s = new FileInputStream( messages[i] );
            BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
            String line;
            String word;

            while ((line = in.readLine()) != null)                      // read a line
            {
                StringTokenizer st = new StringTokenizer(line);         // parse it into words

                while (st.hasMoreTokens())                  // while there are stille words left..
                {
                    addWord(st.nextToken(), type);                  // add them to the vocabulary
                }
            }

            in.close();
        }
    }

    private static void calculatePosterior(MessageType type, double a_priori_regular, double a_priori_spam, Hashtable<String, Double> cond_regular,
    Hashtable<String, Double> cond_spam)
            throws IOException
    {
        File[] messages = new File[0];
        if (type == MessageType.NORMAL){
            messages = listing_regular;
        } else {
            messages = listing_spam;
        }

        int n_normal = 0, n_spam = 0;

        for (int i = 0; i < messages.length; ++i)
        {
            FileInputStream i_s = new FileInputStream( messages[i] );
            BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
            String line;
            String word;

            double post_regular;
            double post_spam;
            post_regular = a_priori_regular;
            post_spam = a_priori_spam;
            //System.out.println(post_regular + ", " + post_spam);
            while ((line = in.readLine()) != null)                      // read a line
            {
                StringTokenizer st = new StringTokenizer(line);         // parse it into words
                while (st.hasMoreTokens())                  // while there are stille words left..
                {
                    //addWord(st.nextToken(), type);                  // add them to the vocabulary
                    //Do calculation here!
                    word = modified(st.nextToken());
                    //System.out.println("Word: " + word);
                    if(word != null && cond_regular.containsKey(word)) {
                        //cond_regular.get(word);
                        //System.out.println("Word2: " + cond_regular.get(word) + ", " + cond_spam.get(word));
                        post_regular += cond_regular.get(word);
                        post_spam += cond_spam.get(word);
                    }
                }

            }
            //System.out.println("Posts: " + post_regular + ", " + post_spam);
            MessageType classification = (post_regular > post_spam? MessageType.NORMAL: MessageType.SPAM);
            //System.out.println(classification);
            if(classification == MessageType.NORMAL) {
                n_normal++;
            } else {
                n_spam++;
            }
            in.close();
        }
        System.out.println("Classifications regular: " + n_normal + ", spam: " + n_spam);
    }

    public static void main(String[] args)
            throws IOException
    {
        // Location of the directory (the path) taken from the cmd line (first arg)
        File dir_location = new File( args[0] );

        // Check if the cmd line arg is a directory
        if ( !dir_location.isDirectory() )
        {
            System.out.println( "- Error: cmd line arg not a directory.\n" );
            Runtime.getRuntime().exit(0);
        }

        // Initialize the regular and spam lists
        listDirs(dir_location);

        // Read the e-mail messages
        readMessages(MessageType.NORMAL);
        readMessages(MessageType.SPAM);

        // Print out the hash table
        printVocab();

        int nRegular = listing_regular.length;
        int nSpam = listing_spam.length;
        int nTotal = nRegular + nSpam;
        System.out.println(nRegular);
        System.out.println(nSpam);
        double a_priori_regular = Math.log((double) nRegular / nTotal);
        double a_priori_spam = Math.log((double) nSpam / nTotal);
        System.out.println(a_priori_regular);
        System.out.println(a_priori_spam);


        Hashtable<String, Multiple_Counter> nvocab = new Hashtable<String, Multiple_Counter>();

        for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
        {
            String word;
            Multiple_Counter counter;

            word = e.nextElement();
            counter  = vocab.get(word);
            word = modified(word);


            //System.out.println( word + " | in regular: " + counter.counter_regular +
             //       " in spam: "    + counter.counter_spam);

            if(word != null) {
                Multiple_Counter counter2 = new Multiple_Counter();

                if (nvocab.containsKey(word)) {                  // if word exists already in the vocabulary..
                    counter2 = nvocab.get(word);                  // get the counter from the hashtable
                }
                counter2.counter_regular += counter.counter_regular;
                counter2.counter_spam += counter.counter_spam;

                nvocab.put(word, counter2);                       // put the word with its counter into the hashtable
            }
        }

        vocab = nvocab;
        printVocab();
        int nWordsRegular = 0;
        int nWordsSpam = 0;
        for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
        {
            String word;
            Multiple_Counter counter;
            word = e.nextElement();
            counter  = vocab.get(word);
            nWordsRegular += counter.counter_regular;
            nWordsSpam += counter.counter_spam;
        }

        Hashtable<String, Double> cond_regular = new Hashtable<String, Double>();
        Hashtable<String, Double> cond_spam = new Hashtable<String, Double>();

        for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
        {
            String word;
            Multiple_Counter counter;
            word = e.nextElement();
            counter  = vocab.get(word);
            System.out.println("conditionals " + word + " " + counter.counter_regular + ", " + counter.counter_spam);
            Double c_r = (double)counter.counter_regular / nWordsRegular;
            Double c_s = (double)counter.counter_spam / nWordsSpam;
            System.out.println("conditionals " + word + " " + c_r + ", " + c_s);
            c_r = c_r > 0? c_r: epsilon / (nWordsRegular + nWordsSpam);
            c_s = c_s > 0? c_s: epsilon / (nWordsRegular + nWordsSpam);
            c_r = Math.log(c_r);
            c_s = Math.log(c_s);
            cond_regular.put(word, c_r);
            cond_spam.put(word, c_s);
        }

        // Location of the directory (the path) taken from the cmd line (first arg)
        dir_location = new File( args[1] );

        // Check if the cmd line arg is a directory
        if ( !dir_location.isDirectory() )
        {
            System.out.println( "- Error: cmd line arg not a directory.\n" );
            Runtime.getRuntime().exit(0);
        }

        // Initialize the regular and spam lists
        listDirs(dir_location);
        System.out.println("\nRegular messages:");
        calculatePosterior(MessageType.NORMAL, a_priori_regular, a_priori_spam, cond_regular, cond_spam);
        System.out.println("\nSpam messages:");
        calculatePosterior(MessageType.SPAM, a_priori_regular, a_priori_spam, cond_regular, cond_spam);
        // Now all students must continue from here:
        //
        // 1) A priori class probabilities must be computed from the number of regular and spam messages
        // 2) The vocabulary must be clean: punctuation and digits must be removed, case insensitive
        // 3) Conditional probabilities must be computed for every word
        // 4) A priori probabilities must be computed for every word
        // 5) Zero probabilities must be replaced by a small estimated value
        // 6) Bayes rule must be applied on new messages, followed by argmax classification
        // 7) Errors must be computed on the test set (FAR = false accept rate (misses), FRR = false reject rate (false alarms))
        // 8) Improve the code and the performance (speed, accuracy)
        //
        // Use the same steps to create a class BigramBayespam which implements a classifier using a vocabulary consisting of bigrams
    }
}