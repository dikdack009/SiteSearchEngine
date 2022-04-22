package crawling;

import org.hibernate.dialect.Database;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static java.lang.Thread.sleep;

public class SiteMapNodeRecursiveTask extends RecursiveAction {
    private final SiteMapNode url;
    private final SiteMapNode rootUrl;
    private static final CopyOnWriteArraySet<String> allLinks = new CopyOnWriteArraySet<>();

    public SiteMapNodeRecursiveTask(SiteMapNode url, SiteMapNode rootUrl) {
        this.url = url;
        this.rootUrl = rootUrl;
    }

    @Override
    protected void compute() {
        Set<SiteMapNodeRecursiveTask> taskList = new HashSet<>();
        try {
            sleep(500);
            Document doc = Jsoup.connect(url.getUrl()).timeout(100000).get();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absUrl = link.attr("abs:href");
                if (isCorrected(absUrl)) {
                    url.addSubLinks(new SiteMapNode(absUrl));
                    allLinks.add(absUrl);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        for (SiteMapNode link : url.getSubLinks()) {
            SiteMapNodeRecursiveTask task = new SiteMapNodeRecursiveTask(link, rootUrl);
            task.fork();
            taskList.add(task);
        }



        taskList.forEach(ForkJoinTask::join);
    }

    private boolean isCorrected(String url) {
        return (!url.isEmpty() && url.startsWith(rootUrl.getUrl())
                && !allLinks.contains(url) && !url.contains("#")
                && !url.matches("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf))$)"));
    }

}
