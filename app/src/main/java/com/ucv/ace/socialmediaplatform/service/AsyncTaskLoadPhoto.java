package com.ucv.ace.socialmediaplatform.service;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.ucv.ace.socialmediaplatform.R;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;


/**
 * In this case, the task in to load an image from URL and display it in an Image View object.
 */
@SuppressWarnings("deprecation")
public class AsyncTaskLoadPhoto extends AsyncTask<String, String, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    Bitmap bitmap;

    /**
     * AsyncTaskLoadPhoto constructor.
     *
     * @param imageView - imageView object.
     */
    public AsyncTaskLoadPhoto(ImageView imageView) {
        imageViewReference = new WeakReference<>(imageView);
    }

    /**
     * This method is executed on the main thread before the background task is started.
     * In this case, the method is empty.
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    /**
     * This method runs in the background and performs the task of loading the image from the UDL address.
     *
     * @param args - strings.
     * @return - Bitmap object.
     */
    protected Bitmap doInBackground(String... args) {
        try {
            /** The URL is converted into an InputStream obj which is later used to decode the image into a Bitmap obj.**/
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
        } catch (Exception e) {
            Log.e("AsyncLoadPhoto", "Error occurred while decode the image into Bitmap object due to: ", e);
        }
        return bitmap;
    }

    /**
     * This method is executed on the main thread after the background task is completed.
     *
     * @param image - Bitmap object that was loaded in the background.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void onPostExecute(Bitmap image) {

        /**Get the reference to the ImageView object.**/
        ImageView imageView = imageViewReference.get();

        /**Check if the image was loaded successfully. **/
        if (imageView != null) {
            if (bitmap != null) {
                /** If yes, the image is set in the ImageView object.**/
                imageView.setImageBitmap(bitmap);
                Log.i("AsyncLoadPhoto", "Image was loaded successfully at post!!");
            } else {
                /** If not, a default image is set.**/
                Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.ic_image);
                imageView.setImageDrawable(placeholder);
                Toast.makeText(imageView.getContext(), "A default image was loaded!!", Toast.LENGTH_LONG).show();
            }
        }
    }
}