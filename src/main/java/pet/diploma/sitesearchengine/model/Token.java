package pet.diploma.sitesearchengine.model;

import lombok.*;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "login", nullable = false)
    private String login;
    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    public Token(String login, String refreshToken) {
        this.login = login;
        this.refreshToken = refreshToken;
    }
}
