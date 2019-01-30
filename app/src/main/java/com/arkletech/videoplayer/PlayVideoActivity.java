package com.arkletech.videoplayer;
//
// https://developer.android.com/guide/topics/media/media-formats
//
//	"Intent mVideoPlayer = new Intent(MainActivity.this, PlayVideoActivity.class);
//
//	Need to set the Bundle to pass the info:
//	mVideoPlayer.putExtra("VideoUrl", et.getText().toString());
//	mVideoPlayer.putExtra("HideStatusBar", (cb_hideStatus.isChecked()?"true":"false"));
//	mVideoPlayer.putExtra("HideAppTitleBar", (cb_hideTitle.isChecked()?"true":"false"));
//
//	 startActivity(mVideoPlayer);"
//

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayVideoActivity extends AppCompatActivity
{

    private static ProgressDialog progressDialog;
    VideoView videoView;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		String url = extras.getString("VideoUrl");
		String hideStatusBar = extras.getString("HideStatusBar");
		String hideAppTitleBar = extras.getString("HideAppTitleBar");

		// Set to Landscape for playing video
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// hide the Status Bar
        if (hideStatusBar.equals("true")) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		// Hide Status Bar has to be before hid App Title
        if (hideAppTitleBar.equals("true")) {
            getSupportActionBar().hide();   // Hide the App's title bar
        }
		// the above hide settings have to be before the setContentView() call
		setContentView(R.layout.video_player);

		videoView = (VideoView) findViewById(R.id.vv_video);

		progressDialog = ProgressDialog.show(PlayVideoActivity.this, "", "Buffering video...", true);
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);  
		progressDialog.show();
		PlayVideo(url);
	}

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
    }

	private void PlayVideo(String video_url)
	{
		try {
			getWindow().setFormat(PixelFormat.TRANSLUCENT);
			MediaController mediaController = new MediaController(PlayVideoActivity.this);
			mediaController.setAnchorView(videoView);           

			Uri video = Uri.parse(video_url);             
			videoView.setMediaController(mediaController);
			videoView.setVideoURI(video);
			videoView.requestFocus();              
			videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

				public void onPrepared(MediaPlayer mp) {                  
					progressDialog.dismiss();     
					videoView.start();
				}
			});           

		}
		catch(Exception e) {
			progressDialog.dismiss();
			System.out.println("Video Play Error :"+e.toString());
			finish();
		}   
	}
}