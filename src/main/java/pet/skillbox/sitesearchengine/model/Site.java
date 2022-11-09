package pet.skillbox.sitesearchengine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "site")
public class Site implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(length = 65535, columnDefinition = "VARCHAR(255)", nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "sites")
    private Set<User> users;

    public Site(Status status, LocalDateTime statusTime, String lastError, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
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
