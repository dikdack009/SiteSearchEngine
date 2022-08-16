package pet.skillbox.sitesearchengine.model;

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

    @Column(length = 65535, columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    @PrimaryKeyJoinColumn
    private String lemma;

    @Column(nullable = false)
    private Integer frequency;

    @Column(name = "site_id", nullable = false)
    private Integer siteId;

    public Lemma(String lemma, int frequency, int siteId) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteId = siteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lemma)) return false;
        Lemma lemma1 = (Lemma) o;
        return Objects.equals(getSiteId(), lemma1.getSiteId()) && getLemma().equals(lemma1.getLemma());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLemma(), getSiteId());
    }

    @Override
    public int compareTo(Lemma o) {
        return this.getLemma().compareTo(o.getLemma())*this.getSiteId().compareTo(o.getSiteId());
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
        int t = Arrays.binarySearch(array, new Lemma(0, name, 0, 1));
        return t >= 0 ? array[t] : null;
    }
}
