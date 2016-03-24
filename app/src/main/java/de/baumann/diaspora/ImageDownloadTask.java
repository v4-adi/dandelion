package de.baumann.diaspora;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Gregor Santner (de-live-gdev) on 24.03.16.
 */
public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
    ImageView imageView;
    String savePath;

    /**
     * Download image from URL
     *
     * @param imageView ImageView to set image to (null = don't set)
     * @param savePath  Save image to file (null = don't save)
     */
    public ImageDownloadTask(@Nullable ImageView imageView, @Nullable String savePath) {
        this.imageView = imageView;
        this.savePath = savePath;
    }

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap bitmap = null;
        FileOutputStream out = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);

            // Save to file if not null
            if (savePath != null) {
                out = new FileOutputStream(savePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }

        } catch (Exception e) {
            Log.e(App.APP_LOG_TAG, e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        // Display on imageview if not null
        if (imageView != null) {
            imageView.setImageBitmap(result);
        }
    }
}
