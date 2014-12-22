/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


import jp.ne.docomo.smt.dev.calendarview.common.CalApplication;
import jp.ne.docomo.smt.dev.calendarview.database.TBTodoEvent;
import jp.ne.docomo.smt.dev.calendarview.entities.TodoEvent;

public class Utility {
    public static ArrayList<String> sNameOfEvent = new ArrayList<String>();
    public static ArrayList<String> sStartDates = new ArrayList<String>();
    public static ArrayList<String> sEndDates = new ArrayList<String>();
    public static ArrayList<String> sDescriptions = new ArrayList<String>();

    public static ArrayList<String> readCalendarEvent(Context context) {
        Cursor cursor = context
                .getContentResolver()
                .query(Uri.parse("content://com.android.calendar/events"),
                        new String[] {
                            "calendar_id", "title", "description",
                            "dtstart", "dtend", "eventLocation"},
                        null, null, null);
        cursor.moveToFirst();
        // fetching calendars name
        String[] cNames = new String[cursor.getCount()];

        // fetching calendars id
        sNameOfEvent.clear();
        sStartDates.clear();
        sEndDates.clear();
        sDescriptions.clear();
        for (int i = 0; i < cNames.length; i++) {

            sNameOfEvent.add(cursor.getString(1));
            sStartDates.add(getDate(Long.parseLong(cursor.getString(3))));
            sEndDates.add(getDate(Long.parseLong(cursor.getString(4))));
            sDescriptions.add(cursor.getString(2));
            cNames[i] = cursor.getString(1);
            cursor.moveToNext();

        }
        cursor.close();
        return sNameOfEvent;
    }

    public static ArrayList<TodoEvent> readCalendarEventFromDB(Context context) {
        TBTodoEvent eventTb = new TBTodoEvent(CalApplication.getInstant().getSqlDb());
        return eventTb.getAllEvent();
    }

    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPANESE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public boolean isHiragana(final char c) {
        return (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HIRAGANA);
    }

    public String removeHiragana(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            if (!isHiragana(str.charAt(i))) {
                result += str.charAt(i);
            }
        }
        return result;
    }
}
