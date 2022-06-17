import crawling.CrawlingSystem;
import repository.MySQLConnection;
import crawling.SearchSystem;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        MySQLConnection.setUpDB();
        System.out.print("?\t");
        if (new Scanner(System.in).nextInt() > 0) {
            System.out.print("->\t");
            String input = new Scanner(System.in).nextLine();
            System.out.println(SearchSystem.request(input));
        }else {
            System.out.print("?\t");
            if (new Scanner(System.in).nextInt() == 0) {
                long m = System.currentTimeMillis();
                CrawlingSystem.startCrawlingSystem("https://www.playback.ru/");
                double time = (double)System.currentTimeMillis() - m;
                System.out.println(time / 60000);
            }
        }
        MySQLConnection.closeConnection();
    }
}

