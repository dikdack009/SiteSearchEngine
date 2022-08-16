package pet.skillbox.sitesearchengine.repositories;

import pet.skillbox.sitesearchengine.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class DBConnection {
    private static Connection connection;

    private static String name = "search_engine";
    private static String user = "root";
    private static String pass = "89257044306mV";

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + name +
                        "?user=" + user + "&password=" + pass);
                createFieldTable();
                createPageTable();
                createLemmaTable();
                createIndexTable();
                createSiteTable();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    private static void createFieldTable() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS field");
        connection.createStatement().execute("create table field (" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "name VARCHAR(255) NOT NULL, " +
                "selector VARCHAR(255) NOT NULL, " +
                "weight FLOAT NOT NULL, " +
                "PRIMARY KEY(id))");
        connection.createStatement().execute("INSERT INTO field(name, selector, weight) " +
                "VALUES( 'title', 'title', 1.0)");
        connection.createStatement().execute("INSERT INTO field(name, selector, weight) " +
                "VALUES( 'body', 'body', 0.8)");
    }

    private static void createPageTable() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS page");
        connection.createStatement().execute("create table page (" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "path TEXT NOT NULL, " +
                "code INT NOT NULL, " +
                "content MEDIUMTEXT NOT NULL, " +
                "site_id INT NOT NULL, " +
                "PRIMARY KEY(id), " +
                "UNIQUE KEY pair_id (path(50), site_id))");
    }

    private static void createLemmaTable() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS lemma");
        connection.createStatement().execute("create table lemma (" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "lemma VARCHAR(255) NOT NULL, " +
                "frequency INT NOT NULL, " +
                "site_id INT NOT NULL, " +
                "PRIMARY KEY(id), " +
                "UNIQUE KEY(lemma(50), site_id))");
    }

    private static void createIndexTable() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS `index`");
        connection.createStatement().execute("create table `index` (" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "page_id INT NOT NULL, " +
                "lemma VARCHAR(255) NOT NULL, " +
                "`rank` FLOAT NOT NULL, " +
                "PRIMARY KEY(id), " +
                "UNIQUE KEY(page_id, lemma(50)))");
    }

    private static void createSiteTable() throws SQLException {
        connection.createStatement().execute("DROP TABLE IF EXISTS site");
        connection.createStatement().execute("create table site (" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "status ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL, " +
                "status_time DATETIME NOT NULL, " +
                "last_error TEXT, " +
                "url VARCHAR(255) NOT NULL, " +
                "name VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY(id), KEY (url))");
    }

    public static void insert(Builder builder) throws SQLException {
        insertAllPages(builder.getPageBuilder().toString());
        insertAllLemmas(builder.getLemmaBuilder().toString());
        insertAllIndexes(builder.getIndexBuilder().toString());
    }

    public static void insertSite(String site) throws SQLException {
        String sql = "INSERT INTO site(id, status, status_time, last_error, url, name) " +
                "VALUES" + site;
        getConnection().createStatement().execute(sql);
    }

    public static List<Field> getAllFields() throws SQLException {
        ResultSet rs = null;
        List<Field> fieldList = new ArrayList<>();
        try {
            rs = getConnection().createStatement().executeQuery("SELECT * FROM field");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (true) {
            assert rs != null;
            if (!rs.next()) break;
            Field field = new Field(rs.getString("name"),
                    rs.getString("selector"),
                    rs.getFloat("weight"));
            fieldList.add(field);
        }
        return fieldList;
    }

    public static int getSiteIdByPath(String path) throws SQLException {
        ResultSet rs = null;
        try {
            rs = getConnection().createStatement().executeQuery("SELECT id FROM site WHERE url = '" + path + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int id = 0;
        while (true) {
            assert rs != null;
            if (!rs.next()) break;
            id = rs.getInt("id");
        }
        return id;
    }

    public static List<Page> getPagesFromResultSet(ResultSet rs) throws SQLException {
        List<Page> pageList = new ArrayList<>();
        while (rs.next()) {
            Page page = new Page(rs.getInt("id"),
                    rs.getString("path"),
                    rs.getInt("code"),
                    rs.getString("content"),
                    rs.getInt("site_id"));
            pageList.add(page);
        }
        return pageList;
    }

    public static double getPageRank(Set<Lemma> lemmaSet, int pageId) throws SQLException {
        StringJoiner stringJoiner = new StringJoiner("' OR i.lemma = '",
                "SELECT SUM(i.rank) AS q FROM `index` AS i WHERE i.page_id = '" + pageId + "' AND (i.lemma = '" ,
                "')");
        lemmaSet.forEach(l -> stringJoiner.add(l.getLemma()));
        ResultSet rs = getConnection().createStatement().executeQuery(stringJoiner.toString());
        double rank = 0;
        while (true) {
            assert rs != null;
            if (!rs.next()) break;
            rank = rs.getDouble("q");
        }
        return rank;
    }

    public static List<Lemma> getRequestLemmas(Set<String> lemmas, int siteId) throws SQLException {
        StringJoiner stringJoiner = new StringJoiner(
                "' OR lemma = '",
                "SELECT * FROM lemma WHERE lemma = '",
                "' AND site_id = " + siteId + " ORDER BY lemma");
        lemmas.forEach(stringJoiner::add);
        ResultSet rs = null;
        List<Lemma> lemmaList = new ArrayList<>();
        try {
            System.out.println(stringJoiner.toString());
            rs = getConnection().createStatement().executeQuery(stringJoiner.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (true) {
            assert rs != null;
            if (!rs.next()) break;
            Lemma lemma = new Lemma(rs.getInt("id"),
                    rs.getString("lemma"),
                    rs.getInt("frequency"),
                    siteId);
            lemmaList.add(lemma);
        }
        return lemmaList;
    }

    public static List<Page> getPagesFromRequest(Set<Lemma> lemmaSet, int siteId) throws SQLException {
        StringJoiner lemmas = new StringJoiner("') IN (SELECT lemma FROM `index` AS i where page.id = i.page_id) and ('",
                "SELECT * FROM page where ('", "') IN (SELECT lemma FROM `index` AS i where page.id = i.page_id) and site_id = ");
        for (Lemma lemma : lemmaSet) {
            lemmas.add(lemma.getLemma());
        }
        String sql = lemmas.toString() + siteId;
        ResultSet rs = null;
        try {
            rs = getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert rs != null;
        return getPagesFromResultSet(rs);
    }

    public static int getPageNumber(int siteId) throws SQLException {
        ResultSet rs = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM page WHERE site_id = " + siteId;
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

    public static int getMaxPageId() throws SQLException {
        ResultSet rs = null;
        try {
            String sql = "SELECT MAX(id) AS c FROM page ";
            rs = getConnection().createStatement().executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert rs != null;
        int c = 1;
        while (rs.next()) {
            c = rs.getInt("c");
        }
        return c;
    }

    public static void insertAllLemmas(String lemmas) throws SQLException {
        String sql = "INSERT INTO lemma(lemma, frequency, site_id) " +
                "VALUES" + lemmas +
                "ON DUPLICATE KEY UPDATE frequency=frequency + 1";
        getConnection().createStatement().execute(sql);
    }

    public static void insertAllPages(String pages) throws SQLException {
        String sql = "INSERT INTO page(id, path, code, content, site_id)  " +
                "VALUES" + pages;
        getConnection().createStatement().execute(sql);
    }

    public static void insertAllIndexes(String indexes) throws SQLException {
        String sql = "INSERT INTO `index`(page_id, lemma, `rank`) " +
                "VALUES" + indexes +
                "AS new ON DUPLICATE KEY UPDATE `index`.`rank`=`index`.`rank` + new.`rank`";
        getConnection().createStatement().execute(sql);
    }
}
