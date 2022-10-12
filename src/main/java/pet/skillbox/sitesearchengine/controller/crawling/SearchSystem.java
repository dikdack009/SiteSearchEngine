package pet.skillbox.sitesearchengine.controller.crawling;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pet.skillbox.sitesearchengine.model.Lemma;
import pet.skillbox.sitesearchengine.model.Page;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.repositories.MorphologyService;
import pet.skillbox.sitesearchengine.services.MorphologyServiceImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SearchSystem {

    private final String query;

    private final int offset;

    private final long limit;

    private final int siteId;


    public SearchSystem(String query, String siteLink, int offset, long limit) throws SQLException {
        this.query = query;
        this.offset = offset;
        this.limit = limit;
        siteId = DBConnection.getSiteIdByPath(siteLink);
    }

    public SearchSystem(String query, String siteLink) throws SQLException {
        this(query, siteLink, 0, 20);
    }

    public SearchSystem(String query, String siteLink, int offset) throws SQLException {
        this(query, siteLink, offset, 20);
    }

    public SearchSystem(String query, String siteLink, long limit) throws SQLException {
        this(query, siteLink, 0, limit);
    }

    public List<SearchResult> request() throws IOException, SQLException {
        long m = System.currentTimeMillis();
        Set<String> requestNormalForms = new MorphologyServiceImpl().getNormalFormsList(query).keySet();
        System.out.println((double)(System.currentTimeMillis() - m) / 1000 + " sec.");
        Set<Lemma> requestLemmas = new TreeSet<>(Comparator.comparingInt(Lemma::getFrequency));

        List<Lemma> lemmaList = DBConnection.getRequestLemmas(requestNormalForms, siteId);
        System.out.println((double)(System.currentTimeMillis() - m) / 1000 + " sec.");
        Map<String, Lemma> requestLemmaMap = lemmaList.stream().collect(Collectors.toMap(Lemma::getLemma, v -> v));
        System.out.println((double)(System.currentTimeMillis() - m) / 1000 + " sec.");

        double quantityPages = DBConnection.getPageNumber(siteId);
        System.out.println((double)(System.currentTimeMillis() - m) / 1000 + " sec.");

        requestNormalForms.forEach(System.out::println);
        lemmaList.forEach(System.out::println);
        for(String re : requestNormalForms) {
            System.out.println(re);
            if (requestLemmaMap.containsKey(re)) {
                Lemma currentLemmaFromDB = requestLemmaMap.get(re);
                System.out.println(currentLemmaFromDB);
                System.out.println(currentLemmaFromDB.getFrequency() / quantityPages);
                if (currentLemmaFromDB.getFrequency() / quantityPages <= 0.9) {
                    System.out.println("Лемма " + currentLemmaFromDB.getLemma() + " " + currentLemmaFromDB.getFrequency());
                    requestLemmas.add(currentLemmaFromDB);
                }
            }
            else {
                return new ArrayList<>();
            }
        }
        System.out.println((double)(System.currentTimeMillis() - m) / 1000 + " sec.");
        return requestLemmas.isEmpty() ? new ArrayList<>() : getSearchResults(requestLemmas);
    }

    private List<SearchResult> getSearchResults(@NotNull Set<Lemma>requestLemmas) throws SQLException, IOException {
        Map<Page, Double> pageDoubleMap = getPages(requestLemmas);
        Optional<Double> optionalDouble = pageDoubleMap.values().stream().max(Comparator.comparingDouble(o -> o));

        Double max = null;
        if (optionalDouble.isPresent()){
            max = optionalDouble.get();
        }

        List<SearchResult> searchResults = new ArrayList<>();
        for (Page page : pageDoubleMap.keySet()) {
            double tmpMapValue = pageDoubleMap.get(page);
            pageDoubleMap.put(page, tmpMapValue / max);
//            Matcher m = Pattern.compile("<title>(\\s*)([\\S\\s]*)</title>").matcher(page.getContent());
            Document d = Jsoup.parse(page.getContent());
            String title = d.select("title").text();
            String contentWithoutTags = d.select("body").text();
//            String contentWithoutTags = page.getContent().replaceAll("<[^>]+>", "");
//            while(m.find()) {
//                title = page.getContent().substring(m.start() + "<title>".length(), m.end() - "</title>".length());
//            }
            searchResults.add(new SearchResult(page.getPath(), title, getSnippet(requestLemmas, contentWithoutTags), pageDoubleMap.get(page)));
        }

        Collections.sort(searchResults);
        System.out.println("Размер - " + searchResults.size());
        return searchResults.subList(offset, Math.min((int) (offset + limit), searchResults.size()));
    }

    private String getSnippet(Set<Lemma> lemmaSet, String content) throws IOException {
        //content = new MorphologyServiceImpl().getNormalText(content);
//        System.out.println("text");
//        System.out.println("<" + content + ">");
//        Map<String, List<In<teger>> stringListMap = new HashMap<>();
//        for (Lemma lemma : lemmaSet) {
//            List<Integer> lemmaIndexes = new ArrayList<>();
//            int i = 0;
//            while (i != -1) {
//                i = content.indexOf(lemma.getLemma(), i);
//                lemmaIndexes.add(i);
//            }
//            stringListMap.put(lemma.getLemma(), lemmaIndexes);
//        }
        return "";
    }

    private Map<Page, Double> getPages(Set<Lemma> lemmaSet) throws SQLException {
        long m = System.currentTimeMillis();
        List<Page> pageList = DBConnection.getPagesFromRequest(lemmaSet, siteId);
        System.out.println((double) (System.currentTimeMillis() - m) / 1000 + " sec.");

        Map<Page, Double> absRelPage = new HashMap<>();
        for (Page page : pageList) {
            absRelPage.put(page, DBConnection.getPageRank(lemmaSet, page.getId()));
        }
        return absRelPage;
    }
}
