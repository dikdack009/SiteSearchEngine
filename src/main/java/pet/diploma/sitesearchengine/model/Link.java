package pet.diploma.sitesearchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "link", uniqueConstraints = {@UniqueConstraint(name = "UniqueLinkAndNameAndUserId",
        columnNames = {"link", "name", "user_id"})})
@Entity
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(length = 65535, columnDefinition = "TEXT")
    private String link;
    @Column(length = 65535, columnDefinition = "TEXT")
    private String name;
    @Column(columnDefinition = "TINYINT(1)")
    private int isSelected;
    @Column(name = "user_id", columnDefinition = "INT default 0", nullable = false)
    private int userId;

    public Link(String link, String name, int isSelected, int userId) {
        this.link = link;
        this.name = name;
        this.isSelected = isSelected;
        this.userId = userId;
    }
}
