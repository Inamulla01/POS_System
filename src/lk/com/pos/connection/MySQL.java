package lk.com.pos.connection;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {
    
    private static final String DATABASE = "pos_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Inamulla2005#";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE;
    
    // Static initializer to load the driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
    
    /**
     * CRITICAL: Returns a NEW connection for each call
     * This prevents ResultSet conflicts between threads
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Create a NEW connection every time
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
            // Optional: Set connection properties for better performance
            conn.setAutoCommit(true);
            
            return conn;
            
        } catch (SQLException e) {
            System.err.println("Failed to create database connection: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Execute a SELECT query
     * IMPORTANT: Caller MUST close the ResultSet, Statement, and Connection
     */
    public static ResultSet executeSearch(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }
    
    /**
     * Execute INSERT, UPDATE, DELETE queries
     */
    public static int executeIUD(String query) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            int result = stmt.executeUpdate(query);
            return result;
            
        } finally {
            // Always close resources
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Test the database connection
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && conn.isValid(2);
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Safely close database resources
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
