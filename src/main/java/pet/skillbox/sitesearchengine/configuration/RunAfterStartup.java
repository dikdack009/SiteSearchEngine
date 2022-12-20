package pet.skillbox.sitesearchengine.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pet.skillbox.sitesearchengine.controller.api.IndexingController;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.model.Status;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
public class RunAfterStartup {

    private final CrawlingService crawlingService;
    private final IndexingController indexingController;

    @Autowired
    public RunAfterStartup(CrawlingService crawlingService, IndexingController indexingController) {
        this.crawlingService = crawlingService;
        this.indexingController = indexingController;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() throws IOException, ExecutionException, InterruptedException, javax.mail.MessagingException, JSONException {
        if (crawlingService.getFields().isEmpty()) {
            crawlingService.insertBasicFields();
        }
        List<Site> failedIndexingSites = crawlingService.getSites().stream()
                .filter(s -> s.getStatus().equals(Status.INDEXING)).collect(Collectors.toList());
        System.out.println(failedIndexingSites);
        StringJoiner stringJoiner = new StringJoiner("\\\",\\\"", "{\"data\":\"{\\\"", "\\\"}\"}");
        for (Site site : failedIndexingSites) {
            stringJoiner.add(site.getUrl() + "\\\":\\\"" + site.getName());
        }
        if (!failedIndexingSites.isEmpty()) {
            indexingController.startIndexing(stringJoiner.toString());
        }
//        DBConnection.addIndexes();
        System.out.println("Yaaah, I am running........");

    }
    //TODO: подмать как пользователя тут
}