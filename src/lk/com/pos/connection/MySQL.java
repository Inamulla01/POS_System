package lk.com.pos.connection;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL {

    private static final String DATABASE = "pos_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Inamulla2005#";
    private static Connection connection;

    static {
        initializeConnection();
    }

    private static void initializeConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + DATABASE, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            // Check if connection is still valid
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                initializeConnection(); // Reconnect if needed
            }
        } catch (SQLException e) {
            e.printStackTrace();
            initializeConnection(); // Reconnect on error
        }
        return connection;
    }

    public static ResultSet executeSearch(String query) throws SQLException {
        return getConnection().createStatement().executeQuery(query);
    }

    public static void executeIUD(String query) {
        try {
            getConnection().createStatement().executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}