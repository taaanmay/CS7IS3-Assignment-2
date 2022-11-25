package app;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author Siuyun Yip
 * @version 1.0
 * @date 2022/11/25 13:31
 */
public class MyAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String s) {
        StandardTokenizer tokenizer = new StandardTokenizer();
        TokenStream filter = new LowerCaseFilter(tokenizer);

        filter = new FlattenGraphFilter(new WordDelimiterGraphFilter(filter,
                WordDelimiterGraphFilter.SPLIT_ON_NUMERICS | WordDelimiterGraphFilter.GENERATE_WORD_PARTS
                        | WordDelimiterGraphFilter.GENERATE_NUMBER_PARTS | WordDelimiterGraphFilter.PRESERVE_ORIGINAL,
                null));
        filter = new TrimFilter(filter);
        filter = new PorterStemFilter(filter);
        filter = new EnglishPossessiveFilter(filter);
        //filter = new StopFilter(filter, stopwords);
        filter = new KStemFilter(filter);
        //return new TokenStreamComponents(tokenizer, filter);
        filter = new StopFilter(filter, getStopword());
        filter = new SnowballFilter(filter, "English");

        return new TokenStreamComponents(tokenizer, filter);
    }

    private CharArraySet getStopword() {
        List<String> stopwordList = Arrays.asList(readFile(Constant.STOPWORD_FILE).toLowerCase().split("\n"));

        return new CharArraySet(stopwordList, true);
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
}
