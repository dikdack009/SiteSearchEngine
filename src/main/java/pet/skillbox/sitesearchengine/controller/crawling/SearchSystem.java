package pet.skillbox.sitesearchengine.controller.crawling;

import javafx.util.Pair;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pet.skillbox.sitesearchengine.model.Lemma;
import pet.skillbox.sitesearchengine.model.Page;
import pet.skillbox.sitesearchengine.model.SearchThread;
import pet.skillbox.sitesearchengine.model.Site;
import pet.skillbox.sitesearchengine.model.response.Data;
import pet.skillbox.sitesearchengine.model.response.SearchResponse;
import pet.skillbox.sitesearchengine.repositories.DBConnection;
import pet.skillbox.sitesearchengine.services.CrawlingService;
import pet.skillbox.sitesearchengine.services.MorphologyServiceImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchSystem {

    private final String query;
    private final int offset;
    private final int limit;
    private int siteId;
    private final String siteLink;
    @Getter
    private String error;
    private final CrawlingService crawlingService;

    //TODO: сделать индексацию больше чем одного сайта

    public SearchSystem(String query, String siteLink, Integer offset, Integer limit, CrawlingService crawlingService) throws SQLException {
        this.query = query;
        this.offset = offset == null ? 0 : offset;
        this.limit = limit == null ? 20 : limit;
        this.crawlingService = crawlingService;
        this.siteLink = siteLink;
         //DBConnection.getSiteIdByPath(siteLink);
    }

    public ResponseEntity<SearchResponse> request() throws IOException, SQLException, InterruptedException {
        Site site = crawlingService.getSiteByUrl(siteLink);
        int id = site == null ? -1 : site.getId();
        siteId = siteLink == null ? 0 : id;
        if (query.isEmpty()){
            error = "Задан пустой поисковый запрос";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.BAD_REQUEST);
        }
        if (siteId < 0) {
            error = "Указанная страница не найдена";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.NOT_FOUND);
        }
        long m = System.currentTimeMillis();
        Set<String> requestNormalForms = new MorphologyServiceImpl().getNormalFormsList(query).keySet();
        List<Lemma> requestLemmas = new ArrayList<>();
        List<Lemma> optionalLemmas = new ArrayList<>();

        List<Lemma> lemmaList = crawlingService.getLemmaList(requestNormalForms, siteId);
        System.out.println(lemmaList);
        if (lemmaList.isEmpty()) {
            error = "Нет результатов";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.BAD_REQUEST);
        }
        Map<String, Lemma> requestLemmaMap = lemmaList.stream().collect(Collectors.toMap(Lemma::getLemma, v -> v));

