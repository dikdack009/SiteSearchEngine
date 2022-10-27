package pet.skillbox.sitesearchengine.controller.crawling;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pet.skillbox.sitesearchengine.model.Builder;
import pet.skillbox.sitesearchengine.model.Field;
import pet.skillbox.sitesearchengine.model.Page;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrawlingSystem {

    public Site site;
    private final Logger rootLogger;
    private final List<Field> fieldList;
    private volatile int pageId;
    private volatile int lemmaId;
    @Getter
    private String lastError;
    private Map<String, Page> allLinks;

    private synchronized int getPageId() {
        return pageId++;
    }

    private synchronized int getLemmaId() {
        return lemmaId++;
    }

    public CrawlingSystem(Site site) throws SQLException {
        DBConnection.setCreateTables(true);
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
        allLinks = new HashMap<>(SiteLinksGenerator.allLinksMap);
        writeSiteMapUrl();
    }

    public void writeSiteMapUrl() {

        if (allLinks.size() == 0) {
            return;
        }
        System.out.println(allLinks.size());
        Collection<List<String>> chunked =
                chunked(allLinks.keySet().stream(), allLinks.keySet().size() / 110).values();

        rootLogger.info("Кол-во ссылок: " + allLinks.keySet().size());
        List<Thread> threadList = new ArrayList<>();
        chunked.forEach(list -> System.out.println(list.size()));
        chunked.forEach(c -> threadList.add(new Thread(() -> {
            System.out.println("Размер в потоке " + Thread.currentThread().getName() + ": " + c.size());
            long p = System.currentTimeMillis();
            Builder builder = new Builder();
            for (String page : c) {
                try {
                    appendPageInDB(page, builder);
                } catch (IOException | InterruptedException | SQLException e) {
                    lastError = e.getMessage();
                    System.out.println(lastError);
                    rootLogger.debug("Ошибка - " + page + " - " + e);
                }
            }
            System.out.print(Thread.currentThread().getName() + "\t");
            System.out.println("total " + (double) (System.currentTimeMillis() - p) + " ");

            try {
                System.out.println(Thread.currentThread().getName() + "\tДобавляем в бд");
                DBConnection.insert(builder);
            } catch (SQLException e) {
                lastError = e.getMessage();
                e.printStackTrace();
                rootLogger.debug("Ошибка вставки - " + lastError);
            }
            System.out.println(Thread.currentThread().getName() + " " + (double) (System.currentTimeMillis() - p)
                    + " " + LocalDateTime.now() + "\tДобавили");
            rootLogger.info("\nDone " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        })));
        threadList.forEach(Thread::start);
    }

    public <T> Map<Object, List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);
        return stream.collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize));
    }

    public void appendPageInDB(String path, Builder builder) throws InterruptedException, IOException, SQLException {
        int id = getPageId();
        Page page = allLinks.get(path);
        page.setId(id);
        updatePageDB(builder, page);
        if (allLinks.get(path).getCode() != 200) {
            return;
        }
        parseTagsContent(page.getContent(), id, builder);
    }

    private void parseTagsContent(String content, Integer pageId, Builder builder) throws SQLException {
        Document d = Jsoup.parse(content);
        Set<String> allWords = new HashSet<>();

        for (Field field : fieldList) {
            String tagContent = d.select(field.getName()).text();
            Map<String, Integer> tagNormalForms;
            try {
                tagNormalForms = new MorphologyServiceImpl().getNormalFormsList(tagContent);
                allWords.addAll(tagNormalForms.keySet());
            } catch (IOException e) {
                rootLogger.debug("Ошибка лемантизатора - " + e.getMessage());
                lastError = e.getMessage();
                return;
            }
            updateIndexDB(builder, field, tagNormalForms, pageId);
        }
        updateLemmaDB(builder, allWords);
    }

    private void updatePageDB(Builder builder, Page page) throws SQLException {
        if (builder.getPageBuilder().length() > 4000000) {
            DBConnection.insertAllPages(builder.getPageBuilder().toString());
            builder.setPageBuilder(new StringBuilder());
        }
        builder.setPageBuilder(builder.getPageBuilder()
                .append(builder.getPageBuilder().length() == 0 ? "" : ",")
                .append("(").append(page.getId()).append(",'").append(page.getPath()).append("', ")
                .append(page.getCode()).append(", '").append(page.getContent())
                .append("', ").append(site.getId()).append(")"));
    }

    private void updateIndexDB(Builder builder, Field field, Map<String, Integer> normalFormsMap, int id) throws SQLException {
        if (builder.getIndexBuilder().length() > 4000000) {
            DBConnection.insertAllIndexes(builder.getIndexBuilder().toString());
            builder.setIndexBuilder(new StringBuilder());
        }
        normalFormsMap.keySet().forEach(word ->
                builder.setIndexBuilder(builder.getIndexBuilder()
                        .append(builder.getIndexBuilder().length() == 0 ? "" : ",")
                        .append("(").append(id)
                        .append(", '").append(word).append("', ")
                        .append(normalFormsMap.get(word) * field.getWeight()).append(", ")
                        .append(site.getId()).append(")")));
    }

    private void updateLemmaDB(Builder builder, Set<String> normalFormsSet) throws SQLException {
        if (builder.getLemmaBuilder().length() > 4000000) {
            DBConnection.insertAllLemmas(builder.getLemmaBuilder().toString());
            builder.setLemmaBuilder(new StringBuilder());
        }
        normalFormsSet.forEach(word ->
                builder.setLemmaBuilder(builder.getLemmaBuilder()
                        .append(builder.getLemmaBuilder().length() == 0 ? "" : ",")
                        .append("('").append(word)
                        .append("', 1, ").append(site.getId()).append(")")));
    }
}