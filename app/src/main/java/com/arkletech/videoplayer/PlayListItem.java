package com.arkletech.videoplayer;

public class PlayListItem {
    String mUrl;
    String mLabel;
    String mThumbnail;
    String mMediaType;

    public PlayListItem ()
    {
        mUrl = mLabel = mThumbnail = mMediaType = "";
    }

    public PlayListItem (String url, String label, String thumbnail)
    {
        mUrl = url;
        mLabel = label;
        mThumbnail = thumbnail;
    }

    public String getUrl() { return mUrl; }
    public void setUrl(String url) { mUrl = url; }
    public String getLabel() { return mLabel; }
    public void setLabel(String label) { mLabel = label; }
    public String getThumbnaiUrll() { return mThumbnail; }
    public void setThumbnailUrl(String thumbnail) { mThumbnail = thumbnail; }
    public String getType() { return mMediaType; }
    public void setType(String type) { mMediaType = type; }
}
