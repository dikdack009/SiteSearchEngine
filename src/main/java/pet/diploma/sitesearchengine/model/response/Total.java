package pet.diploma.sitesearchengine.model.response;
import lombok.Getter;
import pet.diploma.sitesearchengine.repositories.DBConnection;

import java.sql.SQLException;
import java.util.List;

@Getter
public class Total {
    private final int sites;
    private final int pages;
    private final int lemmas;
    private final boolean isIndexing;

    public Total(boolean isIndexing, int userId, List<Integer> siteIdList) throws SQLException {
        int sites = 0;
        int pages = 0;
        int lemmas = 0;
        for (Integer id : siteIdList) {
            pages += DBConnection.countPages(id);
            lemmas += DBConnection.countLemmas(id);
        }
        this.sites = DBConnection.countSites(userId);;
        this.pages = pages;
        this.lemmas = lemmas;
        this.isIndexing = isIndexing;
    }
}
