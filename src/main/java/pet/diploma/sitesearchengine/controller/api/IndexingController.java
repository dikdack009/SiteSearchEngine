package pet.diploma.sitesearchengine.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pet.diploma.sitesearchengine.model.User;
import pet.diploma.sitesearchengine.services.AuthService;
import pet.diploma.sitesearchengine.services.CrawlingService;
import pet.diploma.sitesearchengine.services.EmailService;
import pet.diploma.sitesearchengine.services.UserService;
import pet.diploma.sitesearchengine.configuration.Config;
import pet.diploma.sitesearchengine.configuration.SiteProperty;
import pet.diploma.sitesearchengine.model.response.IndexingResponse;
import pet.diploma.sitesearchengine.model.response.Statistic;
import pet.diploma.sitesearchengine.model.thread.IndexingThread;
import pet.diploma.sitesearchengine.model.thread.StatisticThread;


import javax.mail.MessagingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class IndexingController {
    private final Config config;
    private final CrawlingService crawlingService;
    private final EmailService emailService;
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public IndexingController(Config config, CrawlingService crawlingService, EmailService emailService, UserService userService, AuthService authService) {
        this.config = config;
        this.crawlingService = crawlingService;
        this.emailService = emailService;
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping(path="/api/startIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> startIndexing(@RequestBody String body) throws IOException, InterruptedException, ExecutionException, javax.mail.MessagingException, JSONException, SQLException {

        int userId = userService.getIdByLogin(authService.getAuthInfo().getPrincipal().toString());
        return indexing(userId, authService.getAuthInfo().getPrincipal().toString(), body);
    }

    public ResponseEntity<IndexingResponse> indexing(int userId, String email, String body) throws InterruptedException, ExecutionException, MessagingException, SQLException, IOException, JSONException {
        Map<String, Object> tmp = new ObjectMapper().readValue(body, HashMap.class);
        Map<String, String> result = new ObjectMapper().readValue(tmp.get("data").toString(), HashMap.class);
        AtomicReference<IndexingResponse> response = new AtomicReference<>();
        checkUserInfo(userId);
        if (config.getUserIndexing().get(userId)) {
            response.set(new IndexingResponse(false, "Индексация уже запущена"));
            return new ResponseEntity<>(response.get(), HttpStatus.BAD_REQUEST);
        }
        ExecutorService es = Executors.newFixedThreadPool(100);
        List<IndexingThread> tasks = new ArrayList<>();

        for (String url : result.keySet()) {
            SiteProperty site = new SiteProperty(url, result.get(url));
            tasks.add(new IndexingThread(site, config, crawlingService, userId));
        }
        List<Future<IndexingResponse>> futures;

        futures = es.invokeAll(tasks);
        for (Future<IndexingResponse> f : futures) {
            if (f.get().getError() != null) {
                response.set(f.get());
            }
        }
        es.shutdown();
        config.getUserIndexing().put(userId, false);
        config.getStopIndexing().put(userId, false);
        Optional<User> optionalUser = userService.getByLogin(email);
        if (optionalUser.isPresent() && optionalUser.get().isNotify()) {
            sendMessage(email, userId, result);
        }
        System.out.println("Закончили индексацию !!!");
        return new ResponseEntity<>(response.get(), HttpStatus.OK);
    }

    private void sendMessage(String username, int userId, Map<String, String> sites) throws MessagingException, UnsupportedEncodingException, JSONException, ExecutionException, InterruptedException, SQLException {
        emailService.sendMessage(username, userId, sites);
    }

    @GetMapping(path="/api/stopIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> stopIndexing() {
        int userId = userService.getIdByLogin(authService.getAuthInfo().getPrincipal().toString());
        checkUserInfo(userId);
        IndexingResponse response;
        if (!config.getUserIndexing().get(userId)){
            response = new IndexingResponse(false, "Индексация не запущена");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        config.getStopIndexing().put(userId, true);
        config.getUserIndexing().put(userId, false);
        response = new IndexingResponse(true, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path="/api/statistics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<Statistic> statistics() throws ExecutionException, InterruptedException {
        int userId = userService.getIdByLogin(authService.getAuthInfo().getPrincipal().toString());
        checkUserInfo(userId);
        System.out.println("Зашли в статистику");
        ExecutorService es = Executors.newFixedThreadPool(10);
        List<StatisticThread> tasks = new ArrayList<>();
        tasks.add(new StatisticThread(config.getUserIndexing().get(userId), userId));
        ResponseEntity<Statistic> statistic = es.invokeAny(tasks);
        es.shutdown();
        return statistic;
    }

    private void checkUserInfo(int userId) {
        if (Objects.isNull(config.getUserIndexing().get(userId))) {
            config.getUserIndexing().put(userId, false);
        }
        if (Objects.isNull(config.getStopIndexing().get(userId))) {
            config.getStopIndexing().put(userId, false);
        }
    }
}
