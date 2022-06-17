package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lemma")
public class Lemma implements Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 65535, columnDefinition = "VARCHAR(255)")
    @PrimaryKeyJoinColumn
    private String lemma;

    @Column
    private int frequency;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lemma)) return false;
        Lemma lemma1 = (Lemma) o;
        return Objects.equals(getLemma(), lemma1.getLemma());
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

    public static Lemma getLemmaByName(List<Lemma> lemmaList, String name){
        Lemma[] array = lemmaList.toArray(new Lemma[0]);
        Arrays.sort(array);
        int t = Arrays.binarySearch(array, new Lemma(0, name, 0));
        return t >= 0 ? array[t] : null;
    }
}
