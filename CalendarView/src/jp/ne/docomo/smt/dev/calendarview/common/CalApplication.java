/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.common;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import jp.ne.docomo.smt.dev.calendarview.database.DBHelper;

public class CalApplication extends Application {
    private static CalApplication sInstant;

    public static CalApplication getInstant() {
        return sInstant;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstant = this;
    }

    SQLiteDatabase sqlDb;

    public SQLiteDatabase getSqlDb() {
        if (sqlDb == null) {
            DBHelper dbHelper = new DBHelper(this, "CAL.db", null, 1);
            sqlDb = dbHelper.getWritableDatabase();
        }

        return sqlDb;
    }
}
