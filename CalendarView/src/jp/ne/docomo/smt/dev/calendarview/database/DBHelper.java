/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.database;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import jp.ne.docomo.smt.dev.calendarview.R;

public class DBHelper extends SQLiteOpenHelper {

    private Context mContext;
    // エラー表示用
    AlertDialog.Builder mDlg;

    public DBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
        mDlg = new AlertDialog.Builder(mContext);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String s = null;
        String exceptionMessage = null;

        try {
            InputStream in = mContext.getResources().openRawResource(R.raw.sql);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(in, null);

            NodeList statements = doc.getElementsByTagName("statement");

            for (int i = 0; i < statements.getLength(); i++) {
                s = statements.item(i).getChildNodes().item(0).getNodeValue();
                db.execSQL(s);
            }

        } catch (ParserConfigurationException e) {
            exceptionMessage = "Message: " + e.getMessage();
        } catch (SAXException e) {
            exceptionMessage = "Message: " + e.getMessage();
        } catch (IOException e) {
            exceptionMessage = "Message: " + e.getMessage();
        }

        if (exceptionMessage != null) {
            mDlg.setTitle("xml解析失敗");
            mDlg.setMessage(exceptionMessage + " ");
            mDlg.show();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
