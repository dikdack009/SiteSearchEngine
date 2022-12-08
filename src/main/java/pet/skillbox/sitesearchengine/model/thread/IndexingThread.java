package pet.skillbox.sitesearchengine.model.thread;

import pet.skillbox.sitesearchengine.configuration.Config;
import pet.skillbox.sitesearchengine.configuration.SiteProperty;
import pet.skillbox.sitesearchengine.controller.api.IndexingController;
import pet.skillbox.sitesearchengine.controller.crawling.CrawlingSystem;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.model.Status;
import pet.skillbox.sitesearchengine.model.response.IndexingResponse;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public class IndexingThread implements Callable<IndexingResponse> {
    private final String url;
    private final String name;
    private final IndexingController indexingController;
    private final Config config;
    private final CrawlingService crawlingService;

    public IndexingThread(IndexingController indexingController, SiteProperty siteProperty, Config config, CrawlingService crawlingService) {
        this.url = siteProperty.getUrl();
        this.name = siteProperty.getName();
        this.indexingController = indexingController;
        this.config = config;
        this.crawlingService = crawlingService;
    }

    @Override
    public IndexingResponse call() {
        Site site = new Site(Status.INDEXING, LocalDateTime.now(), null, url, name);
        crawlingService.updateStatus(site);
        CrawlingSystem crawlingSystem =  new CrawlingSystem(config, crawlingService, site);
        try {
            if (config.isStopIndexing()){
                site = new Site(Status.FAILED, LocalDateTime.now(), "Индексация остановлена пользователем", url, name);
                crawlingService.updateStatus(site);
                return new IndexingResponse(false, "Индексация остановлена пользователем");
            }
            indexingController.setIndexing(true);
            System.out.println(site);
            crawlingService.deleteSiteInfo(site.getUrl());
            System.out.println("Удалили");
            if (config.isStopIndexing()){
                site = new Site(Status.FAILED, LocalDateTime.now(), "Индексация остановлена пользователем", url, name);
                crawlingService.updateStatus(site);
                return new IndexingResponse(false, "Индексация остановлена пользователем");
            }
            System.out.println("Удалили?????????????????????");
            crawlingSystem.start(config);
            if (config.isStopIndexing()){
                site = new Site(Status.FAILED, LocalDateTime.now(), "Индексация остановлена пользователем", url, name);
                crawlingService.updateStatus(site);
                return new IndexingResponse(false, "Индексация остановлена пользователем");
            }
            Status status = crawlingSystem.getLastError() == null ? Status.INDEXED : Status.FAILED;
            site = new Site(status, LocalDateTime.now(), crawlingSystem.getLastError(), url, name);
            crawlingService.updateStatus(site);

            return new IndexingResponse(true, null);
        } catch (Exception e) {
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
