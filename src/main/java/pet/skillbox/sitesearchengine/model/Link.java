package pet.skillbox.sitesearchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "link")
@Entity
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = 65535, columnDefinition = "TEXT", unique = true)
    private String link;

    @Column(length = 65535, columnDefinition = "TEXT", unique = true)
    private String name;

    public Link(String link, String name) {
        this.link = link;
        this.name = name;
    }
}