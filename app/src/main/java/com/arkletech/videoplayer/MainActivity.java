package com.arkletech.videoplayer;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    static final String TAG="App::VideoPlayer";
    static final String URL_LIST="url_list";
    static final String APP_THEME="appTheme";
    static final String PLAY_HIDE_STATUS="AppHideStatus";
    static final String PLAY_HIDE_TITLE="AppHideTitle";
    static final String DEFAULT_PLAYLIET_DIR ="AppPlayListLocation";
//    String video_url = "http://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4";
    private static ProgressDialog progressDialog;
    VideoView vv;
    Intent mVideoPlayer = null;
    String[] url_entries;
    Vector<String> mUrlList;
    Vector<String> mUrlName;
    AutoCompleteTextView mActv;
    ArrayAdapter<String> mAdapter;
    String mAppTheme;
    boolean bFabSubShown=false;
    ImageButton mFab;
    boolean mHideStatus=true;
    boolean mHideAppTitle=true;
    String mDefaultPlayListDir;

    private View fabMenuItem1;
    private View fabMenuItem2;
    private View fabMenuItem3;

    private float offset1;
    private float offset2;
    private float offset3;

    private TextView fabMenuText1;
    private TextView fabMenuText2;
    private TextView fabMenuText3;
    private int PLAY_LIST_JSON_FILE_WRITE = 1;
    private int PLAY_LIST_JSON_FILE_LOAD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mUrlList = new Vector<String>();
        mUrlName = new Vector<String>();

        loadPreferences(MainActivity.this);

        if (mAppTheme.compareTo("theme_dark") == 0)
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        else
            setTheme(R.style.AppTheme_Light_NoActionBar);

        super.onCreate(savedInstanceState);

//        importUrlList();
        updateUrlEntries();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ViewGroup fabContainer = (ViewGroup) findViewById(R.id.fab_container);

        fabMenuItem1 = findViewById(R.id.fab_rl1);
        fabMenuItem2 = findViewById(R.id.fab_rl2);
        fabMenuItem3 = findViewById(R.id.fab_rl3);

        fabMenuText1 = findViewById(R.id.tv_fab1);
        fabMenuText2 = findViewById(R.id.tv_fab2);
        fabMenuText3 = findViewById(R.id.tv_fab3);

        ImageView btn;
        btn = findViewById(R.id.fab_iv_1);
        btn.setOnClickListener(this);
        btn = findViewById(R.id.fab_iv_2);
        btn.setOnClickListener(this);
        btn = findViewById(R.id.fab_iv_3);
        btn.setOnClickListener(this);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab::onClick()");
                bFabSubShown = !bFabSubShown;
                if (bFabSubShown)
                    expandFabMenu();
                else
                    collapseFabMenu();
            }
        });

        mFab.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // We don't need to continue listening this during the animation
                fabContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                offset1 = mFab.getY() - fabMenuItem1.getY();
                fabMenuItem1.setTranslationY(offset1);
                offset2 = mFab.getY() - fabMenuItem2.getY();
                fabMenuItem2.setTranslationY(offset2);
                offset3 = mFab.getY() - fabMenuItem3.getY();
                fabMenuItem3.setTranslationY(offset3);

                return true;
            }
        });

        CheckBox cb_hideTitle = findViewById(R.id.cb_hide_title);
        cb_hideTitle.setChecked(mHideAppTitle);
        CheckBox cb_hideStatus = findViewById(R.id.cb_hide_status);
        cb_hideStatus.setChecked(mHideStatus);
        Button button = findViewById(R.id.b_play);
        button.setOnClickListener((View.OnClickListener) this);
//        button = findViewById(R.id.bt_edit_history);
//        button.setOnClickListener(this);
//        button = findViewById(R.id.bt_clear_history);
//        button.setOnClickListener(this);

        //Creating the instance of ArrayAdapter containing list
        // The ArrayAdapter, on being initialized by an array, converts the array into
        // a AbstractList (List) which cannot be modified.
        // Solution: Use an ArrayList<String> instead using an array while initializing the ArrayAdapter.
        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(url_entries));
        mAdapter = new ArrayAdapter<String> (this, R.layout.dropdown_itme_layout, lst) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                v.setBackgroundColor(Color.LTGRAY);
