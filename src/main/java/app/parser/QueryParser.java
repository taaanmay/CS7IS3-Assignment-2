package app.parser;

import app.Constant;
import app.model.childModel.TopicModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Siuyun Yip
 * @version 1.0
 * @date 2022/11/18 11:48
 */
public class QueryParser {

    public List<String> parseQuery(String queryPath) throws IOException {
        File file = new File(queryPath);
        org.jsoup.nodes.Document queryDoc = Jsoup.parse(file, "UTF-8", "");
        Elements topElements = queryDoc.select("top");
        List<TopicModel> modelList = new ArrayList<>();
        for (Element e : topElements) {
            TopicModel topicModel = new TopicModel();
//            String title = e.select("title").text();
            topicModel.setDescription(e.select("desc").text().toLowerCase());
            topicModel.setNarrative(e.select("narr").text().toLowerCase());
            modelList.add(topicModel);
        }

        return getQueryList(modelList);
    }

    private List<String> getQueryList(List<TopicModel> modelList) {
        List<String> qryList = new ArrayList<>();
        for (TopicModel topicModel : modelList) {
            qryList.add(analyseQuery(topicModel.toString()));
        }

        return qryList;
    }

    private String analyseQuery(String qry) {
        String newQry = removeStopword(qry);

        return newQry;
    }

    private String removeStopword(String qry) {
        StringBuilder newQry = new StringBuilder();

        List<String> stopwordList = Arrays.asList(readFile(Constant.STOPWORD_FILE).toLowerCase().split("\n"));
        String[] sentences = qry.split("\\.");
        for (int i = 0; i < sentences.length; i ++ ) {
            String[] words = sentences[i].trim().split("\\s+");
            for (int j = 1; j < words.length; j ++ ) {
                if (!words[j].equals("") && !stopwordList.contains(words[j])) {
                    newQry.append(words[j] + " ");
                }
            }
        }

        return newQry.toString().trim();
    }

    private String readFile(String filePath) {
        String str = new String();
        try {
            str = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }

    public static void main(String[] args) throws IOException {
        QueryParser queryParser = new QueryParser();
        queryParser.parseQuery("topics");
        System.out.println();
    }
}
