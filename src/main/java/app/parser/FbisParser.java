package app.parser;

/**
 * @author Siuyun Yip
 * @version 1.0
 * @date 2022/11/14 21:01
 */


import app.model.childModel.FbisModel;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FbisParser {

    private static List<String> tagList = new ArrayList(
            Arrays.asList("TI", "HT", "PHRASE", "DATE1", "ABS", "FIG"
                    , "F", "F P=100", "F P=101", "F P=102", "F P=103", "F P=104", "F P=105", "F P=106", "F P=107",
                    "TI", "H1", "H2", "H3", "H4", "H5", "H6", "H7", "H8", "TR", "TXT5", "HEADER", "TEXT", "AU"));

    /**
     * @param path fbis directory path
     * @return
     * @throws IOException
     */
    public List<Document> parseFbis(String path) throws IOException {
        System.out.println("Parsing FBI documents");
        File[] files = new File(path).listFiles();
        List<FbisModel> modelList = new ArrayList<>();
        for (File file : files) {
            org.jsoup.nodes.Document document = Jsoup.parse(file, "UTF-8", "");
            Elements elements = document.select("DOC");
            for (Element element : elements) {
                FbisModel fbisModel = new FbisModel();
                fbisModel.setDocNo(element.select("DOCNO").text());
                fbisModel.setContent(element.select("TEXT").text());
                String title = "";
                for (int i = 3; i <= 8; i ++ ) {
                    String cssQuery = "H" + i;
                    String hString = element.select(cssQuery).text();
                    if (!hString.isEmpty()) {
                        title += " " + hString;
                    }
                }
                fbisModel.setTitle(title);
                modelList.add(fbisModel);
            }
        }

        return addDocument(modelList);
    }

    private List<Document> addDocument(List<FbisModel> fbisModels) {
        List<Document> docList = new ArrayList<>();
        for (FbisModel fbisModel : fbisModels) {
            Document document = new Document();
            document.add(new StringField("docNumber", fbisModel.getDocNo(), Field.Store.YES));
            document.add(new TextField("docTitle", removeNonsense(fbisModel.getTitle()), Field.Store.YES));
            document.add(new TextField("docContent", removeNonsense(fbisModel.getContent()), Field.Store.YES));
            docList.add(document);
        }

        return docList;
    }

    private String removeNonsense(String data) {
        if(data.contains("\n")) {
            data = data.replaceAll("\n", " ").trim();
        }
        if (data.contains("[")) {
            data = data.replaceAll("\\[", "").trim();
        }
        if (data.contains("]")) {
            data = data.replaceAll("]", "").trim();
        }
        for (String tag : tagList) {
            if(data.contains(("<" + tag + ">").toLowerCase()))
                data = data.replaceAll("<" + tag.toLowerCase() + ">", "").trim();
            if(data.contains(("</" + tag + ">").toLowerCase()))
                data = data.replaceAll("</" + tag.toLowerCase() + ">", "").trim();
        }

        return data;

    }

    public static void main(String[] args) throws IOException {
        FbisParser parser = new FbisParser();
        List<Document> documents = parser.parseFbis("./Documents/fbis");
    }
}