//                ((TextView)v).setText(url_entries[position]);
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

        Log.d(TAG, "onOptionsItemSelected("+id+")");

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_toggle:
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
                break;
            case R.id.action_import:
                importUrlList();
                break;
            case R.id.action_export:
                exportUrlList();
                break;
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
        Log.d(TAG, "onClick("+view.getId()+")");
        switch (view.getId()) {
            case R.id.b_play:
                EditText et = findViewById(R.id.actv_recent);
                CheckBox cb_hideTitle = findViewById(R.id.cb_hide_title);
                CheckBox cb_hideStatus = findViewById(R.id.cb_hide_status);

                mHideStatus = cb_hideStatus.isChecked();
                mHideAppTitle = cb_hideTitle.isChecked();
                mVideoPlayer.putExtra("VideoUrl", et.getText().toString());
                mVideoPlayer.putExtra("HideStatusBar", (mHideStatus?"true":"false"));
                mVideoPlayer.putExtra("HideAppTitleBar", (mHideAppTitle?"true":"false"));

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
                    mUrlName.add("");
                    updateUrlEntries();
                    updateAdapterEntries();
                }

                savePreferences(this);

                startActivity(mVideoPlayer);
                break;
            case R.id.actv_recent:
                if (mUrlList != null && mUrlList.size() > 0)
                    mActv.showDropDown();
                break;
//            case R.id.bt_edit_history:
            case R.id.fab_iv_3:
                if (bFabSubShown) {
                    collapseFabMenu();
                    bFabSubShown = false;
                }
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
                                        mUrlName.remove(item);
                                        updateUrlEntries();
                                        updateAdapterEntries();
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
                break;
