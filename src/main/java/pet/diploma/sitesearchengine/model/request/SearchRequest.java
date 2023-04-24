package pet.diploma.sitesearchengine.model.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
@Getter
@Setter
public class SearchRequest {
    private Set<String> sites;
}
