package pet.skillbox.sitesearchengine.services;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
public class PropertyProvider {

    @Value("${sites}")
    @Getter
    private static List<SiteProperty> siteList;

    @Value("${user-agent}")
    @Getter
    private static String userAgent;

    @Value("${path}")
    @Getter
    private static String path;
}
