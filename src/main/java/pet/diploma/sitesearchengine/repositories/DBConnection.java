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

    public static List<Integer> getPagesFromResultSet(ResultSet rs) throws SQLException {
        List<Integer> pageIdList = new ArrayList<>();
        while (rs.next()) {
            pageIdList.add(rs.getInt("id"));
        }
        return pageIdList;
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

    public static List<Integer> getPagesFromRequest(List<Lemma> lemmaSet, Set<Integer> idList) throws SQLException {
        System.out.println(lemmaSet);
        StringBuilder result = new StringBuilder("SELECT p.id FROM page as p where p.id");
        for (Lemma lemma : lemmaSet) {
            result.append(" IN (SELECT page_id FROM `index` AS i where i.is_deleted = 0 and i.lemma = '")
                    .append(lemma.getLemma()).append("') and p.id ");
        }
        result.delete(result.length() - 11, result.length() - 1);
        result.append(" and (");
        StringJoiner s = new StringJoiner(" or ");
        for (Integer siteId : idList){
            s.add("i.site_id = " + siteId);
        }
        result.append(s).append("))");
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
}
