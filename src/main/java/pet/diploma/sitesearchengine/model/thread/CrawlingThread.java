package pet.diploma.sitesearchengine.model.thread;

import pet.diploma.sitesearchengine.repositories.DBConnection;
import pet.diploma.sitesearchengine.controller.crawling.CrawlingSystem;
import pet.diploma.sitesearchengine.model.Builder;

import java.sql.SQLException;
import java.util.List;

public class CrawlingThread extends Thread{

    private final List<String> urlPool;
    private final CrawlingSystem crawlingSystem;
    private final int siteId;

    public CrawlingThread(List<String> urlPool, CrawlingSystem crawlingSystem, int siteId, int userId){
        CrawlingSystem crawlingSystem1;
        this.urlPool = urlPool;
        crawlingSystem1 = new CrawlingSystem(crawlingSystem, userId);
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
        crawlingSystem.getRootLogger().info(Thread.currentThread().getId() +  " поток сайта " + siteId + " завершен ");
    }
}
