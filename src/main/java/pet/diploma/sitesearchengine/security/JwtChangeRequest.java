package pet.diploma.sitesearchengine.security;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JwtChangeRequest {
    private String login;
    private String password;
    private String newPassword;
}
