package pet.skillbox.sitesearchengine.controller.crawling;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

public class SiteLinksGenerator extends RecursiveAction {
    private final String rootUrl;
    private final String url;
    public static final CopyOnWriteArraySet<String> allLinks = new CopyOnWriteArraySet<>();

    public SiteLinksGenerator(String url, String rootUrl) {
        this.url = url;
        this.rootUrl = rootUrl;
        allLinks.add(rootUrl);
    }

    @Override
    protected void compute() {
        Set<SiteLinksGenerator> taskList = new HashSet<>();
        List<String> list = new ArrayList<>();
        try {
            sleep(400);
            Connection connection = connectPath(url);
            if(connection.execute().statusCode() != 200){
                System.out.println(url);
            }
            Elements links = connection.get().select("a[href]");
            for (Element link : links) {
                String absUrl = link.attr("abs:href").replace("\\/", "/").trim();
                if (isCorrected(absUrl)) {
                    list.add(absUrl);
                    allLinks.add(absUrl);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }

        for (String link : list) {
            SiteLinksGenerator task = new SiteLinksGenerator(link, rootUrl);
            task.fork();
            taskList.add(task);
        }
        taskList.forEach(ForkJoinTask::join);
    }

    private Connection connectPath(String path) {
        return Jsoup.connect(path)
                .userAgent("DuckSearchBot")
                .referrer("https://www.google.com");
    }

    private boolean isCorrected(String url) {
        return !url.isEmpty()
                && url.startsWith(rootUrl)
                && !allLinks.contains(url)
                && !url.contains("#")
                && !url.matches("(\\S+(\\.(?i)(jpg|png|gif|bmp|pdf|xml))$)")
                && !url.matches("#([\\w\\-]+)?$")
                && !url.contains("?method=");
    }
}
