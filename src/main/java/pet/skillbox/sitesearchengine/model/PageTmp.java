package pet.skillbox.sitesearchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "page_tmp")
public class PageTmp implements Comparable<Page>{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false, unique=true)
    private int id;

    @Column(length = 65535, columnDefinition = "TEXT", unique = true)
    private String path;

    @Column
    private int code;

    @Column(length = 16777215, columnDefinition = "MEDIUMTEXT")
    private String content;

    @JoinColumn(name = "site_id", nullable = false)
    @ManyToOne (cascade=CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

    public PageTmp(String path, int code, String content, Site site) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.site = site;
    }

    public PageTmp(String path, int code, String content) {
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
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        Page page1 = (Page) o;
        return Objects.equals(getSite(), page1.getSite()) && Objects.equals(getPath(), page1.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getSite().getId());
    }

    @Override
    public int compareTo(@NotNull Page o) {
        return this.getPath().compareTo(o.getPath())
                *this.getSite().getId().compareTo(o.getSite().getId());
    }
}
