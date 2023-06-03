package com.example.vchatmessenger.core.image_worker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ImageCreator {

    protected String letter;
    protected int color;

    public ImageCreator(int color, String letter) {
        this.color = color;
        this.letter = letter.toUpperCase();
    }

    public Bitmap createBitmap() {
        int w = 512, h = 512;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(256);
        // нарисовать текст по центру
        int x = canvas.getWidth() / 2;  // x задаем по-обычному, так как есть метод setTextAlign, а вот y придется вычислять
        int y = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        canvas.drawText(letter, x, y, paint);
        return bmp;
    }
}
