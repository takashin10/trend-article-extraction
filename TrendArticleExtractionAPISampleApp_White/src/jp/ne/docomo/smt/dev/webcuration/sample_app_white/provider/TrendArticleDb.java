/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.webcuration.sample_app_white.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.webcuration.constants.Lang;
import jp.ne.docomo.smt.dev.webcuration.data.CurationArticleContentsData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationContentData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationContentsResultData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationGenreData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationImageSizeData;
import jp.ne.docomo.smt.dev.webcuration.data.CurationSubGenreData;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.BuildConfig;
import jp.ne.docomo.smt.dev.webcuration.sample_app_white.oauth.AccessTokenManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/** TrendArticleProviderによって管理される各種テーブル定義 */
public class TrendArticleDb {

    // class
    // ----------------------------------------------------------------

    /** ジャンル情報テーブル */
    public static class Genre implements BaseColumns {

        // field
        // ----------------------------------------------------------------

        /** 親ジャンルが存在しない場合に設定するダミーの親ジャンルID */
        public static final int PARENT_ROOT = Integer.MAX_VALUE;

        /** テーブル名 */
        public static final String TABLE = "genre";

        /** カラム名(ジャンルID) */
        public static final String KEY_GENRE_ID = "genre_id";

        /** カラム名(親ジャンルID, サブジャンル用) */
        public static final String KEY_PARENT = "parent";

        /** カラム名(ジャンル名) */
        public static final String KEY_TITLE = "title";

        /** カラム名(ジャンルの説明文) */
        public static final String KEY_DESCRIPTION = "description";

        /** カラム名(ジャンルの最終更新日時) */
        public static final String KEY_ISSUE_DATE = "issue_date";

        /** カラム名(ジャンル取得時の言語設定) */
        public static final String KEY_LANGUAGE = "language";

        /** Content URI */
        public static final Uri CONTENT_URI = Uri.parse(
                "content://" + TrendArticleProvider.AUTHORITY + "/" + TABLE);

        /** MIMEタイプ(単一レコード) */
        static final String ITEM_TYPE = "vnd.android.cursor.item/vnd.jp.ne.docomo.smt.dev.webcuration.sample_app.provider.trend_article_db/genre";

        /** MIMEタイプ(複数レコード) */
        static final String DIR_TYPE = "vnd.android.cursor.dir/vnd.jp.ne.docomo.smt.dev.webcuration.sample_app.provider.trend_article_db/genre";

        /** テーブル生成用SQLステートメント */
        static final String SQL_CREATE = "create table " + TABLE + " ("
                + _ID + " integer primary key autoincrement,"
                + KEY_GENRE_ID + " integer,"
                + KEY_PARENT + " integer,"
                + KEY_TITLE + " text,"
                + KEY_DESCRIPTION + " text,"
                + KEY_ISSUE_DATE + " integer,"
                + KEY_LANGUAGE + " text"
                + ");";

        private static final String SELECTION = ("( "
                + KEY_PARENT + " == ? ) AND ( "
                + KEY_GENRE_ID + "== ? ) AND ( "
                + KEY_LANGUAGE + " == ? )");

        // method
        // ----------------------------------------------------------------

