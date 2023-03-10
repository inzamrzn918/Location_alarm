package com.rbofficial.inzam.locationalarm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Utils {
    public static final int LOCATION_ACCESS_CODE = 1000;

    public static final String TAG = "NO_TAG";

    public static final int LOCATION_REFRESH_TIME = 15000; // 15 seconds to update
    public static final int LOCATION_REFRESH_DISTANCE = 500; // 500 meters to update

    public static final String[] permissions = new String[]{android.Manifest.permission
            .ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final int LOCATION_SERVICE_NOTIFY_ID = 1200;
    public static final String CHANNEL_ID = "bg_service_notify_channel";


    public static Bitmap getBitmap(Drawable drawable){
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;

    }

    public static void getPermissions(Context context,int PM_CODE, String... permissions){
        boolean isGranted = false;
        for (String permission : permissions){
            if (ContextCompat.checkSelfPermission(context,
                    permission)
                    == PackageManager.PERMISSION_GRANTED) {
                isGranted = true;
            }else {
                isGranted = false;
            }
        }

        if (!isGranted){
            ActivityCompat.requestPermissions((Activity) context, permissions, PM_CODE);
        }

    }
}
