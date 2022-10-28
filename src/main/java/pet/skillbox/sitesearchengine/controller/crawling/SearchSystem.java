package pet.skillbox.sitesearchengine.controller.crawling;

import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pet.skillbox.sitesearchengine.model.Lemma;
import pet.skillbox.sitesearchengine.model.Page;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.repositories.MorphologyService;
import pet.skillbox.sitesearchengine.services.MorphologyServiceImpl;

import javax.persistence.criteria.CriteriaBuilder;
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
        DBConnection.setCreateTables(false);
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
        List<Lemma> requestLemmas = new ArrayList<>();

        List<Lemma> lemmaList = DBConnection.getRequestLemmas(requestNormalForms, siteId);
        Map<String, Lemma> requestLemmaMap = lemmaList.stream().collect(Collectors.toMap(Lemma::getLemma, v -> v));

        double quantityPages = DBConnection.getPageNumber(siteId);
        for(String re : requestNormalForms) {
            if (requestLemmaMap.containsKey(re)) {
                Lemma currentLemmaFromDB = requestLemmaMap.get(re);
                if (currentLemmaFromDB.getFrequency() / quantityPages <= 0.95) {
                    requestLemmas.add(currentLemmaFromDB);
                }
            }
            else {
                return new ArrayList<>();
            }
        }
        requestLemmas.sort(Comparator.comparingInt(Lemma::getFrequency));
        System.out.println("Время получения лемм из БД: " + (double)(System.currentTimeMillis() - m) / 1000 + " sec.");
        return requestLemmas.isEmpty() ? new ArrayList<>() : getSearchResults(requestLemmas);
    }

    private List<SearchResult> getSearchResults(@NotNull List<Lemma>requestLemmas) throws SQLException, IOException {
        Map<Page, Double> pageDoubleMap = getPages(requestLemmas);
        Optional<Double> optionalDouble = pageDoubleMap.values().stream().max(Comparator.comparingDouble(o -> o));

        Double max = null;
        if (optionalDouble.isPresent()){
            max = optionalDouble.get();
        }

        List<SearchResult> searchResults = new ArrayList<>();
        System.out.println("Размер - " + pageDoubleMap.size());
        for (Page page : pageDoubleMap.keySet()) {
            double tmpMapValue = pageDoubleMap.get(page);
            pageDoubleMap.put(page, tmpMapValue / max);
            Document d = Jsoup.parse(page.getContent());
            String title = d.select("title").text();
            String contentWithoutTags = title + " " + d.select("body").text();
//            System.out.println(page.getPath());
            searchResults.add(
                    new SearchResult(page.getPath(), title,
                            getSnippet(requestLemmas, contentWithoutTags),
                            pageDoubleMap.get(page)));
        }

        Collections.sort(searchResults);
        System.out.println("Размер - " + searchResults.size());
        return searchResults.subList(offset, Math.min((int) (offset + limit), searchResults.size()));
    }

    private String getSnippet(List<Lemma> lemmaList, String content) throws IOException {
        Set<Integer> indexes = new TreeSet<>(Comparator.comparingInt(i -> i));
        String normalText = new MorphologyServiceImpl().getNormalText(content).toLowerCase();
        for (Lemma lemma : lemmaList) {
            int spaces = searchSpacesBefore(normalText, lemma.getLemma());

            Pair<Integer, Integer> pair = searchSpacesAfter(content, spaces);
            int i = pair.getValue();
            char[] array = content.replaceAll("[^[a-zA-Zа-яА-Я0-9]]", "-").toLowerCase().toCharArray();
            int tmp = new String(array).indexOf("-", i + 1);
            System.out.println("i" + i);
            System.out.println("cs " + content.length());
            System.out.println("ns " + normalText.length());
            content = content.substring(0, i + 1) + "<b>" + content.substring(i + 1, tmp) + "</b>" + content.substring(tmp);
            indexes.add(pair.getKey());
        }
        StringBuilder snippet = new StringBuilder("...");
        char[] array = normalText.replaceAll("[^[a-zA-Zа-яА-Я0-9]]", "-").toLowerCase().toCharArray();
        for (int i : indexes) {
            int tmp = new String(array).indexOf("-", i - 7);
            System.out.println(i + "\n" + new String(array));
            snippet.append(content, i, Math.min(tmp + 80, content.length() + 7));

            snippet.append("...");
            if (snippet.length() > 80) {
                snippet.append("\n");
            }
        }
        return snippet.toString();
    }

    private int searchSpacesBefore(String text, String word){
        char[] array = text.replaceAll("[^[a-zA-Zа-яА-Я0-9]]", "-").toLowerCase().toCharArray();
        int tmp = new String(array).indexOf("-" + word + "-");
        int count = 0;
        char[] textArray = text.toCharArray();
        int i = 0;
        for ( ; i < tmp; ++i ) {
            if (textArray[i] == ' ') {
                count++;
            }
        }
        System.out.println(word + " - пробелы - " + count + 1);
        return count + 1;
    }

    private Pair<Integer, Integer> searchSpacesAfter(String text, int count){
        char[] textArray = text.toCharArray();
        int i, spaceId = 0, newCount = 0;
        for (i = 0; i < textArray.length; ++i) {
            if(textArray[i] == ' ') {
                newCount++;
            }
            if (newCount == count - 4 || count < 5){
                spaceId = i;
            }
            if (newCount == count) {
                break;
            }
        }
        System.out.println("spaceId " + spaceId);
        return new Pair<>(spaceId, i);
    }

    private Map<Page, Double> getPages (List<Lemma> lemmaList) throws SQLException {
        long m = System.currentTimeMillis();
        List<Page> pageList = DBConnection.getPagesFromRequest(lemmaList, siteId);
        System.out.println("Время получения странниц по запросу: " + (double) (System.currentTimeMillis() - m) / 1000 + " sec.");

        Map<Page, Double> absRelPage = new HashMap<>();
        for (Page page : pageList) {
            absRelPage.put(page, DBConnection.getPageRank(lemmaList, page.getId()));
        }
        return absRelPage;
    }
}
