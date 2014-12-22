/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import jp.ne.docomo.smt.dev.calendarview.entities.TodoEvent;

public class TBTodoEvent {
    SQLiteDatabase mDb;

    private static final String COLUMN_CREATE_ID = "id";
    private static final String COLUMN_CREATE_AT = "create_at";
    private static final String COLUMN_EVENT = "event";

    private static final String[] COLUMNS = {COLUMN_CREATE_ID, COLUMN_CREATE_AT, COLUMN_EVENT};

    private static final String TB_TODO = "todo";

    public TBTodoEvent(SQLiteDatabase db) {
        this.mDb = db;
    }

    public long addEvent(String createAt, String event) {
        ContentValues data = new ContentValues();
        data.put(COLUMN_CREATE_AT, createAt);
        data.put(COLUMN_EVENT, event);
        return mDb.insert(TB_TODO, null, data);
    }

    public ArrayList<TodoEvent> getAllEvent() {
        ArrayList<TodoEvent> result = new ArrayList<TodoEvent>();
        Cursor cursor = mDb.query(TB_TODO, COLUMNS, null, null, null, null, COLUMN_CREATE_ID);
        while (cursor.moveToNext()) {
            result.add(new TodoEvent(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
        }
        cursor.close();
        return result;
    }

    public ArrayList<TodoEvent> getAllEventByDay(String day) {
        ArrayList<TodoEvent> result = new ArrayList<TodoEvent>();
        Cursor cursor = mDb.query(TB_TODO, COLUMNS, COLUMN_CREATE_AT + " = '" + day + "'",
                null, null, null, COLUMN_CREATE_ID);
        while (cursor.moveToNext()) {
            result.add(new TodoEvent(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
        }
        cursor.close();
        return result;
    }

    public void deleteEventById(int id) {
        mDb.delete(TB_TODO, COLUMN_CREATE_ID + " = " + id, null);
    }

    public void deleteEventByCreateAt(String createAt) {
        mDb.delete(TB_TODO, COLUMN_CREATE_AT + " = '" + createAt + "'", null);
    }
}
