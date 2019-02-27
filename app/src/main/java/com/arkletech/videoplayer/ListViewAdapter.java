package com.arkletech.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    static final String TAG="ListViewAdapter";
    private Context mContext;
    private ArrayList<PlayListItem> mList;
    private LayoutInflater inflater;
    private PlayListItem item;
    private String mTheeme = "dark_theme";

    public ListViewAdapter(Context c, ArrayList<PlayListItem> list)
    {
        Log.d(TAG, "ListViewAdapter()");
        mContext = c;
        mList = list;
        inflater = LayoutInflater.from(mContext);
    }

    public void setAppTheme(String theme) { mTheeme = theme; }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount()="+mList.size());
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        Log.d(TAG, "getItem("+i+")");
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        Log.d(TAG, "getItemId("+i+")");
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.d(TAG, "ListViewAdapter::getView("+i+")");
        if (view == null) {
            view = inflater.inflate(R.layout.playlist_item, null);
        }

        PlayListItem newsItem = (PlayListItem) mList.get(i);
        Log.d(TAG, "Item: Label="+newsItem.getLabel()+",url="+newsItem.getUrl()+",thumb="+newsItem.getThumbnaiUrll());
        TextView tv = view.findViewById(R.id.tv_playlist_label);
        tv.setText(newsItem.getLabel());
        tv = view.findViewById(R.id.tv_playlist_url);
        tv.setText(newsItem.getUrl());
        ImageView iv = view.findViewById(R.id.iv_playlist_thumb);
        if (newsItem.getThumbnaiUrll().length() > 0) {
            new ImageLoaderTask(iv).execute(newsItem.getThumbnaiUrll());
        }
        else {
            if (mTheeme.compareTo("theme_dark") == 0)
                iv.setImageResource(R.drawable.baseline_theaters_white);
            else
                iv.setImageResource(R.drawable.baseline_theaters_black);
        }
        return view;
    }
}
