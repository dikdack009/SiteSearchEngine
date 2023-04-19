package pet.diploma.sitesearchengine.services;

import javafx.util.Pair;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MorphologyServiceImpl {

    public Map<String, Integer> getNormalFormsList(String text) throws IOException {
        Map<String, Integer> normalFormsMap = new TreeMap<>();

        String[] russianWords = text.replaceAll("[^[а-яА-Я]]", "-").toLowerCase().split("-");
        String[] englishWords = text.replaceAll("[^[a-zA-Z]]", "-").toLowerCase().split("-");
        Pattern pattern = Pattern.compile("\\b*(?:[-+0-9]\\d*|0)?(?:\\.\\d+)?\\b*");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String number = matcher.group();
            if (!number.trim().isEmpty()) {
                if (normalFormsMap.containsKey(number)) {
                    normalFormsMap.put(number, normalFormsMap.get(number) + 1);
                } else {
                    normalFormsMap.put(number, 1);
                }
            }
        }
        getEnglishFormsList(normalFormsMap, englishWords);
        getRussianFormsList(normalFormsMap, russianWords);
        return normalFormsMap;
    }

    public void getEnglishFormsList(Map<String, Integer> normalFormsMap, String[] englishWords) throws IOException {
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

    public void getRussianFormsList(Map<String, Integer> normalFormsMap, String[] russianWords) throws IOException {
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

    public Pair<String, Boolean> check(LuceneMorphology russianMorphology, String word){
        List<String> normalWordList = russianMorphology.getNormalForms(word);
        for (String s : normalWordList) {
            if(!s.equals(word)){
                word = s;
                break;
            }
        }

        String normalWord = russianMorphology.getNormalForms(word).get(0);
        boolean check = russianMorphology.getMorphInfo(word).stream()
                .anyMatch(w -> w.contains("СОЮЗ") || w.contains("МЕЖД") ||
                        w.contains("ПРЕДЛ") || w.contains("ПРЕДК"));
        return new Pair<>(normalWord, check);
    }

    public String getNormalText(String text) throws IOException {
        text = text.toLowerCase();
        text = russianReplace(text);
        text = englishReplace(text);
        return text;
    }

    public String russianReplace(String text) throws IOException {
        LuceneMorphology russianMorphology = new RussianLuceneMorphology();
        String[] russianWords = text.replaceAll("[^[а-яА-Я\\-]]", "^").toLowerCase().split("\\^");

        int j;
        int div = 0;
        char[] textArray = text.replaceAll("[^[а-яА-Я]]", "^").toLowerCase().toCharArray();

        for (int i = 0; i < textArray.length; ++i){
            if (textArray[i] != '^') {
                int tmp = new String(textArray).indexOf('^', i);
                if (tmp == -1) {
                    String word = new String(textArray).substring(i);
                    if (Arrays.asList(russianWords).contains(word)) {
                        Pair<String, Boolean> pair = check(russianMorphology, word);
                        if (!pair.getValue()) {
                            word = pair.getKey();
                        }
                    }
                    text = text.substring(0, i - div) + word;
                    break;
                }
                j = tmp;
                String word = new String(textArray).substring(i, j);
                int size = word.length();
                j--;
                if (Arrays.asList(russianWords).contains(word)) {
                    word = russianMorphology.getNormalForms(word).get(0);
                }
                text = text.substring(0, i - div) + word + text.substring(j + 1 - div);
                div += - word.length() + size;
                i = j + 1 ;
            }
        }
        return text;
    }

    public String englishReplace(String text) throws IOException {
        LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
        String[] englishWords = text.replaceAll("[^[a-zA-Z\\-]]", "^")
                .toLowerCase().split("\\^");
        int j;
        char[] textArray = text.replaceAll("[^[a-zA-Z\\-]]", "^").toLowerCase().toCharArray();
        int div = 0;

        for (int i = 0; i < textArray.length; ++i){
            if (textArray[i] != '^') {
                int tmp = new String(textArray).indexOf('^', i);

                if (tmp == -1) {
                    String word = new String(textArray).substring(i);
                    if (Arrays.asList(englishWords).contains(word)) {
                        word = englishMorphology.getNormalForms(word).get(0);
                    }
                    text = text.substring(0, i - div) + word;
                    break;
                }
                j = tmp;
                String word = new String(textArray).substring(i, j);
                int size = word.length();
                j--;
                if (Arrays.asList(englishWords).contains(word)) {
                    word = englishMorphology.getNormalForms(word).get(0);
                }
                text = text.substring(0, i - div) + word + text.substring(j + 1 - div);
                div += - word.length() + size;
                i = j + 1 ;
            }
        }
        return text;
    }
}