//            case R.id.bt_clear_history:
            case R.id.fab_iv_2:
                if (bFabSubShown) {
                    collapseFabMenu();
                    bFabSubShown = false;
                }
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
                                mUrlName.clear();
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
                break;
            case R.id.fab_iv_1:
                Log.d(TAG, "OnClick(fab_iv_1)");
                if (bFabSubShown) {
                    collapseFabMenu();
                    bFabSubShown = false;
                }
                TextView tv = findViewById(R.id.actv_recent);
                tv.setText("");
                break;
        }
    }

    //===========================================
    private void exportUrlList() {
//        Log.d(TAG, "exportUrlList():"+getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        Log.d(TAG, "exportUrlList():"+getExternalFilesDir(null));
//        File file = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PlayList.json");
        File file = new File(getExternalFilesDir(null), "PlayList.json");
        if (file != null) {
            JSONObject json;
            JSONArray jsonArray = new JSONArray();
            try {
                // Create JSON Objects
                for (int i=0; i<mUrlList.size(); i++) {
                    json = new JSONObject();
                    if (i >= mUrlName.size())
                        json.put("name", "");
                    else
                        json.put("name", mUrlName.get(i));
                    json.put("url", mUrlList.get(i));
                    jsonArray.put(json);
                }
                JSONObject jsonList = new JSONObject();
                jsonList.put("list", jsonArray);

                String jsonStr = jsonList.toString();

                // Write to file
                FileOutputStream stream=null;
                try {
                    stream = new FileOutputStream(file);
                    stream.write(jsonStr.getBytes());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    stream.close();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void importUrlList() {
//        Log.d(TAG, "importUrlList():"+getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        Log.d(TAG, "importUrlList():"+getExternalFilesDir(null));
//        File file = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PlayList.json");
        File file = new File(getExternalFilesDir(null), "PlayList.json");
        String mResponse=null;
        if (file != null) {
            if (file.exists()) {
                FileInputStream is = null;
                try {
                    is = new FileInputStream(file);
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    mResponse = new String(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                JSONObject json=null;
                try {
                    json = new JSONObject(mResponse);
//                    Log.d(TAG, "Json Content: "+mResponse);
                    if (json != null && mResponse != null && mResponse.length() != 0) {
                        JSONArray jsonArray = json.getJSONArray("list");
                        mUrlList.clear();
                        mUrlName.clear();
                        for (int i=0; i<jsonArray.length(); i++) {
                            JSONObject jo_inside = jsonArray.getJSONObject(i);
                            String name = jo_inside.getString("name");
                            mUrlName.add(name);
                            String url = jo_inside.getString("url");
                            mUrlList.add(url);
                            Log.d(TAG, "name="+mUrlName.get(i)+", url="+mUrlList.get(i));
                        }
                        updateUrlEntries();
                        updateAdapterEntries();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void updateAdapterEntries()
    {
        Log.d(TAG, "updateAdapterEntries(size="+mUrlList.size()+")");
        mAdapter.clear();
        for (int i = 0; i < mUrlList.size(); i++) {
            mAdapter.add(url_entries[i]);
        }
        mAdapter.notifyDataSetChanged();
    }

    protected void updateUrlEntries()
    {
        Log.d(TAG, "updateUrlEntries(size="+mUrlList.size()+")");
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
        Log.d(TAG, "loadPreferences():");
        mAppTheme = preferences.getString(APP_THEME, "theme_dark");
        mHideStatus = preferences.getBoolean(PLAY_HIDE_STATUS, true);
        mHideAppTitle = preferences.getBoolean(PLAY_HIDE_TITLE, true);
        mDefaultPlayListDir = preferences.getString(DEFAULT_PLAYLIET_DIR, "");

        Log.d(TAG, "Theme="+mAppTheme+",HideStatus="+mHideStatus+",HideAppTitle="+mHideAppTitle);
/*
        str = preferences.getString(URL_LIST, null);
        if (str != null) {
            int b=0, e=0;
            while ((e=str.indexOf('\n', b)) != -1) {
                mUrlList.add(str.substring(b, e));
                b = e + 1;
            }
            mUrlList.add(str.substring(b));  // add the last one
        }
        for (int i=0; i<mUrlList.size(); i++)
            Log.d(TAG, mUrlList.get(i));
*/
    }

    protected void savePreferences(Context context)
    {
        Log.d(TAG, "savePreferences():");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(APP_THEME, mAppTheme);
        editor.putBoolean(PLAY_HIDE_STATUS, mHideStatus);
        editor.putBoolean(PLAY_HIDE_TITLE, mHideAppTitle);
        editor.putString(DEFAULT_PLAYLIET_DIR, mDefaultPlayListDir);

/*
        String str = "";
        for (int i = 0; i< mUrlList.size(); i++) {
            str += mUrlList.get(i);
            if (i != mUrlList.size()-1)
                str += "\n";
        }
        editor.putString(URL_LIST, str);
        editor.apply();
*/
    }


//===========================================
    private void collapseFabMenu() {
        mFab.setImageResource(R.drawable.animated_minus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createCollapseAnimator(fabMenuItem1, offset1),
                createCollapseAnimator(fabMenuItem2, offset2),
                createCollapseAnimator(fabMenuItem3, offset3));
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) { }
            @Override
            public void onAnimationEnd(Animator animator) {
                fabMenuText1.setVisibility(View.INVISIBLE);
                fabMenuText2.setVisibility(View.INVISIBLE);
                fabMenuText3.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationCancel(Animator animator) { }
            @Override
            public void onAnimationRepeat(Animator animator) { }
        });
        animatorSet.start();
        animateFab();
    }

    private void expandFabMenu() {
        mFab.setImageResource(R.drawable.animated_plus);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(createExpandAnimator(fabMenuItem1, offset1),
                createExpandAnimator(fabMenuItem2, offset2),
                createExpandAnimator(fabMenuItem3, offset3));
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) { }
            @Override
            public void onAnimationEnd(Animator animator) {
                // Make sure the text is visible at the end of animation
                if (fabMenuText1.getVisibility() != View.VISIBLE)
                    fabMenuText1.setVisibility(View.VISIBLE);
                if (fabMenuText2.getVisibility() != View.VISIBLE)
                    fabMenuText2.setVisibility(View.VISIBLE);
                if (fabMenuText3.getVisibility() != View.VISIBLE)
                    fabMenuText3.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationCancel(Animator animator) { }
            @Override
            public void onAnimationRepeat(Animator animator) { }
        });
        // set the text label visible at the beginning of the animation
        fabMenuText1.setVisibility(View.VISIBLE);
        fabMenuText2.setVisibility(View.VISIBLE);
        fabMenuText3.setVisibility(View.VISIBLE);
        animatorSet.start();
        animateFab();
    }

    private Animator createCollapseAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, "translationY", 0, offset)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    private Animator createExpandAnimator(View view, float offset) {
        return ObjectAnimator.ofFloat(view, "translationY", offset, 0)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    private void animateFab() {
        Drawable drawable = mFab.getDrawable();
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

//===========================================
// Check PERMISSIONs For App
//===========================================
    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
//            Manifest.permission.READ_PHONE_STATE,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.VIBRATE,
//            Manifest.permission.RECORD_AUDIO,
    };
    private static final int PERMISSION_REQUEST_CODE = 10;

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }
}
