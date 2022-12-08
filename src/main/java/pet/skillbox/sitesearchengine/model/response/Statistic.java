package pet.skillbox.sitesearchengine.model.response;

import lombok.Getter;

import java.sql.SQLException;

@Getter
public class Statistic {
    private final boolean result;
    private final Statistics statistics;

    public Statistic(boolean isIndexing) throws SQLException {
        result = true;
        statistics = new Statistics(isIndexing);
    }
}

