package repository;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class BasicConnectionPool {

    private final static String url = "jdbc:mysql://localhost:3306/search_engine?useSSL=false&amp;serverTimezone=UTC";;
    private final static String user = "root";
    private final static String password = "89257044306mV";
    private List<Connection> connectionPool;
    private final List<Connection> usedConnections = new ArrayList<>();
    private final static int INITIAL_POOL_SIZE = 100;


    public static BasicConnectionPool create() throws SQLException {

        List<Connection> pool = new ArrayList<>(INITIAL_POOL_SIZE);

        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            pool.add(createConnection());
        }
        return new BasicConnectionPool(pool);
    }

    // standard constructors

    public Connection getConnection() {
        Connection connection = connectionPool.remove(connectionPool.size() - 1);
        usedConnections.add(connection);
        return connection;
    }

    public boolean releaseConnection(Connection connection) {
        connectionPool.add(connection);
        return usedConnections.remove(connection);
    }

    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public int getSize() {
        return connectionPool.size() + usedConnections.size();
    }

    // standard getters
}
