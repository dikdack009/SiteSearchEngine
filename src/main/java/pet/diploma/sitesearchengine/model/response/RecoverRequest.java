package pet.diploma.sitesearchengine.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RecoverRequest {
    private String login;
    private String password;
    private String code;
}
