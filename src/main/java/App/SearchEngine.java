package App;

// Imports
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import App.Parser.FbisParser;
import App.Parser.LAtimesParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.DFISimilarity;
import org.apache.lucene.search.similarities.IndependenceStandardized;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.core.StopFilter;

public class SearchEngine {


    private ScoringAlgorithm selectedAlgorithm;
    // Define Directories
    private static String INDEX_DIRECTORY = "index";
    private String RESULTS_FILE = "results/out-";
    private static String RESULTS_DIR = "results/";
    private static String QUERY_FILE = "cran/cran.qry";

    private static String FBI_DIR = "Documents/fbis";
    private static String LATIMES_DIR = "Documents/latimes";

    private static String FT_DIR = "Documents/ft";

    private Analyzer analyzer;
    private Directory directory;
    private DirectoryReader ireader;
    private IndexSearcher isearcher;

    private static int MAX_RESULTS = 30;

    public FbisParser fbisParser;
    public LAtimesParser lAtimesParser;


    public enum ScoringAlgorithm { BM25, Classic, Boolean, LMDirichlet, DFISimilarity}



    public SearchEngine(ScoringAlgorithm algorithm){
        this.analyzer = new EnglishAnalyzer();
        this.selectedAlgorithm = algorithm;
        this.lAtimesParser = new LAtimesParser();
//        this.fbisParser = new FbisParser();
        try {
            this.directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void buildIndex() throws IOException
    {

        // 1. Create fields using FieldType and other methods.
        // 2. Create and configure an index writer - 'iwriter'
        // 3. Call a method called populateIndex() to build index using corpus, index writer and field types
        // 4. When Index is built, create a searcher using the algorithm selected by the user - BM25, Classic, Boolean, LMDirichlet, DFISimilarity
        // 5. Return to App.java

        // Create a new field type which will store term vector information
        FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setTokenized(true); //done as default
        ft.setStoreTermVectors(true);
        ft.setStoreTermVectorPositions(true);
        ft.setStoreTermVectorOffsets(true);
        ft.setStoreTermVectorPayloads(true);

        // create and configure an index writer
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);

        List<Document> documents = new ArrayList<Document>();

        // Parse FBI Documents
        documents = lAtimesParser.parseLAtimes(LATIMES_DIR);
//        documents = fbisParser.parseFbis(FBI_DIR);
        // Print the second fbi document
        System.out.println("Document: \n"+documents.get(1) + "\n");

        if(documents.size() != 0){

            iwriter.addDocuments(documents); // Add FBI documents to Index Writer
        }else{
            System.out.println("No documents found to add");
        }

        System.out.println("Built Index");
        //close the writer
        iwriter.close();

        // Call to create searcher (Function)
        try {
            ireader = DirectoryReader.open(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // New Index Searcher
        isearcher = new IndexSearcher(ireader);
        switch (selectedAlgorithm) {
            case BM25:
                isearcher.setSimilarity(new BM25Similarity());
                break;
            case Classic:
                isearcher.setSimilarity(new ClassicSimilarity());
                break;
            case DFISimilarity:
                isearcher.setSimilarity(new DFISimilarity(new IndependenceStandardized()));
                break;
            case LMDirichlet:
                isearcher.setSimilarity(new LMDirichletSimilarity());
            case Boolean:
                isearcher.setSimilarity(new BooleanSimilarity());

        }

    }

//    public void parseFBI(IndexWriter iwriter, FieldType ft){
//        ArrayList<Document> documents = new ArrayList<Document>();
//
//        String document = "";
//
//        try {
//            Path corpusPath = Paths.get("Documents/fbis/fb396001");
//            document = new String(Files.readAllBytes(corpusPath));
//            System.out.println(document);
//
//        } catch (IOException e) {
//            System.out.println("Error with the corpus splitting");
//            throw new RuntimeException(e);
//        }
//    }

    Document processFBIDocuments(String item, FieldType fieldType) throws IOException {
        // 1. Called by parseFBI() method
        // 2. The query is received as an argument along with the field type
        // 3. The query is split using regex on .T,.A,.W,.B and stored in a String array called fields
        // 4. Each field is then added as a String Field using the in-built StringField() method to create documents
        // 5. Document is then returned which will be added to the index writer later.

        Document returnResult = new Document();

        String[] fields = item.split(".[TAWB](\r\n|[\r\n])", -1);
        returnResult.add(new StringField("index", fields[0].trim(), Field.Store.YES));
        returnResult.add(new StringField("filename", fields[1].trim(), Field.Store.YES));
        returnResult.add(new StringField("author(s)", fields[2].trim(), Field.Store.YES));
        returnResult.add(new StringField("metadata", fields[3].trim(), Field.Store.YES));
        returnResult.add(new Field("content", fields[4].trim(), fieldType));

        return returnResult;
    }





    public void runQueries()  throws IOException, ParseException {
        // 1. Called by App.java class
        // 2. Create/Select the Results directory for storing results.
        // 3. Read Query file and split the queries using regex on .I
        // 4. Iterate through each query and start by trimming the extra spaces
        // 5. Then using regex on .W, the query text is extracted.
        // 6. Call to buildQuery method with the query text sent as an argument
        // 7. Top 30 documents are returned with their scores
        // 8. Every documents are iterated and written out to a Results file following the same format as cranqrel
        // 9. Program Ends


        // Create Results directory
        File output = new File(RESULTS_DIR);
        if (!output.exists()) {
            output.mkdir();
        }
        PrintWriter writer = new PrintWriter(RESULTS_FILE+selectedAlgorithm+".txt", "UTF-8");


//        String queryFile = new String(Files.readAllBytes(Paths.get(QUERY_FILE)));
//        String[] queries = queryFile.split(".I (?=\\d+[\n\r]+)");
//
//        int counter = 0;
//
//        for (int index=0; index< queries.length ; index++) {
//
//            // Trim the queries
//            queries[index] = queries[index].trim();
//
//            if (queries[index].length() > 0) {
//
//                String[] question = queries[index].split(".W(\r\n|[\r\n])");
//
//                ScoreDoc[] hits = buildQuery(question[1], false); // Call to build query
//                if (hits.length == 0)
//                    System.out.println("fail");
//                for (int i = 0; i < hits.length; i++) {
//                    Document hitDoc = isearcher.doc(hits[i].doc);
//
//                    // Write in Results files
//                    writer.println(counter + " 0 " + hitDoc.get("index") + " " + (i + 1) + " "
//                            + hits[i].score
//                            + " STANDARD");
//                }
//
//            }
//            counter++;
//        }
        writer.close();

    }

    public void shutdown() throws IOException {
        directory.close();
    }



}
