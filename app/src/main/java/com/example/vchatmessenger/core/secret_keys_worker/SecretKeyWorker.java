package com.example.vchatmessenger.core.secret_keys_worker;

import android.content.Context;
import android.widget.Toast;

import com.example.vchatmessenger.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SecretKeyWorker {
    public static ArrayList<String> getSecretWords(Context context) {
        ArrayList<String> wordsToReturn = new ArrayList<>();
        try (InputStream in = context.getResources().openRawResource(R.raw.words)) {
            byte[] b = new byte[in.available()];
            assert(in.read(b) == b.length);
            String wordList = new String(b);
            String[] words = wordList.split(" ");
            for (int i = 0; i < 5; i++) {
                wordsToReturn.add(words[(int) (Math.random() * words.length)]);
            }
        } catch (IOException e) {
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
        return wordsToReturn;
    }
}
