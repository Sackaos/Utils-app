package com.example.utilsapp;

import android.graphics.Bitmap;

public class WallpaperListModel {
    String str;
    Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public WallpaperListModel(String str, Bitmap bitmap) {
        this.str = str;
        this.bitmap = bitmap;
    }
}
