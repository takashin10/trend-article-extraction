/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/** ジャンル情報、記事コンテンツデータ、記事コンテンツ状態、閲覧ログデータを保持するContentProvider */
public class TrendArticleProvider extends ContentProvider {

    // field
    // ----------------------------------------------------------------

    /** ContentProviderの識別子 */
    public static final String AUTHORITY =
            "jp.ne.docomo.smt.dev.webcuration.sample_app_white.provider.trend_article_provider";

    private static final int GENRE_DIR = 1;
    private static final int GENRE_ITEM = 2;
    private static final int ARTICLE_DIR = 3;
    private static final int ARTICLE_ITEM = 4;
    private static final int ARTICLE_STATE_DIR = 5;
    private static final int ARTICLE_STATE_ITEM = 6;
    private static final int LOG_DATA_DIR = 7;
    private static final int LOG_DATA_ITEM = 8;

    private static final UriMatcher MATCHER;
    static {
        MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        MATCHER.addURI(AUTHORITY, TrendArticleDb.Genre.TABLE, GENRE_DIR);
        MATCHER.addURI(AUTHORITY, (TrendArticleDb.Genre.TABLE + "/#"), GENRE_ITEM);
        MATCHER.addURI(AUTHORITY, TrendArticleDb.Article.TABLE, ARTICLE_DIR);
        MATCHER.addURI(AUTHORITY, (TrendArticleDb.Article.TABLE + "/#"), ARTICLE_ITEM);
        MATCHER.addURI(AUTHORITY, TrendArticleDb.ArticleState.TABLE, ARTICLE_STATE_DIR);
        MATCHER.addURI(AUTHORITY, (TrendArticleDb.ArticleState.TABLE + "/#"), ARTICLE_STATE_ITEM);
        MATCHER.addURI(AUTHORITY, TrendArticleDb.LogData.TABLE, LOG_DATA_DIR);
        MATCHER.addURI(AUTHORITY, (TrendArticleDb.LogData.TABLE + "/#"), LOG_DATA_ITEM);
    }

    private DbOpenHelper mHelper;

    // method
    // ----------------------------------------------------------------

    /** ContentProviderが生成される際に呼び出されるコールバック */
    @Override
    public boolean onCreate() {
        mHelper = new DbOpenHelper(getContext());
        return true;
    }

    /** ContentProviderからレコードを取得 */
    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        switch (MATCHER.match(uri)) {
            case GENRE_DIR:
            case GENRE_ITEM: {
                Cursor cursor = mHelper.getReadableDatabase().query(
                        TrendArticleDb.Genre.TABLE, projection,
                        appendSelection(uri, selection), appendSelectionArgs(uri, selectionArgs),
                        null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            }
            case ARTICLE_DIR:
            case ARTICLE_ITEM: {
                Cursor cursor = mHelper.getReadableDatabase().rawQuery("select * from "
                        + TrendArticleDb.Article.TABLE + " left outer join "
                        + TrendArticleDb.ArticleState.TABLE + " on "
                        + TrendArticleDb.Article.TABLE + "."
                        + TrendArticleDb.Article.KEY_CONTENT_ID + " == "
                        + TrendArticleDb.ArticleState.TABLE + "."
                        + TrendArticleDb.ArticleState.KEY_CONTENT_ID
                        + (TextUtils.isEmpty(selection) ? ""
                                : (" where " + appendSelection(uri, selection)))
                        + (TextUtils.isEmpty(sortOrder) ? ""
                                : (" order by " + TrendArticleDb.Article.TABLE + "." + sortOrder)),
                        appendSelectionArgs(uri, selectionArgs));
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            }
            case ARTICLE_STATE_DIR:
            case ARTICLE_STATE_ITEM: {
                Cursor cursor = mHelper.getReadableDatabase().query(
                        TrendArticleDb.ArticleState.TABLE, projection,
                        appendSelection(uri, selection), appendSelectionArgs(uri, selectionArgs),
                        null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            }
            case LOG_DATA_DIR:
            case LOG_DATA_ITEM: {
                Cursor cursor = mHelper.getReadableDatabase().query(
                        TrendArticleDb.LogData.TABLE, projection,
                        appendSelection(uri, selection), appendSelectionArgs(uri, selectionArgs),
                        null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            }
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }

    /** URIに対応したContent MIMEタイプを返却する */
    @Override
    public String getType(Uri uri) {
        switch (MATCHER.match(uri)) {
            case GENRE_DIR:
                return TrendArticleDb.Genre.DIR_TYPE;
            case GENRE_ITEM:
                return TrendArticleDb.Genre.ITEM_TYPE;
            case ARTICLE_DIR:
                return TrendArticleDb.Article.DIR_TYPE;
            case ARTICLE_ITEM:
                return TrendArticleDb.Article.ITEM_TYPE;
            case ARTICLE_STATE_DIR:
                return TrendArticleDb.ArticleState.DIR_TYPE;
            case ARTICLE_STATE_ITEM:
                return TrendArticleDb.ArticleState.ITEM_TYPE;
            case LOG_DATA_DIR:
                return TrendArticleDb.LogData.DIR_TYPE;
            case LOG_DATA_ITEM:
                return TrendArticleDb.LogData.ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }

    /** ContentProviderに新規レコードを挿入 */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = mHelper.getWritableDatabase()
                .insertOrThrow(getTableName(uri), null, values);
        Uri insertedUri = ContentUris.withAppendedId(uri, rowId);
        switch (MATCHER.match(uri)) {
            case ARTICLE_DIR:
            case ARTICLE_ITEM:
                break;
            default:
                getContext().getContentResolver().notifyChange(insertedUri, null);
                break;
        }
        return insertedUri;
    }

    /** ContentProviderの既存レコードを削除 */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = mHelper.getWritableDatabase().delete(getTableName(uri),
                appendSelection(uri, selection), appendSelectionArgs(uri, selectionArgs));
        switch (MATCHER.match(uri)) {
            case ARTICLE_DIR:
            case ARTICLE_ITEM:
                break;
            default:
                getContext().getContentResolver().notifyChange(uri, null);
                break;
        }
        return count;
    }

    /** ContentProviderの既存レコードを更新 */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = mHelper.getWritableDatabase().update(getTableName(uri), values,
                appendSelection(uri, selection), appendSelectionArgs(uri, selectionArgs));
        switch (MATCHER.match(uri)) {
            case ARTICLE_DIR:
            case ARTICLE_ITEM:
                break;
            default:
                getContext().getContentResolver().notifyChange(uri, null);
                break;
        }
        return count;
    }

    /** ContentProviderに複数の新規レコードを挿入 */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        switch (MATCHER.match(uri)) {
            case GENRE_DIR:
            case GENRE_ITEM:
                return 0;
            case ARTICLE_DIR:
            case ARTICLE_ITEM:
                return TrendArticleDb.Article.bulkInsert(mHelper.getWritableDatabase(), values);
            case ARTICLE_STATE_DIR:
            case ARTICLE_STATE_ITEM:
                return 0;
            case LOG_DATA_DIR:
            case LOG_DATA_ITEM:
                return 0;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }

    /** ContentProviderに対し複数の更新処理および削除処理を要求 */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        ContentProviderResult[] results = null;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            results = super.applyBatch(operations);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return results;
    }

