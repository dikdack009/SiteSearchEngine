package pet.diploma.sitesearchengine.model.thread;

import org.apache.logging.log4j.LogManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.model.response.Data;
import pet.diploma.sitesearchengine.controller.crawling.SearchSystem;
import pet.diploma.sitesearchengine.model.Lemma;
import pet.diploma.sitesearchengine.model.Page;
import pet.diploma.sitesearchengine.services.CrawlingService;

import java.util.List;
import java.util.concurrent.Callable;

public class SearchThread implements Callable<Data> {
    private final Integer pageId;
    private final double value;
    private final SearchSystem searchSystem;
    private final double max;
    private final List<Lemma> requestLemmas;
    private final CrawlingService crawlingService;

    public SearchThread(Integer pageId, double value, SearchSystem searchSystem, double max, List<Lemma> requestLemmas, CrawlingService crawlingService) {
        this.pageId = pageId;
        this.value = value;
        this.searchSystem = searchSystem;
        this.max = max;
        this.requestLemmas = requestLemmas;
        this.crawlingService = crawlingService;
    }

    @Override
    public Data call() throws Exception {
        System.out.println(pageId);
        Page page = crawlingService.getPageById(pageId);
        Document d = Jsoup.parse(page.getContent());
        String title = d.select("title").text();
        String contentWithoutTags = title + " " + d.select("body").text();
        Site site = page.getSite();
        long m = System.currentTimeMillis();
        String snippet = searchSystem.getSnippetFirstStep(requestLemmas, contentWithoutTags);
        LogManager.getLogger("search").info("Время получения сниппета: " + (double)(System.currentTimeMillis() - m) / 1000 + " сек.");
        return new Data(site.getUrl(), site.getName(), page.getPath(), title, snippet, value/max);
    }
}
