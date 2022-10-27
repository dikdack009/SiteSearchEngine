package pet.skillbox.sitesearchengine.controller.crawling;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pet.skillbox.sitesearchengine.model.Page;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class SiteLinksGenerator extends RecursiveAction {
    private final String rootUrl;
    private final String url;
    public static final CopyOnWriteArraySet<String> allLinks = new CopyOnWriteArraySet<>();

    public static final Map<String, Page> allLinksMap = Collections.synchronizedMap(new HashMap<>());

    public SiteLinksGenerator(String url, String rootUrl) {
        this.url = url;
        this.rootUrl = rootUrl;
        allLinks.add(rootUrl);
    }

    @Override
    protected void compute() {
        if (allLinks.size() > 40_000) {
            return;
        }
        Set<SiteLinksGenerator> taskList = new HashSet<>();
        List<String> list = new ArrayList<>();
        try {
            Connection connection = connectPath(url);
//            System.out.println(url);
            int statusCode = connection.execute().statusCode();
            int idPathBegin = rootUrl.indexOf(url) + rootUrl.length();
            String path = rootUrl.equals(url) ?  "/" : url.substring(idPathBegin);
            Page page;
            boolean htmlTest = Objects.requireNonNull(connection.response().contentType()).startsWith("text/html");
//            System.out.println(htmlTest + " " + isCorrected(url));
//            System.out.println(url.matches("#([\\w\\-]+)?$"));
            if (statusCode == 200 && htmlTest) {
                page = new Page(path, statusCode, connection.get().toString().replace("'", "\\'"));
                Elements links = connection.get().select("a[href]");
                for (Element link : links) {
                    String absUrl = link.attr("abs:href").replace("\\/", "/").trim();
                    if (isCorrected(absUrl)) {
                        list.add(absUrl);
                        allLinks.add(absUrl);
                    }
                }
            }
            else {
                page = new Page(path, statusCode, "");
                System.out.println(url + " " + statusCode);
            }
            allLinksMap.put(path, page);
        } catch (IOException e) {
            e.printStackTrace();
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
                .referrer("https://www.google.com")
                .ignoreContentType(true)
                .ignoreHttpErrors(true);
    }

    private boolean isCorrected(String url) {
        return !url.isEmpty()
                && url.startsWith(rootUrl)
                && !allLinks.contains(url)
                && !url.contains("#")
                && !url.matches("(\\S+(\\.(?i)(jpg|png|gif|bmp|pdf|xml))$)")
                && !url.matches("#([\\w\\-]+)?$")
                && !url.contains("?method=")
                && !url.startsWith("https://www.google.com/")
                && !url.startsWith("https://dzen.ru/")
                && !url.startsWith("https://go.mail.ru/")
                && !url.startsWith("https://www.bing.com/")
                && !url.startsWith("https://ya.ru/");
    }
}