//        double quantityPages = DBConnection.countPages(siteId);
        double quantityPages = crawlingService.countPages(siteId);
        System.out.println(quantityPages);
        if (quantityPages == 0) {
            error = "Нет результатов";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.BAD_REQUEST);
        }
        for(String re : requestNormalForms) {
            if (requestLemmaMap.containsKey(re)) {
                Lemma currentLemmaFromDB = requestLemmaMap.get(re);
                if (currentLemmaFromDB.getFrequency() / quantityPages <= 1) {
                    requestLemmas.add(currentLemmaFromDB);
                }
                else {
                    optionalLemmas.add(currentLemmaFromDB);
                }
            }
            else {
                error = "Слово " + re + " не найдено";
                return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.BAD_REQUEST);
            }
        }
        if (requestLemmas.isEmpty()){
            error = "Нет результатов";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.BAD_REQUEST);
        }
        requestLemmas.sort(Comparator.comparingInt(Lemma::getFrequency));
        System.out.println("Время получения лемм из БД: " + (double)(System.currentTimeMillis() - m) / 1000 + " sec.");
        return getSearchResults(requestLemmas, optionalLemmas);
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
        Map<K,V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K,V>> st = map.entrySet().stream();
        st.sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).forEach(e -> result.put(e.getKey(),e.getValue()));
        return result;
    }

    private ResponseEntity<SearchResponse> getSearchResults(@NotNull List<Lemma>requestLemmas, List<Lemma> optionalLemmas) throws SQLException, InterruptedException {
        Map<Page, Double> pageDoubleMap = getPages(requestLemmas);
        pageDoubleMap = sortByValue(pageDoubleMap);
        Optional<Double> optionalDouble = pageDoubleMap.values().stream().max(Comparator.comparingDouble(o -> o));

        Double max = null;
        if (optionalDouble.isPresent()){
            max = optionalDouble.get();
        }

        List<Data> searchResults = new ArrayList<>();
        System.out.println("Размер - " + pageDoubleMap.size());
        ExecutorService es = Executors.newFixedThreadPool(100);
        List<SearchThread> tasks = new ArrayList<>();
        requestLemmas.addAll(optionalLemmas);
        List<Page> subPageList = new ArrayList<>(pageDoubleMap.keySet())
                .subList(offset, Math.min(offset + limit, pageDoubleMap.size()));
        for (Page page : subPageList) {
            System.out.println(page.getId() + " - " + page.getPath());
            SearchThread searchThread = new SearchThread(page, pageDoubleMap.get(page), this, max, requestLemmas);
            tasks.add(searchThread);
        }
        List<Future<Data>> futures = es.invokeAll(tasks);
        futures.forEach(f -> {
            try {
                searchResults.add(f.get());
            } catch (InterruptedException | ExecutionException e) {
                es.shutdown();
                e.printStackTrace();
            }
        });
        es.shutdown();
        Collections.sort(searchResults);
        return new ResponseEntity<>(new SearchResponse(true, pageDoubleMap.size(), searchResults, null), HttpStatus.OK);//subList(offset, Math.min((int) (offset + limit), searchResults.size()));
    }

    public String getSnippetFirstStep(List<Lemma> lemmaList, String content) throws IOException {
        List<Integer> indexes = new ArrayList<>();
        String normalText = new MorphologyServiceImpl().getNormalText(content).toLowerCase();
        for (Lemma lemma : lemmaList) {
            int spaces = searchSpacesBefore(normalText, lemma.getLemma());
            Pair<Integer, Integer> pair = searchSpacesAfter(content, spaces);
            int i = pair.getValue();
            char[] array = content.replaceAll("[^[a-zA-Zа-яА-Я0-9]]", "-").toLowerCase().toCharArray();
            int tmp = new String(array).indexOf("-", i + 1);
            content = content.substring(0, i + 1) + "<b>" + content.substring(i + 1, tmp) + "</b>" + content.substring(tmp);
            indexes.add(pair.getKey());
        }
        return getSnippetSecondStep(indexes, content);
    }

    //TODO проверить сдвиг чтобы не был больше размера

    public String getSnippetSecondStep(List<Integer> indexes, String content) throws IOException {
        StringBuilder snippet = new StringBuilder("...");
        Collections.sort(indexes);
        for (int j = 0; j < indexes.size(); ++j) {
            int indexOfContent = indexes.get(j);
            if (j != indexes.size() - 1 && Math.abs(indexOfContent - indexes.get(j + 1)) < 70) {
                snippet.append(content, indexOfContent + 1, Math.min(indexOfContent + 100, content.length()));
                j += 1;
            }
            else {
                snippet.append(content, indexOfContent + 1, Math.min(indexOfContent + 90, content.length()));
            }
            snippet.append("...");
        }
        return snippet.toString();
    }

    private int searchSpacesBefore(String text, String word) {
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
        return count + 1;
    }

    private Pair<Integer, Integer> searchSpacesAfter(String text, int count) {
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
        return new Pair<>(spaceId, i);
    }

    private Map<Page, Double> getPages(List<Lemma> lemmaList) throws SQLException {
        long m = System.currentTimeMillis();
        List<Page> pageList = DBConnection.getPagesFromRequest(lemmaList, siteId);

        Map<Page, Double> absRelPage = new HashMap<>();
        for (Page page : pageList) {
            absRelPage.put(page, DBConnection.getPageRank(lemmaList, page.getId()));
        }
        System.out.println("Время получения мапы странниц по запросу: " + (double) (System.currentTimeMillis() - m) / 1000 + " sec.");
        return absRelPage;
    }
}
