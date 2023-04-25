package pet.diploma.sitesearchengine.model.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import pet.diploma.sitesearchengine.model.Site;
import pet.diploma.sitesearchengine.repositories.DBConnection;
import pet.diploma.sitesearchengine.services.CrawlingService;

import java.sql.SQLException;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Total {
    private final int sites;
    private final int pages;
    private final int lemmas;
    private final boolean isIndexing;
}
