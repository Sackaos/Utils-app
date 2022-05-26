package com.example.utilsapp;

import android.app.Activity;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WallpaperListAdapter extends ArrayAdapter<String> {
    Context context;
    MainActivity main;

    public WallpaperListAdapter(Context context, int resource, int textViewResourceId, String[] listLength, MainActivity main) {
        super(context, 0, 0,listLength);
        this.context = context;
        this.main=main;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutinflater = ((Activity) context).getLayoutInflater();

        View view = layoutinflater.inflate(R.layout.custom_wallpaper_model, parent, false);

        TextView titleText = (TextView) view.findViewById(R.id.wallpaper_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.wallpaper_image);


        ContextWrapper cw = new ContextWrapper(main);
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        File childfile[] = directory.listFiles();
        int numOfWallpapers = childfile.length;


        titleText.setText("img_"+position);
        imageView.setImageBitmap(main.loadImageFromStorage("/data/data/com.example.utilsapp/app_imageDir",position));
        return view;

    }

    ;

}
