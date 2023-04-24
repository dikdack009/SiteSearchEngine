package pet.diploma.sitesearchengine.model.request;

import lombok.Data;

import java.util.Map;

@Data
public class LinkRequest {
    private Map<String, Integer> data;
}
