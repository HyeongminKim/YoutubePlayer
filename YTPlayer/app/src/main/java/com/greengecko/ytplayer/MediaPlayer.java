package com.greengecko.ytplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;


public class MediaPlayer extends AppCompatActivity {
    PlayerView playerView;
    SimpleExoPlayer player;

    Uri videoSrc;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        playerView.setPlayer(player);
        DataSource.Factory factory = new DefaultDataSourceFactory(this, "YTExoPlayer");
        ProgressiveMediaSource source = new ProgressiveMediaSource.Factory(factory).createMediaSource(videoSrc);
        player.prepare(source);

        player.setPlayWhenReady(true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        playerView.setPlayer(null);
        player.release();
        player = null;
    }

    private void init() {
        playerView = findViewById(R.id.player);

        intent = getIntent();
        videoSrc = Uri.parse(intent.getExtras().getString("src"));
    }
}