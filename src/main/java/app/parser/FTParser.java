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
import java.util.List;

import app.model.childModel.FTModel;

public class FTParser {

//    private final static File FT_DIR = new File("Documents/ft");
    private static ArrayList<FTModel> ftDocsList = new ArrayList<>();
    private List<Document> ftDoc = new ArrayList<>();

    private List<Document> doclist;

    public List<Document> parseFTDocs(String path) throws IOException {
        doclist = new ArrayList<>();
        System.out.println("Parsing FT documents...");
        File[] directories = new File(path).listFiles(File::isDirectory);

        for(File directory : directories){
            File[] files = directory.listFiles();
            for(File file : files) {
                org.jsoup.nodes.Document doc = Jsoup.parse(file, null, "");
                Elements elements = doc.select("DOC");
                for(Element element : elements){
                    FTModel ftdoc = new FTModel();

                    String headlineText = element.select("HEADLINE").text();
//                    String[] arrOfStr = str.split("/", 2);
//                    ftdoc.setDate(arrOfStr[0].split(" ", 2)[1]);
                    ftdoc.setTitle(headlineText);
                    ftdoc.setAuthor(element.select("BYLINE").text());
                    ftdoc.setDocNo(element.select("DOCNO").text());
                    ftdoc.setContent(element.select("TEXT").text());

                    ftDocsList.add(ftdoc);
                }
            }
        }
        System.out.println("Parsing FT done...");
        return returnParsedDocuments();
    }

    public List<Document> returnParsedDocuments() throws IOException{

        for (FTModel ftModel : ftDocsList) {
            Document document = new Document();
            document.add(new StringField("docNumber", ftModel.getDocNo(), Field.Store.YES));
            document.add(new TextField("docTitle", ftModel.getTitle(), Field.Store.YES));
            document.add(new TextField("docAuthor", ftModel.getAuthor(), Field.Store.YES));
            document.add(new TextField("docContent", ftModel.getContent(), Field.Store.YES));
            ftDoc.add(document);
        }

        return ftDoc;
    }
}