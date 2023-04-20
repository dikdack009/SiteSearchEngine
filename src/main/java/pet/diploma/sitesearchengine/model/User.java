package pet.diploma.sitesearchengine.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private int id;
    @Column(nullable = false, unique = true)
    private String login;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private Role roles;
    @Getter
    @Column(name = "email_checked", columnDefinition = "BOOLEAN DEFAULT false", nullable = false)
    private boolean emailChecked;
    @Getter
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean notify;

    public User(int id, String login, String password, Role roles, boolean emailChecked) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.roles = roles;
        this.emailChecked = emailChecked;
        this.notify = true;
    }
}
