package pet.diploma.sitesearchengine.controller.crawling;

import javafx.util.Pair;
import lombok.Getter;
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
import pet.diploma.sitesearchengine.model.Page;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class SearchSystem {

    private final String query;
    private final int offset;
    private final int limit;
    private final Set<Integer> linkIdList;
    private final Set<String> links;
    @Getter
    private String error;
    private final CrawlingService crawlingService;
    private final int userId;

    public SearchSystem(String query, Set<String> links, Integer offset, Integer limit, CrawlingService crawlingService, int userId) {
        this.query = query;
        this.offset = offset == null ? 0 : offset;
        this.limit = limit == null ? 20 : limit;
        this.crawlingService = crawlingService;
        this.links = links;
        this.userId = userId;
        this.linkIdList = new HashSet<>();
        this.error = null;
    }

    public ResponseEntity<SearchResponse> request() throws IOException, SQLException, InterruptedException {
        links.forEach(l -> {
            linkIdList.add(crawlingService.getSiteByUrl(l.trim(), userId).getId());
        });

        if (query.isEmpty()){
            error = "Задан пустой поисковый запрос";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.BAD_REQUEST);
        }

        long m = System.currentTimeMillis();
        Set<String> requestNormalForms = new MorphologyServiceImpl().getNormalFormsList(query).keySet();
        List<Lemma> requestLemmas = new ArrayList<>();
        List<Lemma> optionalLemmas = new ArrayList<>();

        List<Lemma> lemmaList = new ArrayList<>();

        linkIdList.forEach(id -> {
            List<Lemma> l = crawlingService.getLemmaList(requestNormalForms, id);
            l.forEach(lemma -> lemma.setId(id));
            lemmaList.addAll(l);
        });
        if (lemmaList.isEmpty()) {
            error = "Нет результатов";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.BAD_REQUEST);
        }

        AtomicReference<Double> quantityPages = new AtomicReference<>((double) 0);
        linkIdList.forEach(id -> quantityPages.updateAndGet(v -> v + crawlingService.countPages(id)));

        if (quantityPages.get() == 0) {
            error = "Нет результатов";
            return new ResponseEntity<>(new SearchResponse(false, null, null, error), HttpStatus.BAD_REQUEST);
        }
        for(String re : requestNormalForms) {
            if (lemmaList.stream().anyMatch(lemma -> lemma.getLemma().equals(re))) {
                AtomicReference<Lemma> currentLemma = new AtomicReference<>(null);
                lemmaList.forEach(lemma -> {
                    if (lemma.getLemma().equals(re)) {
                        currentLemma.set(lemma);
                    }
                });
                Lemma currentLemmaFromDB = currentLemma.get();
                if (currentLemmaFromDB.getFrequency() / quantityPages.get() <= 1) {
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
        ExecutorService es = Executors.newFixedThreadPool(300);
        List<SearchThread> tasks = new ArrayList<>();
        requestLemmas.addAll(optionalLemmas);
        List<Page> subPageList = new ArrayList<>(pageDoubleMap.keySet())
                .subList(offset, Math.min(offset + limit, pageDoubleMap.size()));
        for (Page page : subPageList) {
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

    public synchronized String getSnippetFirstStep(List<Lemma> lemmaList, String content) throws IOException {

        List<Integer> indexes = new ArrayList<>();
        String normalText = new MorphologyServiceImpl().getNormalText(content).toLowerCase();
        for (Lemma lemma : lemmaList) {
            int spaces = searchSpacesBefore(normalText, lemma.getLemma());
            Pair<Integer, Integer> pair = searchSpacesAfter(content, spaces);
            int i = pair.getValue();

            String newContent = content.replaceAll("\\pP", "-").replaceAll("[^[a-zA-Zа-яА-Я0-9]]", "-").toLowerCase();
            int tmp = newContent.indexOf("-", i + 1);
            content = content.substring(0, i + 1) + "<b>" + content.substring(i + 1, tmp) + "</b>" + content.substring(tmp);
            indexes.add(pair.getKey());
        }
        return getSnippetSecondStep(indexes, content);
    }

    //TODO проверить сдвиг чтобы не был больше размера

    public String getSnippetSecondStep(List<Integer> indexes, String content) {
        StringBuilder snippet = new StringBuilder("...");
        Collections.sort(indexes);
        for (int j = 0; j < indexes.size(); ++j) {
            int indexOfContent = indexes.get(j);
            if (j != indexes.size() - 1 && Math.abs(indexOfContent - indexes.get(j + 1)) < 70) {
                snippet.append(content, indexOfContent + 1, Math.min(indexOfContent + 200, content.length()));
                j += 1;
            }
            else {
                snippet.append(content, indexOfContent + 1, Math.min(indexOfContent + 180, content.length()));
            }
            snippet.append("...");
        }
        return snippet.toString();
    }

    private int searchSpacesBefore(String text, String word) {
        text = text.replaceAll("\\pP", " ");
        String newText = text.replaceAll("[^[a-zA-Zа-яА-Я0-9\\-]]", " ").toLowerCase();
        char[] textArray = newText.toCharArray();
        int tmp = newText.indexOf(" " + word + " ");
        int count = 0;
        int i = 0;
        int newCount = 0;
        for (i = 0; i < textArray.length; ++i) {
            if (textArray[i] == ' ') {
                newCount++;
            }
        }
        i = 0;
//        System.out.println("Пробелов в упрощенном тексте " + newCount);
//        System.out.print("Word = " + word);
        for ( ; i <= tmp; ++i ) {
            if (textArray[i] == ' ') {
                count++;
            }
        }
//        System.out.print(" Next word = ");
//        System.out.print(textArray[i]);
//        System.out.print(textArray[i+1]);
//        System.out.print(textArray[i+2]);
//        System.out.print(textArray[i+3]);
//        System.out.println(textArray[i+4]);
//        System.out.println(newText);
        return count;
    }

    private Pair<Integer, Integer> searchSpacesAfter(String text, int count) {
        text = text.replaceAll("\\pP", " ");
        String newText = text.replaceAll("[^[a-zA-Zа-яА-Я0-9\\-/]]", " ").toLowerCase();
        char[] textArray = newText.toCharArray();
//        System.out.println(text);
        int i, spaceId = 0, newCount = 0;
        for (i = 0; i < textArray.length; ++i) {
            if (textArray[i] == ' ') {
                newCount++;
            }
        }
//        System.out.println("Пробелов в обычном тексте " + newCount);
        newCount = 0;
        for (i = 0; i < textArray.length; ++i) {
            if(textArray[i] == ' ') {
                newCount++;
            }
            if (newCount == count - 14){
                spaceId = i;
            }
            if (newCount == count) {
                break;
            }
        }
//        System.out.print("Next next word = ");
//        System.out.print(textArray[i]);
//        System.out.print(textArray[i+1]);
//        System.out.print(textArray[i+2]);
//        System.out.print(textArray[i+3]);
//        System.out.println(textArray[i+4]);
        return new Pair<>(spaceId, !Character.isLetterOrDigit(text.toCharArray()[i + 1]) ? ++i : i);
    }

    private Map<Page, Double> getPages(List<Lemma> lemmaList) throws SQLException {
        long m = System.currentTimeMillis();
        List<Page> pageList = new ArrayList<>();
        linkIdList.forEach(id -> {
            try {
                pageList.addAll(DBConnection.getPagesFromRequest(lemmaList, id));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("Время получения странниц по запросу: " + (double) (System.currentTimeMillis() - m) / 1000 + " sec.");
        Map<Page, Double> absRelPage = new HashMap<>();
        for (Page page : pageList) {
            absRelPage.put(page, DBConnection.getPageRank(lemmaList, page.getId()));
        }
        System.out.println("Время получения мапы странниц по запросу: " + (double) (System.currentTimeMillis() - m) / 1000 + " sec.");
        return absRelPage;
    }
}
