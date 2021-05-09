package com.greengecko.ytplayer;

import android.app.TabActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TabHost;

import java.util.ArrayList;


public class MainActivity extends TabActivity {
    private TabHost host;
    private GridView library;
    private ContentsDropdownAdapter adapter;
    private Button visitDevSite, visitFFmpeg, visitYoutubeDl, visitDependence;

    private ArrayList<ContentsDropdown> libraryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setAction();
    }

    private void init() {
        host = getTabHost();
        library = findViewById(R.id.library);
        visitDevSite = findViewById(R.id.visitDevSite);
        visitFFmpeg = findViewById(R.id.visitFFmpeg);
        visitYoutubeDl = findViewById(R.id.visitYTdl);
        visitDependence = findViewById(R.id.visitDependence);

        libraryItems = new ArrayList<>();

        tabAdder(host, "HOME", "홈", R.id.tabHome);
        tabAdder(host, "EXPLORE", "탐색", R.id.tabExplore);
        tabAdder(host, "LIBRARY", "라이브러리", R.id.tabLibrary);
        tabAdder(host, "SETTING", "설정", R.id.tabSetting);
    }

    private void setAction() {
        host.setCurrentTab(0);

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                String tabTag = getTabHost().getCurrentTabTag();

                if(tabTag.equals("HOME") || tabTag.equals("SETTING")) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                } else {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
            }
        });

        visitDevSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeb("https://github.com/HyeongminKim/YoutubePlayer");
            }
        });
        visitDevSite.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openWeb("https://github.com/HyeongminKim/YoutubePlayer/blob/master/LICENSE");
                return false;
            }
        });
        visitFFmpeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeb("https://github.com/FFmpeg/FFmpeg");
            }
        });
        visitFFmpeg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openWeb("https://github.com/FFmpeg/FFmpeg/blob/master/LICENSE.md");
                return false;
            }
        });
        visitYoutubeDl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeb("https://github.com/ytdl-org/youtube-dl");
            }
        });
        visitYoutubeDl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openWeb("https://github.com/ytdl-org/youtube-dl/blob/master/LICENSE");
                return false;
            }
        });
        visitDependence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWeb("https://github.com/yausername/youtubedl-android");
            }
        });
        visitDependence.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openWeb("https://github.com/yausername/youtubedl-android/blob/master/LICENSE");
                return false;
            }
        });
    }

    private void tabAdder(TabHost host, String tag, String indicator, int viewId) {
        TabHost.TabSpec tab = host.newTabSpec(tag).setIndicator(indicator);
        tab.setContent(viewId);
        host.addTab(tab);
    }

    private void openWeb(String uri) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(browserIntent);
    }

    private void rowAdder(String name, String author, int imageResID, int index) {
        if(libraryItems.size() > 0 && index == 0) {
            libraryItems.clear();
        }

        libraryItems.add(new ContentsDropdown(name, author, imageResID));
    }

    private void rowPacker() {
        adapter = new ContentsDropdownAdapter(libraryItems, MainActivity.this);
        library.setAdapter(adapter);
    }
}