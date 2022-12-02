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
import pet.skillbox.sitesearchengine.model.thread.CrawlingThread;
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

    private Site site;
    @Getter
    private final Logger rootLogger;
    private List<Field> fieldList;
    private static volatile int pageId;
    @Getter
    @Setter
    private String lastError;
    private Map<String, Page> allLinks;
    private final Config config;
    private final CrawlingService crawlingService;

    private static synchronized int getPageId() {
        return pageId++;
    }

    @Autowired
    public CrawlingSystem(Config config, CrawlingService crawlingService) {
        this.config = config;
        this.crawlingService = crawlingService;
        rootLogger = LogManager.getRootLogger();
    }

    public CrawlingSystem(Config config, CrawlingService crawlingService, Site site) {
        this.config = config;
        this.crawlingService = crawlingService;
        rootLogger = LogManager.getRootLogger();
        fieldList = crawlingService.gelAllFields();
        site.setId(crawlingService.updateStatus(site));
        this.site = site;
    }

    private Collection<List<String>> parsing() {
        long mm = System.currentTimeMillis();
        CopyOnWriteArraySet<String> links = new CopyOnWriteArraySet<>();
        Map<String, Page> allLinksMap = Collections.synchronizedMap(new HashMap<>());
        LinksGenerationSystem linksGenerator = new LinksGenerationSystem(site.getUrl(), site.getUrl(), links, allLinksMap, config);
        new ForkJoinPool().invoke(linksGenerator);

        if (config.isStopIndexing()) {
            rootLogger.info("Остановили - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
            return null;
        }
        allLinks = new HashMap<>(linksGenerator.getAllLinksMap());
        System.out.println("Все найденные ссылки");
        System.out.println(allLinks);
        if (allLinks.keySet().isEmpty()) {
            lastError = "Главная странница сайта недоступна";
            return null;
        }
        System.out.println((double)(System.currentTimeMillis() - mm) / 60000 + " min.");
        System.out.println("Индексация...");
        return chunked(allLinks.keySet().stream(), allLinks.keySet().size() / 50).values();
    }

    public void start(Config config, int id) {
        if (config.isStopIndexing()) {
            return;
        }
        pageId = id;
        System.out.println(site.getUrl() + " pageId = " + pageId);
        rootLogger.info("Новый запуск - " + site.getUrl());
        System.out.println("Парсинг...");
        Collection<List<String>> chunked = parsing();
        if (chunked == null || chunked.isEmpty()) {
            lastError = "Главная странница сайта недоступна";
            return;
        }

        rootLogger.info("На сайте " + site.getUrl() + " Кол-во ссылок: " + allLinks.keySet().size());
        List<CrawlingThread> threadList = new ArrayList<>();
        int finalSiteId = site.getId();
        chunked.forEach(c -> threadList.add(new CrawlingThread(c, this, finalSiteId, crawlingService)));
        threadList.forEach(Thread::start);
        threadList.forEach(t -> {
            try {
                t.join();
                if (config.isStopIndexing()) {
                    t.interrupt();
                }
            } catch (InterruptedException e) {
                lastError = e.getMessage();
                throw new RuntimeException(e);
            }
        });
    }

    public <T> Map<Object, List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);
        return stream.collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize));
    }

    public void appendPageInDB(String path, Builder builder){
        if (config.isStopIndexing()) {
            return;
        }
        Page tmpPage = allLinks.get(path);
        Page page = new Page(tmpPage.getPath(), tmpPage.getCode(), tmpPage.getContent());
        Site tmpSite = new Site(site.getId(), site.getStatus(), site.getStatusTime(),
                site.getLastError(), site.getUrl(), site.getName());

        page.setSite(tmpSite);
        int id = crawlingService.savePage(page);
        page.setId(id);
        System.out.println("id");
        System.out.println(site.getUrl() + path + " id = " + id);
        try {
//            updatePageDB(builder, page);
            if (page.getCode() != 200) {
                return;
            }
            parseTagsContent(page.getContent(), page.getId(), builder);
        } catch (SQLException e) {
            rootLogger.error(e.getMessage());
        }
    }

    private void parseTagsContent(String content, Integer pageId, Builder builder) throws SQLException {
        Document d = Jsoup.parse(content);
        Set<String> allWords = new HashSet<>();

        for (Field field : fieldList) {
            if (config.isStopIndexing()) {
                return;
            }
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
            if (!tagNormalForms.isEmpty()) {
                updateIndexDB(builder, field, tagNormalForms, pageId);
            }
        }
        if (!allWords.isEmpty()) {
            updateLemmaDB(builder, allWords);
        }
    }

//    private void updatePageDB(Builder builder, Page page) throws SQLException {
//        if (builder.getPageBuilder().length() > 2000000) {
//            DBConnection.insertAllPages(builder.getPageBuilder().toString());
//            builder.setPageBuilder(new StringBuilder());
//        }
//        builder.setPageBuilder(builder.getPageBuilder()
//                .append(builder.getPageBuilder().length() == 0 ? "" : ",")
//                .append("(").append(page.getId()).append(",'").append(page.getPath()).append("', ")
//                .append(page.getCode()).append(", '").append(page.getContent())
//                .append("', ").append(site.getId()).append(")"));
//    }

    private void updateIndexDB(Builder builder, Field field, Map<String, Integer> tagLemmas, int id) throws SQLException {
        if (builder.getIndexBuilder().length() > 2000000) {
            DBConnection.insertAllIndexes(builder.getIndexBuilder().toString());
            builder.setIndexBuilder(new StringBuilder());
        }
        tagLemmas.keySet().forEach(lemma ->
                builder.setIndexBuilder(builder.getIndexBuilder()
                        .append(builder.getIndexBuilder().length() == 0 ? "" : ",")
                        .append("(").append(id)
                        .append(", '").append(lemma).append("', ")
                        .append(tagLemmas.get(lemma) * field.getWeight()).append(", ")
                        .append(site.getId()).append(")")));
    }

    private void updateLemmaDB(Builder builder, Set<String> normalFormsSet) throws SQLException {
        if (builder.getLemmaBuilder().length() > 2000000) {
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