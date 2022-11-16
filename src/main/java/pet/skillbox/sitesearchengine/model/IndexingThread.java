package pet.skillbox.sitesearchengine.model;

import pet.skillbox.sitesearchengine.configuration.Config;
import pet.skillbox.sitesearchengine.configuration.SiteProperty;
import pet.skillbox.sitesearchengine.controller.IndexingController;
import pet.skillbox.sitesearchengine.controller.crawling.CrawlingSystem;
import pet.skillbox.sitesearchengine.model.response.IndexingResponse;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public class IndexingThread implements Callable<IndexingResponse> {
    private final String url;
    private final String name;
    private final IndexingController indexingController;
    private final Config config;
    private final CrawlingService crawlingService;
    private final int id;

    public IndexingThread(IndexingController indexingController, SiteProperty siteProperty, Config config, CrawlingService crawlingService, int id) {
        this.url = siteProperty.getUrl();
        this.name = siteProperty.getName();
        this.indexingController = indexingController;
        this.config = config;
        this.crawlingService = crawlingService;
        this.id = id;
    }

    @Override
    public IndexingResponse call() {
        Site site = new Site(Status.INDEXING, LocalDateTime.now(), null, url, name);
        CrawlingSystem crawlingSystem =  new CrawlingSystem(config, crawlingService, site);
        try {
            indexingController.setIndexing(true);
            crawlingService.updateStatus(site);
            crawlingService.deleteSiteInfo(site.getId());
            crawlingSystem.start(config, id);
            if (config.isStopIndexing()){
                site = new Site(Status.INDEXED, LocalDateTime.now(), "Индексация остановлена", url, name);
                crawlingService.updateStatus(site);
                return new IndexingResponse(false, "Индексация остановлена");
            }
//            crawlingService.deleteSiteInfo(site.getId());
//            DBConnection.fromTmpToActualUpdate(site.getId());
//            crawlingService.deleteTmpSiteInfo(site.getId());
            Status status = crawlingSystem.getLastError() == null ? Status.INDEXED : Status.FAILED;
            site = new Site(status, LocalDateTime.now(), crawlingSystem.getLastError(), url, name);
            crawlingService.updateStatus(site);

//            DBConnection.updateSite(url, "INDEXED", config.isStopIndexing() ? "Индексация остановлена" : null);
            return new IndexingResponse(true, null);
        } catch (Exception e) {
//            DBConnection.updateSite(url, "FAILED", crawlingSystem.getLastError());
            System.out.println(site.getUrl());
            e.printStackTrace();
            String error = crawlingSystem.getLastError() == null ? "Неизвестная ошибка" : crawlingSystem.getLastError();
            site = new Site(Status.FAILED, LocalDateTime.now(), error, url, name);
            crawlingService.updateStatus(site);
            indexingController.setIndexing(false);
            return new IndexingResponse(false, crawlingSystem.getLastError());
        }
    }
}
