/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.entities;

import java.util.Comparator;
import java.util.Date;

import jp.ne.docomo.smt.dev.calendarview.entities.SearchResponse.ArticleContents;

public class SearchComparator implements Comparator<ArticleContents> {

    @Override
    public int compare(ArticleContents lhs, ArticleContents rhs) {
        int result = 0;
        Date date1 = lhs.date;
        Date date2 = rhs.date;

        if (date1.compareTo(date2) > 0) {
            result = 1;
        } else if (date1.compareTo(date2) < 0) {
            result = -1;
        }
        return result;
    }

}
