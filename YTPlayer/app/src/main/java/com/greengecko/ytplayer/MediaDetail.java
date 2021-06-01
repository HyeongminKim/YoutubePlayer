package com.greengecko.ytplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MediaDetail extends AppCompatActivity {
    private Button delete, origin, play;

    private TextView title;
    private String mediaDownloadPath, mediaMetadataPath, mediaThumbnailPath, mediaURL, mediaID;
    private ArrayList<String> libraryItems;
    private int mediaIndex;
    private boolean mediaDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_detail);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        init();
        setAction();
    }

    private void init() {
        TextView extension = findViewById(R.id.extension);
        TextView author = findViewById(R.id.author);
        TextView detail = findViewById(R.id.detail);
        RatingBar rating = findViewById(R.id.rating);

        title = findViewById(R.id.title);

        delete = findViewById(R.id.delete);
        origin = findViewById(R.id.show_original);
        play = findViewById(R.id.play);

        Intent intent = getIntent();
        title.setText(intent.getExtras().getString("title"));
        author.setText(intent.getExtras().getString("author"));
        rating.setMax(5);
        rating.setRating((float)intent.getExtras().getDouble("rating"));

        mediaID = intent.getExtras().getString("id");
        mediaURL = intent.getExtras().getString("url");
        mediaDownloadPath = intent.getExtras().getString("download");
        mediaMetadataPath = intent.getExtras().getString("metadata");
        mediaThumbnailPath = intent.getExtras().getString("thumbnail");
        libraryItems = intent.getExtras().getStringArrayList("library");
        mediaIndex = intent.getExtras().getInt("index");

        detail.setText(metadataReader());
        detail.setMovementMethod(new ScrollingMovementMethod());

        String fileExtension = intent.getExtras().getString("extension");
        extension.setText(fileExtension);
        if(fileExtension.toLowerCase().equals("mp3") || fileExtension.toLowerCase().equals("flac") || fileExtension.toLowerCase().equals("m4a") ||
                fileExtension.toLowerCase().equals("wav")) {
            extension.setTextColor(getColor(R.color.purple_500));
        } else {
            extension.setTextColor(Color.MAGENTA);
        }
    }

    private void setAction() {
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder deleteMedia = new AlertDialog.Builder(MediaDetail.this);
                deleteMedia.setTitle(getText(R.string.removeMedia));
                deleteMedia.setMessage(getText(R.string.removeMediaMsg));
                deleteMedia.setNegativeButton(getText(R.string.dialogCancel), null);
                deleteMedia.setPositiveButton(getText(R.string.removeAction), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mediaDelete();
                    }
                });
                deleteMedia.show();
            }
        });
        origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mediaURL));
                startActivity(browserIntent);
            }
        });
        origin.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData content = ClipData.newPlainText("label", mediaURL);
                clipboard.setPrimaryClip(content);
                Toast.makeText(getApplicationContext(), getText(R.string.copyCompleted), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MediaPlayer.class);
                intent.putExtra("src", mediaDownloadPath + "/" + libraryItems.get(mediaIndex));
                startActivity(intent);
            }
        });
    }

    private String metadataReader() {
        String descriptionPath = mediaMetadataPath + "/" + title.getText() + ".description";
        StringBuffer buffer = new StringBuffer();
        try {
            InputStream input = new FileInputStream(descriptionPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            reader.close();
            input.close();
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getText(R.string.failToParseMediaInfo), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void mediaDelete() {
        String mediaLocation = mediaDownloadPath + "/" + libraryItems.get(mediaIndex);
        String metaDataDescription = mediaMetadataPath + "/" + title.getText() + ".description";
        try {
            File mediaPath = new File(mediaLocation);
            File descriptionPath = new File(metaDataDescription);
            File thumbnailPath = new File(mediaThumbnailPath);
            File[] thumbnailList = thumbnailPath.listFiles();

            if (mediaPath.exists() && descriptionPath.exists()) {
                MediaJSONController info = new MediaJSONController(mediaMetadataPath);
                if(info.getCount(info.getMediaID(libraryItems.get(mediaIndex).substring(0, libraryItems.get(mediaIndex).lastIndexOf('.')))) == 0) {
                    for (File thumbnail : thumbnailList) {
                        if (thumbnail.getName().substring(0, thumbnail.getName().lastIndexOf('.')).equals(libraryItems.get(mediaIndex).substring(0, libraryItems.get(mediaIndex).lastIndexOf('.')))) {
                            descriptionPath.delete();
                            thumbnail.delete();
                            break;
                        }
                        throw new FileNotFoundException();
                    }
                }
                mediaPath.delete();
                info.deleteMediaMetadata(mediaID);
                mediaDeleted = true;
                returnToLibrary();
            } else {
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), getText(R.string.removeActionFail), Toast.LENGTH_SHORT).show();
        }
    }

    private void returnToLibrary() {
        Intent result = new Intent();
        result.putExtra("deleted", mediaDeleted);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}