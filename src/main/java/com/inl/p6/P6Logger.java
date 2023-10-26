// Copyright 2023, Battelle Energy Alliance, LLC All Rights Reserved

package com.inl.p6;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class P6Logger {
    static private FileHandler fileTxt;
    static private SimpleFormatter formatterTxt;

    static public void setup() throws IOException {

        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        // suppress the logging output to the console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        logger.setLevel(Level.INFO);
        fileTxt = new FileHandler("/var/app/sqlite/Log.txt");
        fileTxt.setLevel(Level.INFO);

        formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);
    }
}
