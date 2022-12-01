package pet.skillbox.sitesearchengine.model.thread;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pet.skillbox.sitesearchengine.controller.crawling.SearchSystem;
import pet.skillbox.sitesearchengine.model.Lemma;
import pet.skillbox.sitesearchengine.model.Page;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.model.response.Data;
import pet.skillbox.sitesearchengine.repositories.DBConnection;

import java.util.List;
import java.util.concurrent.Callable;

public class SearchThread implements Callable<Data> {
    private final Page page;
    private final double value;
    private final SearchSystem searchSystem;
    private final double max;
    private final List<Lemma> requestLemmas;

    public SearchThread(Page page, double value, SearchSystem searchSystem, double max, List<Lemma> requestLemmas) {
        this.page = page;
        this.value = value;
        this.searchSystem = searchSystem;
        this.max = max;
        this.requestLemmas = requestLemmas;
    }

    @Override
    public Data call() throws Exception {
        Document d = Jsoup.parse(page.getContent());
        String title = d.select("title").text();
        String contentWithoutTags = title + " " + d.select("body").text();
        Site site = DBConnection.getSiteById(page.getSite().getId());
        return new Data(site.getUrl(), site.getName(), page.getPath(), title,
                searchSystem.getSnippetFirstStep(requestLemmas, contentWithoutTags),
                value/max);
    }
}
