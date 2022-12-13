package pet.skillbox.sitesearchengine.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import pet.skillbox.sitesearchengine.controller.crawling.SearchSystem;
import pet.skillbox.sitesearchengine.model.response.SearchResponse;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import java.io.IOException;
import java.util.*;

@RestController
public class SearchController {

    private final CrawlingService crawlingService;

    @Autowired
    public SearchController(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }

    @PostMapping(path="/api/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<SearchResponse> statistics(@RequestParam String query,
                                                     @RequestParam(required = false) Integer offset,
                                                     @RequestParam(required = false) Integer limit, @RequestBody String body) throws IOException {
        System.out.println(body);
        Map<String, Object> tmp = new ObjectMapper().readValue(body, HashMap.class);
        System.out.println("tmp" + tmp);
        Set<String> sites = new ObjectMapper().readValue(tmp.get("sites").toString(), HashSet.class);

        Logger rootLogger = LogManager.getLogger("search");
        rootLogger.info("Поиск - <" + query + ">");
        long mm = System.currentTimeMillis();
        ResponseEntity<SearchResponse> result = null;
        try {
            result = new SearchSystem(query, sites, offset, limit, crawlingService).request();
            rootLogger.info("Нашли за " + (double)(System.currentTimeMillis() - mm) / 1000 + " сек.");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.out.println("Закончили");
        System.out.println((double)(System.currentTimeMillis() - mm) / 1000 + " sec.");
        System.out.println((double)(System.currentTimeMillis() - mm) / 60000 + " min.");
        return result;
    }
}
