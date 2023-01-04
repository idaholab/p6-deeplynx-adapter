package com.example.p6;

public class SQLMigration {
    public static String createConnectionsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS connections (\n"
                   + "    id integer PRIMARY KEY,\n"
                   + "    deepLynxURL text NOT NULL,\n"
                   + "    deepLynxContainerId text NOT NULL,\n"
                   + "    deepLynxDatasourceId text NOT NULL,\n"
                   + "    deepLynxApiKey text NOT NULL,\n"
                   + "    deepLynxApiSecret blob NOT NULL,\n"
                   + "    p6URL text NOT NULL,\n"
                   + "    p6Project text NOT NULL,\n"
                   + "    p6Username text NOT NULL,\n"
                   + "    p6Password blob NOT NULL\n"
                   + ");\n";
        return sql;
    }

    public static String createLogsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS logs (\n"
                   + "    id integer PRIMARY KEY,\n"
                   + "    datetime integer NOT NULL,\n"
                   + "    body text NOT NULL\n"
                   + ");\n";
        return sql;
    }

    public static String createConnectionsUniqueId() {
        String sql = "CREATE UNIQUE INDEX IF NOT EXISTS connections_idx\n"
                   + "    on connections (deepLynxURL, deepLynxContainerId, deepLynxDatasourceId);";
        return sql;
    }

}