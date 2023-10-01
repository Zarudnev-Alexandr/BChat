package com.example.bchatmobile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

public class DateUtils {
    public static String formatToISO8601(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return sdf.format(date);
    }

    public static Date parseFromISO8601(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return sdf.parse(dateString);
    }
}
