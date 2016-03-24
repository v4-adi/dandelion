package de.baumann.diaspora.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;

import de.baumann.diaspora.ImageDownloadTask;

/**
 * Created by Gregor Santner (de-live-gdev) on 24.03.16.
 */
public class AvatarImageLoader {
    private File avatarFile;

    public AvatarImageLoader(Context context){
        avatarFile = new File(context.getFilesDir(), "avatar.png");
    }

    public void clearAvatarImage(){
        if (isAvatarDownloaded()) {
            avatarFile.delete();
        }
    }

    public boolean loadToImageView(ImageView imageView) {
        if (avatarFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(avatarFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    public boolean isAvatarDownloaded() {
        return avatarFile.exists();
    }

    public void startImageDownload(ImageView imageView, String avatarUrl) {
        if(!avatarUrl.equals("")) {
            new ImageDownloadTask(imageView, avatarFile.getAbsolutePath()).execute(avatarUrl);
        }
    }
}
