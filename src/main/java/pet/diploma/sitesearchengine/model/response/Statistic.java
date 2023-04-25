package pet.diploma.sitesearchengine.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;
import pet.diploma.sitesearchengine.services.CrawlingService;

import java.sql.SQLException;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Statistic {
    private final boolean result;
    private final Statistics statistics;

    public Statistic(boolean isIndexing, int userId, CrawlingService crawlingService) {
        result = true;
        statistics = new Statistics(isIndexing, userId, crawlingService);
    }
}

