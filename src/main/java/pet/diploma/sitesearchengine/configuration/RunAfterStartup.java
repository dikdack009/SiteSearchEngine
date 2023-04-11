package pet.diploma.sitesearchengine.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.services.CrawlingService;
import pet.diploma.sitesearchengine.controller.api.IndexingController;
import pet.diploma.sitesearchengine.model.Status;
import pet.diploma.sitesearchengine.services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
public class RunAfterStartup {

    private final CrawlingService crawlingService;
    private final IndexingController indexingController;
    private final UserService userService;

    @Autowired
    public RunAfterStartup(CrawlingService crawlingService, IndexingController indexingController, UserService userService) {
        this.crawlingService = crawlingService;
        this.indexingController = indexingController;
        this.userService = userService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() throws IOException, ExecutionException, InterruptedException, javax.mail.MessagingException, JSONException, SQLException {
        System.out.println("Yaaah, I am running........");
        if (crawlingService.getFields().isEmpty()) {
            crawlingService.insertBasicFields();
        }
        List<Site> failedIndexingSites = crawlingService.getSites().stream()
                .filter(s -> s.getStatus().equals(Status.INDEXING) && s.getIsDeleted() == 0).collect(Collectors.toList());
        Map<Integer, List<Site>> sitesByUserId = failedIndexingSites.stream().collect(
                Collectors.groupingBy(Site::getUserId));
        if (!sitesByUserId.isEmpty()) {
            System.out.println("Ссылки для переиндексации:");
            sitesByUserId.forEach((k, v) -> System.out.println(k + " - " + v));
            for (Integer userId : sitesByUserId.keySet()) {
                StringJoiner stringJoiner = new StringJoiner("\\\",\\\"", "{\"data\":\"{\\\"", "\\\"}\"}");
                for (Site site : sitesByUserId.get(userId)) {
                    stringJoiner.add(site.getUrl() + "\\\":\\\"" + site.getName());
                }
                if (!sitesByUserId.get(userId).isEmpty()) {
                    indexingController.indexing(userId, userService.findUserById(userId).getLogin(), stringJoiner.toString());
                }
            }
        }
    }
}