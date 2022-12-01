package pet.skillbox.sitesearchengine.model;

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
@Table(name = "lemma_tmp")
public class LemmaTmp implements Comparable<Lemma> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 65535, columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    @PrimaryKeyJoinColumn
    private String lemma;

    @Column(nullable = false)
    private Integer frequency;

    @JoinColumn(name = "site_id", nullable = false)
    @ManyToOne (cascade=CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

    public LemmaTmp(String lemma, int frequency,Site site) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.site = site;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lemma)) return false;
        Lemma lemma1 = (Lemma) o;
        return Objects.equals(getSite(), lemma1.getSite()) && getLemma().equals(lemma1.getLemma());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLemma(), getSite().getId());
    }

    @Override
    public int compareTo(Lemma o) {
        return this.getLemma().compareTo(o.getLemma())
                *this.getSite().getId().compareTo(o.getSite().getId());
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
