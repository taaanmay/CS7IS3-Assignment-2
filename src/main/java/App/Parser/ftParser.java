package App.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import App.Model.ChildModel.FTModel;

public class ftParser {

    private final static File FT_DIR = new File("Documents/ft");
    private static ArrayList<FTModel> ftDocsList = new ArrayList<>();

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
    public static void parseAllFTFiles(String path) throws IOException {

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
    }

    public ArrayList<FTModel> getFtDocsList() throws IOException {
        parseAllFTFiles(FT_DIR.getAbsolutePath());
        return ftDocsList;
    }

    public static void main(String Args[]) throws IOException {
        parseAllFTFiles(FT_DIR.getAbsolutePath());
        System.out.println(ftDocsList.size());
    }

}