package pet.skillbox.sitesearchengine.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pet.skillbox.sitesearchengine.model.Link;
import pet.skillbox.sitesearchengine.model.response.IndexingResponse;
import pet.skillbox.sitesearchengine.model.response.LinkModel;
import pet.skillbox.sitesearchengine.model.response.LinksResponse;
import pet.skillbox.sitesearchengine.services.CrawlingService;

import javax.persistence.PostUpdate;
import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.*;

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

    @PostMapping(path="/api/updateLinks", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> updateLinks(@RequestBody String body) throws IOException {
        Map<String, Object> tmp = new ObjectMapper().readValue(body, HashMap.class);
        Map<String, Integer> result  = new ObjectMapper().readValue(tmp.get("data").toString(), HashMap.class);
        System.out.println(result);
        crawlingService.updateLinks(result);
        return new ResponseEntity<>(new LinksResponse(true, null, null),  HttpStatus.OK);
    }

    @PostMapping(path="/api/updateLink", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> updateLink(@RequestParam String url, @RequestParam Integer isSelected) {
        crawlingService.updateLink(url, isSelected);
        return new ResponseEntity<>(new LinksResponse(true, null, null),  HttpStatus.OK);
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
