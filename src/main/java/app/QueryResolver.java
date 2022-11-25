package app;

import app.model.childModel.TopicModel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
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
        List<TopicModel> queryObjects = queryBuilder.parseQuery(Constant.TOPICS);

        List<String> results = new ArrayList<>();
        for (TopicModel queryObject : queryObjects) {
            String qy = QueryParser.escape(queryObject.getQuery().trim());
            Query query = queryParser.parse(qy);
            ScoreDoc[] scoreDocs = searcher.search(query, Constant.MAX_CLAUSE).scoreDocs;
            for (int j = 0; j < scoreDocs.length; j ++ ) {
                ScoreDoc hit = scoreDocs[j];
                // query-id Q0 document-id rank score STANDARD
                int rank = j + 1;
                results.add(queryObject.getTopicNum() + " Q0 " + searcher.doc(hit.doc).get("docNumber") + " " + rank + " " + hit.score + " EnglishAnalyzerBM25");
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
        System.out.println("finish writing");
    }

    public static void main(String[] args) throws Exception {
        QueryResolver queryResolver = new QueryResolver();
        queryResolver.runQuery(new MyAnalyzer(), new BM25Similarity());
    }
}
