package app.parser;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Fr94Parser {
    private ArrayList<Document> doclist;
    public ArrayList<Document> parseFR94(String path) throws IOException {
        doclist = new ArrayList<>();
        System.out.println("Parsing fr94 documents...");
        File[] directories = new File(path).listFiles(File::isDirectory);
        String docNumber, docTitle, docContent;
        for(File directory : directories){
            File[] files = directory.listFiles();
            for(File file : files) {
                org.jsoup.nodes.Document doc = Jsoup.parse(file, null, "");
                Elements elements = doc.select("DOC");
                for(Element element : elements){
                    // ignore child elements
                    // these elements are not relevant to valuable information
                    element.select("ADDRESS").remove();
                    element.select("SIGNER").remove();
                    element.select("SIGNJOB").remove();
                    element.select("BILLING").remove();
                    element.select("FRFILING").remove();
                    element.select("DATE").remove();
                    element.select("RINDOCK").remove();
                    docNumber = element.select("DOCNO").text();
                    docTitle = element.select("DOCTITLE").text();
                    docContent = element.select("TEXT").text();
                    Document document = new Document();
                    System.out.println(docNumber);

                    document.add(new StringField("docNumber", docNumber, Field.Store.YES));
                    document.add(new TextField("docTitle", docTitle, Field.Store.YES));
                    document.add(new TextField("docContent", docContent, Field.Store.YES));
                    doclist.add(document);
                }
            }
        }
        System.out.println("Parsing FR94 done...");
        return doclist;
    }

}