package dao;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionDB {

    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String DRIVER;

    // ThreadLocal physical connection for nested DAO operations and manual transaction control
    private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<>();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream is = ConexionDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
                URL = props.getProperty("db.url", "jdbc:mysql://localhost:3306/restaurante_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
                USER = props.getProperty("db.user", "root");
                PASSWORD = props.getProperty("db.password", "");
                DRIVER = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            } else {
                setDefaults();
            }
        } catch (IOException e) {
            System.err.println("Error loading db.properties, using default values: " + e.getMessage());
            setDefaults();
        }
    }

    private static void setDefaults() {
        URL = "jdbc:mysql://localhost:3306/restaurante_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        USER = "root";
        PASSWORD = "";
        DRIVER = "com.mysql.cj.jdbc.Driver";
    }

    private ConexionDB() {
        // Prevent instantiation
    }

    /**
     * Returns a non-closing proxy wrapping the ThreadLocal physical connection so that
     * try-with-resources blocks in DAOs do not prematurely close the connection during
     * nested queries (e.g., PedidoDAO -> DetallePedidoDAO) or before rollback().
     */
    public static Connection getConnection() throws SQLException {
        Connection realConn = threadConnection.get();
        if (realConn == null || realConn.isClosed()) {
            try {
                Class.forName(DRIVER);
                realConn = DriverManager.getConnection(URL, USER, PASSWORD);
                realConn.setAutoCommit(false);
                threadConnection.set(realConn);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found: " + DRIVER, e);
            }
        }

        final Connection target = realConn;
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        // Do not close physical connection on try-with-resources exit;
                        // physical lifecycle is managed by closeConnection()
                        return null;
                    }
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException ex) {
                        throw ex.getTargetException();
                    }
                }
        );
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
