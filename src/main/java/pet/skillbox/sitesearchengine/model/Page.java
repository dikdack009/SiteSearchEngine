package pet.skillbox.sitesearchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "page")
public class Page implements Comparable<Page>{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = 65535, columnDefinition = "TEXT", unique = true)
    private String path;

    @Column
    private int code;

    @Column(length = 16777215, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "site_id", nullable = false)
    private Integer siteId;

    public Page(String path, int code, String content, Integer siteId) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.siteId = siteId;
    }

    public Page(String path, int code, String content) {
        this.path = path;
        this.code = code;
        this.content = content;
    }

    @Override
    public String toString() {
        return "model.Page{" +
                "\tid=" + id + "" +
                "\tpath='" + path + '\'' + "" +
                "\tcode=" + code + "" +
                "\tsiteId=" + siteId + "" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        Page page1 = (Page) o;
        return Objects.equals(getSiteId(), page1.getSiteId()) && Objects.equals(getPath(), page1.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getSiteId());
    }

    @Override
    public int compareTo(@NotNull Page o) {
        return this.getPath().compareTo(o.getPath())*this.getSiteId().compareTo(o.getSiteId());
    }
}
