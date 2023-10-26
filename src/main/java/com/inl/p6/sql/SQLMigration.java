// Copyright 2023, Battelle Energy Alliance, LLC All Rights Reserved

package com.inl.p6;

public class SQLMigration {
    public static String createConnectionsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS connections (\n"
                   + "    id integer PRIMARY KEY,\n"
                   + "    serviceUserId text NOT NULL,\n"
                   + "    serviceUserKey text NOT NULL,\n"
                   + "    serviceUserSecret blob NOT NULL\n"
                   + ");\n";
        return sql;
    }

    public static String createConnectionsUniqueId() {
        String sql = "CREATE UNIQUE INDEX IF NOT EXISTS connections_idx\n"
                   + "    on connections (serviceUserId, serviceUserKey, serviceUserSecret);";
        return sql;
    }

}
