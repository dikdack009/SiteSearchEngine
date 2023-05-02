package pet.diploma.sitesearchengine.services;

import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import pet.diploma.sitesearchengine.model.Lemma;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MorphologyServiceImpl {

    public Map<String, Integer> getNormalFormsMap(String text) throws IOException {
        Map<String, Integer> normalFormsMap = new TreeMap<>();
        getNumbersFormsMap(normalFormsMap, text);
        getRussianFormsMap(normalFormsMap, text);
        getEnglishFormsMap(normalFormsMap, text);
        return normalFormsMap;
    }

    public Set<String> getNormalFormsList(String text) throws IOException {
        long m = System.currentTimeMillis();
        Set<String> result = new HashSet<>();
        Pattern pattern = Pattern.compile("\\b*(?:[-+0-9]\\d*|0)?(?:\\.\\d+)?\\b*");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String number = matcher.group();
            if (!number.trim().isEmpty() && !number.equals("-")) {
                result.add(number);
            }
        }
        LogManager.getLogger("search").info("Анализ цифр за " + (double)(System.currentTimeMillis() - m) / 1000 + " сек.");
        m = System.currentTimeMillis();
        String[] englishWords = text.replaceAll("[^[a-zA-Z]]", "^").toLowerCase().split("\\^");
        String[] russianWords = text.replaceAll("[^[а-яА-ЯёЁ]]", "^").toLowerCase().split("\\^");
        LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
        Arrays.stream(englishWords).forEach(word -> {
            if (!word.trim().isEmpty()) {
                List<String> normalWordL = englishMorphology.getNormalForms(word);
                for (String s : normalWordL) {
                    if (!s.equals(word)) {
                        word = s;
                        break;
                    }
                }
                String normalWord = englishMorphology.getNormalForms(word).size() <= 1
                        ? word : englishMorphology.getNormalForms(word).get(0);
               result.add(normalWord);
            }
        });
        LogManager.getLogger("search").info("Анализ англ за " + (double)(System.currentTimeMillis() - m) / 1000 + " сек.");
        m = System.currentTimeMillis();
        LuceneMorphology russianMorphology = new RussianLuceneMorphology();
        for (String word : russianWords) {
            if (!word.trim().isEmpty()) {
                Pair<String, Boolean> pair = check(russianMorphology, word);
                Boolean check = pair.getValue();
                if (!check) {
                    result.add(pair.getKey());
                }
            }
        }
        LogManager.getLogger("search").info("Анализ рус за " + (double)(System.currentTimeMillis() - m) / 1000 + " сек.");
        return result;
    }

    public void getNumbersFormsMap(Map<String, Integer> normalFormsMap, String text) {
        Pattern pattern = Pattern.compile("\\b*(?:[-+0-9]\\d*|0)?(?:\\.\\d+)?\\b*");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String number = matcher.group();
            if (!number.trim().isEmpty() && !number.equals("-")) {
                if (normalFormsMap.containsKey(number)) {
                    normalFormsMap.put(number, normalFormsMap.get(number) + 1);
                } else {
                    normalFormsMap.put(number, 1);
                }
            }
        }
    }
    public void getEnglishFormsMap(Map<String, Integer> normalFormsMap, String text) throws IOException {
        String[] englishWords = text.replaceAll("[^[a-zA-Z]]", "^").toLowerCase().split("\\^");
        LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
        Arrays.stream(englishWords).forEach(word -> {
            if (!word.trim().isEmpty()) {
                List<String> normalWordL = englishMorphology.getNormalForms(word);
                for (String s : normalWordL) {
                    if (!s.equals(word)) {
                        word = s;
                        break;
                    }
                }
                String normalWord = englishMorphology.getNormalForms(word).size() <= 1
                        ? word : englishMorphology.getNormalForms(word).get(0);
                if (normalFormsMap.containsKey(normalWord)) {
                    normalFormsMap.put(normalWord, normalFormsMap.get(normalWord) + 1);
                } else {
                    normalFormsMap.put(normalWord, 1);
                }
            }
        });
    }

    public void getRussianFormsMap(Map<String, Integer> normalFormsMap, String text) throws IOException {
        String[] russianWords = text.replaceAll("[^[а-яА-ЯёЁ]]", "^").toLowerCase().split("\\^");
        LuceneMorphology russianMorphology = new RussianLuceneMorphology();
        Arrays.stream(russianWords).forEach(word -> {
            if (!word.trim().isEmpty()) {
                Pair<String, Boolean> pair = check(russianMorphology, word);
                Boolean check = pair.getValue();
                String normalWord = pair.getKey();
                if (!check && !word.contains("-")) {
                    if (normalFormsMap.containsKey(normalWord)) {
                        normalFormsMap.put(normalWord, normalFormsMap.get(normalWord) + 1);
                    } else {
                        normalFormsMap.put(normalWord, 1);
                    }
                }
            }
        });
    }

    private Pair<String, Boolean> check(LuceneMorphology russianMorphology, String word){
        word = word.toLowerCase();
        List<String> normalWordList = russianMorphology.getNormalForms(word);
        for (String s : normalWordList) {
            if(!s.equals(word)){
                word = s.toLowerCase();
                break;
            }
        }
        String normalWord = russianMorphology.getNormalForms(word).get(0);
        boolean check = russianMorphology.getMorphInfo(word).stream().anyMatch(w -> w.contains("СОЮЗ") || w.contains("МЕЖД") || w.contains("ПРЕДЛ") || w.contains("ПРЕДК"));
        return new Pair<>(normalWord, check);
    }

    public Pair<String, Map<String, List<Integer>>> getNormalText(String text, List<Lemma> lemmaList) throws IOException {
        Map<String, List<Integer>> newWordIndex = new HashMap<>();
        text = russianReplace(text, newWordIndex, lemmaList);
        if (lemmaList.stream().anyMatch(lemma -> lemma.getLemma().matches("[a-zA-Z]+"))) {
            text = englishReplace(text, newWordIndex, lemmaList);
        }
        return new Pair<>(text, newWordIndex);
    }

    public String russianReplace(String text, Map<String, List<Integer>> newWordIndex, List<Lemma> lemmaList) throws IOException {
        String copy = text;
        LuceneMorphology russianMorphology = new RussianLuceneMorphology();
        String replaceText = text.replaceAll("[^[а-яА-ЯёЁ]]", "^");
        String lowerCase = replaceText.toLowerCase();
        String[] russianWords = lowerCase.split("\\^");
        char[] textArray = lowerCase.toCharArray();
        char[] tmpArr = replaceText.toCharArray();
        int div = 0;
        for (int i = 0; i < textArray.length; ++i){
            if (textArray[i] != '^') {
                int nextSpaceIndex = new String(textArray).indexOf('^', i);
                if (nextSpaceIndex == -1) {
                    String word = new String(tmpArr).substring(i);
                    String oldWord = word;
                    word = word.toLowerCase();
                    if (Arrays.asList(russianWords).contains(word)) {
                        Pair<String, Boolean> pair = check(russianMorphology, word);
                        if (!pair.getValue()) {
                            word = pair.getKey();
                            copy = getString(copy, newWordIndex, lemmaList, i, div, word, oldWord);
                        }
                    }
                    break;
                } else {
                    if (i != 0 && textArray[i - 1] != '^') {
                        i--;
                    }
                    String word = new String(tmpArr).substring(i, nextSpaceIndex);
                    String oldWord = word;
                    word = word.toLowerCase();
                    if (Arrays.asList(russianWords).contains(word.toLowerCase())) {
                        Pair<String, Boolean> pair = check(russianMorphology, word);
                        if (!pair.getValue()) {
                            word = pair.getKey();
                            String finalWord = word;
                            if (lemmaList.stream().anyMatch(lemma -> lemma.getLemma().equals(finalWord))) {
                                copy = copy.substring(0, i + div) + "<b>" + oldWord + "</b>" + copy.substring(Math.min(i + div + oldWord.length(), textArray.length));
                                if (newWordIndex.containsKey(word)) {
                                    newWordIndex.get(word).add(i);
                                } else {
                                    newWordIndex.put(word, new ArrayList<>(List.of(i)));
                                }
                                div += 7;
                            }
                        }
                    }
                    i = nextSpaceIndex + 1;
                }
            }
        }
        return copy;
    }

    private String getString(String text, Map<String, List<Integer>> newWordIndex, List<Lemma> lemmaList, int i, int div, String word, String oldWord) {
        word = word.toLowerCase();
        String finalWord = word;
        if (lemmaList.stream().anyMatch(lemma -> lemma.getLemma().equals(finalWord))) {
            text = text.substring(0, i + div) + "<b>" + oldWord + "</b>";
            if (newWordIndex.containsKey(word)) {
                newWordIndex.get(word).add(i);
            } else {
                newWordIndex.put(word, new ArrayList<>(List.of(i)));
            }
        }
        return text;
    }

    public String englishReplace(String text, Map<String, List<Integer>> newWordIndex, List<Lemma> lemmaList) throws IOException {
        String copy = text;
        LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
        String replaceText = text.replaceAll("[^[a-zA-Z]]", "^");
        String lowerCase = replaceText.toLowerCase();
        String[] englishWords = lowerCase.split("\\^");
        char[] textArray = lowerCase.toCharArray();
        char[] tmpArr = replaceText.toCharArray();
        int div = 0;
        for (int i = 0; i < textArray.length; ++i){
            if (textArray[i] != '^') {
                int nextSpaceIndex = new String(textArray).indexOf('^', i);
                if (nextSpaceIndex == -1) {
                    String word = new String(tmpArr).substring(i);
                    String oldWord = word;
                    word = word.toLowerCase();
                    if (Arrays.asList(englishWords).contains(word)) {
                        word = englishMorphology.getNormalForms(word).get(0);
                        copy = getString(copy, newWordIndex, lemmaList, i, div, word, oldWord);
                    }
                } else {
                    if (i != 0 && textArray[i - 1] != '^') {
                        i--;
                    }
                    String word = new String(tmpArr).substring(i, nextSpaceIndex);
                    String oldWord = word;
                    word = word.toLowerCase();
                    if (!word.equals("") && Arrays.asList(englishWords).contains(word)) {
                        word = englishMorphology.getNormalForms(word).get(0);
                        String finalWord = word;
                        if (lemmaList.stream().anyMatch(lemma -> lemma.getLemma().equals(finalWord))) {
                            copy = copy.substring(0, i + div) + "<b>" + oldWord + "</b>" + copy.substring(Math.min(i + div + oldWord.length(), textArray.length));
                            if (newWordIndex.containsKey(word)) {
                                newWordIndex.get(word).add(i);
                            } else {
                                newWordIndex.put(word, new ArrayList<>(List.of(i)));
                            }
                            div += 7;
                        }
                    }
                    i = nextSpaceIndex + 1;
                }
            }
        }
        return copy;
    }
}
