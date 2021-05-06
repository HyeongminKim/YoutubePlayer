package com.greengecko.ytplayer;

import android.app.TabActivity;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TabHost;

import java.util.ArrayList;


public class MainActivity extends TabActivity {
    private TabHost host;
    private GridView explore;
    private ListView library;
    private MediaAdapter adapter;

    private ArrayList<Media> exploreItems, libraryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setAction();
    }

    private void init() {
        host = getTabHost();
        explore = findViewById(R.id.explore);
        library = findViewById(R.id.library);

        exploreItems = new ArrayList<>();
        libraryItems = new ArrayList<>();

        tabAdder(host, "HOME", "홈", R.id.tabHome);
        tabAdder(host, "EXPLORE", "탐색", R.id.tabExplore);
        tabAdder(host, "LIBRARY", "라이브러리", R.id.tabLibrary);
        tabAdder(host, "SETTING", "설정", R.id.tabSetting);
    }

    private void setAction() {
        host.setCurrentTab(0);
    }

    private void tabAdder(TabHost host, String tag, String indicator, int viewId) {
        TabHost.TabSpec tab = host.newTabSpec(tag).setIndicator(indicator);
        tab.setContent(viewId);
        host.addTab(tab);
    }

    public void rowAdder(String name, String author, int imageResID, int index) {
        if((exploreItems.size() > 0 || libraryItems.size() > 0) && index == 0) {
            exploreItems.clear();
            libraryItems.clear();
        }

        if(host.getCurrentTab() == 1) {
            exploreItems.add(new Media(name, author, imageResID));
        } else if(host.getCurrentTab() == 2) {
            libraryItems.add(new Media(name, author, imageResID));
        }
    }

    public void rowPacker() {
        if(host.getCurrentTab() == 1) {
            adapter = new MediaAdapter(exploreItems, getApplicationContext());
            explore.setAdapter(adapter);
        } else if(host.getCurrentTab() == 2) {
            adapter = new MediaAdapter(libraryItems, getApplicationContext());
            library.setAdapter(adapter);
        }
    }
}