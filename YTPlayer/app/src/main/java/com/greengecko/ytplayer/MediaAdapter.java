package com.greengecko.ytplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MediaAdapter extends BaseAdapter {
    private ArrayList<Media> items;
    private Context context;

    public MediaAdapter(ArrayList<Media> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.media_list, parent, false);

        TextView name = convertView.findViewById(R.id.name);
        name.setText(items.get(position).getName());

        TextView author = convertView.findViewById(R.id.author);
        author.setText(items.get(position).getAuthor());

        ImageView image = convertView.findViewById(R.id.image);
        image.setImageResource(items.get(position).getResId());

        return convertView;
    }
}
