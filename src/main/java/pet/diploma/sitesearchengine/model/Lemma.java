package pet.diploma.sitesearchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "lemma", uniqueConstraints = {@UniqueConstraint(name = "UniqueLemmaAndDeleted",
                columnNames = {"lemma", "is_deleted", "site_id"})})
public class Lemma implements Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lemma", length = 65535, columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @Column(nullable = false)
    private Integer frequency;

    @Column(name = "is_deleted", columnDefinition = "INT default 0", nullable = false)
    private Integer isDeleted;

    @JoinColumn(name = "site_id", nullable = false)
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

    public Lemma(String lemma, int frequency, Site site) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.site = site;
    }

    @Override
    public int compareTo(Lemma o) {
        return this.getLemma().compareTo(o.getLemma())
                *this.getSite().getId().compareTo(o.getSite().getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lemma lemma1 = (Lemma) o;
        return Objects.equals(lemma, lemma1.lemma) && Objects.equals(site, lemma1.site);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lemma, site);
    }

    @Override
    public String toString() {
        return "Lemma{" +
                "id=" + id +
                ", lemma='" + lemma + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}
