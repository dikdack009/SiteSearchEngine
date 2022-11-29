package pet.skillbox.sitesearchengine.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import pet.skillbox.sitesearchengine.model.Link;

import java.util.List;

@Data
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinksResponse {
    @Getter
    private boolean result;
    @Getter
    private String error;
    @Getter
    private List<Link> links;

    public LinksResponse(Boolean result, String error, List<Link> links) {
        this.result = result;
        this.error = error;
        this.links = links;
    }
}
