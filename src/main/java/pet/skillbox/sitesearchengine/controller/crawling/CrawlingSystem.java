package pet.skillbox.sitesearchengine.controller.crawling;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pet.skillbox.sitesearchengine.configuration.Config;
import pet.skillbox.sitesearchengine.model.*;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.services.CrawlingService;
import pet.skillbox.sitesearchengine.services.MorphologyServiceImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CrawlingSystem {

    public Site site;
    @Getter
    private final Logger rootLogger;
    private List<Field> fieldList;
    private volatile int pageId;
    @Getter
    @Setter
    private String lastError;
    private Map<String, Page> allLinks;
    private final Config config;
    private final CrawlingService crawlingService;

    private synchronized int getPageId() {
        return pageId++;
    }

    @Autowired
    public CrawlingSystem(Config config, CrawlingService crawlingService) {
        this.config = config;
        this.crawlingService = crawlingService;
        rootLogger = LogManager.getRootLogger();
    }

    public void start(Config config, String url, String name) throws SQLException, InterruptedException {
        Site site = new Site(Status.INDEXING, LocalDateTime.now(), null, url, name);
        this.site = site;
        if (config.isStopIndexing()) {
            System.out.println(config.isStopIndexing());
            return;
        }
//        int siteId = DBConnection.getSiteIdByPath(site.getUrl());
        int siteId = crawlingService.getSiteIdByUrl(url);
        System.out.println(siteId);
        if (siteId > 0) {
            rootLogger.info("Переиндексация - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            DBConnection.updateSite(site.getUrl(), Status.INDEXING.toString(), null);
//            crawlingService.updateStatus(site.getUrl(), Status.INDEXING.toString(), null);

        } else {
            System.out.println("AWESOME");
//            crawlingService.addSite(site);
            DBConnection.insertSite(site.toString());
        }
        site.setId(siteId);
        System.out.println("hell12");
        pageId = DBConnection.getMaxPageId() + 1;
//        fieldList = DBConnection.getAllFields();
        System.out.println("hell");
        fieldList = crawlingService.gelAllFields();
        System.out.println(pageId + " " + fieldList);
        rootLogger.info("New launch - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        System.out.println("Parsing...");
        long mm = System.currentTimeMillis();

        CopyOnWriteArraySet<String> links = new CopyOnWriteArraySet<>();
        Map<String, Page> allLinksMap = Collections.synchronizedMap(new HashMap<>());
        SiteLinksGenerator linksGenerator = new SiteLinksGenerator(site.getUrl(), site.getUrl(), links, allLinksMap, config);

        try {
            new ForkJoinPool().invoke(linksGenerator);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            lastError = e.getMessage().substring(e.getMessage().indexOf(":") + 2);
            throw new RuntimeException(e.getMessage());
        }
        if (config.isStopIndexing()) {
            System.out.println(config.isStopIndexing());
            return;
        }
        allLinks = new HashMap<>(linksGenerator.getAllLinksMap());
        allLinks.keySet().forEach(System.out::println);
        if (allLinks.isEmpty()) {
            System.out.println("пусто");
            setLastError("Главная странница сайта недоступна");
            return;
        }
        System.out.println((double)(System.currentTimeMillis() - mm) / 1000 + " sec.");
        System.out.println((double)(System.currentTimeMillis() - mm) / 60000 + " min.");
        System.out.println("Indexing...");
        Collection<List<String>> chunked =
                chunked(allLinks.keySet().stream(), allLinks.keySet().size() / 40).values();

        rootLogger.info("Кол-во ссылок: " + allLinks.keySet().size());
        List<CrawlingThread> threadList = new ArrayList<>();
        chunked.forEach(c -> threadList.add(new CrawlingThread(c, this, siteId, crawlingService)));
        threadList.forEach(Thread::start);
        threadList.forEach(t -> {
            try {
                t.join();
                if (config.isStopIndexing()) {
                    t.interrupt();
                }
            } catch (InterruptedException e) {
                lastError= e.getMessage();
                throw new RuntimeException(e);
            }
        });
    }

    public <T> Map<Object, List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);
        return stream.collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize));
    }

    public void appendPageInDB(String path, Builder builder){
        int id = getPageId();
        Page page = allLinks.get(path);
        page.setId(id);
        try {
            updatePageDB(builder, page);
            if (allLinks.get(path).getCode() != 200) {
                return;
            }
            parseTagsContent(page.getContent(), id, builder);
        } catch (SQLException e) {
            rootLogger.error(e.getMessage());
        }
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
        if (builder.getPageBuilder().length() > 3000000) {
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
        if (builder.getIndexBuilder().length() > 3000000) {
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
        if (builder.getLemmaBuilder().length() > 3000000) {
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