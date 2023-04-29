package pet.diploma.sitesearchengine.controller.crawling;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pet.diploma.sitesearchengine.model.Builder;
import pet.diploma.sitesearchengine.model.Field;
import pet.diploma.sitesearchengine.model.Page;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.model.thread.CrawlingThread;
import pet.diploma.sitesearchengine.repositories.DBConnection;
import pet.diploma.sitesearchengine.services.CrawlingService;
import pet.diploma.sitesearchengine.services.MorphologyServiceImpl;
import pet.diploma.sitesearchengine.configuration.Config;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Component
public class CrawlingSystem {

    private final Site site;
    @Getter
    private final Logger rootLogger;
    private List<Field> fieldList;
    @Getter
    @Setter
    private String lastError;
    private Map<String, Page> allLinks;
    private final Config config;
    private final CrawlingService crawlingService;
    private int userId;


    @Autowired
    public CrawlingSystem(Config config, CrawlingService crawlingService) {
        this.site = new Site();
        this.config = config;
        this.crawlingService = crawlingService;
        rootLogger = LogManager.getLogger("index");
    }

    public CrawlingSystem(CrawlingSystem newCrawlingSystem, int userId) {
        this.site = newCrawlingSystem.getSite();
        this.rootLogger = newCrawlingSystem.getRootLogger();
        this.config = newCrawlingSystem.getConfig();
        this.crawlingService = newCrawlingSystem.getCrawlingService();
        this.allLinks = newCrawlingSystem.getAllLinks();
        this.fieldList = newCrawlingSystem.getFieldList();
        this.lastError = newCrawlingSystem.getLastError();
        this.userId = userId;
    }

    public CrawlingSystem(Config config, CrawlingService crawlingService, Site site, int userId) {
        this.config = config;
        this.crawlingService = crawlingService;
        this.userId = userId;
        rootLogger = LogManager.getLogger("index");
        fieldList = crawlingService.gelAllFields();
        site.setId(crawlingService.updateStatus(site));
        this.site = site;
    }

    private Collection<List<String>> parsing(String email) {
        CopyOnWriteArraySet<String> links = new CopyOnWriteArraySet<>();
        Map<String, Page> allLinksMap = Collections.synchronizedMap(new HashMap<>());
        LinksGenerationSystem linksGenerator = new LinksGenerationSystem(site.getUrl(), site.getUrl(), links, allLinksMap, config, userId);
        new ForkJoinPool().invoke(linksGenerator);

        if (config.getStopIndexing().get(userId)) {
            rootLogger.info(email + ":\tОстановили индексацию" + " - " + site.getUrl() );
            return null;
        }
        allLinks = new HashMap<>(linksGenerator.getAllLinksMap());
        if (allLinks.keySet().isEmpty()) {
            lastError = "Главная странница сайта недоступна";
            return null;
        }
        rootLogger.info(email + ":\tИндексация..." + " - " + site.getUrl());
        return chunked(allLinks.keySet().stream(), allLinks.keySet().size() / 50 == 0 ? allLinks.keySet().size() : allLinks.keySet().size() / 50).values();
    }

    public void start(String email, Config config) {
        if (config.getStopIndexing().get(userId)) {
            return;
        }
        rootLogger.info(email + ":\tНовый запуск индексации сайта с id = " + site.getId() + " - " + site.getUrl());
        rootLogger.info(email + ":\tПарсинг..." + " - " + site.getUrl());
        Collection<List<String>> chunked = parsing(email);
        if (chunked == null || chunked.isEmpty()) {
            lastError = "Главная странница сайта недоступна";
            return;
        }
        rootLogger.info(email + ":\tНа сайте " + site.getUrl() + " Кол-во ссылок: " + allLinks.keySet().size());
        List<CrawlingThread> threadList = new ArrayList<>();
        int finalSiteId = site.getId();
        chunked.forEach(c -> threadList.add(new CrawlingThread(c, this, finalSiteId, userId)));
        threadList.forEach(Thread::start);
        threadList.forEach(t -> {
            try {
                t.join();
                if (config.getStopIndexing().get(userId)) {
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
        if (config.getStopIndexing().get(userId)) {
            return;
        }
        Page tmpPage = allLinks.get(path);
        Page page = new Page(tmpPage.getPath(), tmpPage.getCode(), tmpPage.getContent());
        Site tmpSite = new Site(site.getId(), site.getStatus(), site.getStatusTime(),
                site.getLastError(), site.getUrl(), site.getName(), 0, 0);

        page.setSite(tmpSite);
        int siteId = site.getId();
        crawlingService.savePage(page);
        try {
            if (page.getCode() != 200) {
                return;
            }
            parseTagsContent(page.getContent(), page.getId(), builder, siteId);
        } catch (SQLException e) {
            rootLogger.error(e.getMessage());
        }
    }

    private void parseTagsContent(String content, Integer pageId, Builder builder, int siteId) throws SQLException {
        Document d = Jsoup.parse(content);
        Set<String> allWords = new HashSet<>();

        for (Field field : fieldList) {
            if (config.getStopIndexing().get(userId)) {
                return;
            }
            String tagContent = d.select(field.getName()).text();
            Map<String, Integer> tagNormalForms;
            try {
                tagNormalForms = new MorphologyServiceImpl().getNormalFormsList(tagContent);
                allWords.addAll(tagNormalForms.keySet());
            } catch (IOException e) {
                rootLogger.debug("Ошибка лемантизатора " + " - " + site.getUrl() + " - " + e.getMessage());
                lastError = e.getMessage();
                return;
            }
            if (!tagNormalForms.isEmpty()) {
                updateIndexDB(builder, field, tagNormalForms, pageId, siteId);
            }
        }
        if (!allWords.isEmpty()) {
            updateLemmaDB(builder, allWords, siteId);
        }
    }

    private void updateIndexDB(Builder builder, Field field, Map<String, Integer> tagLemmas, int id, int siteId) throws SQLException {
        if (builder.getIndexBuilder().length() > 1000000) {
            DBConnection.insertAllIndexes(builder.getIndexBuilder().toString());
            builder.setIndexBuilder(new StringBuilder());
        }
        tagLemmas.keySet().forEach(lemma -> builder.setIndexBuilder(builder.getIndexBuilder()
                        .append(builder.getIndexBuilder().length() == 0 ? "" : ",")
                        .append("(").append(id).append(", '").append(lemma).append("', ")
                        .append(tagLemmas.get(lemma) * field.getWeight()).append(", ")
                        .append(siteId).append(", 0)")));
    }

    private void updateLemmaDB(Builder builder, Set<String> normalFormsSet, int siteId) throws SQLException {
        if (builder.getLemmaBuilder().length() > 1000000) {
            DBConnection.insertAllLemmas(builder.getLemmaBuilder().toString());
            builder.setLemmaBuilder(new StringBuilder());
        }
        normalFormsSet.forEach(word -> builder.setLemmaBuilder(builder.getLemmaBuilder()
                        .append(builder.getLemmaBuilder().length() == 0 ? "" : ",")
                        .append("('").append(word).append("', 1, ").append(siteId).append(", 0)")));
    }
}