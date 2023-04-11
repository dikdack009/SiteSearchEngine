package pet.diploma.sitesearchengine.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtResponse {

    private final String type;
    private final String accessToken;
    private final String refreshToken;
    private final String error;

    public JwtResponse(String accessToken, String refreshToken) {
        this.type = "Bearer";
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.error = null;
    }

    public JwtResponse(String error) {
        this.type = null;
        this.accessToken = null;
        this.refreshToken = null;
        this.error = error;
    }
}
