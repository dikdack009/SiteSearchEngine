package pet.skillbox.sitesearchengine.repositories;

import lombok.Setter;
import pet.skillbox.sitesearchengine.model.*;
import pet.skillbox.sitesearchengine.model.response.DetailedSite;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;

public class DBConnection {
    private static Connection connection;

    private static String name = "search_engine";
    private static String user = "root";
    private static String pass = "89257044306mV";
    @Setter
    private static boolean createTables;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + name +
                        "?user=" + user + "&password=" + pass);
//                if (createTables) {
//                    createFieldTable();
//                    createPageTable();
//                    createLemmaTable();
//                    createIndexTable();
//                    createSiteTable();
//                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void insert(Builder builder) throws SQLException {
//        insertAllPages(builder.getPageBuilder().toString());
        insertAllLemmas(builder.getLemmaBuilder().toString());
        insertAllIndexes(builder.getIndexBuilder().toString());
    }

    public static void tmpInsert(Builder builder) throws SQLException {
        tmpInsertAllPages(builder.getPageBuilder().toString());
        tmpInsertAllLemmas(builder.getLemmaBuilder().toString());
        tmpInsertAllIndexes(builder.getIndexBuilder().toString());
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
        StringBuilder result = new StringBuilder("SELECT * FROM page where ('");
        for (Lemma lemma : lemmaSet) {
            result.append(lemma.getLemma()).append("') IN (SELECT lemma FROM `index` AS i where page.id = i.page_id and i.lemma = '")
                    .append(lemma.getLemma()).append("') and ('");
        }
        result.delete(result.length() - 9, result.length() - 1);
        if (siteId > 0){
            result.append(") and site_id = ").append(siteId);
        } else {
            result.append(")");
        }
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
        String sql = "INSERT INTO lemma(lemma, frequency, site_id) " +
                "VALUES" + lemmas +
                "ON DUPLICATE KEY UPDATE frequency=frequency + 1";
        getConnection().createStatement().execute(sql);
    }

    public static void tmpInsertAllLemmas(String lemmas) throws SQLException {
        String sql = "INSERT INTO lemma_tmp(lemma, frequency, site_id) " +
                "VALUES" + lemmas +
                "ON DUPLICATE KEY UPDATE frequency=frequency + 1";
        getConnection().createStatement().execute(sql);
    }

    public static void insertAllPages(String pages) throws SQLException {
        String sql = "INSERT INTO page(id, path, code, content, site_id)  " +
                "VALUES" + pages;
        getConnection().createStatement().execute(sql);
    }

    public static void tmpInsertAllPages(String pages) throws SQLException {
        String sql = "INSERT INTO page_tmp(id, path, code, content, site_id)  " +
                "VALUES" + pages;
        getConnection().createStatement().execute(sql);
    }

    public static void insertAllIndexes(String indexes) throws SQLException {
        String sql = "INSERT INTO `index`(page_id, lemma, `rank`, site_id) " +
                "VALUES" + indexes +
                "AS new ON DUPLICATE KEY UPDATE `index`.`rank`=`index`.`rank` + new.`rank`";
//        System.out.println(sql);
        getConnection().createStatement().execute(sql);
    }

    public static void tmpInsertAllIndexes(String indexes) throws SQLException {
        String sql = "INSERT INTO index_tmp(page_id, lemma, `rank`, site_id) " +
                "VALUES" + indexes +
                "AS new ON DUPLICATE KEY UPDATE index_tmp.`rank`=index_tmp.`rank` + new.`rank`";
//        System.out.println(sql);
        getConnection().createStatement().execute(sql);
    }
    public static int countSites(int siteId) throws SQLException {
        String sql = "SELECT COUNT(distinct id) AS c FROM site WHERE is_deleted = 0";
        return sqlRequest(siteId, sql);
    }

    public static int countLemmas(int siteId) throws SQLException {
        String sql = "SELECT COUNT(distinct id) AS c FROM lemma WHERE is_deleted = 0";
        return sqlRequest(siteId, sql);
    }

    public static int countPages(int siteId) throws SQLException {
        String sql = "SELECT COUNT(distinct id) AS c FROM page WHERE is_deleted = 0";
        return sqlRequest(siteId, sql);
    }

    private static int sqlRequest(int siteId, String sql) throws SQLException {
//        System.out.println(sql);
        ResultSet rs = null;
        if (siteId > 0){
            sql += " and site_id = " + siteId;
        }
//        System.out.println(sql);
        try {
            rs = getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert rs != null;
        int c = 0;
        while (rs.next()) {
            c = rs.getInt("c");
        }
        return c;
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

    public static List<DetailedSite> getDBStatistic() throws SQLException {
        List<DetailedSite> stat = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = getConnection().createStatement().executeQuery("SELECT * FROM site");
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        System.out.println("SELECT id FROM site WHERE site.url = '" + path + "'");
        while (true) {
            assert rs != null;
            if (!rs.next()) break;
            int id = rs.getInt("id");
            String url = rs.getString("url");
            String dateTime = rs.getString("status_time");
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = new Date();
            try {
                date =  simpleDateFormat.parse(dateTime);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            Long time = date.getTime();

            String status = rs.getString("status");
            String name = rs.getString("name");
            String error = rs.getString("last_error");
            stat.add(new DetailedSite(url, name, status, time,
                    error, countPages(id),  countLemmas(id)));
        }
        return stat;
    }

    public static void fromTmpToActualUpdate(int id) throws SQLException {
//        deleteSiteInfo(id);
        String sql = "INSERT INTO `index`(page_id, lemma, `rank`, site_id)" +
                "SELECT page_id, lemma, `rank`, site_id FROM index_tmp WHERE site_id = " + id;
        getConnection().createStatement().execute(sql);
        sql = "INSERT INTO lemma(lemma, frequency, site_id)" +
                "SELECT lemma, frequency, site_id FROM lemma_tmp WHERE site_id = " + id;
        getConnection().createStatement().execute(sql);
        sql = "INSERT INTO page(id, path, code, content, site_id)" +
                "SELECT id, path, code, content, site_id FROM page_tmp WHERE site_id = " + id;
        getConnection().createStatement().execute(sql);
//        deleteTmpSiteInfo(id);
    }
}
