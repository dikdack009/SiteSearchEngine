package pet.skillbox.sitesearchengine.model;

import pet.skillbox.sitesearchengine.configuration.Config;
import pet.skillbox.sitesearchengine.controller.IndexingController;
import pet.skillbox.sitesearchengine.controller.crawling.CrawlingSystem;
import pet.skillbox.sitesearchengine.model.response.IndexingResponse;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import java.sql.SQLException;
import java.util.concurrent.Callable;

public class IndexingThread implements Callable<IndexingResponse> {
    private final String url;
    private final String name;
    private final IndexingController indexingController;
    private final Config config;
    private final CrawlingService crawlingService;

    public IndexingThread(IndexingController indexingController,
                          String url, String name, Config config, CrawlingService crawlingService) {
        this.url = url;
        this.name = name;
        this.indexingController = indexingController;
        this.config = config;
        this.crawlingService = crawlingService;
    }

    @Override
    public IndexingResponse call() throws SQLException {
        CrawlingSystem crawlingSystem =  new CrawlingSystem(config, crawlingService);
        try {
            indexingController.setIndexing(true);
            DBConnection.updateSite(url, "INDEXING", null);
            if (config.isStopIndexing()){
                indexingController.setIndexing(false);
                return new IndexingResponse(false, "Индексация остановлена");
            }
            crawlingSystem.start(config, url, name);
//            crawlingService.updateStatus(url, Status.INDEXED.toString(), null);

            DBConnection.updateSite(url, "INDEXED", config.isStopIndexing() ? "Индексация остановлена" : null);
            return new IndexingResponse(true, null);
        } catch (Exception e) {
            DBConnection.updateSite(url, "FAILED", crawlingSystem.getLastError());
//            crawlingService.updateStatus(url, Status.FAILED.toString(), crawlingSystem.getLastError());
            indexingController.setIndexing(false);
            return new IndexingResponse(false, crawlingSystem.getLastError());
        }
    }
}
