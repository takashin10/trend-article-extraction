/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.entities;

import jp.ne.docomo.smt.dev.sentenceunderstanding.common.SentenceProjectSpecific;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceContentData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceDialogStatusData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceExtractedWordsData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceUserUtteranceData;

import java.util.ArrayList;
import java.util.List;

public class TaskResponse {

    public String projectKey;
    public AppInfo appInfo;
    public String clientVer;
    public String dialogMode;
    public String language;
    public String userId;
    public SentenceDialogStatusData dialogStatus;
    public SentenceContentData content;
    public SentenceUserUtteranceData userUtterance;
    public List<String> taskIdList;
    public ArrayList<SentenceExtractedWordsData> extractedWords;
    public String serverSendTime;
    public SentenceProjectSpecific projectSpecific;

}
