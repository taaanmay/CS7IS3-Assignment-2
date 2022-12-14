package app;

/**
 * @author Siuyun Yip
 * @version 1.0
 * @date 2022/11/18 11:57
 */
public interface Constant {
    String INDEX_DIRECTORY = "index";

    String RESULTS_FILE = "results/out-";

    String RESULTS_DIR = "results/results.txt";

    String FBI_DIR = "Documents/fbis";
    
    String FR94_DIR = "Documents/fr94";
    
    String LATIMES_DIR = "Documents/latimes";

    String FT_DIR = "Documents/ft";

    String STOPWORD_FILE = "stopwords.txt";

    String TOPICS = "topics";

//    String[] searchFields = {"docTitle", "docAuthor", "docContent"};
    String[] searchFields = {"docTitle", "docContent"};

    Integer MAX_CLAUSE = 1000;

}
