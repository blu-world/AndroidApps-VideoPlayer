package com.arkletech.videoplayer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Vector;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final String TAG="App::VideoPlayer";
    static final String URL_LIST="url_list";
    static final String APP_THEME="appTheme";
    String video_url = "http://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4";
    private static ProgressDialog progressDialog;
    VideoView vv;
    Intent mVideoPlayer = null;
    String[] url_entries;
    Vector<String> mUrlList;
    AutoCompleteTextView mActv;
    ArrayAdapter<String> mAdapter;
    String mAppTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mUrlList = new Vector<String>();

        loadPreferences(MainActivity.this);

        if (mAppTheme.compareTo("theme_dark") == 0)
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        else
            setTheme(R.style.AppTheme_Light_NoActionBar);

        super.onCreate(savedInstanceState);

        updateAdapterUrlEntries();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab::onClick()");
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
                TextView tv = findViewById(R.id.actv_recent);
                tv.setText("");
            }
        });

        CheckBox cb_hideTitle = findViewById(R.id.cb_hide_title);
        cb_hideTitle.setChecked(true);
        CheckBox cb_hideStatus = findViewById(R.id.cb_hide_status);
        cb_hideStatus.setChecked(true);
        Button button = findViewById(R.id.b_play);
        button.setOnClickListener((View.OnClickListener) this);
        button = findViewById(R.id.bt_edit_history);
        button.setOnClickListener(this);
        button = findViewById(R.id.bt_clear_history);
        button.setOnClickListener(this);

        //Creating the instance of ArrayAdapter containing list
        mAdapter = new ArrayAdapter<String> (this, R.layout.dropdown_itme_layout, url_entries) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                v.setBackgroundColor(Color.LTGRAY);
                return v;
            }
        };
        mActv = (AutoCompleteTextView)findViewById(R.id.actv_recent);
        mActv.setThreshold(1);
        mActv.setAdapter(mAdapter);
        mActv.setOnClickListener(this);

        mVideoPlayer = new Intent(getApplicationContext(), PlayVideoActivity.class);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_toggle) {
            if (mAppTheme.compareTo("theme_dark") == 0) {
                setTheme(R.style.AppTheme_Light);
                mAppTheme = "theme_light";
            }
            else {
                setTheme(R.style.AppTheme_Dark);
                mAppTheme = "theme_dark";
            }
            savePreferences(this);
            // restart the app to use the new Theme
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        int o = getResources().getConfiguration().orientation;
        Log.d("VideoPlayer", "onResume("+o+")");
        if (o == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onClick(View view) {
        Log.d("VideoPlayer", "onClick("+view+")");
        if (view.getId() == R.id.b_play) {
            EditText et = findViewById(R.id.actv_recent);
            CheckBox cb_hideTitle = findViewById(R.id.cb_hide_title);
            CheckBox cb_hideStatus = findViewById(R.id.cb_hide_status);

            mVideoPlayer.putExtra("VideoUrl", et.getText().toString());
            mVideoPlayer.putExtra("HideStatusBar", (cb_hideStatus.isChecked()?"true":"false"));
            mVideoPlayer.putExtra("HideAppTitleBar", (cb_hideTitle.isChecked()?"true":"false"));

            // save to preference
            String str=et.getText().toString();
            boolean found = false;
            for (int i = 0; i< mUrlList.size(); i++) {
                if (str.compareTo(mUrlList.get(i)) == 0) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                mUrlList.add(str);
                updateAdapterUrlEntries();
                mAdapter.add(str);
            }

            savePreferences(this);

            startActivity(mVideoPlayer);
        }
        else if (view.getId() == R.id.actv_recent) {
            mActv.showDropDown();
        }
        else if (view.getId() == R.id.bt_edit_history) {
            final Dialog diagEditUrl = new Dialog(this);
            diagEditUrl.setTitle("Edit Recent");
            diagEditUrl.setContentView(R.layout.dialog_listview_layout);
            ListView lv = diagEditUrl.findViewById(R.id.lv_url_list);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.dialog_itme_layout, url_entries);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, "onItemClick("+i+")");
                    final int item = i;
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(MainActivity.this);
                    }
                    builder.setTitle("Delete URL ...")
                            .setMessage("Are you sure?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mAdapter.remove(url_entries[item]);
                                    mUrlList.remove(item);
                                    updateAdapterUrlEntries();
                                    savePreferences(MainActivity.this);
                                    diagEditUrl.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
            Button bt = diagEditUrl.findViewById(R.id.bt_dialog_ok);
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    diagEditUrl.dismiss();
                }
            });
            diagEditUrl.show();
        }
        else if (view.getId() == R.id.bt_clear_history) {
            // Display "Are you sure? Dialog
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Delete URLs History")
                    .setMessage("Are you sure?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mAdapter.clear();
                            mUrlList.clear();
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.clear().apply();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }


    protected void updateAdapterUrlEntries()
    {
        Log.d(TAG, "updateAdapterUrlEntries(size="+mUrlList.size()+")");
        if (mUrlList == null || mUrlList.size() == 0) {
            url_entries = new String[1];
            url_entries[0] = "";
        }
        else {
            url_entries = new String[mUrlList.size()];
            for (int i = 0; i < mUrlList.size(); i++) {
                url_entries[i] = mUrlList.get(i);
            }
        }
    }

    protected void loadPreferences(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String str;
        mAppTheme = preferences.getString(APP_THEME, "theme_dark");

        str = preferences.getString(URL_LIST, null);
        if (str != null) {
            int b=0, e=0;
            while ((e=str.indexOf('\n', b)) != -1) {
                mUrlList.add(str.substring(b, e));
                b = e + 1;
            }
            mUrlList.add(str.substring(b));  // add the last one
        }
        Log.d(TAG, "loadPreferences():");
        for (int i=0; i<mUrlList.size(); i++)
            Log.d(TAG, mUrlList.get(i));
    }

    protected void savePreferences(Context context)
    {
        Log.d(TAG, "savePreferences():");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(APP_THEME, mAppTheme);
        String str = "";
        for (int i = 0; i< mUrlList.size(); i++) {
            str += mUrlList.get(i);
            if (i != mUrlList.size()-1)
                str += "\n";
        }
        editor.putString(URL_LIST, str);
        editor.apply();
    }
}
