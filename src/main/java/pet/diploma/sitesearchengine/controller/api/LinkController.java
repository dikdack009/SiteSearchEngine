package pet.diploma.sitesearchengine.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pet.diploma.sitesearchengine.services.AuthService;
import pet.diploma.sitesearchengine.services.CrawlingService;
import pet.diploma.sitesearchengine.services.UserService;
import pet.diploma.sitesearchengine.model.Link;
import pet.diploma.sitesearchengine.model.response.IndexingResponse;
import pet.diploma.sitesearchengine.model.response.LinksResponse;

import java.io.IOException;
import java.util.*;

@RestController
public class LinkController {
    private final CrawlingService crawlingService;
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public LinkController(CrawlingService crawlingService, UserService userService, AuthService authService) {
        this.crawlingService = crawlingService;
        this.userService = userService;
        this.authService = authService;
    }

    private int getUserId() {
        return userService.getIdByLogin(authService.getAuthInfo().getPrincipal().toString());
    }

    @GetMapping(path="/api/getLinks", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> getLinks() {
        int userId = getUserId();
        return new ResponseEntity<>(new LinksResponse(true, null, crawlingService.getLinks(userId)), HttpStatus.OK);
    }

    @PostMapping(path="/api/addLink", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> saveLink(@RequestParam String url, @RequestParam String name, @RequestParam int isSelected) {
        int userId = getUserId();
        boolean result = crawlingService.saveLink(new Link(url, name, isSelected, userId));
        LinksResponse linksResponse = new LinksResponse(result, result ? null : "Ссылка уже добавлена", crawlingService.getLinks(userId));
        return new ResponseEntity<>(linksResponse, result ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @PostMapping(path="/api/updateLinks", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> updateLinks(@RequestBody String body) throws IOException {
        Map<String, Object> tmp = new ObjectMapper().readValue(body, HashMap.class);
        Map<String, Integer> links  = new ObjectMapper().readValue(tmp.get("data").toString(), HashMap.class);
        System.out.println(links);
        int userId = getUserId();
        crawlingService.updateLinks(links, userId);
        return new ResponseEntity<>(new LinksResponse(true, null, null),  HttpStatus.OK);
    }

    @PostMapping(path="/api/updateLink", produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
    public ResponseEntity<LinksResponse> updateLink(@RequestParam String url, @RequestParam Integer isSelected) {
        int userId = getUserId();
        crawlingService.updateLink(url, isSelected, userId);
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
