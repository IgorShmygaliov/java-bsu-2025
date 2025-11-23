package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static Database instance;
    private Connection conn;

    private static final String DB_URL = "jdbc:sqlite:bank.db";

    private Database() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(DB_URL);
        }
        return conn;
    }

    private void createTables() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id TEXT PRIMARY KEY, " +
                    "nickname TEXT NOT NULL" +
                    ")");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (" +
                    "id TEXT PRIMARY KEY, " +
                    "user_id TEXT, " +
                    "user_name TEXT, " +
                    "balance TEXT, " +
                    "status TEXT, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ")");
        }
    }
}
