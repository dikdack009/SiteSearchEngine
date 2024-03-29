package pet.diploma.sitesearchengine.controller.crawling;

import javafx.util.Pair;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pet.diploma.sitesearchengine.model.response.Data;
import pet.diploma.sitesearchengine.model.response.SearchResponse;
import pet.diploma.sitesearchengine.model.thread.SearchThread;
import pet.diploma.sitesearchengine.repositories.DBConnection;
import pet.diploma.sitesearchengine.services.CrawlingService;
import pet.diploma.sitesearchengine.services.MorphologyServiceImpl;
import pet.diploma.sitesearchengine.model.Lemma;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchSystem {

    private final String query;
    private final int offset;
    private final int limit;
    private final Set<Integer> linkIdList;
    @Getter
    private String error;
    private final CrawlingService crawlingService;
    private final MorphologyServiceImpl morphologyService;

    public SearchSystem(String query, Set<String> links, Integer offset, Integer limit, CrawlingService crawlingService, int userId) {
        this.query = query;
        this.offset = offset == null ? 0 : offset;
        this.limit = limit == null ? 20 : limit;
        this.crawlingService = crawlingService;
        this.linkIdList = new HashSet<>();
        links.forEach(l -> linkIdList.add(crawlingService.getSiteByUrl(l.trim(), userId).getId()));
        this.error = null;
        morphologyService = new MorphologyServiceImpl();
    }

    public ResponseEntity<SearchResponse> request() throws IOException, SQLException, InterruptedException {
        long m = System.currentTimeMillis();
        if (query.isEmpty()) {
            error = "Задан пустой поисковый запрос";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.NOT_FOUND);
        }
        Set<String> requestNormalForms = morphologyService.getNormalFormsList(query);
        LogManager.getLogger("search").info("Анализ лемм за " + (double)(System.currentTimeMillis() - m)/1000 + " сек.");
        List<Lemma> lemmaList = new ArrayList<>();
        linkIdList.forEach(id -> {
            List<Lemma> l = crawlingService.getLemmaList(requestNormalForms, id);
            l.forEach(lemma -> lemma.setId(id));
            lemmaList.addAll(l);
        });
        LogManager.getLogger("search").info("Анализ запроса за " + (double)(System.currentTimeMillis() - m)/1000 + " сек.");
        if (lemmaList.isEmpty()) {
            error = "Нет результатов";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.NOT_FOUND);
        }
        AtomicReference<Double> quantityPages = new AtomicReference<>((double) 0);
        linkIdList.forEach(id -> quantityPages.updateAndGet(v -> v + crawlingService.countPages(id)));
        Pair<List<Lemma>, List<Lemma>> p = getLemmas(requestNormalForms, lemmaList, quantityPages.get());
        if (error != null) {
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.NOT_FOUND);
        }
        if (p.getKey().isEmpty()){
            error = "Нет результатов";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.NOT_FOUND);
        }
        p.getKey().sort(Comparator.comparingInt(Lemma::getFrequency));
        LogManager.getLogger("search").info("Анализ входных параметров за " + (double)(System.currentTimeMillis() - m)/1000 + " сек.");
        return getSearchResults(p.getKey(), p.getValue());
    }

    private Pair<List<Lemma>, List<Lemma>> getLemmas(Set<String> requestNormalForms, List<Lemma> lemmaList, double countPages) {
        List<Lemma> requestLemmas = new ArrayList<>();
        List<Lemma> optionalLemmas = new ArrayList<>();
        for(String word : requestNormalForms) {
            if (countPages == 0) {
                error = "Нет результатов";
                break;
            }
            if (checkAndAddLemma(lemmaList, word, countPages, requestLemmas, optionalLemmas)) {
                if (checkAndAddLemma(lemmaList, swapKeyboard(word), countPages, requestLemmas, optionalLemmas)) {
                    error = "Слово \"" + word + "\" не найдено";
                    break;
                }
            }
        }
        return new Pair<>(requestLemmas, optionalLemmas);
    }
    private String swapKeyboard(String word) {
        StringBuilder translit = new StringBuilder();
        String eng = "qwertyuiop[]asdfghjkl;'zxcvbnm,. ";
        String rus = "йцукенгшщзхъфывапролджэячсмитьбю ";
        char[] engArr = eng.toCharArray();
        char[] rusArr = rus.toCharArray();
        if (word.matches("[a-zA-Z]+")) {
            for (char i : word.toCharArray()) {
                int engIndex = eng.indexOf(i);
                translit.append(rusArr[engIndex]);
            }
        }
        else if (word.matches("[а-яА-ЯЁё]+")) {
            for (char i : word.toCharArray()) {
                int rusIndex = rus.indexOf(i);
                translit.append(engArr[rusIndex]);
            }
        }
        return translit.toString();
    }

    private boolean checkAndAddLemma(List<Lemma> lemmaList, String word, double countPages, List<Lemma> requestLemmas, List<Lemma> optionalLemmas) {
        if (lemmaList.stream().anyMatch(lemma -> lemma.getLemma().equals(word))) {
            AtomicReference<Lemma> currentLemma = new AtomicReference<>(null);
            lemmaList.forEach(lemma -> {
                if (lemma.getLemma().equals(word)) {
                    currentLemma.set(lemma);
                }
            });
            Lemma currentLemmaFromDB = currentLemma.get();
            if (currentLemmaFromDB.getFrequency() / countPages <= 0.6) {
                requestLemmas.add(currentLemmaFromDB);
            }
            else {
                optionalLemmas.add(currentLemmaFromDB);
            }
            return false;
        }
        return true;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
        Map<K,V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K,V>> st = map.entrySet().stream();
        st.sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).forEach(e -> result.put(e.getKey(),e.getValue()));
        return result;
    }

    private ResponseEntity<SearchResponse> getSearchResults(@NotNull List<Lemma>requestLemmas, List<Lemma> optionalLemmas) throws SQLException, InterruptedException {
        System.out.println(requestLemmas);
        Map<Integer, Double> pageDoubleMap = getPages(requestLemmas);
        if (pageDoubleMap.isEmpty()) {
            error = "Нет результатов";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.NOT_FOUND);
        }
        pageDoubleMap = sortByValue(pageDoubleMap);
        Double max = sortByValue(pageDoubleMap).values().stream().limit(1).collect(Collectors.toList()).get(0);
        List<Data> searchResults = new ArrayList<>();
        ExecutorService es = Executors.newFixedThreadPool(300);
        List<SearchThread> tasks = new ArrayList<>();
        requestLemmas.addAll(optionalLemmas);
        List<Integer> subPageList = new ArrayList<>(pageDoubleMap.keySet()).subList(offset, Math.min(offset + limit, pageDoubleMap.size()));
        for (Integer pageId : subPageList) {
            SearchThread searchThread = new SearchThread(pageId, pageDoubleMap.get(pageId), this, max, requestLemmas ,crawlingService);
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
        return new ResponseEntity<>(new SearchResponse(true, pageDoubleMap.size(), searchResults, null), HttpStatus.OK);
    }

    public String getSnippetFirstStep(List<Lemma> lemmaList, String content) throws IOException {
        List<Integer> indexes = new ArrayList<>();
        Pair<String, Map<String, List<Integer>>> pair = new MorphologyServiceImpl().getNormalText(content, lemmaList);
        String normalText = pair.getKey();
        for (Lemma lemma : lemmaList) {
            String l = lemma.getLemma();
            int i = pair.getValue().get(l).get(0);
            indexes.add(i);
        }
        return getSnippetSecondStep(indexes, normalText);
    }

    public String getSnippetSecondStep(List<Integer> indexes, String content) {
        StringBuilder snippet = new StringBuilder("...");
        Collections.sort(indexes);
        for (int j = 0; j < indexes.size(); ++j) {
            String subSnippet;
            int indexOfContent = indexes.get(j);
            if (indexOfContent - 50 < 0) {
                int end = content.indexOf(" ", indexOfContent + 200);
                subSnippet = content.substring(0, Math.min(end, content.length()));
            }
            else {
                int start = content.indexOf(" ", indexOfContent - 50);
                int end = content.indexOf(" ", indexOfContent + 100);
                subSnippet = content.substring(start, Math.min(end, content.length()));
            }
            snippet.append(subSnippet);
            if (j != indexes.size() - 1 && Math.abs(indexOfContent - indexes.get(j + 1)) < 150) {
                j += 1;
            }
            snippet.append("...");
        }
        return snippet.toString().trim();
    }

    private Map<Integer, Double> getPages(List<Lemma> lemmaList) throws SQLException {
        long m = System.currentTimeMillis();
        List<Integer> pageList = new ArrayList<>(DBConnection.getPagesFromRequest(lemmaList, linkIdList));
        LogManager.getLogger("search").info("Получение странниц по запросу " + (double)(System.currentTimeMillis() - m) / 1000 + " сек.");
        m = System.currentTimeMillis();
        Map<Integer, Double> absRelPage = new HashMap<>();
        for (Integer pageId : pageList) {
            absRelPage.put(pageId, DBConnection.getPageRank(lemmaList, pageId));
        }
        LogManager.getLogger("search").info("Получение рангов странниц по запросу " + (double)(System.currentTimeMillis() - m) / 1000 + " сек.");
        return absRelPage;
    }
}
