/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.g3d.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * More simple formatter than the default one used in Java logging.
 * Example output: <br/>
 * INFO Display3D 12:00 PM: Display created.
 */
public class G3DFormatter extends Formatter {

    private Calendar calendar = new GregorianCalendar();
    private String lineSeperator;

    public G3DFormatter(){
        lineSeperator = System.getProperty("line.separator");
    }

    @Override
    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();

        calendar.setTimeInMillis(record.getMillis());

        String clazz = null;
        try{
            clazz = Class.forName(record.getSourceClassName()).getSimpleName();
        } catch (ClassNotFoundException ex){
        }
        
        sb.append(record.getLevel().getLocalizedName()).append(" ");
        sb.append(clazz).append(" ");
        sb.append(calendar.get(Calendar.HOUR)).append(":");
        sb.append(calendar.get(Calendar.MINUTE)).append(":");
        sb.append(calendar.get(Calendar.SECOND)).append(" ");
        sb.append(calendar.get(Calendar.AM_PM) == 1 ? "PM" : "AM").append(": ");
        sb.append(record.getMessage()).append(lineSeperator);

        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
        }

        return sb.toString();
    }
}
