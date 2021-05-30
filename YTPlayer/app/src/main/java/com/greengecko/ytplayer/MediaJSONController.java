package com.greengecko.ytplayer;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MediaJSONController {
    private String metadataDir;
    private String metadataPath;

    public MediaJSONController(@NonNull String metadataDir) {
        this.metadataDir = metadataDir;
        metadataPath = metadataDir + "/media.info.json";
    }

    public void createMetadata(String sourceID, String title, String uploader, String sourceUrl, String thumbnailUrl, double averageRating) throws JSONException, IOException {
        JSONObject object = new JSONObject();
        JSONObject source = new JSONObject();
        String usableTitle = title.replaceAll("[|\\\\?*<\":>/]", "_");

        source.put("TITLE", usableTitle);
        source.put("UPLOADER", uploader);
        source.put("URL", sourceUrl);
        source.put("THUMBNAIL", thumbnailUrl);
        source.put("RATING", averageRating);

        object.put(sourceID, source);
        Log.println(Log.DEBUG, "JSON_INIT", object.toString());
        if(new File(metadataPath).exists()) {
            commitMetadata(object.toString(), sourceID, true);
        } else {
            FileOutputStream writer = new FileOutputStream(metadataPath, false);
            writer.write(object.toString().getBytes());
            writer.close();
        }
    }

    private JSONObject getMetadata() throws JSONException, IOException {
        FileInputStream jsonInput = new FileInputStream(metadataPath);
        InputStreamReader jsonReader = new InputStreamReader(jsonInput);
        BufferedReader buffer = new BufferedReader(jsonReader);
        return new JSONObject(buffer.readLine());
    }

    private void commitMetadata(String json, String targetID, boolean append) {
        JSONObject input, output;
        try {
            JSONObject source = new JSONObject(getMetadata().toString());
            if(append) {
                input = new JSONObject(json);
                output = new JSONObject();

                output.put("TITLE", input.getJSONObject(targetID).getString("TITLE"));
                output.put("UPLOADER", input.getJSONObject(targetID).getString("UPLOADER"));
                output.put("URL", input.getJSONObject(targetID).getString("URL"));
                output.put("THUMBNAIL", input.getJSONObject(targetID).getString("THUMBNAIL"));
                output.put("RATING", input.getJSONObject(targetID).getDouble("RATING"));
                source.put(targetID, output);
            } else {
                source = new JSONObject(json);
            }
            FileOutputStream writer = new FileOutputStream(metadataPath, false);
            writer.write(source.toString().getBytes());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMediaMetadata(String id) {
        try {
            Log.println(Log.DEBUG, "CAT", getMetadata().toString());
            JSONObject object = new JSONObject(getMetadata().toString());
            object.remove(id);
            Log.println(Log.DEBUG, "JSON_DEL", object.toString());
            commitMetadata(object.toString(), id, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getMediaID(String title) {
        try {
            JSONObject json = getMetadata();
            json.names();
            for (int i = 0; i < json.names().length(); i++) {
                Log.println(Log.DEBUG, "JSON_ID", json.names().getString(i));
                if(getString(json.names().getString(i), "TITLE").equals(title)) {
                    return json.names().getString(i);
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getString(String id, String name) {
        try {
            return getMetadata().getJSONObject(id).getString(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getURL(String id, String name) {
        try {
            return getMetadata().getJSONObject(id).getString(name).replace("\\", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getRating(String id) {
        try {
            return getMetadata().getJSONObject(id).getDouble("RATING");
        } catch (Exception e) {
            e.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }
}

