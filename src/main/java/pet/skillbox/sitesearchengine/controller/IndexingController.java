package pet.skillbox.sitesearchengine.controller;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pet.skillbox.sitesearchengine.configuration.Config;
import pet.skillbox.sitesearchengine.configuration.SiteProperty;
import pet.skillbox.sitesearchengine.model.*;
import pet.skillbox.sitesearchengine.model.response.IndexingResponse;
import pet.skillbox.sitesearchengine.model.response.Statistic;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    @GetMapping(path="/api/startIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
//    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<IndexingResponse> startIndexing() {
        AtomicReference<IndexingResponse> response = new AtomicReference<>();
        if (isIndexing){
            response.set(new IndexingResponse(false, "Индексация уже запущена"));
            return new ResponseEntity<>(response.get(), HttpStatus.BAD_REQUEST);
        }
        ExecutorService es = Executors.newFixedThreadPool(10);
        List<IndexingThread> tasks = new ArrayList<>();
        for (SiteProperty site : config.getSites()) {
            System.out.println(site.getUrl());
            tasks.add(new IndexingThread(this, site.getUrl(), site.getName(),
                    config, crawlingService));
        }
        List<Future<IndexingResponse>> futures;
        try {
            futures = es.invokeAll(tasks);
            for(Future<IndexingResponse> f : futures) {
                response.set(f.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        es.shutdown();
        isIndexing = false;
        config.setStopIndexing(false);
        return new ResponseEntity<>(response.get(), HttpStatus.OK);
    }

    @PostMapping(path="/api/startSiteIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
//    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<IndexingResponse> startSiteIndexing() {
        return new ResponseEntity<>(new IndexingResponse(true, null), HttpStatus.OK);
    }

    @GetMapping(path="/api/stopIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
//    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
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
//    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Statistic> statistics() throws SQLException {
        return new ResponseEntity<>(new Statistic(isIndexing), HttpStatus.OK);
    }
}
