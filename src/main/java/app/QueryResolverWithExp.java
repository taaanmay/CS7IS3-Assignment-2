package app;

import app.model.childModel.TopicModel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThisQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
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
 * @author Jiaming Deng
 * @version 2.0
 * @date 2022/12/06 16:45
 */

public class QueryResolverWithExp {

    public void runQuery(Analyzer analyzer, Similarity similarity) throws Exception {
        System.out.println("Running Queries Now...");
        Directory dir = FSDirectory.open(Paths.get(Constant.INDEX_DIRECTORY));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Constant.searchFields, analyzer, getBoostMap());

        QueryBuilder queryBuilder = new QueryBuilder();
        List<TopicModel> queryObjects = queryBuilder.parseQuery(Constant.TOPICS);
        //System.out.println(queryObjects.size());
        List<String> results = new ArrayList<>();
        for (int i = 0; i < queryObjects.size(); i++) {
            TopicModel queryObject = queryObjects.get(i);
            List<String> narrative = parseTopicContent(queryObject.getNarrative(), queryObject);
            //String negativeQuery = narrative.get(1);
            String stringQuery = QueryParser.escape(queryObject.getTitle() + " " + queryObject.getDescription() + " " + narrative.get(0));
            if (queryObject.isRelevant()) {
                stringQuery = QueryParser.escape(queryObject.getTitle() + " " + queryObject.getDescription());
                //negativeQuery = QueryParser.escape(narrative.get(1));
            }

            if (stringQuery.contains(".")) {
                stringQuery = stringQuery.replaceAll("\\.", "");
            }
            Query queryContents = queryParser.parse(stringQuery);

            ScoreDoc[] scoreDocs = {};
            Query expandedQuery = expandQuery(searcher, analyzer, queryContents, scoreDocs, reader);

            scoreDocs = searcher.search(expandedQuery, Constant.MAX_CLAUSE).scoreDocs;
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
        map.put("docTitle", 0.1f);
//        map.put("docAuthor", 0.05f);
        map.put("docContent", 0.9f);

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

    private static List<String> parseTopicContent(String text, TopicModel topicModel){
        StringBuilder positiveResult = new StringBuilder();
        StringBuilder negativeResult = new StringBuilder();
//        String[] contentSplit = text.toLowerCase().split("\\.");
        String[] contentSplit = text.toLowerCase().split("[.;?]");
        List<String> result = new ArrayList();
        for (String content : contentSplit) {

            if (!content.contains("not relevant") && !content.contains("irrelevant")) {

                String re = content.replaceAll(
                        "a relevant document|a document will|to be relevant|relevant documents|a document must|relevant|will contain|will discuss|will provide|must cite",
                        "");
                positiveResult.append(re);

                topicModel.setRelevant(false);
            } else {
                String re = content.replaceAll("are also not relevant|are not relevant|are irrelevant|is not relevant",
                        "");
                negativeResult.append(re);

                topicModel.setRelevant(true);
            }
        }
        result.add(positiveResult.toString());
        result.add(negativeResult.toString());

        return result;
    }

    private static Query expandQuery(IndexSearcher searcher, Analyzer analyzer, Query queryContents, ScoreDoc[] hits,
                                     IndexReader reader) throws Exception {
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        queryBuilder.add(queryContents, BooleanClause.Occur.SHOULD);
        TopDocs topDocs = searcher.search(queryContents, 4);

        for (ScoreDoc score : topDocs.scoreDocs) {
            Document hitDoc = reader.document(score.doc);
            String fieldText = hitDoc.getField("docContent").stringValue();
            String[] moreLikeThisField = { "docContent" };
            MoreLikeThisQuery expandedQueryMoreLikeThis = new MoreLikeThisQuery(fieldText, moreLikeThisField, analyzer,
                    "docContent");
            Query expandedQuery = expandedQueryMoreLikeThis.rewrite(reader);
            queryBuilder.add(expandedQuery, BooleanClause.Occur.SHOULD);
        }
        return queryBuilder.build();
    }

    public static void main(String[] args) throws Exception {
        QueryResolver queryResolver = new QueryResolver();
        queryResolver.runQuery(new MyAnalyzer(), new BM25Similarity());
    }
}