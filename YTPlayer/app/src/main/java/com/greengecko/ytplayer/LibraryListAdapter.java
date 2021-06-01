package com.greengecko.ytplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class LibraryListAdapter extends BaseAdapter {
    private ArrayList<LibraryListItem> library = new ArrayList<>();

    @Override
    public int getCount() {
        return library.size();
    }

    @Override
    public Object getItem(int position) {
        return library.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_library_layout, parent, false);
        }

        ImageView albumArt = convertView.findViewById(R.id.albumArt);
        TextView title = convertView.findViewById(R.id.title);
        TextView author = convertView.findViewById(R.id.uploader);
        TextView extension = convertView.findViewById(R.id.extension);

        LibraryListItem list = library.get(position);

        albumArt.setImageBitmap(list.getAlbumArt());
        title.setText(list.getTitle());
        author.setText(list.getUploader());
        extension.setText(list.getExtension());
        if(list.getExtension().toLowerCase().equals("mp3") || list.getExtension().toLowerCase().equals("flac") || list.getExtension().toLowerCase().equals("m4a") ||
                list.getExtension().toLowerCase().equals("wav")) {
            extension.setTextColor(Color.parseColor("#FF6200EE"));
        } else {
            extension.setTextColor(Color.MAGENTA);
        }
        return convertView;
    }

    public void clearItem() {
        library.clear();
    }

    public String getTitle(int position) {
        LibraryListItem list = library.get(position);
        return list.getTitle();
    }

    public String getFileName(int position) {
        LibraryListItem list = library.get(position);
        return String.format("%s.%s", list.getTitle(), list.getExtension());
    }

    public Bitmap getAlbumArt(int position) {
        LibraryListItem list = library.get(position);
        return list.getAlbumArt();
    }

    public String getUploader(int position) {
        LibraryListItem list = library.get(position);
        return list.getUploader();
    }

    public String getExtension(int position) {
        LibraryListItem list = library.get(position);
        return list.getExtension();
    }

    public void addItem(Bitmap albumArt, String title, String author, String extension) {
        LibraryListItem item = new LibraryListItem();
        item.setAlbumArt(albumArt);
        item.setTitle(title.substring(0, title.lastIndexOf('.')));
        item.setUploader(author);
        item.setExtension(extension);

        library.add(item);
    }
}
