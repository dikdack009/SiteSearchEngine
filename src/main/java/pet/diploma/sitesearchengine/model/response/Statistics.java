package pet.diploma.sitesearchengine.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.services.CrawlingService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Statistics {
    private final Total total;
    private final List<DetailedSite> detailed;

    public Statistics(boolean isIndexing, int userId, CrawlingService crawlingService) {
        List<Site> siteList = crawlingService.getSitesByUserAndDelete(userId);
        int pages = 0;
        int lemmas = 0;
        List<DetailedSite> stat = new ArrayList<>();
        for (Site site : siteList) {
            int currentPages = crawlingService.countPagesByIsDeletedAndSite(site);
            int currentLemmas = crawlingService.countLemmasByIsDeletedAndSite(site);
            DetailedSite d = new DetailedSite(site.getUrl(), site.getName(), site.getStatus().toString(), Timestamp.valueOf(site.getStatusTime()).getTime(),
                    site.getLastError(), currentPages, currentLemmas);
            stat.add(d);
            pages += currentPages;
            lemmas += currentLemmas;
        }
        detailed = stat;
        total = new Total(siteList.size(), pages, lemmas, isIndexing);
    }
}
