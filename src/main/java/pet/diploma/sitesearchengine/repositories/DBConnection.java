package pet.diploma.sitesearchengine.repositories;

import pet.diploma.sitesearchengine.model.Builder;
import pet.diploma.sitesearchengine.model.Lemma;
import pet.diploma.sitesearchengine.model.Page;
import pet.diploma.sitesearchengine.model.Site;

import java.sql.*;
import java.util.*;

public class DBConnection {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            try {
                String name = "search_engine";
                String user = "root";
                String pass = "89257044306mV";
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + name +
                        "?user=" + user + "&password=" + pass);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static void insert(Builder builder) throws SQLException {
        insertAllLemmas(builder.getLemmaBuilder().toString());
        insertAllIndexes(builder.getIndexBuilder().toString());
    }

    public static List<Page> getPagesFromResultSet(ResultSet rs) throws SQLException {
        List<Page> pageList = new ArrayList<>();
        Site site = new Site();
        while (rs.next()) {
            site.setId(rs.getInt("site_id"));
            Page page = new Page(rs.getInt("id"),
                    rs.getString("path"),
                    rs.getInt("code"),
                    rs.getString("content"),
                    site,
                    rs.getInt("is_deleted"));
            pageList.add(page);
        }
        return pageList;
    }

    public static double getPageRank(List<Lemma> lemmaList, int pageId) throws SQLException {
        StringJoiner stringJoiner = new StringJoiner("' OR i.lemma = '",
                "SELECT SUM(i.rank) AS q FROM `index` AS i WHERE i.page_id = '" + pageId + "' AND (i.lemma = '" ,
                "')");
        lemmaList.forEach(l -> stringJoiner.add(l.getLemma()));
        ResultSet rs = getConnection().createStatement().executeQuery(stringJoiner.toString());
        double rank = 0;
        while (true) {
            assert rs != null;
            if (!rs.next()) break;
            rank = rs.getDouble("q");
        }
        return rank;
    }

    public static List<Page> getPagesFromRequest(List<Lemma> lemmaSet, int siteId) throws SQLException {
        System.out.println(lemmaSet);
        StringBuilder result = new StringBuilder("SELECT * FROM page where ('");
        for (Lemma lemma : lemmaSet) {
            result.append(lemma.getLemma()).append("') IN (SELECT lemma FROM `index` AS i where page.id = i.page_id and i.is_deleted = 0 and i.lemma = '")
                    .append(lemma.getLemma()).append("') and ('");
        }
        result.delete(result.length() - 9, result.length() - 1);
        if (siteId > 0){
            result.append("and site_id = ").append(siteId);
        }
        result.append(")");
        System.out.println(result);
        ResultSet rs = null;
        try {
            rs = getConnection().createStatement().executeQuery(result.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert rs != null;
        return getPagesFromResultSet(rs);
    }

    public static void insertAllLemmas(String lemmas) throws SQLException {
        String sql = "INSERT INTO lemma(lemma, frequency, site_id, is_deleted) " +
                "VALUES " + lemmas + " ON DUPLICATE KEY UPDATE frequency=frequency + 1";
        getConnection().createStatement().execute(sql);
    }

    public static void insertAllIndexes(String indexes) throws SQLException {
        String sql = "INSERT INTO `index`(page_id, lemma, `rank`, site_id, is_deleted) " +
                "VALUES " + indexes +
                " AS new ON DUPLICATE KEY UPDATE `index`.`rank`=`index`.`rank` + new.`rank`";
        getConnection().createStatement().execute(sql);
    }


    public static Site getSiteById(int id) throws SQLException {
        ResultSet rs = null;
        try {
            rs = getConnection().createStatement().executeQuery("SELECT * FROM site WHERE id = " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Site site = new Site();
        while (true) {
            assert rs != null;
            if (!rs.next()) break;
            String url = rs.getString("url");
            String name = rs.getString("name");
            site.setUrl(url);
            site.setName(name);

        }
        return site;
    }
}
