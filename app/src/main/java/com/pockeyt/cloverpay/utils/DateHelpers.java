package com.pockeyt.cloverpay.utils;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelpers {

    public static CharSequence formatToRelative(Context context, String timeStampDateTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date formattedDate = simpleDateFormat.parse(timeStampDateTime);
            Long dateInMilliseconds = formattedDate.getTime();
            CharSequence relDate = DateUtils.getRelativeDateTimeString(context, dateInMilliseconds, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
            return relDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return "error";
        }
    }
}
