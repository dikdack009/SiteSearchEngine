package pet.diploma.sitesearchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "site", uniqueConstraints = {@UniqueConstraint(name = "UniqueUrlAndDeleted",
        columnNames = {"url", "is_deleted", "user_id"})})
public class Site implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "status_time", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(length = 65535, columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;

    @Column(length = 65535, columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @Column(name = "is_deleted", columnDefinition = "INT default 0", nullable = false)
    private Integer isDeleted;

    @Column(name = "user_id", columnDefinition = "INT default 0", nullable = false)
    private int userId;

    @OneToMany(targetEntity = Page.class, mappedBy = "site")
    private Set<Page> pages;

    @OneToMany(targetEntity = Lemma.class, mappedBy = "site")
    private Set<Lemma> lemmas;

    @OneToMany(targetEntity = pet.diploma.sitesearchengine.model.Index.class, mappedBy = "site")
    private Set<Index> indexes;

    public Site(Status status, LocalDateTime statusTime, String lastError, String url, String name, int userId) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
        this.userId = userId;
        pages = new HashSet<>();
        lemmas = new HashSet<>();
        indexes = new HashSet<>();
    }

    public Site(int id, Status status, LocalDateTime statusTime, String lastError, String url, String name, Integer isDeleted, int userId) {
        this.id = id;
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
        this.isDeleted = isDeleted;
        this.userId = userId;
        pages = new HashSet<>();
        lemmas = new HashSet<>();
        indexes = new HashSet<>();
    }

    @Override
    public String getAuthority() {
        return getUrl();
    }

    @Override
    public String toString() {
        return  "( " + id +
                ",'" + status +
                "', '" + statusTime +
                "', '" + lastError + '\'' +
                ", '" + url + '\'' +
                ", '" + name + '\'' +
                ')';
    }
}
