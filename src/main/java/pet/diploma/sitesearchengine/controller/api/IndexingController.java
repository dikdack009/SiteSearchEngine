package pet.diploma.sitesearchengine.controller.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pet.diploma.sitesearchengine.model.User;
import pet.diploma.sitesearchengine.model.request.IndexingRequest;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class IndexingController {
    private final Config config;
    private final Logger rootLogger;
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
        rootLogger = LogManager.getRootLogger();
    }

    @PostMapping(path="/api/startIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> startIndexing(@RequestBody IndexingRequest body) throws IOException, InterruptedException, ExecutionException, javax.mail.MessagingException, SQLException {
        int userId = userService.getIdByLogin(authService.getAuthInfo().getPrincipal().toString());
        return indexing(userId, authService.getAuthInfo().getPrincipal().toString(), body.getData());
    }

    public ResponseEntity<IndexingResponse> indexing(int userId, String email, Map<String,String> sites) throws InterruptedException, ExecutionException, MessagingException, SQLException, IOException {
        long start = System.currentTimeMillis();
        rootLogger.info(email + ":\tЗапуск индексации " + sites.size() + " сайтов.");
        AtomicReference<IndexingResponse> response = new AtomicReference<>();
        checkUserInfo(userId);
        if (config.getUserIndexing().get(userId)) {
            rootLogger.error(email + ":\tОшибка индексации: " + "Индексация уже запущена");
            response.set(new IndexingResponse(false, "Индексация уже запущена"));
            return new ResponseEntity<>(response.get(), HttpStatus.BAD_REQUEST);
        }
        ExecutorService es = Executors.newFixedThreadPool(100);
        List<IndexingThread> tasks = new ArrayList<>();

        for (String url : sites.keySet()) {
            SiteProperty site = new SiteProperty(url, sites.get(url), email);
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
            sendMessage(email, userId, sites);
            rootLogger.info(email + ":\tПисьмо отправлено");
        }
        rootLogger.info(email + ":\tЗакончили индексацию за " + (double)(System.currentTimeMillis() - start) / 1000 + " сек.");
        return new ResponseEntity<>(response.get(), HttpStatus.OK);
    }

    private void sendMessage(String username, int userId, Map<String, String> sites) throws MessagingException, UnsupportedEncodingException, SQLException {
        emailService.sendMessage(username, userId, sites);
    }

    @GetMapping(path="/api/stopIndexing", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<IndexingResponse> stopIndexing() {
        long start = System.currentTimeMillis();
        String email = authService.getAuthInfo().getPrincipal().toString();
        rootLogger.info(email + ":\tОстанавливаем индексацию");
        int userId = userService.getIdByLogin(email);
        checkUserInfo(userId);
        IndexingResponse response;
        if (!config.getUserIndexing().get(userId)){
            response = new IndexingResponse(false, "Индексация не запущена");
            rootLogger.error(email + ":\tОшибка индексации: " + "Индексация не запущена");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        config.getStopIndexing().put(userId, true);
        config.getUserIndexing().put(userId, false);
        response = new IndexingResponse(true, null);
        rootLogger.info(email + ":\tОстановили за " + (double)(System.currentTimeMillis() - start) / 1000 + " сек.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path="/api/statistics", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<Statistic> statistics() throws ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        String email = authService.getAuthInfo().getPrincipal().toString();
        int userId = userService.getIdByLogin(email);
        checkUserInfo(userId);
        ExecutorService es = Executors.newFixedThreadPool(10);
        List<StatisticThread> tasks = new ArrayList<>();
        tasks.add(new StatisticThread(config.getUserIndexing().get(userId), userId));
        ResponseEntity<Statistic> statistic = es.invokeAny(tasks);
        es.shutdown();
        rootLogger.info(email + ":\tВернули сатистику за " + (double)(System.currentTimeMillis() - start) / 1000 + " сек.");
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
