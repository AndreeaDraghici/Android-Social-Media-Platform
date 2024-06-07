package com.ucv.ace.socialmediaplatform.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

/**
 * Created by LittleDuck on 6/6/2024
 * Name of project: SocialMediaPlatform
 */
public class Utils {

    public Bitmap decodeBitmapFromBase64(String image) {
        byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    public String encodeBitmapToBase64(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

            return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        }
        return "";
    }
}