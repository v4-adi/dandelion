/*
    This file is part of the Diaspora for Android.

    Diaspora for Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora for Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora for Android.

    If not, see <http://www.gnu.org/licenses/>.
 */
 
package com.github.dfa.diaspora_android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.github.dfa.diaspora_android.task.ImageDownloadTask;

import java.io.File;

public class AvatarImageLoader {
    private File avatarFile;

    public AvatarImageLoader(Context context) {
        avatarFile = new File(context.getFilesDir(), "avatar0.png");
    }

    public boolean clearAvatarImage() {
        return (!isAvatarDownloaded() || avatarFile.delete());
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
        if (!avatarUrl.equals("")) {
            new ImageDownloadTask(imageView, avatarFile.getAbsolutePath()).execute(avatarUrl);
        }
    }
}
