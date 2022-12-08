package pet.skillbox.sitesearchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "`index`", uniqueConstraints = {@UniqueConstraint(name = "UniquePageIdAndLemmaAndDeleted",
        columnNames = {"page_id", "lemma", "is_deleted"})})
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "page_id")
    private int pageId;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "`rank`")
    private float rank;

    @Column(name = "is_deleted", columnDefinition = "INT default 0", nullable = false)
    private Integer isDeleted;

    @JoinColumn(name = "site_id", nullable = false)
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

    public Index() {

    }

    public Index(int pageId, String lemma, float rank) {
        this.pageId = pageId;
        this.lemma = lemma;
        this.rank = rank;
    }
}
