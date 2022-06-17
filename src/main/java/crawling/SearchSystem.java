package crawling;

import model.Index;
import model.Lemma;
import model.Page;
import org.hibernate.Session;
import repository.MySQLConnection;
import lemmatizer.Lemmatizer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static model.Lemma.getLemmaByName;

public class SearchSystem {
    @SuppressWarnings("unchecked")
    public static List<SearchResult> request(String r) throws IOException {
        Set<String> requestNormalForms = Lemmatizer.getNormalFormsList(r).keySet();
        Set<Lemma> requestLemmas = new TreeSet<>(Comparator.comparingInt(Lemma::getFrequency));
        StringJoiner stringJoiner = new StringJoiner("' OR lemma = '","FROM " + Lemma.class.getSimpleName() + " WHERE lemma = '", "' ORDER   BY lemma");
        requestNormalForms.forEach(stringJoiner::add);
        Session session = MySQLConnection.openConnection();
        List<Lemma> lemmaList = session.getSession().createQuery(stringJoiner.toString()).getResultList();
        session.close();
        Set<String> lemmaStringSet = lemmaList.stream().map(Lemma::getLemma)
                .sorted().collect(Collectors.toCollection(LinkedHashSet::new));

        List<Page> pageList =  getPages();
        double quantityPages = pageList.size();
        for(String re : requestNormalForms) {
            if (lemmaStringSet.contains(re)) {
                Lemma currentLemmaFromDB = getLemmaByName(lemmaList, re);
                assert currentLemmaFromDB != null;
                if (currentLemmaFromDB.getFrequency() / quantityPages <= 1) {
                    requestLemmas.add(currentLemmaFromDB);
                }
            }
            else {
                return new ArrayList<>();
            }
        }
        return requestLemmas.isEmpty() ? new ArrayList<>() : getSearchResults(requestLemmas, pageList);
    }

    private static List<SearchResult> getSearchResults(@NotNull Set<Lemma>requestLemmas, List<Page> t){
        long s = System.currentTimeMillis();
        Map<Page, Double> pageDoubleMap = getPages(requestLemmas, t);
        System.out.println((double)System.currentTimeMillis() - s);
        Optional<Double> optionalDouble = pageDoubleMap.values().stream().max(Comparator.comparingDouble(o -> o));
        Double max = null;
        if (optionalDouble.isPresent()){
            max = optionalDouble.get();
        }
        List<SearchResult> searchResults = new ArrayList<>();
        for (Page page : pageDoubleMap.keySet()) {
            double tmpMapValue = pageDoubleMap.get(page);
            pageDoubleMap.put(page, tmpMapValue / max);
            Matcher m = Pattern.compile("<title>[^>]+</title>").matcher(page.getContent());
            String title = null;
            while(m.find()) {
                title = page.getContent().substring(m.start() + "<title>".length(), m.end() - "</title>".length());
            }
            searchResults.add(new SearchResult(page.getPath(), title, "", pageDoubleMap.get(page)));
        }
        Collections.sort(searchResults);
        return searchResults;
    }
    @SuppressWarnings("unchecked")
    private static List<Page> getPages(){
        Session session = MySQLConnection.openConnection();
        List<Page> res = session.createQuery("FROM " + Page.class.getSimpleName()).getResultList();
        session.close();
        return res;
    }
    @SuppressWarnings("unchecked")
    private static Map<Page, Double> getPages(Set<Lemma> lemmaSet, List<Page> pageList){
        for (Lemma lemma : lemmaSet) {
            int id = lemma.getId();
            String selectQuery = "FROM " + Index.class.getSimpleName() + " AS i WHERE i.lemmaId = '" + id + "'";
            Session session = MySQLConnection.openConnection();
            List<Index> indexList = session.createQuery(selectQuery).getResultList();
            session.close();

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

            StringJoiner stringJoiner = new StringJoiner("' OR i.lemmaId = '",
                    "SELECT SUM(i.rank) FROM " + Index.class.getSimpleName() + " AS i WHERE i.pageId = '" + page.getId() + "' AND (i.lemmaId = '" ,
                    "')");
            lemmaSet.forEach(l -> stringJoiner.add(l.getId().toString()));
            Session session = MySQLConnection.openConnection();
            double total = (double) session.createQuery(stringJoiner.toString()).list().get(0);
            session.close();

            absRelPage.put(page, total);
        }
        return absRelPage;
    }
}
