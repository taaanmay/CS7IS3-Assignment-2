package app;

// Imports
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import app.parser.FbisParser;
import app.parser.Fr94Parser;
import app.parser.FtParser;
import app.parser.LAtimesParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import static app.Constant.*;

public class SearchEngine {


    private ScoringAlgorithm selectedAlgorithm;
    // Define Directories
    private static String QUERY_FILE = "cran/cran.qry";

    private Analyzer analyzer;
    private Similarity similarity;
    private Directory directory;
    private DirectoryReader ireader;
    private IndexSearcher isearcher;

    private static int MAX_RESULTS = 30;

    private FbisParser fbisParser;
    private LAtimesParser lAtimesParser;


    public enum ScoringAlgorithm { BM25, Classic, Boolean, LMDirichlet, DFISimilarity}



    public SearchEngine(ScoringAlgorithm algorithm){
        this.analyzer = new EnglishAnalyzer();
        this.selectedAlgorithm = algorithm;
        this.lAtimesParser = new LAtimesParser();
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
        FtParser ftParser = new FtParser();
        LAtimesParser lAtimesParser = new LAtimesParser();

        // Parse Documents
        List<Document> documentsFBI = fbisParser.parseFbis(FBI_DIR);
        List<Document> documentsFR94 = fr94Parser.parseFR94(FR94_DIR);
        //ftParser.parseAllFTFiles(FT_DIR.getAbsolutePath());
        //List<Document> documentsFT = ftParser.getFtDocsList();
        List<Document> documentsLAtimes = lAtimesParser.parseLAtimes(LATIMES_DIR);
        indexBulider.CreateIndex(documentsFBI, analyzer, similarity);
        indexBulider.CreateIndex(documentsFR94, analyzer, similarity);
        indexBulider.CreateIndex(documentsLAtimes, analyzer, similarity);
        //indexBulider.CreateIndex(documentsFT, analyzer, similarity);
    }


    public void shutdown() throws IOException {
        directory.close();
    }



}
