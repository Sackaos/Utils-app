package com.example.utilsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout[] layoutsArr;
    private WallpaperManager wallpaperManager;
    SharedPreferences sharedPref;
    String CURRENTWALLPAPER = "currentWallpaper";
    int numOfWallpapers;
    String TAG = "pickle";
    MainActivity main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: created");

        main = this;

        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: new start");
        } else Log.d(TAG, "onCreate: already happen");

        sharedPref = getSharedPreferences("myPrefs", MODE_PRIVATE);
        layoutsArr = new LinearLayout[]{findViewById(R.id.lyNotifications), findViewById(R.id.lyCustomize), findViewById(R.id.lySettings), findViewById(R.id.lyWallpaper)};
        wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        updateNumberOfWallpapers();
        setOnClicks();
        changeWallpaper(wallpaperManager.FLAG_SYSTEM);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("wakeup", false)) {
            // We were woken up by the alarm manager
            Log.d(TAG, "onNewIntent: We were woken up by the alarm manager ");
            //changeWallpaper(WallpaperManager.FLAG_LOCK);
        }


        String[] a = new String[numOfWallpapers];
        WallpaperListAdapter adapter = new WallpaperListAdapter(MainActivity.this, 0, 0, a, MainActivity.this);
        ListView list = (ListView) findViewById(R.id.lvWallpapers);
        list.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(Receiver);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("wakeup", false)) {
            Log.d(TAG, "onNewIntent: We were already woken");
            //scheduleWakeup(getApplicationContext(), 1000 * 15);
            //changeWallpaper(WallpaperManager.FLAG_SYSTEM);
        }
    }

    public static void scheduleWakeup(Context context, long timeMillis) {
        Log.d("byotch", "Scheduling wakeup for " + timeMillis);
        Toast.makeText(context, "Alarm...." + timeMillis, Toast.LENGTH_SHORT).show();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("wakeup", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        //manager.set(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 10, pendingIntent);
    }

    private void setBroadcast() {
        Intent myIntent = new Intent(getBaseContext(), Receiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, myIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);
        myIntent.putExtra("wakeup", true);
        long interval = 24 * 60 * 60 * 1000;
        interval = 10 * 1000;
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60, pendingIntent);


//        Intent intent = new Intent(this, Receiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                this.getApplicationContext(), 234324243, intent, 0);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
//                + (30 * 1000), pendingIntent);
    }

    public void changeWallpaper(int flagLock) {
        int currentWallpaperNum = Integer.parseInt(sharedPref.getString(CURRENTWALLPAPER, "0"));
        Log.d(TAG, "changeWallpaper: " + currentWallpaperNum);
        Bitmap bitmap = loadImageFromStorage("/data/data/com.example.utilsapp/app_imageDir", currentWallpaperNum);
        if (bitmap == null) showToast("UWOOOOOOH bitmap null");
        else {
            try {
                wallpaperManager.setBitmap(bitmap, null, true, flagLock);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int nextWallpaperNum;
        if (currentWallpaperNum < numOfWallpapers - 1) {
            nextWallpaperNum = currentWallpaperNum + 1;
        } else nextWallpaperNum = 0;

        sharedPref.edit().putString(CURRENTWALLPAPER, nextWallpaperNum + "").apply();


    }

    private void updateNumberOfWallpapers() {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        File childfile[] = directory.listFiles();
        numOfWallpapers = childfile.length;
    }

    private void setOnClicks() {
        TextView tvNotifs = findViewById(R.id.tvNotifications);
        tvNotifs.setOnClickListener(view -> showLayout(findViewById(R.id.lyNotifications)));

        TextView tvCustomize = findViewById(R.id.tvCustomize);
        tvCustomize.setOnClickListener(view -> showLayout(findViewById(R.id.lyCustomize)));

        TextView tvWallpaper = findViewById(R.id.tvWallpaper);
        tvWallpaper.setOnClickListener(view -> showLayout(findViewById(R.id.lyWallpaper)));

        TextView tvSettings = findViewById(R.id.tvSettings);
        tvSettings.setOnClickListener(view -> showLayout(findViewById(R.id.lySettings)));

        TextView tvbtns = findViewById(R.id.customButton);
        tvbtns.setOnClickListener(view -> {
            changeWallpaper(wallpaperManager.FLAG_SYSTEM);
        });

        ImageView ivMenuBig = findViewById(R.id.ivMenu);
        ivMenuBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout lyMenuBig = findViewById(R.id.MenuPanel);
                LinearLayout lyMenuSmall = findViewById(R.id.MenuPanelSmall);


                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.2f);
                lyMenuSmall.setLayoutParams(param);

                param = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0f);
                lyMenuBig.setLayoutParams(param);

            }
        });

        ImageView ivMenuSmall = findViewById(R.id.ivMenuSmall);
        ivMenuSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout lyMenuSmall = findViewById(R.id.MenuPanelSmall);

                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0f);
                lyMenuSmall.setLayoutParams(param);


                LinearLayout lyMenuBig = findViewById(R.id.MenuPanel);
                param = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0.4f);
                lyMenuBig.setLayoutParams(param);

            }
        });


        Button btnSetWallpaper = findViewById(R.id.btnSetWallpaper);
        btnSetWallpaper.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {


                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                int height = metrics.heightPixels;
                int width = metrics.widthPixels;

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setBorderLineColor(Color.CYAN)
                        .setBorderLineThickness(2).setFixAspectRatio(true).setAspectRatio(width, height)
                        .start(MainActivity.this);

//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper2);
            }
        });

    }

    private void showLayout(LinearLayout layoutToShow) {
        for (LinearLayout ly : layoutsArr) {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    0,
                    50,
                    0f
            );
            ly.setLayoutParams(param);
        }
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.6f
        );
        layoutToShow.setLayoutParams(param);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    saveToInternalStorage(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        // Create imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        updateNumberOfWallpapers();
        File mypath = new File(directory, "wallpaper" + numOfWallpapers + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public Bitmap loadImageFromStorage(String path, int num) {
        try {
            File f = new File(path, "wallpaper" + num + ".jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        showToast("ohNyo big oopsie when loadingImageFromStorage:( :(");
        return null;
    }

    @Override
    public void onClick(View view) {
        if (view instanceof TextView && false) {
            LinearLayout ly = (LinearLayout) view;
            showLayout(ly);
        }
    }


}