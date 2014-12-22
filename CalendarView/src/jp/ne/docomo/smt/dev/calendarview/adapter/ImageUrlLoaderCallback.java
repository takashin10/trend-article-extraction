/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.adapter;

import android.graphics.Bitmap;

/**
 * ImageUrlLoaderクラスのコールバック用のインタフェース
 */
public interface ImageUrlLoaderCallback {
    /**
     * コールバック
     * @param bmp
     * @param url
     */
    void onResponse(Bitmap bmp, String url);
}
