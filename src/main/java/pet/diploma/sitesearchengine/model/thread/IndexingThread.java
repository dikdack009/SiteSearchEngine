package pet.diploma.sitesearchengine.model.thread;

import org.apache.logging.log4j.LogManager;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.model.Status;
import pet.diploma.sitesearchengine.model.response.IndexingResponse;
import pet.diploma.sitesearchengine.services.CrawlingService;
import pet.diploma.sitesearchengine.configuration.Config;
import pet.diploma.sitesearchengine.configuration.SiteProperty;
import pet.diploma.sitesearchengine.controller.crawling.CrawlingSystem;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public class IndexingThread implements Callable<IndexingResponse> {
    private final String url;
    private final String name;
    private final String email;
    private final Config config;
    private final CrawlingService crawlingService;
    private final int userId;

    public IndexingThread(SiteProperty siteProperty, Config config, CrawlingService crawlingService, int userId) {
        this.url = siteProperty.getUrl();
        this.name = siteProperty.getName();
        this.email = siteProperty.getEmail();
        this.config = config;
        this.crawlingService = crawlingService;
        this.userId = userId;
    }

    @Override
    public IndexingResponse call() {
        Site site = new Site(Status.INDEXING, LocalDateTime.now(), null, url, name, userId);
        crawlingService.updateStatus(site);
        CrawlingSystem crawlingSystem =  new CrawlingSystem(config, crawlingService, site, userId);
        try {
            if (config.getStopIndexing().get(userId)){
                return check();
            }
            config.getUserIndexing().put(userId, true);
            crawlingService.deleteSiteInfo(site.getUrl(), userId);
            if (config.getStopIndexing().get(userId)){
                return check();
            }
            crawlingSystem.start(email, config);
            if (config.getStopIndexing().get(userId)){
                return check();
            }
            Status status = crawlingSystem.getLastError() == null ? Status.INDEXED : Status.FAILED;
            site = new Site(status, LocalDateTime.now(), crawlingSystem.getLastError(), url, name, userId);
            crawlingService.updateStatus(site);
            return new IndexingResponse(true, null);

        } catch (Exception e) {
            String error = crawlingSystem.getLastError() == null ? "Неизвестная ошибка" : crawlingSystem.getLastError();
            site = new Site(Status.FAILED, LocalDateTime.now(), error, url, name, userId);
            LogManager.getLogger("index").error(email + ":\t" + (
                    crawlingSystem.getLastError() == null ? "Внутренняя ошибка индексации: "
                            + e.getMessage() : "Ошибка индексации: " + crawlingSystem.getLastError()));
            crawlingService.updateStatus(site);
            config.getUserIndexing().put(userId, false);
            return new IndexingResponse(false, crawlingSystem.getLastError());
        }
    }

    private IndexingResponse check() {
        Site site = new Site(Status.FAILED, LocalDateTime.now(), "Индексация остановлена пользователем", url, name, userId);
        crawlingService.updateStatus(site);
        LogManager.getLogger("index").error(email + ":\t" + "Ошибка индексации: Индексация остановлена пользователем");
        return new IndexingResponse(false, "Индексация остановлена пользователем");
    }
}