        /**
         * DB上のジャンル情報をサブジャンル含め一括更新、存在しないレコードは新規に挿入する
         * 
         * @param context Contextオブジェクト
         * @param language 取得時に設定された言語
         * @param list SDKにより取得されたジャンル一覧
         */
        public static void updateGenres(Context context,
                Lang language, List<CurationGenreData> list) {
            String languageName = language.name();
            String[] selectionArgs = new String[3];
            selectionArgs[2] = languageName;
            ContentValues values = new ContentValues();
            ContentResolver resolver = context.getContentResolver();
            for (CurationGenreData genre : list) {
                values.clear();
                int genreId = genre.getGenreId();
                values.put(KEY_TITLE, genre.getTitle());
                values.put(KEY_DESCRIPTION, genre.getDescription());
                selectionArgs[0] = Integer.toString(PARENT_ROOT);
                selectionArgs[1] = Integer.toString(genreId);
                int count = resolver.update(CONTENT_URI, values, SELECTION, selectionArgs);
                if (count <= 0) {
                    values.put(KEY_PARENT, PARENT_ROOT);
                    values.put(KEY_GENRE_ID, genreId);
                    values.put(KEY_LANGUAGE, languageName);
                    resolver.insert(CONTENT_URI, values);
                }

                // サブジャンルの取得とDB更新
                ArrayList<CurationSubGenreData> children =
                        genre.getCurationSubGenreDataList();
                if ((children == null) || children.isEmpty()) {
                    continue;
                }
                for (CurationSubGenreData subGenre : children) {
                    values.clear();
                    int subGenreId = subGenre.getSubGenreId();
                    values.put(KEY_TITLE, subGenre.getTitle());
                    values.put(KEY_DESCRIPTION, subGenre.getDescription());
                    selectionArgs[0] = Integer.toString(genreId);
                    selectionArgs[1] = Integer.toString(subGenreId);
                    count = resolver.update(CONTENT_URI, values, SELECTION, selectionArgs);
                    if (count <= 0) {
                        values.put(KEY_PARENT, genreId);
                        values.put(KEY_GENRE_ID, subGenreId);
                        values.put(KEY_LANGUAGE, languageName);
                        resolver.insert(CONTENT_URI, values);
                    }
                }
            }
        }

    }

    /** 記事コンテンツテーブル */
    public static class Article implements BaseColumns {

        // field
        // ----------------------------------------------------------------

        /** レコメンドジャンル取得時に指定するジャンルID */
        public static final int GENRE_ID_RECOMEND = Integer.MAX_VALUE;

        /** キーワード検索で全てのジャンルの結果を取得する際に指定するジャンルID */
        public static final int GENRE_ID_SEARCH_ALL = Integer.MAX_VALUE - 1;

        /** テーブル名 */
        public static final String TABLE = "article";

        /** カラム名(記事のジャンルID) */
        public static final String KEY_GENRE_ID = "genre_id";

        /** カラム名(記事コンテンツID) */
        public static final String KEY_CONTENT_ID = "content_id";

        /** カラム名(記事コンテンツ種別) */
        public static final String KEY_CONTENT_TYPE = "content_type";

        /** カラム名(Twitter上でのシェア数) */
        public static final String KEY_TW_COUNT = "tw_count";

        /** カラム名(記事コンテンツタイトル) */
        public static final String KEY_TITLE = "title";

        /** カラム名(記事コンテンツ本文) */
        public static final String KEY_BODY = "body";

        /** カラム名(元記事URL) */
        public static final String KEY_LINK_URL = "link_url";

        /** カラム名(配信元名) */
        public static final String KEY_SOURCE_NAME = "source_name";

        /** カラム名(配信元ドメイン) */
        public static final String KEY_SOURCE_DOMAIN = "source_domain";

        /** カラム名(記事コンテンツ画像の横幅) */
        public static final String KEY_IMAGE_WIDTH = "image_width";

        /** カラム名(記事コンテンツ画像の高さ) */
        public static final String KEY_IMAGE_HEIGHT = "image_height";

        /** カラム名(記事コンテンツ画像URL) */
        public static final String KEY_IMAGE_URL = "image_url";

        /** カラム名(記事コンテンツ作成日時) */
        public static final String KEY_CREATED_DATE = "created_date";

        /** Content URI */
        public static final Uri CONTENT_URI = Uri.parse(
                "content://" + TrendArticleProvider.AUTHORITY + "/" + TABLE);

        /** MIMEタイプ(単一レコード) */
        static final String ITEM_TYPE = "vnd.android.cursor.item/vnd.jp.ne.docomo.smt.dev.webcuration.sample_app.provider.trend_article_db/article";

