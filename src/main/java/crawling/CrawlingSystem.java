package crawling;

import connection.MySQLConnection;
import lemmatizer.Lemmatizer;
import org.hibernate.Transaction;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static crawling.Lemma.getLemmaIdByName;

public class CrawlingSystem {

    public static String webUrl;

    public static void startCrawlingSystem(String url) {
        MySQLConnection.setUpDB();
        MySQLConnection.openConnection();
        webUrl = url;
        SiteMapNode rootUrl = new SiteMapNode(webUrl);
        new ForkJoinPool().invoke(new SiteMapNodeRecursiveTask(rootUrl,rootUrl));
        writeSitemapUrl(rootUrl);
        MySQLConnection.closeConnection();
    }

    public static void writeSitemapUrl(SiteMapNode node) {
        try {
            if (node.getDepth() != 0) {
                appendStringInDB(node.getUrl());
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
        node.getSubLinks().forEach(CrawlingSystem::writeSitemapUrl);
    }

    private static void appendStringInDB(String data) throws IOException {
        int idPathBegin = webUrl.indexOf(data) + webUrl.length();
        String path = webUrl.equals(data) ?  "/" : data.substring(idPathBegin);
        int code = connectPath(data).execute().statusCode();
        String content = connectPath(data).get().toString();
        Transaction transaction = MySQLConnection.getSession().beginTransaction();
        Page page = new Page();
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);
        MySQLConnection.getSession().save(page);
        transaction.commit();
        appendTagInDB(connectPath(data), page);
    }

    private static Connection connectPath(String path) {
        return Jsoup.connect(path)
                .data("query", "Java")
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://www.google.com")
                .timeout(3000)
                .method(Connection.Method.GET);
    }

    private static void appendTagInDB(Connection path, Page page) throws IOException {
        MySQLConnection.openConnection();
        Document document = path.get();
        String regex = "<[^[>]]+>";
        String hql = "FROM " + Field.class.getSimpleName();
        List <Field> fieldList = MySQLConnection.getSession().createQuery(hql).getResultList();
        Map<Lemma, Float> pairLemmaWeight = new TreeMap<>();

        appendInLemmaDB(Lemmatizer.getNormalFormsList(document.text()),0f);

        fieldList.forEach(field -> {
            String tagContent = document.select(field.getName()).toString();
            tagContent = tagContent.replaceAll(regex, "");
            try {
                Map<String, Integer> t = Lemmatizer.getNormalFormsList(tagContent);
                Map<Lemma, Float> a = appendInLemmaDB(t, field.getWeight());
                a.keySet().forEach(p -> {
                    try {
                        pairLemmaWeight.put(p, a.get(p));
                    }catch (Exception ex){
                        System.out.println(ex.getMessage());
                    }
                });
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });

        Map<Lemma, Float> pairs = new HashMap<>();
        pairLemmaWeight.keySet().forEach(p -> pairs.put(p, 0f));
        pairs.keySet().forEach(p -> pairs.put(p, (float) pairLemmaWeight.keySet().stream()
                .filter(p::equals)
                .mapToDouble(pairLemmaWeight::get)
                .sum()));

//        pairs.keySet().forEach(k -> System.out.println(k + " " + pairs.get(k)));
//        System.out.println(page.getId());
        for (Lemma lemma : pairs.keySet()){
            Transaction transaction = MySQLConnection.getSession().beginTransaction();
            Index tmp = new Index();
            tmp.setLemmaId(lemma.getId());
            tmp.setPageId(page.getId());
            tmp.setRank(pairs.get(lemma));
            MySQLConnection.getSession().save(tmp);
            transaction.commit();
        }
    }

    private static Map<Lemma, Float> appendInLemmaDB(Map<String, Integer> normalFormsMap, float weight){

        String hqlLemma = "FROM " + Lemma.class.getSimpleName();
        List<Lemma> lemmaList = MySQLConnection.getSession().createQuery(hqlLemma).getResultList();
        List<String> lemmaStringList = new ArrayList<>();
        lemmaList.forEach(l -> lemmaStringList.add(l.getLemma()));
        Map<Lemma, Float> pairsLemmaQuantity = new HashMap<>();
//        System.out.println(lemmaList);

        if (weight == 0f) {
            List<Lemma> finalLemmaList1 = lemmaList;
            normalFormsMap.keySet().forEach(key -> {
                Transaction transaction = MySQLConnection.getSession().beginTransaction();
                Lemma lemma;
                if (lemmaStringList.contains(key)) {
                    lemma = MySQLConnection.getSession().get(Lemma.class, getLemmaIdByName(finalLemmaList1, key));
                    lemma.setFrequency(lemma.getFrequency() + 1);
                } else {
                    lemma = new Lemma();
                    lemma.setLemma(key);
                    lemma.setFrequency(1);
                }
                MySQLConnection.getSession().save(lemma);
                transaction.commit();
            });
        }
        lemmaList = MySQLConnection.getSession().createQuery(hqlLemma).getResultList();
        List<Lemma> finalLemmaList = lemmaList;
        normalFormsMap.keySet().forEach(key -> {
            Transaction transaction = MySQLConnection.getSession().beginTransaction();
            Lemma lemma = MySQLConnection.getSession().get(Lemma.class, getLemmaIdByName(finalLemmaList, key));
            pairsLemmaQuantity.put(lemma, normalFormsMap.get(key) * weight);
            transaction.commit();
        });
        return pairsLemmaQuantity;
    }
}
