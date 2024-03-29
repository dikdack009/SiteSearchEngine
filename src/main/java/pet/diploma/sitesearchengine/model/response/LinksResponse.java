package pet.diploma.sitesearchengine.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;

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
    private List<LinkModel> links;

    public LinksResponse(Boolean result, String error, List<LinkModel> links) {
        this.result = result;
        this.error = error;
        this.links = links;
    }
}
