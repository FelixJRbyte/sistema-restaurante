package dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionDB {

    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String DRIVER;

    // ThreadLocal para conexiones por hilo (evita problemas con llamadas anidadas)
    private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream is = ConexionDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
                URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/restaurante_db?useSSL=false&serverTimezone=UTC");
                USER = props.getProperty("db.user", "root");
                PASSWORD = props.getProperty("db.password", "");
                DRIVER = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            } else {
                // Valores por defecto si no existe el archivo
                URL = "jdbc:mysql://localhost:3306/restaurante_db?useSSL=false&serverTimezone=UTC";
                USER = "root";
                PASSWORD = "";
                DRIVER = "com.mysql.cj.jdbc.Driver";
            }
        } catch (IOException e) {
            System.err.println("Error cargando db.properties, usando valores por defecto: " + e.getMessage());
            URL = "jdbc:mysql://localhost:3306/restaurante_db?useSSL=false&serverTimezone=UTC";
            USER = "root";
            PASSWORD = "";
            DRIVER = "com.mysql.cj.jdbc.Driver";
        }
    }

    private ConexionDB() {
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = threadConnection.get();
        if (conn == null || conn.isClosed()) {
            try {
                Class.forName(DRIVER);
                conn = DriverManager.getConnection(URL, USER, PASSWORD);
                conn.setAutoCommit(false); // Manual transaction control
                threadConnection.set(conn);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found: " + DRIVER, e);
            }
        }
        return conn;
    }

    public static void closeConnection() {
        Connection conn = threadConnection.get();
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            } finally {
                threadConnection.remove();
            }
        }
    }

    public static void commit() throws SQLException {
        Connection conn = threadConnection.get();
        if (conn != null && !conn.isClosed()) {
            conn.commit();
        }
    }

    public static void rollback() {
        Connection conn = threadConnection.get();
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.rollback();
                }
            } catch (SQLException e) {
                System.err.println("Error rolling back: " + e.getMessage());
            }
        }
    }
    
    // Método para obtener conexión sin ThreadLocal (para casos especiales)
    public static Connection getNewConnection() throws SQLException {
        try {
            Class.forName(DRIVER);
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(false);
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }
}