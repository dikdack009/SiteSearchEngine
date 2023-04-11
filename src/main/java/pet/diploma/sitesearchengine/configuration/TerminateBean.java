package pet.diploma.sitesearchengine.configuration;

import pet.diploma.sitesearchengine.repositories.DBConnection;

import javax.annotation.PreDestroy;
import java.sql.SQLException;

public class TerminateBean {

    @PreDestroy
    public void onDestroy() throws SQLException {
        DBConnection.closeConnection();
        System.out.println("Spring Application is destroyed (");
    }
}
