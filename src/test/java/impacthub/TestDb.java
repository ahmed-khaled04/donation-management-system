package impacthub;

import impacthub.persistence.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Empties every table in the test database (target/test.db), so tests do not affect each other. */
public final class TestDb {
    private TestDb() {
    }

    public static void clear() {
        try (Statement st = DatabaseManager.getInstance().getConnection().createStatement()) {
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = st.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'")) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
            for (String table : tables) {
                st.executeUpdate("DELETE FROM " + table);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
