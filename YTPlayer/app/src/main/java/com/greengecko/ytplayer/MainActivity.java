package com.greengecko.ytplayer;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
    private Button                visitDevSite, visitFFmpeg, visitYoutubeDl, visitDependence, goExplore;
    private TextView              detail, downloadInfo, mediaConvertGuide, emptyLibrary;
    private EditText              exploreInput;
    private ProgressBar           downloadProgress;
    private CheckBox              mediaConvertEnable;
    private Spinner               mediaConvertExtension;
    private String                mediaConvert;
    private CompositeDisposable   compositeDisposable;
    private InputMethodManager    inputMethod;

    private LibraryListAdapter    libraryAdapter;
    private String[]              convertibleItems;
    private boolean               initialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        init();
        setAction();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && requestCode == 100 && resultCode == RESULT_OK) {
            rowAdder();
        }
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
        emptyLibrary = findViewById(R.id.emptyLibrary);
        goExplore = findViewById(R.id.goExplore);
        visitDevSite = findViewById(R.id.visitDevSite);
        visitFFmpeg = findViewById(R.id.visitFFmpeg);
        visitYoutubeDl = findViewById(R.id.visitYTdl);
        visitDependence = findViewById(R.id.visitDependence);

        libraryAdapter = new LibraryListAdapter();
        convertibleItems = new String[] {getString(R.string.choose), "mp4", "m4a", "3gp", "flac", "mp3", "mkv", "wav", "ogg", "webm", "gif"};
        compositeDisposable = new CompositeDisposable();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getApplicationContext(), android.R.layout.simple_spinner_item, convertibleItems
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mediaConvertExtension.setAdapter(adapter);
        mediaConvertExtension.setSelection(0);
        mediaConvert = convertibleItems[0];

        if(dependenceInitialize(getApplicationContext())) {
            Toast.makeText(this, getText(R.string.packageInitSuccess), Toast.LENGTH_SHORT).show();
            dependenceUpdate();
            initialized = true;
            tabAdder(host, "LIBRARY", getString(R.string.library), R.id.tabLibrary);
            tabAdder(host, "EXPLORE", getString(R.string.explore), R.id.tabExplore);
            tabAdder(host, "INFO", getString(R.string.info), R.id.tabInfo);
        } else {
            AlertDialog.Builder fatalError = new AlertDialog.Builder(MainActivity.this);
            fatalError.setTitle(getText(R.string.packageInitFail));
            fatalError.setMessage(getText(R.string.packageInitFailMsg));
            fatalError.setNeutralButton(getText(R.string.packageInitFailReport), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openWeb("https://github.com/HyeongminKim/YoutubePlayer/issues");
                    View view = findViewById(R.id.tabExplore);
                    view.setVisibility(View.GONE);
                    tabAdder(host, "LIBRARY", getString(R.string.library), R.id.tabLibrary);
                    tabAdder(host, "INFO", getString(R.string.info), R.id.tabInfo);
                }
            });
            fatalError.setPositiveButton(getText(R.string.dialogOK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    View view = findViewById(R.id.tabExplore);
                    view.setVisibility(View.GONE);
                    tabAdder(host, "LIBRARY", getString(R.string.library), R.id.tabLibrary);
                    tabAdder(host, "INFO", getString(R.string.info), R.id.tabInfo);
                }
            });
            fatalError.show();
        }
    }

    private void setAction() {
        host.setCurrentTab(0);
        rowAdder();

        host.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                String tabTag = getTabHost().getCurrentTabTag();

                if(tabTag.equals("INFO")) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                } else {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }

                if(tabTag.equals("LIBRARY")) {
                    rowAdder();
                }
            }
        });

        goExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(initialized) {
                    host.setCurrentTab(1);
                } else {
                    AlertDialog.Builder fatalError = new AlertDialog.Builder(MainActivity.this);
                    fatalError.setTitle(getText(R.string.packageInitFail));
                    fatalError.setMessage(getText(R.string.packageInitFailMsg));
                    fatalError.setPositiveButton(getText(R.string.dialogOK), null);
                    fatalError.show();
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

                mediaDownloader(exploreInput.getText().toString());
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
                if(initialized) {
                    MediaJSONController info = new MediaJSONController(getMediaMetadataPath().getAbsolutePath());
                    try {
                        String targetName = libraryAdapter.getTitle(position);
                        if (!getMediaInfo(info.getURL(info.getMediaID(targetName), "URL")).getTitle().replaceAll("[|\\\\?*<\":>/]", "_").equals(info.getString(info.getMediaID(targetName), "TITLE"))) {
                            throw new NullPointerException();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), getText(R.string.playActionFail), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getText(R.string.failToParseMediaInfo), Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(getApplicationContext(), MediaPlayer.class);
                intent.putExtra("src", getMediaDownloadPath().getPath() + "/" + libraryAdapter.getFileName(position));
                startActivity(intent);
            }
        });
        library.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(initialized) {
                    MediaJSONController info = new MediaJSONController(getMediaMetadataPath().getAbsolutePath());
                    try {
                        String targetName = libraryAdapter.getTitle(position);
                        if (!getMediaInfo(info.getURL(info.getMediaID(targetName), "URL")).getTitle().replaceAll("[|\\\\?*<\":>/]", "_").equals(info.getString(info.getMediaID(targetName), "TITLE"))) {
                            throw new NullPointerException();
                        }
                        ArrayList<String> library = new ArrayList<>();
                        for (int i = 0; i < libraryAdapter.getCount(); i++) {
                            library.add(libraryAdapter.getFileName(i));
                        }
                        Intent intent = new Intent(getApplicationContext(), MediaDetail.class);
                        intent.putExtra("title", targetName);
                        intent.putExtra("author", libraryAdapter.getUploader(position));
                        intent.putExtra("rating", info.getRating(info.getMediaID(targetName)));
                        intent.putExtra("url", info.getURL(info.getMediaID(targetName), "URL"));
                        intent.putExtra("id", info.getMediaID(targetName));
                        intent.putExtra("extension", libraryAdapter.getExtension(position));
                        intent.putExtra("download", getMediaDownloadPath().getAbsolutePath());
                        intent.putExtra("metadata", getMediaMetadataPath().getAbsolutePath());
                        intent.putExtra("thumbnail", getMediaThumbnailPath().getAbsolutePath());
                        intent.putExtra("library", library);
                        intent.putExtra("index", position);
                        startActivityForResult(intent, 100);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), getText(R.string.failToParseMediaInfo), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getText(R.string.failToParseMediaInfo), Toast.LENGTH_SHORT).show();
                }
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
        if(files == null || files.length == 0) {
            library.setVisibility(View.GONE);
            emptyLibrary.setVisibility(View.VISIBLE);
            goExplore.setVisibility(View.VISIBLE);
            return;
        } else {
            library.setVisibility(View.VISIBLE);
            emptyLibrary.setVisibility(View.GONE);
            goExplore.setVisibility(View.GONE);
        }

        libraryAdapter.clearItem();
        MediaJSONController info = new MediaJSONController(getMediaMetadataPath().getAbsolutePath());
        for (File file : files) {
            File[] images = getMediaThumbnailPath().listFiles();
            for(File image : images) {
                String parsedImageName = image.getName().substring(0, image.getName().lastIndexOf('.'));
                String parsedFileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                if(parsedImageName.equals(parsedFileName)) {
                    libraryAdapter.addItem(BitmapFactory.decodeFile(image.getAbsolutePath()), file.getName(), info.getString(info.getMediaID(parsedFileName), "UPLOADER"), file.getName().substring(file.getName().lastIndexOf('.') + 1));
                    break;
                }
            }
        }
        library.setAdapter(libraryAdapter);
    }

    private void mediaDeleteRecent(String fileName) {
        String  mediaLocation = getMediaDownloadPath().getPath() + "/";
        String  metaDataLocation = getMediaMetadataPath().getPath() + "/";
        String  infoTarget;
        long    lastMedia = Integer.MIN_VALUE,
                lastDescription = Integer.MIN_VALUE;
        File[]  listedMedia = new File(mediaLocation).listFiles(),
                listedDescription = new File(metaDataLocation).listFiles();

        File    target = null;

        if(listedMedia != null && listedMedia.length > 0) {
            for(File file : listedMedia) {
                if(lastMedia < file.lastModified() && file.getName().substring(0, fileName.lastIndexOf('.')).equals(fileName)) {
                    lastMedia = file.lastModified();
                    target = file;
                }
            }
            if(target != null) {
                target.delete();
                target = null;
            }
        }

        if(listedDescription != null && listedDescription.length > 0) {
            for(File file : listedDescription) {
                if(file.getName().toLowerCase().endsWith("." + "description")) {
                    if(lastDescription < file.lastModified() && file.getName().equals(fileName + ".description")) {
                        lastDescription = file.lastModified();
                        target = file;
                    }
                }
            }
            if(target != null) {
                MediaJSONController info = new MediaJSONController(getMediaMetadataPath().getAbsolutePath());
                infoTarget = info.getMediaID(target.getName().substring(0, fileName.lastIndexOf('.')));
                try {
                    info.deleteMediaMetadata(infoTarget);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                target.delete();
            }
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
        File path = new File(getApplicationContext().getFilesDir(), "media");
        if (!path.exists()) {
            path.mkdir();
        }
        return path;
    }

    private File getMediaMetadataPath() {
        File path = new File(getApplicationContext().getFilesDir(), "data");
        if (!path.exists()) {
            path.mkdir();
        }
        return path;
    }

    private File getMediaThumbnailPath() {
        File path = new File(getApplicationContext().getFilesDir(), "thumbnail");
        if (!path.exists()) {
            path.mkdir();
        }
        return path;
    }

    private void getMediaThumbnail(@NonNull String url) {
        File path = getMediaThumbnailPath();
        YoutubeDLRequest request = new YoutubeDLRequest(url.trim());
        request.addOption("-o", path.getAbsolutePath() + "/%(title)s.%(ext)s");
        request.addOption("--write-thumbnail");
        request.addOption("--skip-download");

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
                    exploreInput.setText(null);
                    mediaDeleteRecent(getMediaInfo(url).getTitle());
                });
        compositeDisposable.add(disposable);
    }

    private void getMediaMetadata(@NonNull String url) {
        File path = getMediaMetadataPath();
        YoutubeDLRequest request = new YoutubeDLRequest(url.trim());
        request.addOption("-o", path.getAbsolutePath() + "/%(title)s.%(ext)s");
        request.addOption("--write-description");
        request.addOption("--skip-download");

        try {
            MediaJSONController info = new MediaJSONController(getMediaMetadataPath().getAbsolutePath());
            info.createMetadata(getMediaInfo(url).getId(), getMediaInfo(url).getTitle(), getMediaInfo(url).getUploader(), url.trim(), getMediaInfo(url).getThumbnail(), Double.parseDouble(getMediaInfo(url).getAverageRating()));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getText(R.string.downloadFail), Toast.LENGTH_SHORT).show();
            downloadInfo.setVisibility(View.GONE);
            downloadProgress.setVisibility(View.GONE);
            mediaDeleteRecent(getMediaInfo(url).getTitle());
            return;
        }

        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(youtubeDLResponse -> getMediaThumbnail(url), e -> {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getText(R.string.downloadFail), Toast.LENGTH_SHORT).show();
                downloadInfo.setVisibility(View.GONE);
                downloadProgress.setVisibility(View.GONE);
                exploreInput.setText(null);
                mediaDeleteRecent(getMediaInfo(url).getTitle());
            });
        compositeDisposable.add(disposable);
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
            .subscribe(youtubeDLResponse -> getMediaMetadata(url), e -> {
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