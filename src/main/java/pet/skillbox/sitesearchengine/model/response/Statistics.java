package pet.skillbox.sitesearchengine.model.response;

import lombok.Getter;
import pet.skillbox.sitesearchengine.repositories.DBConnection;

import java.sql.SQLException;
import java.util.List;

@Getter
public class Statistics {
    private final Total total;
    private final List<DetailedSite> detailed;

    public Statistics(boolean isIndexing) throws SQLException {
        total = new Total(isIndexing);
        detailed = DBConnection.getDBStatistic();
    }
}
