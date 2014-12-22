/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.views;

import jp.ne.docomo.smt.dev.calendarview.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EventInputView extends RelativeLayout {

    private Context mContent;
    private TextView mEventName;
    private Button mRemoveBtn;

    public EventInputView(Context context) {
        super(context);
        this.mContent = context;
        initLayout();
    }

    public EventInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContent = context;
        initLayout();
    }

    public EventInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContent = context;
        initLayout();
    }

    void initLayout() {
        LayoutInflater inflater =
                (LayoutInflater) mContent.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.event_input_row, this);
        mEventName = (TextView) findViewById(R.id.event_name);
        mRemoveBtn = (Button) findViewById(R.id.remove_btn_id);
    }

    public void setOnRemoveclickListener(OnClickListener onRemoveclickListener) {
        mRemoveBtn.setOnClickListener(onRemoveclickListener);
    }

    public TextView getEventName() {
        return mEventName;
    }

    public Button getRemoveBtn() {
        return mRemoveBtn;
    }

}
