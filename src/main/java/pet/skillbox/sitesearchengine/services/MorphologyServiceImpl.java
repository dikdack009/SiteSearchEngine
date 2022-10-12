package pet.skillbox.sitesearchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class MorphologyServiceImpl {

    public Map<String, Integer> getNormalFormsList(String text) throws IOException {

        Map<String, Integer> normalFormsMap = new TreeMap<>();
        LuceneMorphology russianMorphology = new RussianLuceneMorphology();
        LuceneMorphology englishMorphology = new EnglishLuceneMorphology();

        String[] russianWords = text.replaceAll("[^[а-яА-Я]]", "-").toLowerCase().split("-");
        String[] englishWords = text.replaceAll("[^[a-zA-Z]]", "-").toLowerCase().split("-");

        Arrays.stream(englishWords).forEach(word -> {
                if (!word.trim().isEmpty()) {
                    if(word.matches("[0-9]")){
                        if (normalFormsMap.containsKey(word)) {
                            normalFormsMap.put(word, normalFormsMap.get(word) + 1);
                        } else {
                            normalFormsMap.put(word, 1);
                        }
                    }
                    else {
                        List<String> normalWordL = englishMorphology.getNormalForms(word);
                        for (String s : normalWordL) {
                            if (!s.equals(word)) {
                                word = s;
                                break;
                            }
                        }

                        if (!englishMorphology.checkString(word)) {
                            System.out.println(word);
                        }
                        String normalWord = englishMorphology.getNormalForms(word).size() <= 1
                                ? word :
                                englishMorphology.getNormalForms(word).get(0);
                        if (normalFormsMap.containsKey(normalWord)) {
                            normalFormsMap.put(normalWord, normalFormsMap.get(normalWord) + 1);
                        } else {
                            normalFormsMap.put(normalWord, 1);
                        }
                    }
            }
        });

        Arrays.stream(russianWords).forEach(word -> {
            String[] tmp;
            if (!word.trim().isEmpty()) {
                List<String> normalWordL = russianMorphology.getNormalForms(word);
                for (String s : normalWordL) {
                    if(!s.equals(word)){
                        word = s;
                        break;
                    }
                }

                tmp = russianMorphology.getMorphInfo(word).get(0).split("\\s+");
                String wordType = tmp[1];
                boolean check1 = wordType.equals("СОЮЗ");
                boolean check2 = wordType.equals("МЕЖД");
                boolean check3 = wordType.equals("ПРЕДЛ");
                boolean check4 = wordType.equals("ПРЕДК");
                if (!(check1 || check2 || check3 || check4) && !word.contains("-")) {
                    String normalWord = russianMorphology.getNormalForms(word).get(0);
                    if (normalFormsMap.containsKey(normalWord)) {
                        normalFormsMap.put(normalWord, normalFormsMap.get(normalWord) + 1);
                    } else {
                        normalFormsMap.put(normalWord, 1);
                    }
                }
            }
        });
        return normalFormsMap;
    }

    public String getNormalText(String text) throws IOException {
        LuceneMorphology russianMorphology = new RussianLuceneMorphology();
        LuceneMorphology englishMorphology = new EnglishLuceneMorphology();

        String[] russianWords = text.replaceAll("[^[а-яА-Я]]", "-").toLowerCase().split("-");
        String[] englishWords = text.replaceAll("[^[a-zA-Z]]", "-").toLowerCase().split("-");

        for (String word : russianWords)  {
            if (!word.isEmpty()) {
                String[] tmp;
                List<String> normalWordL = russianMorphology.getNormalForms(word);
                for (String s : normalWordL) {
                    if (!s.equals(word)) {
                        word = s;
                        break;
                    }
                }
                String normalWord = russianMorphology.getNormalForms(word).get(0);
                tmp = normalWord.split("\\s+");
                System.out.println(Arrays.toString(tmp));
                String wordType = tmp[1];
                boolean check1 = wordType.equals("СОЮЗ");
                boolean check2 = wordType.equals("МЕЖД");
                boolean check3 = wordType.equals("ПРЕДЛ");
                boolean check4 = wordType.equals("ПРЕДК");
                if (!(check1 || check2 || check3 || check4) && !word.contains("-")) {
                    text = text.replace(word, normalWord);
                }
            }
        }
        for (String word : englishWords)  {
            if (!word.isEmpty()) {
                List<String> normalWordL = englishMorphology.getNormalForms(word);
                for (String s : normalWordL) {
                    if (!s.equals(word)) {
                        word = s;
                        break;
                    }
                }
                String normalWord = russianMorphology.getNormalForms(word).get(0);
                text = text.replace(word, normalWord);
            }
        }
        return text;
    }
}
