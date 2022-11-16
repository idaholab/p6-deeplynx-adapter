package com.example.p6;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.util.HashMap;
import java.util.ArrayList;

/**
* Class for connecting to the adapter's SQLite instance, for storing 
*    configuration data and for logging activity and access.
*/
public class SQLConnect {
     
    public static Connection conn = null;
    private static String url = "jdbc:sqlite:/var/app/sqlite/p6.db"; // TODO: replace with env var reference.
    private static DatabaseMetaData mdata = null;

    /**
    * Connect to a database
    */
    public static boolean connect() {
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
    * Close a database connection
    */
    public static boolean close() {
        try {
            if (conn != null) {
                System.out.println("Connection to SQLite has been closed.");
                conn.close();
            } else {
                System.out.println("Connection to SQLite not closed (was already null).");
            }
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /**
    * Run migrations
    */
    public static boolean migrate() {
        try {
            if (conn != null) {
                // initialize statement
                Statement stmt = conn.createStatement();

                // run migrations
                SQLMigration sqlmig = new SQLMigration();
                stmt.execute(sqlmig.createConnectionsTable());
                stmt.execute(sqlmig.createLogsTable());
                stmt.execute(sqlmig.createConnectionsUniqueId());
                return true;
            } else {
                System.out.println("Migration to SQLite not completed (connection was null).");
                return false;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /**
    * Add entry to the connections table
    */
    public static boolean addConnection(HashMap<String, String> connection_map) {
        try {
            if (conn != null) {
                // initialize statement
                Statement stmt = conn.createStatement();

                // addition
                SQLAction sqlact = new SQLAction();
                stmt.execute(sqlact.addConnectionsEntry(
                    connection_map.get("deepLynxURL"),
                    connection_map.get("deepLynxContainer"),
                    connection_map.get("deepLynxDatasource"),
                    connection_map.get("deepLynxApiKey"),
                    connection_map.get("deepLynxApiSecret"),
                    connection_map.get("p6URL"),
                    connection_map.get("p6Project"),
                    connection_map.get("p6Username"),
                    connection_map.get("p6Password")
                ));
                return true;
            } else {
                System.out.println("Connection entry to SQLite not completed (connection was null).");
                return false;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /**
    * Get all entries from connections table
    */
    public static ArrayList<HashMap<String, String>> getConnections() {
        ArrayList<HashMap<String, String>> connectionsList = new ArrayList<HashMap<String, String>>();

        try {
            if (conn != null) {
                // initialize statement
                Statement stmt = conn.createStatement();

                // query
                SQLAction sqlact = new SQLAction();
                ResultSet rs = stmt.executeQuery(sqlact.getConnections());
                while (rs.next()) {
                    HashMap<String, String> tempMap = new HashMap<String, String>();
                    tempMap.put("deepLynxURL", rs.getString("deepLynxURL"));
                    tempMap.put("deepLynxContainer", rs.getString("deepLynxContainer"));
                    tempMap.put("deepLynxDatasource", rs.getString("deepLynxDatasource"));
                    tempMap.put("deepLynxApiKey", rs.getString("deepLynxApiKey"));
                    tempMap.put("deepLynxApiSecret", rs.getString("deepLynxApiSecret"));
                    tempMap.put("p6URL", rs.getString("p6URL"));
                    tempMap.put("p6Project", rs.getString("p6Project"));
                    tempMap.put("p6Username", rs.getString("p6Username"));
                    tempMap.put("p6Password", rs.getString("p6Password"));
                    connectionsList.add(tempMap);
                }
            } else {
                System.out.println("Connection query to SQLite not completed (connection was null).");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println(connectionsList);
        return connectionsList;
    }

    /**
    * Get the database access driver name
    */
    public static String getDriverName() {
        try {
            if (conn != null) {
                DatabaseMetaData mdata = conn.getMetaData();
                return mdata.getDriverName();
            } else {
                System.out.println("Connection is null.");
                return "null";
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return "null";
        }
        
    }

    /**
    * Code to execute on CL execution
    */
    public static void main(String[] args) {
        connect();
        System.out.println("This database driver is " + getDriverName());
        close();
    }
}