package pet.diploma.sitesearchengine.model.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Data
public class IndexingRequest {
    private Map<String, String> data;
}
