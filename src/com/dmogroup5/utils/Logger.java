package com.dmogroup5.utils;

import java.io.*;

public class Logger {

    private String logFileName = "log/log.txt";

    public Logger() {
        File logFile = new File(logFileName);
        if (logFile.exists()) {
            logFile.delete();
        }
    }
    public Logger(String logFileName) {
        super();
        this.logFileName = logFileName;
    }

    public void appendCurrentBest(double value) throws IOException {
        FileWriter fw = new FileWriter(this.logFileName, true);
        BufferedWriter writer = new BufferedWriter(fw);
        writer.append(Double.toString(value));
        writer.newLine();
        writer.close();
    }
}