        /** MIMEタイプ(複数レコード) */
        static final String DIR_TYPE = "vnd.android.cursor.dir/vnd.jp.ne.docomo.smt.dev.webcuration.sample_app.provider.trend_article_db/article";

        /** テーブル生成用SQLステートメント */
        static final String SQL_CREATE = "create table " + TABLE + " ("
                + _ID + " integer primary key autoincrement,"
                + KEY_GENRE_ID + " integer,"
                + KEY_CONTENT_ID + " integer,"
                + KEY_CONTENT_TYPE + " integer,"
                + KEY_TW_COUNT + " integer,"
                + KEY_TITLE + " text,"
                + KEY_BODY + " text,"
                + KEY_LINK_URL + " text,"
                + KEY_SOURCE_NAME + " text,"
                + KEY_SOURCE_DOMAIN + " text,"
                + KEY_IMAGE_WIDTH + " integer,"
                + KEY_IMAGE_HEIGHT + " integer,"
                + KEY_IMAGE_URL + " text,"
                + KEY_CREATED_DATE + " integer"
                + ");";

        // method
        // ----------------------------------------------------------------

        /**
         * DB上の記事データをジャンルを指定して更新、以前取得したデータは削除される
         * 
         * @param context Contextオブジェクト
         * @param genreId 更新を行うジャンルID
         * @param data SDKにより取得された記事情報
         * @param forceUpdate trueの場合記事の作成日時を問わず強制的にDBを更新する
         * @return 指定したジャンルの記事データが正常に更新された場合はtrue
         */
        public static boolean updateArticles(Context context, int genreId,
                CurationContentsResultData data, boolean forceUpdate) {
            if (data == null) {
                return false;
            }

            // 記事一覧データを取得
            ArrayList<CurationArticleContentsData> list =
                    data.getCurationArticleContentsDataList();
            if (list == null) {
               return false;
            }

            // 記事のセットの作成日時を取得
            long issueDate = 0;
            try {
                issueDate = data.getIssueDateOnCalendar().getTimeInMillis();
            } catch (SdkException e) {
                issueDate = 0;
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }

            // 強制更新時は以下の更新判定ロジックをスキップ
            ContentResolver resolver = context.getContentResolver();
            if (!forceUpdate) {
                // ジャンルの最終更新日時を取得
                long lastUpdate = 0;
                Cursor cursor = resolver.query(Genre.CONTENT_URI, null,
                        ("( " + Genre.KEY_PARENT + " == " + Genre.PARENT_ROOT + " ) AND ( "
                                + Genre.KEY_GENRE_ID + " == " + genreId + " )"), null, null);
                if ((cursor != null) && !cursor.isClosed()) {
                    try {
                        if ((cursor.getCount() > 0) && cursor.moveToFirst()) {
                            lastUpdate = cursor.getLong(
                                    cursor.getColumnIndex(Genre.KEY_ISSUE_DATE));
                        }
                    } finally {
                        cursor.close();
                    }
                }

                // 記事の作成日時が最終更新日時より前の場合は更新しない
                if (issueDate < lastUpdate) {
                    return true;
                }
            }

            // ジャンルの最終更新日時を更新
            ContentValues values = new ContentValues();
            values.put(Genre.KEY_ISSUE_DATE, issueDate);
            int count = resolver.update(Genre.CONTENT_URI, values,
                    ("( " + Genre.KEY_PARENT + " == " + Genre.PARENT_ROOT + " ) AND ( "
                            + Genre.KEY_GENRE_ID + " == " + genreId + " )"), null);
            if (BuildConfig.DEBUG) {
                Log.d("TrendArticleDb.Genre", "Genre " + genreId + " is updated:Count=" + count);
            }

            // DB上の古い記事データを削除
            count = resolver.delete(CONTENT_URI, (KEY_GENRE_ID + " == " + genreId), null);
            if (BuildConfig.DEBUG) {
                Log.d("TrendArticleDb.Article", "Genre " + genreId
                        + " - Articles are deleted:Count=" + count);
            }

            // 取得した記事データをDBに挿入
            int length = list.size();
            ContentValues[] valuesArray = new ContentValues[length];
            for (int i = 0; i < length; i++) {
                values = new ContentValues();
                values.put(KEY_GENRE_ID, genreId);

                CurationArticleContentsData article = list.get(i);
                values.put(KEY_CONTENT_ID, article.getContentId());
                values.put(KEY_CONTENT_TYPE, article.getContentType());

                CurationContentData content = article.getCurationContentData();
                if (content != null) {
                    values.put(KEY_TITLE, content.getTitle());
                    values.put(KEY_BODY, content.getBody());
                    values.put(KEY_LINK_URL, content.getLinkUrl());
                    values.put(KEY_SOURCE_NAME, content.getSourceName());
                    values.put(KEY_SOURCE_DOMAIN, content.getSourceDomain());
                    values.put(KEY_IMAGE_URL, content.getImageUrl());

                    // 記事コンテンツ作成日時文字列をlongに変換して設定
                    long createdDate = 0;
                    try {
                        createdDate = content.getCreatedDateOnCalendar().getTimeInMillis();
                    } catch (SdkException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    } finally {
                        values.put(KEY_CREATED_DATE, createdDate);
                    }

                    CurationImageSizeData imageSize = content.getCurationImageSizeData();
                    if (imageSize != null) {
                        values.put(KEY_IMAGE_WIDTH, imageSize.getWidth());
                        values.put(KEY_IMAGE_HEIGHT, imageSize.getHeight());
                    }
                }
                valuesArray[i] = values;
            }
            count = resolver.bulkInsert(CONTENT_URI, valuesArray);
            if (BuildConfig.DEBUG) {
                Log.d("TrendArticleDb.Article", "Genre " + genreId
                        + " - Articles are inserted:Count=" + count);
            }
            resolver.notifyChange(CONTENT_URI, null);
            return true;
        }

