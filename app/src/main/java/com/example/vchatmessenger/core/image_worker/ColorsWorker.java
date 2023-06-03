package com.example.vchatmessenger.core.image_worker;

import android.graphics.Color;

import java.util.Random;

public class ColorsWorker {
    public static int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN,
            Color.MAGENTA, Color.GRAY, Color.DKGRAY, Color.LTGRAY,
            Color.parseColor("#FF0000"), Color.parseColor("#00FF00"),
            Color.parseColor("#0000FF"), Color.parseColor("#FFFF00"),
            Color.parseColor("#00FFFF"), Color.parseColor("#FF00FF"),
            Color.parseColor("#808080"), Color.parseColor("#808000"),
            Color.parseColor("#008080"), Color.parseColor("#800080"),
            Color.parseColor("#000080"), Color.parseColor("#008000"),
            Color.parseColor("#800000")};

    public static int getRandomColor() {
        return colors[new Random().nextInt(colors.length)];
    }
}
