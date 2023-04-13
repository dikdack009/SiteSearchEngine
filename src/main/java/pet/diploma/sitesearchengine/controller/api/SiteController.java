package pet.diploma.sitesearchengine.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.model.Status;
import pet.diploma.sitesearchengine.services.AuthService;
import pet.diploma.sitesearchengine.services.CrawlingService;
import pet.diploma.sitesearchengine.configuration.Config;
import pet.diploma.sitesearchengine.configuration.SiteProperty;
import pet.diploma.sitesearchengine.model.response.IndexingResponse;
import pet.diploma.sitesearchengine.services.UserService;

@RestController
public class SiteController {

    private final Config config;
    private final CrawlingService crawlingService;
    private final AuthService authService;

    private final UserService userService;

    @Autowired
    public SiteController(Config config, CrawlingService crawlingService, AuthService authService, UserService userService) {
        this.config = config;
        this.crawlingService = crawlingService;
        this.authService = authService;
        this.userService = userService;
    }

    @DeleteMapping(path="/api/deleteSite", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> deleteSite(@RequestParam String url) {
        int userId = userService.getIdByLogin(authService.getAuthInfo().getPrincipal().toString());
        if (config.getUserIndexing().get(userId)){
            return new ResponseEntity<>(new IndexingResponse(false, "Запущена индексация"), HttpStatus.BAD_REQUEST);
        }
        Site site = crawlingService.getSiteByUrl(url, userId);
        site.setStatus(Status.DELETING);
        crawlingService.updateStatus(site);
        boolean result = crawlingService.deleteSiteInfo(url, userId);
        crawlingService.deleteSite(url, userId);
        IndexingResponse indexingResponse = new IndexingResponse(result, result ? null : "Сайт " + url + " не проиндексирован");
        return new ResponseEntity<>(indexingResponse, result ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(path="/api/deleteAllSites", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> deleteAllSites() {
        int userId = userService.getIdByLogin(authService.getAuthInfo().getPrincipal().toString());
        IndexingResponse indexingResponse = new IndexingResponse(true, null);
        boolean result = true;
        for (SiteProperty site : config.getSites()) {
            String url = site.getUrl();
            result = crawlingService.deleteSiteInfo(url, userId);
            crawlingService.deleteSite(url, userId);
        }
        return new ResponseEntity<>(indexingResponse, result ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}
