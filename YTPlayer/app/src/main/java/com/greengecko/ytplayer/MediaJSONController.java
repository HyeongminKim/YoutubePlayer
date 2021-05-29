package com.greengecko.ytplayer;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        String usableTitle = title.replaceAll("[|\\\\?*<\":>/]", "_");

        source.put("TITLE", usableTitle);
        source.put("UPLOADER", uploader);
        source.put("URL", sourceUrl);
        source.put("TAG", tags);
        source.put("THUMBNAIL", thumbnailUrl);
        source.put("RATING", averageRating);

        object.put(sourceID, source);
        Log.println(Log.DEBUG, "JSON_INIT", object.toString());

        String metaDataInfo = metadataDir + "/" + usableTitle + ".info.json";
        File infoPath = new File(metaDataInfo);
        File directory = new File(metadataDir);
        if (!directory.exists()) {
            directory.mkdir();
        }
        if (infoPath.exists()) {
            infoPath.delete();
        }
        FileOutputStream writer = new FileOutputStream(metaDataInfo, false);
        writer.write(object.toString().getBytes());
        writer.close();
    }

    private JSONObject getMetadata(String path) throws JSONException, IOException {
        FileInputStream jsonInput = new FileInputStream(path);
        InputStreamReader jsonReader = new InputStreamReader(jsonInput);
        BufferedReader buffer = new BufferedReader(jsonReader);
        return new JSONObject(buffer.readLine());
    }

    public String getMediaID(String path, String title) {
        try {
            JSONObject json = getMetadata(path);
            json.names();
            for (int i = 0; i < json.names().length(); i++) {
                Log.println(Log.DEBUG, "JSON_ID", json.names().getString(i));
                if(getString(path, json.names().getString(i), "TITLE").equals(title)) {
                    return json.names().getString(i);
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
}

