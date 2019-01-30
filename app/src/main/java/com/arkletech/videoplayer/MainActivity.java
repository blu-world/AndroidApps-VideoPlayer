package com.arkletech.videoplayer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String video_url = "http://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4";
    private static ProgressDialog progressDialog;
    VideoView vv;
    Intent mVideoPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText et = findViewById(R.id.et_url);
        et.setText(video_url);
        CheckBox cb_hideTitle = findViewById(R.id.cb_hide_title);
        cb_hideTitle.setChecked(true);
        CheckBox cb_hideStatus = findViewById(R.id.cb_hide_status);
        cb_hideStatus.setChecked(true);
        Button bPlay = findViewById(R.id.b_play);

        bPlay.setOnClickListener((View.OnClickListener) this);

        mVideoPlayer = new Intent(getApplicationContext(), PlayVideoActivity.class);
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
        if (view.getId() == R.id.b_play) {
            EditText et = findViewById(R.id.et_url);
            CheckBox cb_hideTitle = findViewById(R.id.cb_hide_title);
            CheckBox cb_hideStatus = findViewById(R.id.cb_hide_status);

            mVideoPlayer.putExtra("VideoUrl", et.getText().toString());
            mVideoPlayer.putExtra("HideStatusBar", (cb_hideStatus.isChecked()?"true":"false"));
            mVideoPlayer.putExtra("HideAppTitleBar", (cb_hideTitle.isChecked()?"true":"false"));
            startActivity(mVideoPlayer);
        }
    }
}
