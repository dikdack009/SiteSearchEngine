package pet.diploma.sitesearchengine.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndexingResponse {
    @Getter
    private boolean result;
    @Getter
    private String error;

    public IndexingResponse(Boolean result, String error) {
        this.result = result;
        this.error = error;
    }

}
