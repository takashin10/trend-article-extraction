/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.calendarview.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import jp.ne.docomo.smt.dev.calendarview.R;
import jp.ne.docomo.smt.dev.calendarview.entities.SearchResponse.ArticleContents;

public class InformationAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ArrayList<ArticleContents> mData;
    private Context mContext;

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public InformationAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (mData != null) {
            return mData.size();
        }
        return 10;
    }

    @Override
    public ArticleContents getItem(int position) {
        if (mData != null && position < mData.size()) {
            return mData.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.information_rows, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.information_image);
            holder.title = (TextView) convertView.findViewById(R.id.information_title);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(null, null, position, getItemId(position));
                }
            }
        });
        final ArticleContents item = getItem(position);
        holder.image.setImageBitmap(null);
        if (item != null) {
            holder.title.setText(item.contentData.title);
            holder.image.setTag(item.contentData.imageUrl);
            // 画像の読込
            ImageUrlLoader request = new ImageUrlLoader(
                    new ImageUrlLoaderCallback() {
                        /**
                         * コールバック
                         */
                        @Override
                        public void onResponse(Bitmap bmp, String url) {
                            holder.image.setImageBitmap(null);
                            if (bmp != null) {
                                if (holder.image.getTag() != null && holder.image.getTag().equals(url)) {
                                    // 画像を表示
                                    holder.image.setImageBitmap(bmp);
                                }
                            }
                        }
                    }
                    );
            request.startRequest(mContext, item.contentData.imageUrl);
        }
        return convertView;
    }

    private class ViewHolder {
        public ImageView image;
        public TextView title;
    }

    public void setData(ArrayList<ArticleContents> searchRespondList) {
        mData = searchRespondList;
        notifyDataSetChanged();
    }
}
