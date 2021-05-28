package com.greengecko.ytplayer;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MediaJSONController {
    private String metadataDir;

    public MediaJSONController(@NonNull String metadataDir) {
        this.metadataDir = metadataDir;
    }

    public void createMetadata(String sourceID, String title, String uploader, String sourceUrl, ArrayList<String> tags, String thumbnailUrl, double averageRating) throws JSONException, IOException {
        JSONObject object = new JSONObject();
        JSONObject source = new JSONObject();

        source.put("TITLE", title);
        source.put("UPLOADER", uploader);
        source.put("URL", sourceUrl);
        source.put("PATH", tags);
        source.put("THUMBNAIL", thumbnailUrl);
        source.put("RATING", averageRating);

        object.put(sourceID, source);
        Log.println(Log.DEBUG, "JSON_INIT", object.toString());

        String metaDataInfo = metadataDir + "/" + title + ".info.json";
        File infoPath = new File(metaDataInfo);
        if (infoPath.exists()) {
            infoPath.delete();
        }
        FileWriter writer = new FileWriter(metaDataInfo);
        BufferedWriter buffer = new BufferedWriter(writer);
        buffer.write(object.toString());
        buffer.close();
    }

    public JSONObject getMetadata(String path) throws JSONException, IOException {
        FileInputStream jsonInput = new FileInputStream(path);
        InputStreamReader jsonReader = new InputStreamReader(jsonInput);
        BufferedReader buffer = new BufferedReader(jsonReader);
        return new JSONObject(buffer.readLine());
    }
}

