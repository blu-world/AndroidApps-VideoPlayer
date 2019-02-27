package com.arkletech.videoplayer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class LoadPlaylist extends AsyncTask<String, Integer, String> {
    static final String TAG="LoadPlaylist";
    static final int FORMAT_JSON = 0;
    static final int FORMAT_M3U = 2;
    private OnPlaylistLoardedListener onPlaylistLoadedListener;
    Context mContext;
    static public ArrayList<PlayListItem> mItemList=null;
    private ProgressDialog progressDialog;
    private int mPlaylistFormat=FORMAT_JSON;  // either json=10 or m3u=2

    public LoadPlaylist(Context context)
    {
        mContext = context;
        mItemList = new ArrayList<PlayListItem>();
    }

    public static ArrayList<PlayListItem> getPlaylist() { return mItemList; }

    // Setup callback after the playlist loaded
    public interface OnPlaylistLoardedListener
    {
        public void onPlaylistLoaded();
    }

    public void setOnPlaylistLoaredListener(OnPlaylistLoardedListener listener)
    {
        onPlaylistLoadedListener = listener;
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute()");
        progressDialog = ProgressDialog.show(mContext, null, "Loading Play List ...", true);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        mItemList.clear();
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG, "doInBackground("+strings[0]+")");
        if (strings[0].endsWith(".m3u"))
            mPlaylistFormat = FORMAT_M3U;
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", "Error loading List from server:"+e.getMessage(), e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute("+s+")");

        if (mPlaylistFormat == FORMAT_JSON) {
            try {
                JSONObject json = new JSONObject(s);
//                    Log.d(TAG, "Json Content: "+mResponse);
                if (s.length() != 0) {
                    JSONArray jsonArray = json.getJSONArray("list");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo_inside = jsonArray.getJSONObject(i);
                        String name = jo_inside.getString("name");
                        String url = jo_inside.getString("url");
                        String thumb = jo_inside.getString("thumbnail");
                        PlayListItem item = new PlayListItem(url, name, thumb);
                        Log.d(TAG, "name=\"" + name + "\", url=\"" + url + "\", thumbnail=\"" + thumb + "\"");
                        mItemList.add(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            ParsingM3U(s);
        }

        progressDialog.dismiss();

        onPlaylistLoadedListener.onPlaylistLoaded();
    }

    public int ParsingM3U(String data) {
        int cnt=0;
        String line;
        int b=0, e=0;
        Log.d(TAG, "ParsingM3U()");
        // Check format: the fiirst line has to be "#EXTM3U"
        e = data.indexOf('\n');
        line = data.substring(b, e);
        if (!line.startsWith("#EXTM3U")) {
            Log.e(TAG, "Not m3u file...");
            return -1;
        }
        String name="", url="";
        Log.d(TAG, "b="+b+", e="+e);
        b = e + 1;
        do {
            e = data.indexOf('\n', b);
            Log.d(TAG, "b="+b+", e="+e);
            if (e == -1)
                line = data.substring(b);
            else
                line = data.substring(b, e);
//            Log.d(TAG, "line="+line);
            if (line.startsWith("#EXTINF:")) {
                int idx = line.lastIndexOf(',');
                name = line.substring(idx+1);
            }
            else if (line.length() > 0){
                PlayListItem item = new PlayListItem(line, name, "");
                mItemList.add(item);
                cnt++;
                Log.d(TAG, "item["+cnt+"]: name=\"" + name + "\", url=\"" + line + "\", thumbnail=\"\"");
            }
            b = e + 1;
        } while (e != -1);

        return cnt;
    }
}
