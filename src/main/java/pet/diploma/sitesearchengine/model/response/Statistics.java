package pet.diploma.sitesearchengine.model.response;

import lombok.Getter;
import pet.diploma.sitesearchengine.repositories.DBConnection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Statistics {
    private final Total total;
    private final List<DetailedSite> detailed;

    public Statistics(boolean isIndexing, int userId) throws SQLException {
        List<Integer> siteIdList = new ArrayList<>();
        detailed = DBConnection.getDBStatistic(userId, siteIdList);
        total = new Total(isIndexing, userId, siteIdList);
    }
}
