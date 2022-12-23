package pet.diploma.sitesearchengine.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Getter
@Component
@ConfigurationProperties(prefix = "local")
public class Config {
    @Getter
    public List<SiteProperty> sites;

//    @Value("${user-agent}")
//    @Getter
//    private String userAgent;
//    vpgsbcogqgoyutsi

    @Getter
    private String path;
    @Getter
    @Setter
    private Map<Integer, Boolean> userIndexing = new HashMap<>();
    @Getter
    @Setter
    private Map<Integer, Boolean> stopIndexing = new HashMap<>();

}
