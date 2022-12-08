package pet.skillbox.sitesearchengine.model.thread;

import pet.skillbox.sitesearchengine.controller.crawling.CrawlingSystem;
import pet.skillbox.sitesearchengine.model.Builder;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CrawlingThread extends Thread{

    private final List<String> urlPool;
    private final CrawlingSystem crawlingSystem;
    private final int siteId;

    public CrawlingThread(List<String> urlPool, CrawlingSystem crawlingSystem, int siteId){
        CrawlingSystem crawlingSystem1;
        this.urlPool = urlPool;
        crawlingSystem1 = new CrawlingSystem(crawlingSystem);
        this.crawlingSystem = crawlingSystem1;
        this.siteId = siteId;
    }

    @Override
    public void run() {
        Builder builder = new Builder();
        try {
            for (String page : urlPool) { crawlingSystem.appendPageInDB(page, builder); }
            if (!builder.getLemmaBuilder().toString().equals("")) {
                DBConnection.insert(builder);
            }
        } catch (SQLException e) {
            crawlingSystem.setLastError(e.getMessage());
            crawlingSystem.getRootLogger().debug("Ошибка - " + e.getMessage().substring(e.getMessage().indexOf(":") + 2));
            throw new RuntimeException(e.getMessage().substring(e.getMessage().indexOf(":") + 2));
        }
        crawlingSystem.getRootLogger().info(siteId + " Done " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }
}
