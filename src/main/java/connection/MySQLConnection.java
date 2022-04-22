package connection;

import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class MySQLConnection {

    private final static String CONFIGURE_FILE_NAME = "hibernate.cfg.xml";
    @Getter
    private static Session session;
    private static SessionFactory sessionFactory;

    public static void setUpDB() {

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure(CONFIGURE_FILE_NAME).build();
        Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
        sessionFactory = metadata.getSessionFactoryBuilder().build();
    }

    public static void openConnection(){
        session = sessionFactory.openSession();
    }

    public static void closeConnection(){
        sessionFactory.close();
    }

}
