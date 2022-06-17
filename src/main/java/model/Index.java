package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "`index`")
@PrimaryKeyJoinColumns({
        @PrimaryKeyJoinColumn(name="page_id"),
        @PrimaryKeyJoinColumn(name="lemma_id")})
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "page_id")
    private int pageId;

    @Column(name = "lemma_id")
    private int lemmaId;

    @Column(name = "`rank`")
    private float rank;

    public Index() {

    }
}