        /**
         * 記事情報の一括挿入
         * 
         * @param db SQLiteデータベースオブジェクト
         * @param valuesArray 記事情報を設定したContentValues配列
         * @return 挿入した記事件数
         */
        static int bulkInsert(SQLiteDatabase db, ContentValues[] valuesArray) {
            int count = 0;
            int result = 0;
            try {
                // トランザクション開始
                db.beginTransaction();

                // Insert文をプリコンパイル
                SQLiteStatement statement = db.compileStatement("insert into " + TABLE + "("
                        + KEY_GENRE_ID + ", "
                        + KEY_CONTENT_ID + ", "
                        + KEY_CONTENT_TYPE + ", "
                        + KEY_TW_COUNT + ", "
                        + KEY_TITLE + ", "
                        + KEY_BODY + ", "
                        + KEY_LINK_URL + ", "
                        + KEY_SOURCE_NAME + ", "
                        + KEY_SOURCE_DOMAIN + ", "
                        + KEY_IMAGE_WIDTH + ", "
                        + KEY_IMAGE_HEIGHT + ", "
                        + KEY_IMAGE_URL + ", "
                        + KEY_CREATED_DATE + ")"
                        + " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

                for (ContentValues values : valuesArray) {
                    // 値をバインドしてコンパイル済みのInsert文を実行
                    int index = 0;
                    bind(statement, ++index, values.getAsLong(KEY_GENRE_ID));
                    bind(statement, ++index, values.getAsLong(KEY_CONTENT_ID));
                    bind(statement, ++index, values.getAsLong(KEY_CONTENT_TYPE));
                    bind(statement, ++index, values.getAsLong(KEY_TW_COUNT));
                    bind(statement, ++index, values.getAsString(KEY_TITLE));
                    bind(statement, ++index, values.getAsString(KEY_BODY));
                    bind(statement, ++index, values.getAsString(KEY_LINK_URL));
                    bind(statement, ++index, values.getAsString(KEY_SOURCE_NAME));
                    bind(statement, ++index, values.getAsString(KEY_SOURCE_DOMAIN));
                    bind(statement, ++index, values.getAsLong(KEY_IMAGE_WIDTH));
                    bind(statement, ++index, values.getAsLong(KEY_IMAGE_HEIGHT));
                    bind(statement, ++index, values.getAsString(KEY_IMAGE_URL));
                    bind(statement, ++index, values.getAsLong(KEY_CREATED_DATE));
                    if (statement.executeInsert() != -1) {
                        count++;
                    }
                }

                // トランザクション内の処理を完了
                db.setTransactionSuccessful();
                result = count;
            } finally {
                // トランザクション終了
                db.endTransaction();
            }
            return result;
        }

        /**
         * LongオブジェクトをSQLiteStatementにバインド
         * 
         * @param statement バインド先のSQLiteStatementオブジェクト
         * @param index バインド先のインデックス、1始まりの整数値で指定する
         * @param value バインドするLongオブジェクト、nullの場合は0に変換される
         */
        private static void bind(SQLiteStatement statement, int index, Long value) {
            if (value == null) {
                value = 0L;
            }
            statement.bindLong(index, value);
        }

        /**
         * StringオブジェクトをSQLiteStatementにバインド
         * 
         * @param statement バインド先のSQLiteStatementオブジェクト
         * @param index バインド先のインデックス、1始まりの整数値で指定する
         * @param value バインドするStringオブジェクト、nullの場合は空文字列("")に変換される
         */
        private static void bind(SQLiteStatement statement, int index, String value) {
            if (TextUtils.isEmpty(value)) {
                value = "";
            }
            statement.bindString(index, value);
        }

    }

