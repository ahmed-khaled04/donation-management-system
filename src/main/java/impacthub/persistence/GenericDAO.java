package impacthub.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic CRUD for any class with an {@code int id} field.
 * Each class gets its own table (named after the class) holding the serialized objects.
 */
public class GenericDAO<T extends Serializable> {
    private final Class<T> type;
    private final String table;

    public GenericDAO(Class<T> type) {
        this.type = type;
        this.table = type.getSimpleName();
        try (Statement st = connection().createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS " + table + " (id INTEGER PRIMARY KEY AUTOINCREMENT, data BLOB)");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public int insert(T item) {
        try (PreparedStatement ps = connection().prepareStatement(
                "INSERT INTO " + table + " (data) VALUES (NULL)", Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                idField(item).setInt(item, id);
                update(item);
                return id;
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public T findById(int id) {
        try (PreparedStatement ps = connection().prepareStatement("SELECT data FROM " + table + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? read(rs.getBytes(1)) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<T> findAll() {
        List<T> items = new ArrayList<>();
        try (Statement st = connection().createStatement();
             ResultSet rs = st.executeQuery("SELECT data FROM " + table + " ORDER BY id")) {
            while (rs.next()) {
                items.add(read(rs.getBytes(1)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return items;
    }

    public void update(T item) {
        try (PreparedStatement ps = connection().prepareStatement("UPDATE " + table + " SET data = ? WHERE id = ?")) {
            ps.setBytes(1, write(item));
            ps.setInt(2, idField(item).getInt(item));
            ps.executeUpdate();
        } catch (SQLException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = connection().prepareStatement("DELETE FROM " + table + " WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Connection connection() {
        return DatabaseManager.getInstance().getConnection();
    }

    /** Finds the private "id" field, which may be declared in a superclass. */
    private Field idField(T item) {
        for (Class<?> c = item.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField("id");
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
                // keep looking in the superclass
            }
        }
        throw new IllegalArgumentException(type.getSimpleName() + " has no id field");
    }

    private byte[] write(T item) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(item);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return bytes.toByteArray();
    }

    private T read(byte[] data) {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return type.cast(in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
