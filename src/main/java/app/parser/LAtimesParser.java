package app.parser;

import app.model.childModel.LatimesModel;
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

public class LAtimesParser {
    /**
     * @param path latimes directory path
     * @return
     * @throws IOException
     */
    public List<Document> parseLAtimes(String path) throws IOException {
        System.out.println("Parsing LA Times documents");
        File[] files = new File(path).listFiles();
        List<LatimesModel> modelList = new ArrayList<>();
        for (File file : files) {
            org.jsoup.nodes.Document document = Jsoup.parse(file, "UTF-8", "");
            Elements elements = document.select("DOC");

            for (Element element : elements) {
                LatimesModel latimesModel = new LatimesModel();
                latimesModel.setDocNo(element.select("DOCNO").text());
                latimesModel.setContent(element.select("TEXT").text());
                latimesModel.setHeadline(element.select("HEADLINE").text());

                modelList.add(latimesModel);
            }
        }

        return addDocument(modelList);
    }

    private List<Document> addDocument(List<LatimesModel> latimesModels) {
        List<Document> docList = new ArrayList<>();
        for (LatimesModel latimesModel : latimesModels) {
            Document document = new Document();
            document.add(new StringField("docNumber", latimesModel.getDocNo(), Field.Store.YES));
            document.add(new TextField("docContent", latimesModel.getContent(), Field.Store.YES));
            document.add(new TextField("docHeadline", latimesModel.getHeadline(), Field.Store.YES));
            docList.add(document);
        }

        return docList;
    }

    public static void main(String[] args) throws IOException {
        LAtimesParser parser = new LAtimesParser();
        List<Document> documents = parser.parseLAtimes("./Documents/latimes");
        System.out.println();
    }
}
