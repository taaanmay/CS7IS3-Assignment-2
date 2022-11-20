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

public class FtParser {

    private final static File FT_DIR = new File("Documents/ft");
    private static ArrayList<FTModel> ftDocsList = new ArrayList<>();
    private List<Document> ftDoc = new ArrayList<>();

    private static void parseFTFile(String file) throws IOException {
        System.out.println("Parsing ft documents...");
        String docNumber, docTitle, docContent, docAuthor, headlinestring, docDate;
        org.jsoup.nodes.Document doc = Jsoup.parse(file, "utf-8");
        Elements elements = doc.select("DOC");
        for(Element element : elements){
            FTModel ftdoc = new FTModel();

            String str = element.select("HEADLINE").text();
            String[] arrOfStr = str.split("/", 2);
            ftdoc.setDate(arrOfStr[0].split(" ", 2)[1]);
            ftdoc.setTitle(arrOfStr[1]);
            ftdoc.setAuthor(element.select("BYLINE").text());
            ftdoc.setDocNo(element.select("DOCNO").text());
            ftdoc.setContent(element.select("TEXT").text());

            ftDocsList.add(ftdoc);
        }

    }

    public List<Document> parseAllFTFiles(String path) throws IOException {

        File root = new File(path);
        File[] list = root.listFiles();

        if (list != null) {
            for (File file : list) {
                if (file.isDirectory()) {
                    parseFTFile(file.getAbsolutePath());
                } else {
                    if (!file.getName().equals("readmeft") && !file.getName().equals("readmefrcg")
                            && !file.getName().contains("Zone.Identifier")) {
                        parseFTFile(file.getAbsolutePath());
                    }
                }
            }
        }

        return addDocument(ftDocsList);
    }

    private List<Document> addDocument(List<FTModel> ftModels) {
        for (FTModel ftModel : ftModels) {
            Document document = new Document();
            document.add(new StringField("docNumber", ftModel.getDocNo(), Field.Store.YES));
            document.add(new TextField("docTitle", ftModel.getTitle(), Field.Store.YES));
            document.add(new TextField("docAuthor", ftModel.getAuthor(), Field.Store.YES));
            document.add(new TextField("docContent", ftModel.getContent(), Field.Store.YES));
            ftDoc.add(document);
        }

        return ftDoc;
    }

    public ArrayList<FTModel> getFtDocsList() throws IOException {
        parseAllFTFiles(FT_DIR.getAbsolutePath());
        return ftDocsList;
    }

    public static void main(String Args[]) throws IOException {
        FtParser ftParser = new FtParser();
        ftParser.parseAllFTFiles(FT_DIR.getAbsolutePath());
        System.out.println(ftDocsList.size());
    }

}