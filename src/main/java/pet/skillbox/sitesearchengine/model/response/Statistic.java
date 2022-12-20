package pet.skillbox.sitesearchengine.model.response;

import lombok.Getter;

import java.sql.SQLException;

@Getter
public class Statistic {
    private final boolean result;
    private final Statistics statistics;
    private final int userId;

    public Statistic(boolean isIndexing, int userId) throws SQLException {
        this.userId = userId;
        result = true;
        statistics = new Statistics(isIndexing, userId);
    }
}

