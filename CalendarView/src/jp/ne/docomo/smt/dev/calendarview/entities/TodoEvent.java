/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.entities;

public class TodoEvent {
    public int id;
    public String createAt;
    public String event;

    public TodoEvent(int id, String createAt, String event) {
        super();
        this.id = id;
        this.createAt = createAt;
        this.event = event;
    }

}
