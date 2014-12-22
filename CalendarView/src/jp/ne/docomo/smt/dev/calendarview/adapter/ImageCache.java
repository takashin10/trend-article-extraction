/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.adapter;

import android.graphics.Bitmap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 画像をキャッシュするクラス
 */
public final class ImageCache {
    // キャッシュの最大サイズ
    private static final int MAX_CACHE = 20;

    // 画像のキャッシュ(FIFO方式)
    private static Map<String, Bitmap> sCache =
            new LinkedHashMap<String, Bitmap>(MAX_CACHE) {
                private static final long serialVersionUID = 1L;
                protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest)  {
                    return size() > MAX_CACHE;
                }
            };

    private ImageCache() { }

    /**
     * 画像の格納
     * @param key キー
     * @param bmp ビットマップ
     */
    public static void setImageCache(String key, Bitmap bmp) {
        sCache.put(key, bmp);
    }

    /**
     * 画像の取り出し
     * @param key
     * @return 画像データ
     */
    public static Bitmap getImageCache(String key) {
        return sCache.get(key);
    }

    /**
     * キャッシュサイズを取得
     * @return キャッシュサイズ
     */
    public static int getSize() {
        return sCache.size();
    }
}
