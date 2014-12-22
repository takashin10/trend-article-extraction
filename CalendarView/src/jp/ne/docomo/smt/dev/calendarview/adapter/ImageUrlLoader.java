/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * インターネット上の画像ファイルを取得するクラス
 */
public class ImageUrlLoader {
    // コールバック用のインスタンス
    private ImageUrlLoaderCallback mImageUrlLoaderCallback;

    /**
     * コンストラクタ
     * @param imageUrlLoaderCallback コールバック用のインスタンス
     */
    public ImageUrlLoader(ImageUrlLoaderCallback imageUrlLoaderCallback) {
        this.mImageUrlLoaderCallback = imageUrlLoaderCallback;
    }

    /**
     * 非同期通信用インナークラス
     */
    public class ImageUrlLoaderAsyncTask extends
            AsyncTask<String, Integer, Bitmap> {
        private AlertDialog.Builder mDlg;
        private String mUrl = null;
        private String mExceptionMessage = null;

        public ImageUrlLoaderAsyncTask(AlertDialog.Builder dlg) {
            super();
            mDlg = dlg;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            mUrl = params[0];
            // キャッシュから画像を取り出す
            Bitmap resultData = ImageCache.getImageCache(mUrl);
            if (resultData == null) {
                try {
                    // インターネットから画像を取得
                    URL url = new URL(mUrl);
                    InputStream istream = url.openStream();
                    resultData = BitmapFactory.decodeStream(istream);
                    istream.close();
                    // キャッシュに格納
                    ImageCache.setImageCache(mUrl, resultData);
                } catch (MalformedURLException ex) {
                    mExceptionMessage = "Message: " + ex.getMessage();
                } catch (IOException ex) {
                    mExceptionMessage = "Message: " + ex.getMessage();
                }
            }

            return resultData;
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPostExecute(Bitmap resultData) {
            if (resultData == null) {
                // エラー時の処理
                mDlg.setTitle("画像ファイル取得失敗");
                mDlg.setMessage(mExceptionMessage + " ");
                mDlg.show();

            } else {
                // コールバック
                mImageUrlLoaderCallback.onResponse(resultData, mUrl);
            }
        }
    }

    /**
     * リクエスト開始メソッド
     * @param context
     * @param url
     */
    public void startRequest(Context context, String url) {
        // 画像のURLが無い場合はスキップ
        if (url != null) {
            // エラー表示用
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);

            // 実行
            ImageUrlLoaderAsyncTask task = new ImageUrlLoaderAsyncTask(dlg);
            task.execute(url);
        }
    }
}
