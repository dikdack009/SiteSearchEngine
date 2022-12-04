package pet.skillbox.sitesearchengine.controller.crawling;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pet.skillbox.sitesearchengine.configuration.Config;
import pet.skillbox.sitesearchengine.model.Page;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

public class LinksGenerationSystem extends RecursiveAction {
    private final String rootUrl;
    private final String url;
    private final CopyOnWriteArraySet<String> allLinks;
    @Getter
    private final Map<String, Page> allLinksMap;
    private final Config config;
    @Getter
    private String error;

    public LinksGenerationSystem(String rootUrl, String url,
                                 CopyOnWriteArraySet<String> allLinks, Map<String, Page> allLinksMap, Config config) {
        this.rootUrl = rootUrl;
        this.url = url;
        this.allLinks = allLinks;
        this.allLinksMap = allLinksMap;
        this.config = config;
        error = null;
    }

    @Override
    protected void compute() {
        if (allLinks.size() > 40_000 || config.isStopIndexing()) {
            return;
        }
        Set<LinksGenerationSystem> taskList = new HashSet<>();
        List<String> list = new ArrayList<>();
        try {
            sleep(1000);
            Connection connection = connectPath(url);
            int statusCode = connection.execute().statusCode();
            int idPathBegin = rootUrl.length();
            String path = rootUrl.equals(url)  ?  "/" : url.substring(idPathBegin);
            Page page;
            boolean htmlTest = Objects.requireNonNull(connection.response().contentType()).startsWith("text/html");
            if (statusCode == 200 && htmlTest) {
                page = new Page(path, statusCode, connection.get().toString().replace("'", "\""));
                Elements links = connection.get().select("a[href]");
                for (Element link : links) {
                    String absUrl = link.attr("abs:href").trim();
                    absUrl = absUrl.startsWith("/") ? rootUrl + absUrl : absUrl;
                    absUrl = absUrl.endsWith("/") ? absUrl.substring(0, absUrl.length() - 1) : absUrl;
                    if (isCorrected(absUrl)) {
                        list.add(absUrl);
                        allLinks.add(absUrl);
                    }
                }
            }
            else {
                page = new Page(path, statusCode, "");
            }
            allLinksMap.put(path, page);
        } catch (Exception e) {
            return;
        }

        for (String link : list) {
            LinksGenerationSystem task = new LinksGenerationSystem(rootUrl, link, allLinks, allLinksMap, config);
            task.fork();
            taskList.add(task);
        }
        taskList.forEach(ForkJoinTask::join);
    }

    private Connection connectPath(String path) {
        return Jsoup.connect(path)
                .userAgent("DuckSearchBot")
                .referrer("https://www.google.com")
                .ignoreContentType(true)
                .ignoreHttpErrors(true);
    }

    private boolean isCorrected(String url) {
        return !url.isEmpty()
                && !url.equals(rootUrl)
                && url.startsWith(rootUrl)
                && !allLinks.contains(url)
                && !url.contains("#")
                && !url.matches("(\\S+(\\.(?i)(jpg|png|gif|bmp|pdf|xml))$)")
                && !url.matches("#([\\w\\-]+)?$")
                && !url.contains("?method=")
                && !url.startsWith("https://www.google.com")
                && !url.startsWith("https://dzen.ru")
                && !url.startsWith("https://go.mail.ru")
                && !url.startsWith("https://www.bing.com")
                && !url.startsWith("https://ya.ru");
    }
}
