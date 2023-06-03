package com.example.vchatmessenger.core.constants;

import static com.example.vchatmessenger.core.image_worker.ColorsWorker.getRandomColor;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.vchatmessenger.core.image_worker.ImageCreator;
import com.vdurmont.emoji.EmojiParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class Constants {
    public static String serverUrl = "http://80.90.191.25:3000/";
    public static int ok = 200;
    public static int lengthError = 333;
    public static int noNumberError = 401;
    public static int noLowercaseLetter = 402;
    public static int noUppercaseLetter = 403;
    public static int noSpecialSymbolError = 404;
    public static int contentError = 222;
    public static int matchError = 500;
    public static int connectionError = -1;

    public static OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS);

    public static byte[] getBytesOfUri(Uri uri, ContentResolver contentResolver) throws IOException {
        InputStream inputStream = contentResolver.openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static byte[] getBytesOfBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap getGeneratedBitmap(String name) {
        String text = String.valueOf(name.charAt(0));
        List<String> emojis = EmojiParser.extractEmojis(name);
        if (emojis.size() != 0 && name.startsWith(emojis.get(0))) {
            text = emojis.get(0);
        }
        ImageCreator imageCreator = new ImageCreator(getRandomColor(), text);
        return imageCreator.createBitmap();
    }
}
