package pet.diploma.sitesearchengine.model.response;

import lombok.Getter;

import java.sql.SQLException;

@Getter
public class Statistic {
    private final boolean result;
    private final Statistics statistics;

    public Statistic(boolean isIndexing, int userId) throws SQLException {
        result = true;
        statistics = new Statistics(isIndexing, userId);
    }
}

