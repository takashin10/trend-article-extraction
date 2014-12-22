/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.entities;

import java.util.ArrayList;
import java.util.Date;

public class SearchResponse {

    public static class ImageSize {
        public int height;
        public int width;
    }

    public static class ContentData {
        public String title;
        public String body;
        public String linkUrl;
        public String imageUrl;
        public ImageSize imageSize;
        public String createdDate;
        public String sourceDomain;
        public String sourceName;
    }

    public static class ArticleContents {
        public long contentId;
        public int contentType;
        public int genreId;
        public ContentData contentData;
        public String relatedContents;
        public Date date;
    }

    public int totalResults;
    public int startIndex;
    public int itemsPerPage;
    public String issueDate;
    public ArrayList<ArticleContents> articleContents;
}
