package pet.diploma.sitesearchengine.model.response;
import lombok.Getter;
import pet.diploma.sitesearchengine.repositories.DBConnection;

import java.sql.SQLException;

@Getter
public class Total {
    private final int sites;
    private final int pages;
    private final int lemmas;
    private final boolean isIndexing;

    public Total(boolean isIndexing, int userId) throws SQLException {
        sites = DBConnection.countSites(0, userId);
        pages = DBConnection.countPages(0, userId);
        lemmas = DBConnection.countLemmas(0, userId);
        this.isIndexing = isIndexing;
    }
}
