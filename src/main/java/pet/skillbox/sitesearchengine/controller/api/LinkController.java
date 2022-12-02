package pet.skillbox.sitesearchengine.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pet.skillbox.sitesearchengine.model.Link;
import pet.skillbox.sitesearchengine.model.response.IndexingResponse;
import pet.skillbox.sitesearchengine.model.response.LinksResponse;
import pet.skillbox.sitesearchengine.services.CrawlingService;

@RestController
public class LinkController {
    private final CrawlingService crawlingService;

    @Autowired
    public LinkController(CrawlingService crawlingService) {
        this.crawlingService = crawlingService;
    }

    @GetMapping(path="/api/getLinks", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> getLinks() {
        return new ResponseEntity<>(new LinksResponse(true, null, crawlingService.getLinks()), HttpStatus.OK);
    }

    @PostMapping(path="/api/addLink", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> saveLink(@RequestParam String url, @RequestParam String name, @RequestParam int isSelected) {
        boolean result = crawlingService.saveLink(new Link(url, name, isSelected));
        LinksResponse linksResponse = new LinksResponse(result, result ? null : "Ссылка уже добавлена", crawlingService.getLinks());
        return new ResponseEntity<>(linksResponse, result ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(path="/api/deleteLink", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> deleteLink(@RequestParam String url) {
        boolean result = crawlingService.deleteLink(url);
        IndexingResponse indexingResponse = new IndexingResponse(result, result ? null : "Ссылка не добавлена");
        return new ResponseEntity<>(indexingResponse, result ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(path="/api/deleteAllLinks", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<IndexingResponse> deleteAllLinks() {
        IndexingResponse indexingResponse = new IndexingResponse(true, null);
        crawlingService.deleteAllLinks();
        return new ResponseEntity<>(indexingResponse, HttpStatus.OK);
    }
}
