package repository;

import model.Field;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class MySQLConnection {

    private final static String CONFIGURE_FILE_NAME = "hibernate.cfg.xml";
    private static SessionFactory sessionFactory;

    public static void setUpDB() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure(CONFIGURE_FILE_NAME).build();
        Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
        sessionFactory = metadata.getSessionFactoryBuilder().build();
    }

    public static Session openConnection(){
        if (sessionFactory.isClosed()){
            setUpDB();
        }
        return sessionFactory.openSession();
    }

    public static void fillFieldDB(){
        Session session = openConnection();
        Transaction transaction = session.beginTransaction();
        Field field1 = new Field();
        field1.setName("title");
        field1.setSelector("title");
        field1.setWeight(1.0f);
        session.delete(field1);
        session.save(field1);
        Field field2 = new Field();
        field2.setName("body");
        field2.setSelector("body");
        field2.setWeight(0.8f);
        session.delete(field2);
        session.save(field2);
        transaction.commit();
        session.close();
    }

    public static void closeConnection(){
        sessionFactory.close();
    }

}