    /**
     * 記事コンテンツ状態テーブル 本サンプルアプリでは記事の未読・既読状態のみを管理している
     * 記事コンテンツテーブルは頻繁にレコードの削除が行われるため永続化すべき情報は本テーブルに格納し 記事コンテンツIDをキーとして結合して使用する
     */
    public static class ArticleState implements BaseColumns {

        // field
        // ----------------------------------------------------------------

        /** 記事の未読・既読状態：未読 */
        public static final int READ_STATE_UNREAD = 0;

        /** 記事の未読・既読状態：既読 */
        public static final int READ_STATE_ALREADY_READ = 1;

        /** テーブル名 */
        public static final String TABLE = "article_state";

        /** カラム名(記事コンテンツID) */
        public static final String KEY_CONTENT_ID = "article_content_id";

        /** カラム名(記事の未読・既読状態) */
        public static final String KEY_READ_STATE = "read_state";

        /** Content URI */
        public static final Uri CONTENT_URI = Uri.parse(
                "content://" + TrendArticleProvider.AUTHORITY + "/" + TABLE);

        /** MIMEタイプ(単一レコード) */
        static final String ITEM_TYPE = "vnd.android.cursor.item/vnd.jp.ne.docomo.smt.dev.webcuration.sample_app.provider.trend_article_db/article_state";

        /** MIMEタイプ(複数レコード) */
        static final String DIR_TYPE = "vnd.android.cursor.dir/vnd.jp.ne.docomo.smt.dev.webcuration.sample_app.provider.trend_article_db/article_state";

        /** テーブル生成用SQLステートメント */
        static final String SQL_CREATE = "create table " + TABLE + " ("
                + _ID + " integer primary key autoincrement,"
                + KEY_CONTENT_ID + " integer,"
                + KEY_READ_STATE + " integer"
                + ");";

        // method
        // ----------------------------------------------------------------

