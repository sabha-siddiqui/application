package com.easyfitness.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler defaultUEH;

    private String localPath;


    public CustomExceptionHandler(String localPath) {
        this.localPath = localPath;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread t, Throwable e) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_H_m_s");
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = timestamp + ".txt";

        if (localPath != null) {
            writeToFile(stacktrace, filename);
        }

        defaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String stacktrace, String filename) {
        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(
                localPath + "/" + filename));
            bos.write(stacktrace);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
