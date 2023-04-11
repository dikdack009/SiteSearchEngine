package pet.diploma.sitesearchengine.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import pet.diploma.sitesearchengine.services.AuthService;
import pet.diploma.sitesearchengine.services.CrawlingService;
import pet.diploma.sitesearchengine.controller.crawling.SearchSystem;
import pet.diploma.sitesearchengine.model.response.SearchResponse;
import pet.diploma.sitesearchengine.services.UserService;

import java.io.IOException;
import java.util.*;

@RestController
public class SearchController {

    private final CrawlingService crawlingService;
    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public SearchController(CrawlingService crawlingService, AuthService authService, UserService userService) {
        this.crawlingService = crawlingService;
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping(path="/api/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<SearchResponse> statistics(@RequestParam String query,
                                                     @RequestParam(required = false) Integer offset,
                                                     @RequestParam(required = false) Integer limit, @RequestBody String body) throws IOException {
        Map<String, Object> tmp = new ObjectMapper().readValue(body, HashMap.class);
        Set<String> sites = new ObjectMapper().readValue(tmp.get("sites").toString(), HashSet.class);

        Logger rootLogger = LogManager.getLogger("search");
        rootLogger.info("Поиск - <" + query + ">");
        int userId = userService.getIdByLogin(authService.getAuthInfo().getPrincipal().toString());
        long mm = System.currentTimeMillis();
        ResponseEntity<SearchResponse> result = null;
        try {
            result = new SearchSystem(query, sites, offset, limit, crawlingService, userId).request();
            rootLogger.info("Нашли за " + (double)(System.currentTimeMillis() - mm) / 1000 + " сек.");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.out.println("Закончили поиск");
        System.out.println((double)(System.currentTimeMillis() - mm) / 1000 + " sec.");
        System.out.println((double)(System.currentTimeMillis() - mm) / 60000 + " min.");
        return result;
    }
}
