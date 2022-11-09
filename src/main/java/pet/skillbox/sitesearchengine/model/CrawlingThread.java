package pet.skillbox.sitesearchengine.model;

import pet.skillbox.sitesearchengine.controller.crawling.CrawlingSystem;
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
    private final CrawlingService crawlingService;

    public CrawlingThread(List<String> urlPool, CrawlingSystem crawlingSystem, int siteId, CrawlingService crawlingService){
        this.urlPool = urlPool;
        this.crawlingSystem = crawlingSystem;
        this.siteId = siteId;
        this.crawlingService = crawlingService;
    }

    @Override
    public void run() {
        Builder builder = new Builder();
        try {
            crawlingService.deleteSiteInfo(siteId);
            for (String page : urlPool) { crawlingSystem.appendPageInDB(page, builder); }
            DBConnection.insert(builder);
            crawlingService.deleteTmpSiteInfo(siteId);
        } catch (SQLException e) {
            crawlingSystem.setLastError(e.getMessage());
            System.out.println("Ошибка - ");
            e.printStackTrace();
            crawlingSystem.getRootLogger().debug("Ошибка - " + e.getMessage().substring(e.getMessage().indexOf(":") + 2));
            throw new RuntimeException(e.getMessage().substring(e.getMessage().indexOf(":") + 2));
        }
        crawlingSystem.getRootLogger().info("\nDone " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }
}
