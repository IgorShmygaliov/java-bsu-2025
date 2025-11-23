package service;

import db.Database;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserService {
    private final Connection conn;

    public UserService() {
        Connection temp = null;
        try {
            temp = Database.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.conn = temp;
    }

    public void save(User user) {
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO users(id, nickname) VALUES(?,?)")) {
            ps.setString(1, user.getId().toString());
            ps.setString(2, user.getNickname());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(UUID id) {
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM users WHERE id=?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User get(UUID id) {
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT nickname FROM users WHERE id=?")) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String nick = rs.getString("nickname");
                User user = new User(nick);
                java.lang.reflect.Field idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, id);
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        if (conn == null) return list;
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT id FROM users");
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                User user = get(id);
                if (user != null) list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
