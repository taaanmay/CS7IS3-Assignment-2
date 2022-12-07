package app;

// Imports

import app.parser.FbisParser;
import app.parser.Fr94Parser;
import app.parser.FTParser;
import app.parser.LAtimesParser;
import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import static app.Constant.*;

public class SearchEngine {


    private ScoringAlgorithm selectedAlgorithm;
    // Define Directories
    private static String QUERY_FILE = "cran/cran.qry";

    private Analyzer analyzer;
    private Similarity similarity;
    private Directory directory;

    private QueryResolver queryResolver;

    private QueryResolverWithExp queryResolverWithExp;


    public enum ScoringAlgorithm { BM25, Classic, Boolean, LMDirichlet, DFISimilarity}



    public SearchEngine(ScoringAlgorithm algorithm){
        this.analyzer = new MyAnalyzer();
        this.selectedAlgorithm = algorithm;
        this.queryResolver = new QueryResolver();
        this.queryResolverWithExp = new QueryResolverWithExp();

        if(selectedAlgorithm == ScoringAlgorithm.BM25){
            this.similarity = new BM25Similarity();
        } else if(selectedAlgorithm == ScoringAlgorithm.Classic){
            this.similarity = new ClassicSimilarity();
        } else if(selectedAlgorithm == ScoringAlgorithm.Boolean) {
            this.similarity = new BooleanSimilarity();
        } else if(selectedAlgorithm == ScoringAlgorithm.DFISimilarity) {
            //similarity = new DFISimilarity();
        } else if(selectedAlgorithm == ScoringAlgorithm.LMDirichlet) {
            this.similarity = new LMDirichletSimilarity();
        }
//        this.fbisParser = new FbisParser();
        try {
            this.directory = FSDirectory.open(Paths.get(Constant.INDEX_DIRECTORY));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void buildIndex() throws IOException{
        IndexBulider indexBulider = new IndexBulider();
        // Instantiate Parsers
        FbisParser fbisParser = new FbisParser();
        Fr94Parser fr94Parser = new Fr94Parser();
        FTParser ftParser = new FTParser();
        LAtimesParser lAtimesParser = new LAtimesParser();

        List<Document> parsedDocument = ftParser.parseFTDocs(FT_DIR);
        indexBulider.CreateIndex(parsedDocument, analyzer, similarity);

        parsedDocument = fbisParser.parseFbis(FBI_DIR);
        indexBulider.CreateIndex(parsedDocument, analyzer, similarity);

        parsedDocument = fr94Parser.parseFR94(FR94_DIR);
        indexBulider.CreateIndex(parsedDocument, analyzer, similarity);

        parsedDocument = lAtimesParser.parseLAtimes(LATIMES_DIR);
        indexBulider.CreateIndex(parsedDocument, analyzer, similarity);
    }

    public void runQueries() throws Exception {
        System.out.println("Running Queries");
        System.out.println(
                "Select query method:\n"
                        + "1)\tnormal query\n"
                        + "2)\tquery with expansion\n");
        Scanner scanner = new Scanner(System.in);
        String userResponse = scanner.nextLine();
        switch (userResponse) {
            case "1":
                queryResolver.runQuery(new MyAnalyzer(), similarity);
                break;
            case "2":
                queryResolverWithExp.runQuery(new MyAnalyzer(), similarity);
                break;
            default:
                break;
        }
    }

    public void shutdown() throws IOException {
        directory.close();
    }
}