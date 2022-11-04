package pet.skillbox.sitesearchengine.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {
    private final boolean result;
    private final Integer count;
    private final List<Data> data;
    private final String error;

    public SearchResponse(boolean result, Integer count, List<Data> data, String error) {
        this.result = result;
        this.count = count;
        this.data = data;
        this.error = error;
    }
}
