package com.greengecko.ytplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;


public class MediaPlayer extends AppCompatActivity {
    private PlayerView playerView;
    private SimpleExoPlayer player;

    private Uri videoSrc;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        init();
        startService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.setPlayWhenReady(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerView.setPlayer(null);
        player.release();
        player = null;
    }

    private void init() {
        setContentView(R.layout.activity_media_player);

        playerView = findViewById(R.id.player);

        intent = getIntent();
        videoSrc = Uri.parse(intent.getExtras().getString("src"));
    }

    private void startService() {
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        playerView.setPlayer(player);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        DataSource.Factory factory = new DefaultDataSourceFactory(this, "YTExoPlayer");
        ProgressiveMediaSource source = new ProgressiveMediaSource.Factory(factory).createMediaSource(videoSrc);
        player.prepare(source);

        player.setPlayWhenReady(true);
    }
}