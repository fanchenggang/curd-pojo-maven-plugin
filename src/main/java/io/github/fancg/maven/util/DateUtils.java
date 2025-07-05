package io.github.fancg.maven.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author fancg
 * @date 2025-07-05
 */
public class DateUtils {


    public static Date parseDate(String dateStr) {
        if (dateStr.length() == 10) {
            dateStr = dateStr + " 00:00:00";
        }
        if (dateStr.length() == 16) {
            dateStr = dateStr + ":00";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }
}
