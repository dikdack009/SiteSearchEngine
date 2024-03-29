package pet.diploma.sitesearchengine.configuration;

import org.apache.logging.log4j.LogManager;
import pet.diploma.sitesearchengine.repositories.DBConnection;

import javax.annotation.PreDestroy;
import java.sql.SQLException;

public class TerminateBean {

    @PreDestroy
    public void onDestroy() throws SQLException {
        DBConnection.closeConnection();
        LogManager.getLogger("index").info("I am destroyed..");
    }
}
