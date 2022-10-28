package pet.skillbox.sitesearchengine.model;

import pet.skillbox.sitesearchengine.controller.crawling.CrawlingSystem;
import pet.skillbox.sitesearchengine.repositories.DBConnection;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CrawlingThread extends Thread{

    private final List<String> urlPool;
    private final CrawlingSystem crawlingSystem;



    public CrawlingThread(List<String> urlPool, CrawlingSystem crawlingSystem){
        this.urlPool = urlPool;
        this.crawlingSystem = crawlingSystem;
    }

    @Override
    public void run() {
        Builder builder = new Builder();
        try {
            for (String page : urlPool) { crawlingSystem.appendPageInDB(page, builder); }
            DBConnection.insert(builder);
        } catch (IOException | InterruptedException | SQLException e) {
            crawlingSystem.setLastError(e.getMessage());
            System.out.println("Ошибка - " + e.getMessage());
            crawlingSystem.getRootLogger().debug("Ошибка - " + e.getMessage());
        }
        crawlingSystem.getRootLogger().info("\nDone " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }
}
