package impacthub.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Singleton: the one shared connection to the SQLite database file. */
public class DatabaseManager {
    private static DatabaseManager instance;
    private final Connection connection;

    private DatabaseManager() {
        String url = System.getProperty("impacthub.db.url", "jdbc:sqlite:impacthub.db");
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot open database " + url, e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
