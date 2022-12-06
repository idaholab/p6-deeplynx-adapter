package com.example.p6;

public class SQLAction {
    public static String replaceIntoConnectionsEntry(
        String deepLynxURL,
        String deepLynxContainerId,
        String deepLynxDatasource,
        String deepLynxApiKey,
        String deepLynxApiSecret,
        String p6URL,
        String p6Project,
        String p6Username,
        String p6Password
    ) {
        String sql = "REPLACE INTO connections (deepLynxURL, deepLynxContainerId, deepLynxDatasource, deepLynxApiKey, deepLynxApiSecret, p6URL, p6Project, p6Username, p6Password)\n"
                   + "    VALUES("
                   + "'" + deepLynxURL + "', "
                   + "'" + deepLynxContainerId + "', "
                   + "'" + deepLynxDatasource + "', "
                   + "'" + deepLynxApiKey + "', "
                   + "'" + deepLynxApiSecret + "', "
                   + "'" + p6URL + "', "
                   + "'" + p6Project + "', "
                   + "'" + p6Username + "', "
                   + "'" + p6Password + "'"
                   + ");\n";
        return sql;
    }

    public static String getConnections() {
        String sql = "SELECT deepLynxURL, deepLynxContainerId, deepLynxDatasource, deepLynxApiKey, deepLynxApiSecret, p6URL, p6Project, p6Username, p6Password\n"
                   + "    FROM connections;";
        return sql;
    }

    public static String makeLog(String timestamp, String log) {
        String sql = "INSERT INTO logs (datetime, body)\n" 
                   + "     VALUES(" + timestamp+ ", '" + log + "');\n";
        return sql;
    }
}