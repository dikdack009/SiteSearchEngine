package pet.diploma.sitesearchengine.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SiteProperty {
    private String url;
    private String name;
    private String email;
}
