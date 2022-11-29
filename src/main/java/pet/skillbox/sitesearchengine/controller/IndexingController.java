package pet.skillbox.sitesearchengine.controller;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pet.skillbox.sitesearchengine.configuration.Config;
import pet.skillbox.sitesearchengine.configuration.SiteProperty;
import pet.skillbox.sitesearchengine.model.Link;
import pet.skillbox.sitesearchengine.model.response.IndexingResponse;
import pet.skillbox.sitesearchengine.model.response.LinksResponse;
import pet.skillbox.sitesearchengine.model.response.Statistic;
import pet.skillbox.sitesearchengine.model.thread.IndexingThread;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class IndexingController {
    @Setter
    private boolean isIndexing = false;
    private final Config config;
    private final CrawlingService crawlingService;

    @Autowired
    public IndexingController(Config config, CrawlingService crawlingService) {
        this.config = config;
        this.crawlingService = crawlingService;
    }

    //TODO: добавить стобец юзер в ссылки
    @GetMapping(path="/api/getLinks", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> getLinks() {
        return new ResponseEntity<>(new LinksResponse(true, null, crawlingService.getLinks()), HttpStatus.OK);
    }

    @PostMapping(path="/api/saveLink", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> saveLink(@RequestParam String link, @RequestParam String name) {
        boolean result = crawlingService.saveLink(new Link(link, name));
        LinksResponse linksResponse = new LinksResponse(result, result ? null : "Ссылка уже добавлена", crawlingService.getLinks());
        return new ResponseEntity<>(linksResponse, result ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @GetMapping(path="/api/startIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> startIndexing(@RequestBody Map<String, String> body) {
        AtomicReference<IndexingResponse> response = new AtomicReference<>();
        if (isIndexing){
            response.set(new IndexingResponse(false, "Индексация уже запущена"));
            return new ResponseEntity<>(response.get(), HttpStatus.BAD_REQUEST);
        }
        ExecutorService es = Executors.newFixedThreadPool(100);
        List<IndexingThread> tasks = new ArrayList<>();

        int id = crawlingService.getMaxPageId() + 1;
        List<SiteProperty> newSiteList = new ArrayList<>();
        for (String url : body.keySet()) {
            SiteProperty site = new SiteProperty(url, body.get(url));
            site.setUrl(url);
            newSiteList.add(site);
            tasks.add(new IndexingThread(this, site, config, crawlingService, id));
        }
        config.setSites(newSiteList);
        List<Future<IndexingResponse>> futures;
        try {
            futures = es.invokeAll(tasks);
            for(Future<IndexingResponse> f : futures) {
                response.set(f.get());
                if (response.get().getError() != null) {
                    break;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        es.shutdown();
        isIndexing = false;
        config.setStopIndexing(false);
        System.out.println("Закончили индексацию !!!");
        return new ResponseEntity<>(response.get(), HttpStatus.OK);
    }

    @PostMapping(path="/api/startSiteIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> startSiteIndexing() {
        return new ResponseEntity<>(new IndexingResponse(true, null), HttpStatus.OK);
    }

    @GetMapping(path="/api/stopIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> stopIndexing() {
        IndexingResponse response;
        if (!isIndexing){
            response = new IndexingResponse(false, "Индексация не запущена");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        config.setStopIndexing(true);
        isIndexing = false;
        response = new IndexingResponse(true, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path="/api/statistics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<Statistic> statistics() throws SQLException {
        return new ResponseEntity<>(new Statistic(isIndexing), HttpStatus.OK);
    }

    @DeleteMapping(path="/api/deleteSite", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> deleteSite(@RequestParam String url) {
        int res = crawlingService.deleteSiteInfo(url);
        crawlingService.deleteSite(url);
        IndexingResponse indexingResponse = res == 1 ? new IndexingResponse(true, null)
                : new IndexingResponse(false, "Сайт " + url + " не проиндексирован");
        return new ResponseEntity<>(indexingResponse, res == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(path="/api/deleteAll", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> deleteAll() {
        IndexingResponse indexingResponse = new IndexingResponse(false, "Сайты не проиндексированы");
        int res = -1;
        for (SiteProperty site : config.getSites()) {
            String url = site.getUrl();
            res = crawlingService.deleteSiteInfo(url);
            crawlingService.deleteSite(url);
        }
        return new ResponseEntity<>(indexingResponse, res == 1 ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}
