package pet.skillbox.sitesearchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "`index`")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "page_id")
    private int pageId;

    @Column(name = "lemma", length = 65535, columnDefinition = "VARCHAR(255)")
    private String lemma;

    @Column(name = "`rank`")
    private float rank;

    @Column(name = "site_id")
    private int siteId;

    public Index() {

    }

    public Index(int pageId, String lemma, float rank) {
        this.pageId = pageId;
        this.lemma = lemma;
        this.rank = rank;
    }
}
