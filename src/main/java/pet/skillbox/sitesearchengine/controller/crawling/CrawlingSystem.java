package pet.skillbox.sitesearchengine.controller.crawling;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pet.skillbox.sitesearchengine.model.Builder;
import pet.skillbox.sitesearchengine.model.Field;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.services.MorphologyServiceImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrawlingSystem {

    public Site site;
    private final Logger rootLogger;
    private final List<Field> fieldList;
    private volatile int pageId;
    @Getter
    private String lastError;

    public synchronized int getPageId() {
        return pageId++;
    }

    public CrawlingSystem(Site site) throws SQLException {
        rootLogger = LogManager.getRootLogger();
        this.site = site;
        pageId = DBConnection.getMaxPageId();
        fieldList = DBConnection.getAllFields();
    }

    public void start() throws SQLException {
        rootLogger.info("\n\nNew launch - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        DBConnection.insertSite(site.toString());
        System.out.println("Parsing ");
        new ForkJoinPool().invoke(new SiteLinksGenerator(site.getUrl(), site.getUrl()));
        writeSitemapUrl();
    }

    public void writeSitemapUrl() {

        Collection<List<String>> chunked =
                chunked(SiteLinksGenerator.allLinks.stream(), SiteLinksGenerator.allLinks.size()/ 100).values();


        System.out.println("Кол-во ссылок: " + SiteLinksGenerator.allLinks.size());
        List<Thread> threadList = new ArrayList<>();
        chunked.forEach(list -> System.out.println(list.size()));
        chunked.forEach(c -> threadList.add(new Thread(() -> {
            System.out.println("Размер в потоке " + Thread.currentThread().getName() + ": " + c.size());
            long p = System.currentTimeMillis();
            Builder builder = new Builder();
            for (String page : c) {
                try {
                    appendStringInDB(page, builder);
                } catch (IOException | InterruptedException | SQLException e) {
                    lastError = e.getMessage();
                    rootLogger.debug("Ошибка - " + page + " - " + e);
                }
            }
            System.out.print(Thread.currentThread().getName() + "\t");
            System.out.println("total " + (double) (System.currentTimeMillis() - p) + " ");
            try {
                System.out.println(Thread.currentThread().getName() + "\tДобавляем в бд" );
                DBConnection.insert(builder);
            } catch (SQLException e) {
                lastError = e.getMessage();
                rootLogger.debug("Ошибка - " + e.getMessage());
            }
            System.out.println(Thread.currentThread().getName() + " " + (double) (System.currentTimeMillis() - p)
                    + " " + LocalDateTime.now() + "\tДобавили" );
        })));
        threadList.forEach(Thread::start);
    }

    public <T> Map<Object, List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);
        return stream.collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize));
    }

    public void appendStringInDB(String data, Builder builder) throws InterruptedException, IOException, SQLException {
        int idPathBegin = site.getUrl().indexOf(data) + site.getUrl().length();
        String path = site.getUrl().equals(data) ?  "/" : data.substring(idPathBegin);
        String content = "";
        Connection connection = connectPath(data);
        int code = connection.execute().statusCode();
        if (code == 200) {
            content = connection.get().toString().replace("'", "\\'");
        }
        rootLogger.info("Ссылка - " + path);
        if (builder.getPageBuilder().length() > 4000000) {
            DBConnection.insertAllPages(builder.getPageBuilder().toString());
            builder.setPageBuilder(new StringBuilder());
        }
        int id = getPageId();
        builder.setPageBuilder(builder.getPageBuilder().append(builder.getPageBuilder().length() == 0 ? "" : ",")
                .append("(").append(id).append(",'").append(path).append("', ")
                .append(code).append(", '").append(content)
                .append("', ").append(site.getId()).append(")"));

        if (code != 200) {
            return;
        }
        appendTagInDB(connection.get(), id, builder);
    }

    private Connection connectPath(String path) {
        return Jsoup.connect(path)
                .userAgent("DuckSearchBot")
                .referrer("https://www.google.com");
    }

    private void appendTagInDB(Document document, Integer pageId, Builder builder) throws IOException, SQLException {
        StringBuilder tmp = new StringBuilder();

         for (Field field : fieldList) {
             String tagContent = document.select(field.getName()).text();
             tmp.append(tagContent);
             Map<String, Integer> t;
             try {
                 t = new MorphologyServiceImpl().getNormalFormsList(tagContent);
             } catch (IOException e) {
                 rootLogger.debug("Ошибка лемантизатора - " + e.getMessage());
                 lastError = e.getMessage();
                 System.out.println(e.getMessage());
                 throw new RuntimeException(e.getMessage());
             }
             if (builder.getIndexBuilder().length() > 4000000) {
                 System.out.println("Index " + builder.getIndexBuilder().length());
                 DBConnection.insertAllIndexes(builder.getIndexBuilder().toString());
                 builder.setIndexBuilder(new StringBuilder());
             }
             t.keySet().forEach(s ->
                    builder.setIndexBuilder(builder.getIndexBuilder().append(builder.getIndexBuilder().length() == 0 ? "" : ",")
                            .append("(").append(pageId)
                            .append(", '").append(s).append("', ").append(t.get(s) * field.getWeight()).append(")")));
        }

        updateLemmaDB(new MorphologyServiceImpl().getNormalFormsList(tmp.toString()), builder);
    }

    private void updateLemmaDB(Map<String, Integer> normalFormsMap, Builder builder) throws SQLException {
        if (builder.getLemmaBuilder().length() > 4000000) {
            System.out.println("Lemma " + builder.getLemmaBuilder().length());
            DBConnection.insertAllLemmas(builder.getLemmaBuilder().toString());
            builder.setLemmaBuilder(new StringBuilder());
        }
        normalFormsMap.keySet().forEach(key ->
                builder.setLemmaBuilder(builder.getLemmaBuilder().append(builder.getLemmaBuilder().length() == 0 ? "" : ",")
                .append("('").append(key)
                .append("', 1, ").append(site.getId()).append(")")));
    }
}