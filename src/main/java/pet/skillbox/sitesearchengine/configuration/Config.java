package pet.skillbox.sitesearchengine.configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

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
    private boolean stopIndexing;

}
