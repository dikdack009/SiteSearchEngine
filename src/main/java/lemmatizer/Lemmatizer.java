package lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class Lemmatizer {

    public static Map<String, Integer> getNormalFormsList(String text) throws IOException {

        Map<String, Integer> normalFormsMap = new TreeMap<>();

        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        String[] words = text.replaceAll("[^[а-яА-Я ]]", " ")
                .toLowerCase().split("\\s+");

        Arrays.stream(words).forEach(word -> {
            String[] tmp;
            if (!word.trim().isEmpty()) {
                List<String> normalWordL = luceneMorph.getNormalForms(word);
                for (String s : normalWordL) {
                    if(!s.equals(word)){
                        word = s;
                        break;
                    }
                }

                tmp = luceneMorph.getMorphInfo(word).get(0).split("\\s+");
                String wordType = tmp[1];
                boolean check1 = wordType.equals("СОЮЗ");
                boolean check2 = wordType.equals("МЕЖД");
                boolean check3 = wordType.equals("ПРЕДЛ");
                boolean check4 = wordType.equals("ПРЕДК");
                if (!(check1 || check2 || check3 || check4) && !word.contains("-")) {
                    String normalWord = luceneMorph.getNormalForms(word).get(0);
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
}