    /**
     * URIに対応するテーブル名を返却
     * 
     * @param uri リクエストされたURI
     * @return 対応するテーブル名
     */
    private String getTableName(Uri uri) {
        switch (MATCHER.match(uri)) {
            case GENRE_DIR:
            case GENRE_ITEM:
                return TrendArticleDb.Genre.TABLE;
            case ARTICLE_DIR:
            case ARTICLE_ITEM:
                return TrendArticleDb.Article.TABLE;
            case ARTICLE_STATE_DIR:
            case ARTICLE_STATE_ITEM:
                return TrendArticleDb.ArticleState.TABLE;
            case LOG_DATA_DIR:
            case LOG_DATA_ITEM:
                return TrendArticleDb.LogData.TABLE;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }

    /**
     * URIにIDが指定されている場合にselection文字列にIDの指定を追加する
     * 
     * @param uri リクエストされたURI
     * @param selection IDの指定を追加するselection文字列
     * @return　IDの指定が追加されたselection文字列
     */
    private String appendSelection(Uri uri, String selection) {
        if (uri.getPathSegments().size() <= 1) {
            return selection;
        }
        return BaseColumns._ID + " = ?" + (selection == null ? "" : " AND (" + selection + ")");
    }

    /**
     * URIにIDが指定されている場合にselectionArgs配列にIDの指定を追加する
     * 
     * @param uri リクエストされたURI
     * @param selectionArgs IDの指定を追加するselectionArgs配列
     * @return IDの指定が追加されたselectionArgs配列
     */
    private String[] appendSelectionArgs(Uri uri, String[] selectionArgs) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() <= 1) {
            return selectionArgs;
        }
        if ((selectionArgs == null) || (selectionArgs.length <= 0)) {
            return new String[] {
                    pathSegments.get(1)
            };
        }
        String[] appendedArgs = new String[selectionArgs.length + 1];
        appendedArgs[0] = pathSegments.get(1);
        System.arraycopy(selectionArgs, 0, appendedArgs, 1, selectionArgs.length);
        return appendedArgs;
    }

    // class
    // ----------------------------------------------------------------

    /** TrendArticleProviderが管理するアプリ内DBを取り扱うためのSQLiteOpenHelper */
    private static class DbOpenHelper extends SQLiteOpenHelper {

        // field
        // ----------------------------------------------------------------

        private static final int DB_VERSION = 1;
        private static final String DB_FILE_NAME = "trend_article_db.sqlite";

        // method
        // ----------------------------------------------------------------

        /** DBが生成される際に呼び出されるコールバック */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TrendArticleDb.Genre.SQL_CREATE);
            db.execSQL(TrendArticleDb.Article.SQL_CREATE);
            db.execSQL(TrendArticleDb.ArticleState.SQL_CREATE);
            db.execSQL(TrendArticleDb.LogData.SQL_CREATE);
        }

        /** DBのバージョンが更新された際に呼び出されるコールバック */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /* NOOP */
        }

        // constructor
        // ----------------------------------------------------------------

        /**
         * コンストラクタ
         * 
         * @param context Contextオブジェクト
         */
        DbOpenHelper(Context context) {
            super(context, DB_FILE_NAME, null, DB_VERSION);
        }

    }

}
