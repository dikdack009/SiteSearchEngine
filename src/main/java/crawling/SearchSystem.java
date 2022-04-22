package crawling;

import connection.MySQLConnection;
import crawling.Lemma;
import lemmatizer.Lemmatizer;

import java.io.IOException;
import java.util.*;

public class SearchSystem {

    public static void request (String r) throws IOException {
        MySQLConnection.setUpDB();
        MySQLConnection.openConnection();
        Set<String> requestNormalForms = Lemmatizer.getNormalFormsList(r).keySet();
        Set<Lemma> requestLemmas = new TreeSet<>(Comparator.comparingInt(Lemma::getFrequency));

        String hqlLemma = "FROM " + Lemma.class.getSimpleName();
        List<Lemma> lemmaList = MySQLConnection.getSession().createQuery(hqlLemma).getResultList();
        List<String> lemmaStringList = new ArrayList<>();
        lemmaList.forEach(l -> lemmaStringList.add(l.getLemma()));

        double quantityPages = (double) getPagesSize();
        for (String requestNormalForm : requestNormalForms) {
            if (lemmaStringList.contains(requestNormalForm)) {
                Lemma currentLemmaFromDB = Lemma.getLemmaByName(lemmaList, requestNormalForm);
                if (currentLemmaFromDB.getFrequency() / quantityPages < 0.7) {
                    requestLemmas.add(currentLemmaFromDB);
                }
            }
        }
        if (requestLemmas.isEmpty()){
            return;
        }

        Map<Page, Double> pageDoubleMap = getPages(requestLemmas);
        Optional<Double> optionalDouble = pageDoubleMap.values().stream().max(Comparator.comparingDouble(o -> o));
        Double max = null;
        if (optionalDouble.isPresent()){
            max = optionalDouble.get();
         }
        System.out.println(max);
        for (Page page : pageDoubleMap.keySet()) {
            System.out.println(pageDoubleMap.get(page));
            pageDoubleMap.put(page, pageDoubleMap.get(page) / max);
            System.out.print(page.getPath() + "\t");
            System.out.println(pageDoubleMap.get(page));
        }

    }

    private static long getPagesSize(){
        String hqlLemma = "FROM " + Page.class.getSimpleName();
        return MySQLConnection.getSession().createQuery(hqlLemma).getResultList().size();
    }

    private static Map<Page, Double> getPages(Set<Lemma> lemmaSet){
        List<Page> pageList = MySQLConnection.getSession()
                .createQuery("FROM " + Page.class.getSimpleName())
                .getResultList();

        for (Lemma lemma : lemmaSet) {
            int id = lemma.getId();
            List<Index> indexList = MySQLConnection.getSession()
                    .createQuery("FROM " + Index.class.getSimpleName() + " AS i WHERE i.lemmaId = '" + id + "'")
                    .getResultList();

            List<Page> newPageList = new ArrayList<>();
            List<Page> finalPageList = pageList;
            indexList.stream().mapToInt(Index::getPageId)
                    .forEach(p -> {
                        Page cur = Page.getPageById(finalPageList, p);
                        if (cur != null){
                            newPageList.add(cur);
                        }
                    });
            pageList = newPageList;
        }
        Map<Page, Double> absRelPage = new HashMap<>();
        for (Page page : pageList) {
            double totalRank = 0f;
            for (Lemma lemma : lemmaSet) {
                Index index = (Index) MySQLConnection.getSession()
                        .createQuery("FROM " + Index.class.getSimpleName() + " AS i " +
                                "WHERE i.lemmaId = '" + lemma.getId() + "' AND i.pageId = '" + page.getId() + "'")
                        .getSingleResult();
                totalRank += index.getRank();
            }
            absRelPage.put(page, totalRank);
        }

        return absRelPage;
    }
}
