package crawling;

import model.Field;
import model.Index;
import model.Lemma;
import model.Page;
import org.hibernate.Session;
import repository.MySQLConnection;
import lemmatizer.Lemmatizer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.hibernate.Transaction;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;
import static model.Lemma.getLemmaByName;

public class CrawlingSystem {

    public static String webUrl;
    private static Logger rootLogger;
    private static List<Field> fieldList = getFieldList();

    public static void startCrawlingSystem(String url) throws InterruptedException {
        MySQLConnection.fillFieldDB();
        rootLogger = LogManager.getRootLogger();
        webUrl = url;
        fieldList = getFieldList();
        System.out.println("Parsing ");
        new ForkJoinPool().invoke(new SiteLinksGenerator(webUrl,webUrl));
        rootLogger.info("\n\n");
        writeSitemapUrl();
    }
    public static void writeSitemapUrl() throws InterruptedException {
        ProgressBar pb = new ProgressBar("Indexing", SiteLinksGenerator.allLinks.size(), ProgressBarStyle.ASCII).start();
        pb.setExtraMessage("Reading...");

        Collection<List<String>> chunked =
                chunked(SiteLinksGenerator.allLinks.stream(), SiteLinksGenerator.allLinks.size()/ 11 + 1).values();

        sleep(1000);
        chunked.forEach(c -> System.out.println(c.size()));
        System.out.println(SiteLinksGenerator.allLinks.size());
        List<Thread> threadList = new ArrayList<>();
        chunked.forEach(c -> threadList.add(new Thread(() -> {
            long p = System.currentTimeMillis();
            for (String link : c) {
                try {
                    appendStringInDB(link);
                } catch (IOException | InterruptedException e) {
                    rootLogger.debug("Ошибка - " + link + " - " + e);
                }
                pb.step();
                System.out.print(Thread.currentThread().getName() + "\t");
                System.out.println("total " + (double) (System.currentTimeMillis() - p) + " ");
            }
        })));
        threadList.forEach(Thread::start);
        pb.stop();
        System.out.println(Thread.activeCount());

        threadList.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Session session = MySQLConnection.openConnection();
        List<Lemma> lemmaList = session.createQuery("FROM " + Lemma.class.getSimpleName()).getResultList();
        session.close();
        Collection<List<Lemma>> chunk =
                chunked(lemmaList.stream(), SiteLinksGenerator.allLinks.size()/ 5 + 1).values();
        chunk.forEach(c -> new Thread(() -> {
            for (Lemma l : c) {
                Session session1 = MySQLConnection.openConnection();
                int freq = session1.createQuery("FROM " + Index.class.getSimpleName()
                        + " AS i WHERE i.lemmaId = '" + l.getId() + "'").list().size();
                l.setFrequency(freq);
                session1.delete(l);
                session1.save(l);
                session1.close();
            }

        }).start());
    }
    public static <T> Map<Object, List<T>> chunked(Stream<T> stream, int chunkSize) {
        AtomicInteger index = new AtomicInteger(0);
        return stream.collect(Collectors.groupingBy(x -> index.getAndIncrement() / chunkSize));
    }
    public static void appendStringInDB(String data) throws InterruptedException, IOException {
        int idPathBegin = webUrl.indexOf(data) + webUrl.length();
        String path = webUrl.equals(data) ?  "/" : data.substring(idPathBegin);
        Connection connection = null;
        int code = 0;
        String content = "";
        try {
            connection = connectPath(data);
            code = connection.execute().statusCode();
            content = connection.get().toString();
        } catch (RuntimeException | IOException exception){
            rootLogger.debug("Ошибка - " + path + " - " + exception);
        }

        rootLogger.info("Ссылка - " + path);
        Session session = MySQLConnection.openConnection();
        Transaction transaction = session.beginTransaction();
        Page page = new Page();
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        session.save(page);
        transaction.commit();
        session.close();
        assert connection != null;
        appendTagInDB(connection.get(), page);
    }
    private static Connection connectPath(String path) {
        return Jsoup.connect(path)
                .data("query", "Java")
                .userAgent("DuckSearchBot")
                .referrer("https://www.google.com")
                .timeout(20000)
                .method(Connection.Method.GET);
    }
    @SuppressWarnings("unchecked")
    private static List<Field> getFieldList(){
        Session session = MySQLConnection.openConnection();
        List<Field> fieldList = session.createQuery("FROM " + Field.class.getSimpleName()).getResultList();
        session.close();
        return fieldList;
    }
    private static void appendTagInDB(Document document, Page page) throws IOException {
        String tagDeleteRegex = "<[^>]+>";
        System.out.println(fieldList);
        Map<Lemma, Float> pairLemmaWeight = new TreeMap<>();

        StringBuilder tmp = new StringBuilder();
        fieldList.forEach(field -> tmp.append(document.select(field.getName())));
        updateLemmaDB(Lemmatizer.getNormalFormsList(tmp.toString()));

        fieldList.forEach(field -> {
            String tagContent = document.select(field.getName()).toString();
            tagContent = tagContent.replaceAll(tagDeleteRegex, "");
            try {
                Map<String, Integer> t = Lemmatizer.getNormalFormsList(tagContent);
                Map<Lemma, Float> a = getPairs(t, field.getWeight());
                a.keySet().forEach(p -> pairLemmaWeight.put(p, a.get(p)));
            } catch (IOException | InterruptedException e) {
                rootLogger.error(e.getMessage());
            }
        });
        appendInIndexDB(pairLemmaWeight, page);
    }
    private static synchronized void appendInIndexDB(Map<Lemma, Float> pairLemmaWeight, Page page) {
        pairLemmaWeight.keySet().forEach(lemma -> {
            float tmpRank = (float) pairLemmaWeight.keySet().stream()
                    .filter(lemma::equals)
                    .mapToDouble(pairLemmaWeight::get)
                    .sum();
            Session session = MySQLConnection.openConnection();
            Transaction transaction = session.beginTransaction();
            Index tmp = new Index();
            tmp.setLemmaId(lemma.getId());
            tmp.setPageId(page.getId());
            tmp.setRank(tmpRank);
            session.save(tmp);
            transaction.commit();
            session.close();
        });
    }
    @SuppressWarnings("unchecked")
    private static synchronized List<Lemma> getLemmaList(Map<String, Integer> normalFormsMap) {
        StringJoiner stringJoiner = new StringJoiner("' OR lemma = '","FROM " + Lemma.class.getSimpleName() + " WHERE lemma = '", "' ORDER   BY lemma");
        normalFormsMap.keySet().forEach(stringJoiner::add);
        Session session = MySQLConnection.openConnection();
        List<Lemma> lemmaList = session.createQuery(stringJoiner.toString()).getResultList();
        session.close();
        return lemmaList;
    }
    private static synchronized void updateLemmaDB(Map<String, Integer> normalFormsMap){
        List<Lemma> lemmaList = getLemmaList(normalFormsMap);
        Set<String> lemmaStringSet = lemmaList.stream().map(Lemma::getLemma)
                .sorted().collect(Collectors.toCollection(LinkedHashSet::new));

        normalFormsMap.keySet().forEach(key -> {
            if (!lemmaStringSet.contains(key)) {
                Session session = MySQLConnection.openConnection();
                Transaction transaction = session.beginTransaction();
                Lemma lemma = new Lemma();
                lemma.setLemma(key);
                lemma.setFrequency(1);
                session.save(lemma);
                transaction.commit();
                session.close();
            }

        });
    }
    private static synchronized Map<Lemma, Float> getPairs(Map<String, Integer> normalFormsMap, float weight) throws InterruptedException {
        List<Lemma> lemmaList = getLemmaList(normalFormsMap);
        Map<Lemma, Float> pairsLemmaQuantity = new HashMap<>();
        lemmaList.forEach(lemma -> pairsLemmaQuantity.put(lemma, normalFormsMap.get(lemma.getLemma()) * weight));
        return pairsLemmaQuantity;
    }
}