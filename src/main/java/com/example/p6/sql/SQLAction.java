package com.example.p6;

public class SQLAction {
    public static String replaceIntoConnectionsEntry(
        String deepLynxURL,
        String deepLynxContainerId,
        String deepLynxDatasourceId,
        String deepLynxApiKey,
        String deepLynxApiSecret,
        String p6URL,
        String p6Project,
        String p6Username,
        String p6Password
    ) {
        String sql = "REPLACE INTO connections (deepLynxURL, deepLynxContainerId, deepLynxDatasourceId, deepLynxApiKey, deepLynxApiSecret, p6URL, p6Project, p6Username, p6Password)\n"
                   + "    VALUES("
                   + "'" + deepLynxURL + "', "
                   + "'" + deepLynxContainerId + "', "
                   + "'" + deepLynxDatasourceId + "', "
                   + "'" + deepLynxApiKey + "', "
                   + "'" + deepLynxApiSecret + "', "
                   + "'" + p6URL + "', "
                   + "'" + p6Project + "', "
                   + "'" + p6Username + "', "
                   + "'" + p6Password + "'"
                   + ");\n";
        return sql;
    }

    public static String deleteConnectionsEntry(
        String deepLynxURL,
        String deepLynxContainerId,
        String deepLynxDatasourceId,
        String deepLynxApiKey,
        String p6URL,
        String p6Project,
        String p6Username,
        String p6Password
    ) {
        String sql = "DELETE FROM connections \n"
                   + "    WHERE("
                   + "deepLynxURL = '" + deepLynxURL + "' AND "
                   + "deepLynxContainerId = '" + deepLynxContainerId + "' AND "
                   + "deepLynxDatasourceId = '" + deepLynxDatasourceId + "' AND "
                   + "deepLynxApiKey = '" + deepLynxApiKey + "' AND "
                   + "p6URL = '" + p6URL + "' AND "
                   + "p6Project = '" + p6Project + "' AND "
                   + "p6Username = '" + p6Username + "' AND "
                   + "p6Password = '" + p6Password + "'"
                   + ");\n";
        return sql;
    }

    public static String getConnections() {
        String sql = "SELECT deepLynxURL, deepLynxContainerId, deepLynxDatasourceId, deepLynxApiKey, deepLynxApiSecret, p6URL, p6Project, p6Username, p6Password\n"
                   + "    FROM connections;";
        return sql;
    }

    static String getConnectionEncDLSecret(
        String deepLynxURL,
        String deepLynxContainerId,
        String deepLynxDatasourceId
    ) {
        String sql = "SELECT deepLynxApiSecret\n"
                   + "    FROM connections WHERE("
                   + "deepLynxURL = '" + deepLynxURL + "' AND "
                   + "deepLynxContainerId = '" + deepLynxContainerId + "' AND "
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