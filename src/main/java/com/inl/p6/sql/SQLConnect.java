// Copyright 2023, Battelle Energy Alliance, LLC All Rights Reserved

package com.inl.p6;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Base64;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Class for connecting to the adapter's SQLite instance, for storing
*    configuration data and for logging activity and access.
*/
public class SQLConnect {

    private static final Logger LOGGER = Logger.getLogger( Logger.GLOBAL_LOGGER_NAME );

    public static Connection conn = null;
    private static String url = System.getenv("P6_DB_LOC");
    private static DatabaseMetaData mdata = null;

    /**
    * Connect to a database
    */
    public static boolean connect() {
        try {
            conn = DriverManager.getConnection(url);
            LOGGER.log(Level.INFO, "Connection to SQLite has been established.");
            boolean migrateSuccess = migrate();
            return migrateSuccess;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    /**
    * Close a database connection
    */
    public static boolean close() {
        try {
            if (conn != null) {
                LOGGER.log(Level.INFO, "Connection to SQLite has been closed.");
                conn.close();
            } else {
                LOGGER.log(Level.INFO, "Connection to SQLite not closed (was already null).");
            }
            return true;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
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
                stmt.execute(sqlmig.createConnectionsUniqueId());
                return true;
            } else {
                LOGGER.log(Level.SEVERE, "Migration to SQLite not completed (connection was null).");
                return false;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }
    }

    /**
    * Add entry to the connections table, it encrypts the key/password values
    */
    public static int addConnection(HashMap<String, String> connection_map) {
        try {
            // setup cipher for encryption
            String key = System.getenv("P6_ENCRYPTION_KEY");
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            // encrypt sensitive values
            byte[] encDLAPISecret = cipher.doFinal(connection_map.get("serviceUserSecret").getBytes());
            String encDLAPISecretString = Base64.getEncoder().encodeToString(encDLAPISecret);

            // initialize statement
            Statement stmt = conn.createStatement();

            // addition
            SQLAction sqlact = new SQLAction();
            int rowsAffected = stmt.executeUpdate(sqlact.replaceIntoConnectionsEntry(
                connection_map.get("serviceUserId"),
                connection_map.get("serviceUserKey"),
                encDLAPISecretString
            ));
            return rowsAffected;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return 0;
        }
    }

    /**
    * Add entry to the connections table, it encrypts the key/password values
    */
    // public static int updateConnection(HashMap<String, String> connection_map) {
    //     try {
    //         // setup cipher for encryption
    //         String key = System.getenv("P6_ENCRYPTION_KEY");
    //         Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
    //         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    //         cipher.init(Cipher.ENCRYPT_MODE, aesKey);
    //
    //         // encrypt sensitive values
    //         byte[] encP6Password = cipher.doFinal(connection_map.get("p6Password").getBytes());
    //         String encP6PasswordString = Base64.getEncoder().encodeToString(encP6Password);
    //
    //         // initialize statement
    //         Statement stmt = conn.createStatement();
    //
    //         // addition
    //         SQLAction sqlact = new SQLAction();
    //
    //         // get old DL secret
    //         ResultSet rs = stmt.executeQuery(sqlact.getConnectionEncDLSecret(
    //             connection_map.get("deepLynxURL"),
    //             connection_map.get("deepLynxContainerId"),
    //             connection_map.get("deepLynxDatasourceId")
    //         ));
    //
    //         int rowsAffected = 0;
    //         while (rs.next()) {
    //             String old_DL_secret = rs.getString("deepLynxApiSecret");
    //
    //             // insert updated entry
    //             rowsAffected = stmt.executeUpdate(sqlact.replaceIntoConnectionsEntry(
    //                 connection_map.get("deepLynxURL"),
    //                 connection_map.get("deepLynxContainerId"),
    //                 connection_map.get("deepLynxDatasourceId"),
    //                 connection_map.get("deepLynxApiKey"),
    //                 old_DL_secret,
    //                 connection_map.get("p6URL"),
    //                 connection_map.get("p6Project"),
    //                 connection_map.get("p6Username"),
    //                 encP6PasswordString
    //             ));
    //         }
    //
    //         return rowsAffected;
    //
    //     } catch (Exception ex) {
    //         LOGGER.log(Level.SEVERE, ex.getMessage());
    //         return 0;
    //     }
    // }

    /**
    * Delete an entry from the connections table
    */
    // public static int deleteConnection(HashMap<String, String> connection_map) {
    //     try {
    //         // setup cipher for encryption
    //         String key = System.getenv("P6_ENCRYPTION_KEY");
    //         Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
    //         Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    //         cipher.init(Cipher.ENCRYPT_MODE, aesKey);
    //
    //         // encrypt sensitive values
    //         byte[] encP6Password = cipher.doFinal(connection_map.get("p6Password").getBytes());
    //         String encP6PasswordString = Base64.getEncoder().encodeToString(encP6Password);
    //
    //         // initialize statement
    //         Statement stmt = conn.createStatement();
    //
    //         // addition
    //         SQLAction sqlact = new SQLAction();
    //         int rowsAffected = stmt.executeUpdate(sqlact.deleteConnectionsEntry(
    //             connection_map.get("deepLynxURL"),
    //             connection_map.get("deepLynxContainerId"),
    //             connection_map.get("deepLynxDatasourceId"),
    //             connection_map.get("deepLynxApiKey"),
    //             connection_map.get("p6URL"),
    //             connection_map.get("p6Project"),
    //             connection_map.get("p6Username"),
    //             encP6PasswordString
    //         ));
    //         return rowsAffected;
    //
    //     } catch (Exception ex) {
    //         LOGGER.log(Level.SEVERE, ex.getMessage());
    //         return 0;
    //     }
    // }

    /**
    * Get all entries from connections table
    */
    public static ArrayList<HashMap<String, String>> getConnections() {
        ArrayList<HashMap<String, String>> connectionsList = new ArrayList<HashMap<String, String>>();

        try {
            // setup cipher for decryption
            String key = System.getenv("P6_ENCRYPTION_KEY");
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);

            // initialize statement
            Statement stmt = conn.createStatement();

            // query
            SQLAction sqlact = new SQLAction();
            ResultSet rs = stmt.executeQuery(sqlact.getConnections());
            while (rs.next()) {
                HashMap<String, String> tempMap = new HashMap<String, String>();

                // decrypt sensitive values
                byte[] encDLAPISecretBytes = Base64.getDecoder().decode(rs.getString("serviceUserSecret").getBytes());
                String decDLAPISecret = new String(cipher.doFinal(encDLAPISecretBytes));

                tempMap.put("serviceUserId", rs.getString("serviceUserId"));
                tempMap.put("serviceUserKey", rs.getString("serviceUserKey"));
                tempMap.put("serviceUserSecret", decDLAPISecret);

                connectionsList.add(tempMap);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }

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
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return "null";
        }

    }

    /**
    * Code to execute on CL execution
    */
    public static void main(String[] args) {
        connect();
        LOGGER.log(Level.INFO, "This database driver is " + getDriverName());
        close();
    }
}
