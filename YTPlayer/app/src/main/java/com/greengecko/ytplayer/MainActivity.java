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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private TabHost               host;
    private ListView              library;
    private Button                visitDevSite, visitFFmpeg, visitYoutubeDl, visitDependence;
    private TextView              detail, downloadInfo, mediaConvertGuide;
    private EditText              exploreInput;
    private ProgressBar           downloadProgress;
    private CheckBox              mediaConvertEnable;
    private Spinner               mediaConvertExtension;
    private String                mediaConvert;
    private CompositeDisposable   compositeDisposable;
    private InputMethodManager    inputMethod;

    private ArrayList<String>     libraryItems;
    private String[]              convertibleItems;

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
        mediaConvertEnable = findViewById(R.id.convertEnable);
        mediaConvertExtension = findViewById(R.id.convertExtension);
        mediaConvertGuide = findViewById(R.id.convertToText);
        library = findViewById(R.id.library);
        visitDevSite = findViewById(R.id.visitDevSite);
        visitFFmpeg = findViewById(R.id.visitFFmpeg);
        visitYoutubeDl = findViewById(R.id.visitYTdl);
        visitDependence = findViewById(R.id.visitDependence);

        libraryItems = new ArrayList<>();
        convertibleItems = new String[] {getString(R.string.choose), "mp4", "m4a", "3gp", "flac", "mp3", "mkv", "wav", "ogg", "webm", "gif"};
        compositeDisposable = new CompositeDisposable();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getApplicationContext(), android.R.layout.simple_spinner_item, convertibleItems
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mediaConvertExtension.setAdapter(adapter);

        tabAdder(host, "LIBRARY", getString(R.string.library), R.id.tabLibrary);
        tabAdder(host, "EXPLORE", getString(R.string.explore), R.id.tabExplore);
        tabAdder(host, "INFO", getString(R.string.info), R.id.tabInfo);

        mediaConvertExtension.setSelection(0);
        mediaConvert = convertibleItems[0];

        if(dependenceInitialize(getApplicationContext())) {
            Toast.makeText(this, getText(R.string.packageInitSuccess), Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder fatalError = new AlertDialog.Builder(MainActivity.this);
            fatalError.setTitle(getText(R.string.packageInitFail));
            fatalError.setMessage(getText(R.string.packageInitFailMsg));
            fatalError.setNeutralButton(getText(R.string.packageInitFailReport), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openWeb("https://github.com/HyeongminKim/YoutubePlayer/issues");
                    finish();
                }
            });
            fatalError.setPositiveButton(getText(R.string.dialogOK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            fatalError.show();
        }

        dependenceUpdate();
    }

    private void setAction() {
        host.setCurrentTab(0);
        rowAdder();

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                String tabTag = getTabHost().getCurrentTabTag();

                if(tabTag.equals("HOME")) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                } else {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }

                if(tabTag.equals("LIBRARY")) {
                    rowAdder();
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
                    Toast.makeText(getApplicationContext(), getText(R.string.accessFailDownloads), Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        mediaConvertEnable.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaConvertExtension.setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
                mediaConvertGuide.setVisibility(((CheckBox) view).isChecked() ? View.VISIBLE : View.GONE);
                mediaConvertEnable.setText(((CheckBox) view).isChecked() ? getText(R.string.convertTo) : getText(R.string.convertMedia));

                mediaConvertExtension.setSelection(0);
                mediaConvert = convertibleItems[0];
            }
        });

        mediaConvertExtension.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mediaConvert = convertibleItems[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mediaConvertExtension.setSelection(0);
                mediaConvert = convertibleItems[0];
            }
        });

        library.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getApplicationContext(), MediaPlayer.class);
                intent.putExtra("src", getMediaDownloadPath().getPath() + "/" + libraryItems.get(position));
                startActivity(intent);
            }
        });
        library.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                AlertDialog.Builder deleteMedia = new AlertDialog.Builder(MainActivity.this);
                deleteMedia.setTitle(getText(R.string.removeMedia));
                deleteMedia.setMessage(getText(R.string.removeMediaMsg));
                deleteMedia.setNegativeButton(getText(R.string.dialogCancel), null);
                deleteMedia.setPositiveButton(getText(R.string.removeAction), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String deleteLocation = getMediaDownloadPath().getPath() + "/" + libraryItems.get(position);
                        try {
                            File path = new File(deleteLocation);
                            if (path.exists()) {
                                path.delete();
                                rowAdder();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), getText(R.string.removeActionFail), Toast.LENGTH_SHORT).show();
                            rowAdder();
                        }
                    }
                });
                deleteMedia.show();

                return true;
            }
        });

        addButtonEventListener(visitDevSite, "https://github.com/HyeongminKim/YoutubePlayer", false);
        addButtonEventListener(visitDevSite, "https://github.com/HyeongminKim/YoutubePlayer/blob/master/LICENSE", true);

        addButtonEventListener(visitFFmpeg, "https://github.com/FFmpeg/FFmpeg", false);
        addButtonEventListener(visitFFmpeg, "https://github.com/FFmpeg/FFmpeg/blob/master/LICENSE.md", true);

        addButtonEventListener(visitYoutubeDl, "https://github.com/ytdl-org/youtube-dl", false);
        addButtonEventListener(visitYoutubeDl, "https://github.com/ytdl-org/youtube-dl/blob/master/LICENSE", true);

        addButtonEventListener(visitDependence, "https://github.com/yausername/youtubedl-android", false);
        addButtonEventListener(visitDependence, "https://github.com/yausername/youtubedl-android/blob/master/LICENSE", true);
    }

    private void tabAdder(@NonNull TabHost host, String tag, String indicator, int viewId) {
        TabHost.TabSpec tab = host.newTabSpec(tag).setIndicator(indicator);
        tab.setContent(viewId);
        host.addTab(tab);
    }

    private void openWeb(String uri) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(browserIntent);
    }

    private void addButtonEventListener(Button target, String uri, boolean longClick) {
        if(!longClick) {
            target.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWeb(uri);
                }
            });
        } else {
            target.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openWeb(uri);
                    return false;
                }
            });

        }
    }

    private void rowAdder() {
        File[] files = getMediaDownloadPath().listFiles();
        if(files == null) return;
        libraryItems.clear();
        for (File file : files) {
            libraryItems.add(file.getName());
        }

        ArrayAdapter<String> libraryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, libraryItems);
        library.setAdapter(libraryAdapter);
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

    private void dependenceUpdate() {
        Toast.makeText(getApplicationContext(), getText(R.string.packageUpdating), Toast.LENGTH_SHORT).show();
        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().updateYoutubeDL(getApplication()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    switch (status) {
                        case DONE:
                            Toast.makeText(getApplicationContext(), getText(R.string.packageUpdateSuccess), Toast.LENGTH_SHORT).show();
                            break;
                        case ALREADY_UP_TO_DATE:
                            Toast.makeText(getApplicationContext(), getText(R.string.packageUpToDate), Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_LONG).show();
                            break;
                    }
                }, e -> {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getText(R.string.packageUpdateFail), Toast.LENGTH_SHORT).show();
                });
        compositeDisposable.add(disposable);
    }

    @Nullable
    private VideoInfo getMediaInfo(String url) {
        try {
            return YoutubeDL.getInstance().getInfo(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private File getMediaDownloadPath() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YT Player");
    }

    private void mediaDownloader(@NonNull String url) {
        File path = getMediaDownloadPath();
        YoutubeDLRequest request = new YoutubeDLRequest(url.trim());
        request.addOption("-o", path.getAbsolutePath() + "/%(title)s.%(ext)s");
        if(mediaConvertEnable.isChecked() && !mediaConvert.equals(convertibleItems[0])) {
            if(mediaConvert.equals("mp3") || mediaConvert.equals("flac") || mediaConvert.equals("m4a") ||
                    mediaConvert.equals("wav")) {
                request.addOption("--embed-thumbnail");
                request.addOption("--extract-audio");
                request.addOption("--audio-format", mediaConvert);
                request.addOption("--audio-quality", "0");
            } else {
                request.addOption("-f", mediaConvert);
            }
        }

        try {
            detail.setText(String.format(getText(R.string.title) + ": %s\n" + getText(R.string.author) + ": %s", getMediaInfo(url).getTitle(), getMediaInfo(url).getUploader()));

            detail.setVisibility(View.VISIBLE);
            downloadInfo.setVisibility(View.VISIBLE);
            downloadProgress.setVisibility(View.VISIBLE);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getText(R.string.mediaURL), Toast.LENGTH_SHORT).show();

            exploreInput.setText(null);
            detail.setVisibility(View.GONE);
            downloadInfo.setVisibility(View.GONE);
            downloadProgress.setVisibility(View.GONE);

            return;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getText(R.string.failToParseMediaInfo), Toast.LENGTH_SHORT).show();

            detail.setVisibility(View.GONE);
            downloadInfo.setVisibility(View.GONE);
            downloadProgress.setVisibility(View.GONE);

            return;
        }

        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(youtubeDLResponse -> {
                Toast.makeText(getApplicationContext(), getText(R.string.downloadSuccess), Toast.LENGTH_SHORT).show();
                downloadInfo.setVisibility(View.GONE);
                downloadProgress.setVisibility(View.GONE);
                exploreInput.setText(null);
            }, e -> {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getText(R.string.downloadFail), Toast.LENGTH_SHORT).show();
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
                    downloadInfo.setText(String.format(getString(R.string.remainSec), second));
                } else if(hour == 0) {
                    downloadInfo.setText(String.format(getString(R.string.remainMin), finalMin, second));
                } else {
                    downloadInfo.setText(String.format(getString(R.string.remainHour), hour, finalMin, second));
                }
            });
        }
    };
}