package app;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Siuyun Yip
 * @version 1.0
 * @date 2022/11/20 17:42
 */
public class QueryResolver {

    public void runQuery(Analyzer analyzer, Similarity similarity) throws Exception {
        System.out.println("Running Queries Now...");
        Directory dir = FSDirectory.open(Paths.get(Constant.INDEX_DIRECTORY));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Constant.searchFields, analyzer, getBoostMap());

        QueryBuilder queryBuilder = new QueryBuilder();
        List<String> queries = queryBuilder.parseQuery(Constant.TOPICS);

        List<String> results = new ArrayList<>();
        for (int i = 0; i < queries.size(); i ++ ) {
            String qy = QueryParser.escape(queries.get(i).trim());
            Query query = queryParser.parse(qy);
            ScoreDoc[] scoreDocs = searcher.search(query, Constant.MAX_CLAUSE).scoreDocs;
            for (int j = 0; j < scoreDocs.length; j ++ ) {
                Document doc = searcher.doc(scoreDocs[j].doc);
                // query-id Q0 document-id rank score STANDARD
                int rank = j + 1;
                if(!results.contains(i + 401 + " Q0 " + doc.get("docNumber") + " " + rank + " " + scoreDocs[j].score + " EnglishAnalyzerBM25")){
                    results.add(i + 401 + " Q0 " + doc.get("docNumber") + " " + rank + " " + scoreDocs[j].score + " EnglishAnalyzerBM25");
                }

            }
        }

        dir.close();
        reader.close();
        writeRank2File(results);
    }

    private Map<String, Float> getBoostMap() {
        Map<String, Float> map = new HashMap<>();
        map.put("docTitle", 0.3f);
        map.put("docAuthor", 0.05f);
        map.put("docContent", 0.7f);

        return map;
    }

    private static void writeRank2File(List<String> results) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(Constant.RESULTS_DIR));
        for (String res : results) {
            bw.write(res);
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }
}
