package pet.skillbox.sitesearchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "field")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 65535, columnDefinition = "VARCHAR(255)", unique = true, nullable = false)
    private String name;

    @Column(length = 65535, columnDefinition = "VARCHAR(255)", unique = true, nullable = false)
    private String selector;

    @Column(nullable = false)
    private float weight;

    public Field(String name, String selector, float weight) {
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Field{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", selector='" + selector + '\'' +
                ", weight=" + weight +
                '}';
    }
}
