package pet.skillbox.sitesearchengine.model.response;
import lombok.Getter;
import pet.skillbox.sitesearchengine.repositories.*;

import java.sql.SQLException;

@Getter
public class Total {
    private final int sites;
    private final int pages;
    private final int lemmas;
    private final boolean isIndexing;

    public Total(boolean isIndexing) throws SQLException {
        sites = DBConnection.countSites(0);
        pages = DBConnection.countPages(0);
        lemmas = DBConnection.countLemmas(0);
        this.isIndexing = isIndexing;
    }
}
