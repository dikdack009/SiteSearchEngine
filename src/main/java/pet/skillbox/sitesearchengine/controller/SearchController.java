package pet.skillbox.sitesearchengine.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pet.skillbox.sitesearchengine.controller.crawling.SearchSystem;
import pet.skillbox.sitesearchengine.model.response.SearchResponse;
import pet.skillbox.sitesearchengine.services.CrawlingService;


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
                                                @RequestParam(required = false) String site,
                                                @RequestParam(required = false) Integer offset,
                                                @RequestParam(required = false) Integer limit) {
        Logger rootLogger = LogManager.getLogger("search");
        rootLogger.info("Поиск - <" + query + ">");
        long mm = System.currentTimeMillis();
        ResponseEntity<SearchResponse> result = null;
        try {
            result = new SearchSystem(query, site, offset, limit, crawlingService).request();
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
