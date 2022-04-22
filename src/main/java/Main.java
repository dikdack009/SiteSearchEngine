import crawling.CrawlingSystem;
import crawling.SearchSystem;
import lemmatizer.Lemmatizer;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
//        CrawlingSystem.startCrawlingSystem("http://www.playback.ru/");

        SearchSystem.request(new Scanner(System.in).nextLine());
    }
}
