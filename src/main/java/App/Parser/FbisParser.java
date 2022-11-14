package App.Parser;

/**
 * @author Siuyun Yip
 * @version 1.0
 * @date 2022/11/14 21:01
 */


import App.Model.ChildModel.FbisModel;
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

public class FbisParser {

    /**
     * @param path fbis directory path
     * @return
     * @throws IOException
     */
    public List<Document> parseFbis(String path) throws IOException {
        File[] files = new File(path).listFiles();
        List<FbisModel> modelList = new ArrayList<>();
        for (File file : files) {
            org.jsoup.nodes.Document document = Jsoup.parse(file, "UTF-8", "");
            Elements elements = document.select("DOC");
            for (Element element : elements) {
                FbisModel fbisModel = new FbisModel();
                fbisModel.setDocNo(element.select("DOCNO").text());
                fbisModel.setContent(element.select("TEXT").text());
                fbisModel.setFig(element.select("F").text());
                String title = "";
                for (int i = 3; i <= 8; i ++ ) {
                    String cssQuery = "H" + i;
                    String hString = element.select(cssQuery).text();
                    if (!hString.isEmpty()) {
                        title += " " + hString;
                    }
                }
                fbisModel.setTitle(title);
                fbisModel.setTxt5(element.select("TXT5").text());
                fbisModel.setFig(element.select("FIG").text());
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
            document.add(new TextField("docTitle", fbisModel.getTitle(), Field.Store.YES));
            document.add(new TextField("docContent", fbisModel.getContent(), Field.Store.YES));
            document.add(new TextField("docContentExt", fbisModel.getTxt5(), Field.Store.YES));
            document.add(new TextField("docFigure", fbisModel.getFig(), Field.Store.YES));
            docList.add(document);
        }

        return docList;
    }

    public static void main(String[] args) throws IOException {
        FbisParser parser = new FbisParser();
        List<Document> documents = parser.parseFbis("./Documents/fbis");
    }
}
