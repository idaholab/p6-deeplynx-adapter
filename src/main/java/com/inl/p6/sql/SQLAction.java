package com.inl.p6;

public class SQLAction {
    public static String replaceIntoConnectionsEntry(
        String serviceUserId,
        String serviceUserKey,
        String serviceUserSecret
    ) {
        String sql = "REPLACE INTO connections (serviceUserId, serviceUserKey, serviceUserSecret)\n"
                   + "    VALUES("
                   + "'" + serviceUserId + "', "
                   + "'" + serviceUserKey + "', "
                   + "'" + serviceUserSecret + "'"
                   + ");\n";
        return sql;
    }

    public static String deleteConnectionsEntry(
        String serviceUserId,
        String serviceUserKey,
        String serviceUserSecret
    ) {
        String sql = "DELETE FROM connections \n"
                   + "    WHERE("
                   + "serviceUserId = '" + serviceUserId + "' AND "
                   + "serviceUserKey = '" + serviceUserKey + "' AND "
                   + "serviceUserSecret = '" + serviceUserSecret + "'"
                   + ");\n";
        return sql;
    }

    public static String getConnections() {
        String sql = "SELECT serviceUserId, serviceUserKey, serviceUserSecret\n"
                   + "    FROM connections;";
        return sql;
    }

    static String getConnectionEncDLSecret(
        String serviceUserId,
        String serviceUserKey,
        String deepLynxDatasourceId
    ) {
        String sql = "SELECT deepLynxApiSecret\n"
                   + "    FROM connections WHERE("
                   + "serviceUserId = '" + serviceUserId + "' AND "
                   + "serviceUserKey = '" + serviceUserKey + "' AND "
                   + "deepLynxDatasourceId = '" + deepLynxDatasourceId + "'"
                   + ");";
        return sql;
    }

    public static String makeLog(String timestamp, String log) {
        String sql = "INSERT INTO logs (datetime, body)\n"
                   + "     VALUES(" + timestamp+ ", '" + log + "');\n";
        return sql;
    }
}
