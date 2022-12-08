package pet.skillbox.sitesearchengine.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Objects;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "delete_index")
public class DeleteIndex {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Getter
    @Column(columnDefinition = "INT default 1", nullable = false)
    private Integer deleteNumber;
}
