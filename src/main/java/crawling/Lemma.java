package crawling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "lemma")
public class Lemma implements Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 65535, columnDefinition = "VARCHAR(255)")
    private String lemma;

    @Column
    private int frequency;

    public Lemma() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lemma)) return false;
        Lemma lemma1 = (Lemma) o;
        return getFrequency() == lemma1.getFrequency()  &&
                Objects.equals(getLemma(), lemma1.getLemma());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLemma());
    }

    @Override
    public int compareTo(Lemma o) {
        return this.getLemma().compareTo(o.getLemma());
    }

    @Override
    public String toString() {
        return "Lemma{" +
                "id=" + id +
                ", lemma='" + lemma + '\'' +
                ", frequency=" + frequency +
                '}';
    }

    public static int getLemmaIdByName(List<Lemma> lemmaList, String name){
        for (Lemma lemma :lemmaList){
            if (lemma.getLemma().equals(name)){
                return lemma.getId();
            }
        }
        return 0;
    }

    public static Lemma getLemmaByName(List<Lemma> lemmaList, String name){
        for (Lemma lemma :lemmaList){
            if (lemma.getLemma().equals(name)){
                return lemma;
            }
        }
        return null;
    }

//    public static Lemma getLemmaByName(List<Lemma> lemmaList, String name){
//        int pos = Arrays.binarySearch(lemmaList.toArray(), name);
//        return pos >= 0 ? lemmaList.get(pos) : null;
//    }
}
