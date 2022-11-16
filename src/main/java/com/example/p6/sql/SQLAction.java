package com.example.p6;

public class SQLAction {
    public static String addConnectionsEntry(
        String deepLynxURL,
        String deepLynxContainer,
        String deepLynxDatasource,
        String deepLynxApiKey,
        String deepLynxApiSecret,
        String p6URL,
        String p6Project,
        String p6Username,
        String p6Password
    ) {
        String sql = "REPLACE INTO connections (deepLynxURL, deepLynxContainer, deepLynxDatasource, deepLynxApiKey, deepLynxApiSecret, p6URL, p6Project, p6Username, p6Password)\n"
                   + "    VALUES("
                   + "'" + deepLynxURL + "', "
                   + "'" + deepLynxContainer + "', "
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
        String sql = "SELECT deepLynxURL, deepLynxContainer, deepLynxDatasource, deepLynxApiKey, deepLynxApiSecret, p6URL, p6Project, p6Username, p6Password\n"
                   + "    FROM connections;";
        return sql;
    }
}