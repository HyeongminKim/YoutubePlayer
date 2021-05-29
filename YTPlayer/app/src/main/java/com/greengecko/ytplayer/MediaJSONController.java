package com.greengecko.ytplayer;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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
        source.put("TAG", tags);
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

    public String getString(String path, String id, String name) {
        try {
            return getMetadata(path).getJSONObject(id).getString(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getURL(String path, String id, String name) {
        try {
            return getMetadata(path).getJSONObject(id).getString(name).replace("\\", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getRating(String path, String id) {
        try {
            return getMetadata(path).getJSONObject(id).getDouble("RATING");
        } catch (Exception e) {
            e.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    public ArrayList<String> getTags(String path, String id) {
        try {
            ArrayList<String> result = new ArrayList<>();
            JSONArray input = getMetadata(path).getJSONObject(id).getJSONArray("TAG");
            for (int i = 0; i < input.length(); i++) {
                result.add(input.getString(i));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject getMetadata(String path) throws JSONException, IOException {
        FileInputStream jsonInput = new FileInputStream(path);
        InputStreamReader jsonReader = new InputStreamReader(jsonInput);
        BufferedReader buffer = new BufferedReader(jsonReader);
        return new JSONObject(buffer.readLine());
    }
}
