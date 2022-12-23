package pet.diploma.sitesearchengine.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pet.diploma.sitesearchengine.services.CrawlingService;
import pet.diploma.sitesearchengine.configuration.Config;
import pet.diploma.sitesearchengine.configuration.SiteProperty;
import pet.diploma.sitesearchengine.model.response.IndexingResponse;

@RestController
public class SiteController {

    private final Config config;
    private final CrawlingService crawlingService;

    @Autowired
    public SiteController(Config config, CrawlingService crawlingService) {
        this.config = config;
        this.crawlingService = crawlingService;
    }

    @DeleteMapping(path="/api/deleteSite", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> deleteSite(@RequestParam String url) {
        boolean result = crawlingService.deleteSiteInfo(url);
        crawlingService.deleteSite(url);
        IndexingResponse indexingResponse = new IndexingResponse(result, result ? null : "Сайт " + url + " не проиндексирован");
        return new ResponseEntity<>(indexingResponse, result ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(path="/api/deleteAllSites", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> deleteAllSites() {
        IndexingResponse indexingResponse = new IndexingResponse(true, null);
        boolean result = true;
        for (SiteProperty site : config.getSites()) {
            String url = site.getUrl();
            result = crawlingService.deleteSiteInfo(url);
            crawlingService.deleteSite(url);
        }
        return new ResponseEntity<>(indexingResponse, result ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}
