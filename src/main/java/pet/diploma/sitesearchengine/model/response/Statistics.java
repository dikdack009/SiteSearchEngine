package pet.diploma.sitesearchengine.model.response;

import lombok.Getter;
import pet.diploma.sitesearchengine.repositories.DBConnection;

import java.sql.SQLException;
import java.util.List;

@Getter
public class Statistics {
    private final Total total;
    private final List<DetailedSite> detailed;
    private final int userId;

    public Statistics(boolean isIndexing, int userId) throws SQLException {
        total = new Total(isIndexing, userId);
        this.userId = userId;
        detailed = DBConnection.getDBStatistic(userId);
    }
}
