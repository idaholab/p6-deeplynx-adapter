package com.example.p6;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
* Class for connecting to the adapter's SQLite instance, for storing 
*    configuration data and for logging activity and access.
*/
public class SQLConnect {
     
    public static Connection conn = null;
    private static DatabaseMetaData mdata = null;

    /**
    * Connect to a database
    */
    public static boolean connect() {
        try {
            // db parameters
            String url = "jdbc:sqlite:/var/app/sqlite/p6.db";
            // create a connection to the database
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
    * Get the database access driver name
    */
    public static String driverName() {
        try {
            if (conn != null) {
                DatabaseMetaData mdata = conn.getMetaData();
                return mdata.getDriverName();
            } else {
                System.out.println("Connection is already null.");
                return "null";
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return "null";
        }
        
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String[] args) {
        connect();
        System.out.println("This database driver is " + driverName());
        close();
    }
}