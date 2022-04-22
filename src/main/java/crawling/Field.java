package crawling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "field")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 65535, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(length = 65535, columnDefinition = "VARCHAR(255)")
    private String selector;

    @Column
    private float weight;

    public Field() {

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
