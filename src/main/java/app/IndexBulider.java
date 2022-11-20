package app;

import app.parser.FbisParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static app.Constant.FBI_DIR;
import static app.Constant.INDEX_DIRECTORY;

public class IndexBulider {
    public void CreateIndex(List<Document> documentList, Analyzer analyzer, Similarity similarity) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        indexWriterConfig.setSimilarity(similarity);
        IndexWriter indexWriter = new IndexWriter(dir, indexWriterConfig);
        for(Document document : documentList) {
            try{
                indexWriter.addDocument(document);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Index done....");
        indexWriter.close();
        dir.close();
    }

    public static void main(String[] args) throws IOException {
        FbisParser fbisParser = new FbisParser();
        List<Document> documentList = fbisParser.parseFbis(FBI_DIR);
        IndexBulider indexBulider = new IndexBulider();
        indexBulider.CreateIndex(documentList, new StandardAnalyzer(), new BM25Similarity());
    }
}
