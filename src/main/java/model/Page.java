package model;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "page")
public class Page implements Comparable<Page>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 65535, columnDefinition = "TEXT")
    private String path;

    @Column
    private int code;

    @Column(length = 16777215, columnDefinition = "MEDIUMTEXT")
    private String content;

    public Page() {

    }

    @Override
    public String toString() {
        return "model.Page{" +
                "\tid=" + id + "" +
                "\tpath='" + path + '\'' + "" +
                "\tcode=" + code + "" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        Page page1 = (Page) o;
        return Objects.equals(getId(), page1.getId());
    }

    public static Page getPageById(List<Page> pageList, int id){
        Page[] array = pageList.toArray(new Page[0]);
        Arrays.sort(array);
        int t = Arrays.binarySearch(array, new Page(id, "", 0, ""));
        return t >= 0 ? array[t] : null;
    }

    @Override
    public int compareTo(@NotNull Page o) {
        return this.getId().compareTo(o.getId());
    }
}
