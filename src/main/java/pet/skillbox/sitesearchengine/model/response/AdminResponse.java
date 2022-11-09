package pet.skillbox.sitesearchengine.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import pet.skillbox.sitesearchengine.model.User;

import java.util.List;

@Data
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminResponse {
    List<User> users;

    public AdminResponse(List<User> users) {
        this.users = users;
    }
}