        /**
         * 記事コンテンツIDを指定して未読・既読状態を更新する
         * 
         * @param context Contextオブジェクト
         * @param contentId 未読・既読状態を更新する記事コンテンツID
         * @param isAlreadyRead trueなら既読、falseなら未読とする
         */
        public static void updateReadState(Context context, long contentId, boolean isAlreadyRead) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(KEY_READ_STATE, (isAlreadyRead
                    ? READ_STATE_ALREADY_READ : READ_STATE_UNREAD));
            int count = resolver.update(CONTENT_URI, values,
                    (KEY_CONTENT_ID + " == " + contentId), null);
            if (count <= 0) {
                values.put(KEY_CONTENT_ID, contentId);
                resolver.insert(CONTENT_URI, values);
            }
            resolver.notifyChange(Article.CONTENT_URI, null);
        }

    }

    /** 閲覧ログデータテーブル */
    public static class LogData implements BaseColumns {

        // field
        // ----------------------------------------------------------------

        /** テーブル名 */
        public static final String TABLE = "log_data";

        /** カラム名(記事コンテンツID) */
        public static final String KEY_CONTENT_ID = "content_id";

        /** カラム名(記事コンテンツ種別) */
        public static final String KEY_CONTENT_TYPE = "content_type";

        /** カラム名(ログデータ発生日付) */
        public static final String KEY_DATE = "date";

        /** カラム名(ジャンルID) */
        public static final String KEY_GENRE_ID = "genre_id";

        /** カラム名(キーワード) */
        public static final String KEY_KEYWORD = "keyword";

        /** カラム名(記事コンテンツ遷移先URL) */
        public static final String KEY_URL = "url";

        /** Content URI */
        public static final Uri CONTENT_URI = Uri.parse(
                "content://" + TrendArticleProvider.AUTHORITY + "/" + TABLE);

        /** MIMEタイプ(単一レコード) */
        static final String ITEM_TYPE = "vnd.android.cursor.item/vnd.jp.ne.docomo.smt.dev.webcuration.sample_app.provider.trend_article_db/log_data";

        /** MIMEタイプ(複数レコード) */
        static final String DIR_TYPE = "vnd.android.cursor.dir/vnd.jp.ne.docomo.smt.dev.webcuration.sample_app.provider.trend_article_db/log_data";

        /** テーブル生成用SQLステートメント */
        static final String SQL_CREATE = "create table " + TABLE + " ("
                + _ID + " integer primary key autoincrement,"
                + KEY_CONTENT_ID + " integer,"
                + KEY_CONTENT_TYPE + " integer,"
                + KEY_DATE + " integer,"
                + KEY_GENRE_ID + " integer,"
                + KEY_KEYWORD + " text,"
                + KEY_URL + " text"
                + ");";

        // method
        // ----------------------------------------------------------------

        /**
         * DBに送信予定ログデータを追加
         * 
         * @param context Contextオブジェクト
         * @param contentId 閲覧した記事コンテンツID
         * @param contentType 閲覧した記事コンテンツ種別
         * @param genreId 閲覧した記事コンテンツのジャンルID
         * @param url 閲覧した記事コンテンツの遷移先URL
         * @param keyword 記事コンテンツ閲覧時のキーワード
         */
        public static void insertLogData(Context context, long contentId,
                int contentType, int genreId, String url, String keyword) {
            // docomo ID未認証時はログデータを蓄積しない
            if (!AccessTokenManager.getInstance(context).hasAccessToken()) {
                return;
            }

            // DBにログデータを挿入
            ContentValues values = new ContentValues();
            values.put(KEY_CONTENT_ID, contentId);
            values.put(KEY_CONTENT_TYPE, contentType);
            values.put(KEY_DATE, new Date().getTime());
            values.put(KEY_GENRE_ID, genreId);
            values.put(KEY_URL, url);
            if (!TextUtils.isEmpty(keyword)) {
                values.put(KEY_KEYWORD, keyword);
            }
            Uri uri = context.getContentResolver().insert(CONTENT_URI, values);
            if (BuildConfig.DEBUG) {
                Log.d(LogData.class.getSimpleName(),
                        "Inserted Log Data:" + uri + "\n" + values.toString());
            }
        }

    }

}
