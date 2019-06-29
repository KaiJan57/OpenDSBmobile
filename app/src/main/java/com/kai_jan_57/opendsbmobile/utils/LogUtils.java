package com.kai_jan_57.opendsbmobile.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {

    public static String getStackTrace(Exception pException) {
        if (pException == null) {
            return "No Exception provided.";
        }
        StringWriter stringWriter = new StringWriter();
        pException.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}
