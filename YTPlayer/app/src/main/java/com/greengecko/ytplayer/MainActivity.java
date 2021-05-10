package com.greengecko.ytplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.yausername.youtubedl_android.mapper.VideoInfo;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends TabActivity {
    private TabHost host;
    private GridView library;
    private ContentsDropdownAdapter adapter;
    private Button visitDevSite, visitFFmpeg, visitYoutubeDl, visitDependence;
    private TextView detail, downloadInfo;
    private EditText exploreInput;
    private ProgressBar downloadProgress;
    private CompositeDisposable compositeDisposable;
    private InputMethodManager inputMethod;

    private ArrayList<ContentsDropdown> libraryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setAction();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void init() {
        host = getTabHost();
        exploreInput = findViewById(R.id.exploreInput);
        inputMethod = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        detail = findViewById(R.id.detail);
        downloadInfo = findViewById(R.id.downloadInfo);
        downloadProgress = findViewById(R.id.downloadProgress);
        library = findViewById(R.id.library);
        visitDevSite = findViewById(R.id.visitDevSite);
        visitFFmpeg = findViewById(R.id.visitFFmpeg);
        visitYoutubeDl = findViewById(R.id.visitYTdl);
        visitDependence = findViewById(R.id.visitDependence);

        libraryItems = new ArrayList<>();
        compositeDisposable = new CompositeDisposable();

        tabAdder(host, "HOME", "홈", R.id.tabHome);
        tabAdder(host, "EXPLORE", "탐색", R.id.tabExplore);
        tabAdder(host, "LIBRARY", "라이브러리", R.id.tabLibrary);
        tabAdder(host, "SETTING", "설정", R.id.tabSetting);

        if(dependenceInitialize(getApplicationContext())) {
            Toast.makeText(this, "라이브러리 초기화 성공", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder fatalError = new AlertDialog.Builder(MainActivity.this);
            fatalError.setTitle("라이브러리를 초기화 예외");
            fatalError.setMessage("의존성 패키지를 초기화할 수 없어 앱을 종료합니다. ");
            fatalError.setNeutralButton("이슈 제보", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openWeb("https://github.com/HyeongminKim/YoutubePlayer/issues");
                    finish();
                }
            });
            fatalError.setPositiveButton("승인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            fatalError.show();
        }

        new Thread() {
            public void run() {
                dependenceUpdate(getApplicationContext());
            }
        }.start();
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

        exploreInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    inputMethod.hideSoftInputFromWindow(exploreInput.getWindowToken(), 0);
                }

                if(exploreInput.getText().toString().isEmpty()) {
                    detail.setVisibility(View.GONE);
                    downloadInfo.setVisibility(View.GONE);
                    downloadProgress.setVisibility(View.GONE);
                    return false;
                }

                if(isStoragePermissionGranted()) {
                    mediaDownloader(exploreInput.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), "다운로드 경로에 접근 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                }

                return true;
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

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean dependenceInitialize(Context context) {
        try {
            YoutubeDL.getInstance().init(context);
            FFmpeg.getInstance().init(context);
            return true;
        } catch (YoutubeDLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void dependenceUpdate(Context context) {
        try {
            YoutubeDL.getInstance().updateYoutubeDL(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VideoInfo getMediaInfo(String url) {
        try {
            return YoutubeDL.getInstance().getInfo(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mediaDownloader(String url) {
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YT Player");
        YoutubeDLRequest request = new YoutubeDLRequest(url.trim());
        request.addOption("-o", path.getAbsolutePath() + "/%(title)s.%(ext)s");

        detail.setVisibility(View.VISIBLE);
        downloadInfo.setVisibility(View.VISIBLE);
        downloadProgress.setVisibility(View.VISIBLE);

        try {
            detail.setText(String.format("제목: %s\n업로더: %s", getMediaInfo(url).getTitle(), getMediaInfo(url).getUploader()));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "미디어 정보 사용 불가", Toast.LENGTH_SHORT).show();
            detail.setVisibility(View.GONE);
        }

        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(youtubeDLResponse -> {
                Toast.makeText(getApplicationContext(), "다운로드 성공", Toast.LENGTH_SHORT).show();
                downloadInfo.setVisibility(View.GONE);
                downloadProgress.setVisibility(View.GONE);
            }, e -> {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "다운로드 실패", Toast.LENGTH_SHORT).show();
            detail.setVisibility(View.GONE);
            downloadInfo.setVisibility(View.GONE);
            downloadProgress.setVisibility(View.GONE);
        });
        compositeDisposable.add(disposable);
    }

    private DownloadProgressCallback callback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds) {
            int min = (int)etaInSeconds / 60;
            int hour = min / 60;
            int second = (int)etaInSeconds % 60;
            int finalMin = min % 60;

            runOnUiThread(() -> {
                downloadProgress.setProgress((int) progress);
                if(hour == 0 && finalMin == 0) {
                    downloadInfo.setText(String.format("%d초 남음", second));
                } else if(hour == 0) {
                    downloadInfo.setText(String.format("%d분 %d초 남음", finalMin, second));
                } else {
                    downloadInfo.setText(String.format("%d시간 %d분 %d초 남음", hour, finalMin, second));
                }
            });
        }
    };
}