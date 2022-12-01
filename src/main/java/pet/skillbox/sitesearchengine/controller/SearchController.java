package pet.skillbox.sitesearchengine.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.*;
import pet.skillbox.sitesearchengine.controller.crawling.SearchSystem;
import pet.skillbox.sitesearchengine.model.response.SearchResponse;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
public class SearchController {

    private final CrawlingService crawlingService;

    @Autowired
    public SearchController(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }

    @GetMapping(path="/api/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<SearchResponse> statistics(@RequestParam String query,
                                                     @RequestParam(required = false) Integer offset,
                                                     @RequestParam(required = false) Integer limit, @RequestBody String body) throws IOException {
        System.out.println(body);
        Map<String, Object> tmp = new ObjectMapper().readValue(body, HashMap.class);
        System.out.println("tmp" + tmp);
        ObjectMapper objectMapper = new ObjectMapper();
//        String[] ttt = new ObjectMapper().readValue(tmp.get("data").toString(), String[].class);
        body = tmp.get("data").toString();
        body = body.substring(1, body.length() - 1);

        Set<String> links = Arrays.stream(body.split(",")).collect(Collectors.toSet());
        System.out.println(tmp.get("data").toString());

        Logger rootLogger = LogManager.getLogger("search");
        rootLogger.info("Поиск - <" + query + ">");
        long mm = System.currentTimeMillis();
        ResponseEntity<SearchResponse> result = null;
        try {
            result = new SearchSystem(query, links, offset, limit, crawlingService).request();
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
