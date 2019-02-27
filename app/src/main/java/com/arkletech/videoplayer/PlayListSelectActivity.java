package com.arkletech.videoplayer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

public class PlayListSelectActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {
    static final String TAG="PlayListSelect";
    ListView mPlayListView;
    PlayListItem mPlayListItem;
    ArrayList<PlayListItem> mPlaylist;
    private LoadPlaylist mLoadPlaylist;
    private ListViewAdapter mAdapter;
    private Intent mPlayVideoIntent;
    private String mHideStatus, mHideAppTitle;

    private int mScreenMode= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private SwipeRefreshLayout pullToRefresh;
    private String mAppTheme;
    private String mPlaylistAddress="http://www.arkletech.com/data/PlayList.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        mAppTheme = extras.getString("AppTheme");
        mHideStatus = extras.getString("HideStatusBar");
        mHideAppTitle = extras.getString("HideAppTitleBar");

        if (mAppTheme.compareTo("theme_dark") == 0)
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        else
            setTheme(R.style.AppTheme_Light_NoActionBar);

        setContentView(R.layout.activity_playlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.INVISIBLE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh()");
                mLoadPlaylist = new LoadPlaylist(PlayListSelectActivity.this);
                mLoadPlaylist.setOnPlaylistLoaredListener(new LoadPlaylist.OnPlaylistLoardedListener() {
                    @Override
                    public void onPlaylistLoaded() {
                        Log.d(TAG, "onPlaylistLoaded()");
                        mAdapter.notifyDataSetChanged();
                        // Stop showing refresh progress
                        pullToRefresh.setRefreshing(false);
                    }
                });
                mLoadPlaylist.execute(mPlaylistAddress);
            }
        });

//        mPlayVideoIntent = new Intent(getApplicationContext(), PlayVideoActivity.class);
        mPlayVideoIntent = new Intent(getApplicationContext(), ExoPlayerActivity.class);

        mPlaylist = LoadPlaylist.getPlaylist();
        mAdapter = new ListViewAdapter(this, mPlaylist);
        mAdapter.setAppTheme(mAppTheme);
        mPlayListView = findViewById(R.id.lv_playlist_selection);
        mPlayListView.setAdapter(mAdapter);
        mPlayListView.setOnItemClickListener(this);
    }
/*
    @Override
    public void onPlaylistLoaded() {
        Log.d(TAG, "onPlaylistLoaded()");
        mPlaylist = mLoadPlaylist.getPlaylist();
        mAdapter = new ListViewAdapter(this, mPlaylist);
        mPlayListView = findViewById(R.id.lv_playlist_selection);
        mPlayListView.setAdapter(mAdapter);
        mPlayListView.setOnItemSelectedListener(this);
        mAdapter.notifyDataSetChanged();
    }
*/

    @Override
    protected void onPostResume() {
        super.onPostResume();
        int o = getResources().getConfiguration().orientation;
        if (o != mScreenMode)
            setRequestedOrientation(mScreenMode);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.playlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected("+id+")");

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d(TAG, "onNavigationItemSelected("+id+")");

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemClick("+i+")");
        mScreenMode = getResources().getConfiguration().orientation;

        mPlayVideoIntent.putExtra("AppTheme", mAppTheme);
        mPlayVideoIntent.putExtra("VideoUrl", mPlaylist.get(i).getUrl());
        mPlayVideoIntent.putExtra("HideStatusBar", mHideStatus);
        mPlayVideoIntent.putExtra("HideAppTitleBar", mHideAppTitle);
        startActivity(mPlayVideoIntent);
    }
}
