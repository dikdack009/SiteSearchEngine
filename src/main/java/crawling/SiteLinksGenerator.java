package crawling;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
            sleep(300);
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                if (isCorrected(absUrl)) {
                    list.add(absUrl);
                    allLinks.add(absUrl);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error " + url);
        }

        for (String link : list) {
            SiteLinksGenerator task = new SiteLinksGenerator(link, rootUrl);
            task.fork();
            taskList.add(task);
        }
        taskList.forEach(ForkJoinTask::join);
    }

    private boolean isCorrected(String url) {
        return (!url.isEmpty()
                && url.startsWith(rootUrl)
                && !allLinks.contains(url) && !url.contains("#")
                && !url.equals(rootUrl)
                && !url.matches("(\\S+(\\.(?i)(jpg|png|gif|bmp|pdf))$)"))
                && !url.matches("#([\\w\\-]+)?$")
                && !url.contains("?method=");
    }

}
