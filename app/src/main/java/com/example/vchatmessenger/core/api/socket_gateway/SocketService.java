package com.example.vchatmessenger.core.api.socket_gateway;

import static com.example.vchatmessenger.core.shared_preferences.AuthWorker.getAuthData;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.vchatmessenger.gui.activities.ChatViewActivity;
import com.example.vchatmessenger.gui.fragments.ChatViewFragment;
import com.example.vchatmessenger.gui.fragments.GroupViewFragment;
import com.example.vchatmessenger.gui.activities.GroupViewActivity;
import com.example.vchatmessenger.gui.fragments.MiddleOfGroupViewFragment;
import com.example.vchatmessenger.R;
import com.example.vchatmessenger.VchatApplication;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.Credentials;

public class SocketService extends Service {

    private static final String TAG = "SocketService";
    private final IBinder binder = new LocalBinder();
    private static Socket socket;
    private static boolean isConnected;
    static int notificationId;

    @Override
    public void onCreate() {
        super.onCreate();
        if (isServerRunning()) {
            return;
        }
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnection = true;
            options.transports = new String[]{WebSocket.NAME};

            ArrayList<String> authData = getAuthData(getApplicationContext());
            options.query = Credentials.basic(authData.get(0), authData.get(1));
            socket = IO.socket("https://35b3-188-162-64-81.ngrok-free.app/", options);
            socket.on(Socket.EVENT_CONNECT, args -> {
                isConnected = true;
            });
            socket.on(Socket.EVENT_DISCONNECT, args -> {
                isConnected = false;
            });
            socket.on("newMessage", args -> {
                Long chatId = Long.valueOf((Integer) args[2]);
                if (
                        !((VchatApplication) getApplicationContext()).isAppForeground() ||
                        (
                                (
                                        !((VchatApplication) getApplicationContext()).isGroupViewActivityStarted() ||
                                        GroupViewFragment.id != chatId
                                ) &&
                                !((VchatApplication) getApplicationContext()).isChatActivityStarted()
                        )
                ) {
                    Long userId = Long.valueOf((Integer) args[0]);
                    String messageContent = (String) args[1];
                    Integer chatType = (Integer) args[3];
                    String chatName = (String) args[4];
                    byte[] imageData = Base64.decode((String) args[5], Base64.DEFAULT);
                    Bitmap chatImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    Bitmap circleBitmap = Bitmap.createBitmap(chatImage.getWidth(), chatImage.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(circleBitmap);
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    RectF rect = new RectF(0, 0, chatImage.getWidth(), chatImage.getHeight());
                    canvas.drawOval(rect, paint);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                    canvas.drawBitmap(chatImage, null, rect, paint);
                    String senderName = (String) args[6];

                    Bundle data = new Bundle();
                    data.putLong("chatId", chatId);
                    data.putString("nameOfChat", chatName);
                    data.putLong("userId", userId);
                    data.putInt("chatType", chatType);
                    data.putBoolean("noCheck", true);
                    Intent notificationIntent;
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        notificationIntent = new Intent(getApplicationContext(), GroupViewActivity.class);
                    } else {
                        notificationIntent = new Intent(getApplicationContext(), ChatViewActivity.class);
                    }
                    notificationIntent.putExtras(data);
                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                            0, notificationIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    NotificationChannel channel = null;
                    if (notificationManager != null) {
                        channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(channel);
                        channel.setShowBadge(true);
                    }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel.getId())
                            .setContentTitle(chatName)
                            .setContentText(senderName + ": " + messageContent)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setLargeIcon(circleBitmap)
                            .setAutoCancel(true)
                            .setContentIntent(contentIntent)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setPriority(NotificationCompat.PRIORITY_MAX);
                    if (notificationManager != null) {
                        notificationId += 1;
                        notificationManager.notify(notificationId, builder.build());
                    }
                } else {
                    if (((VchatApplication) getApplicationContext()).isGroupViewActivityStarted() && GroupViewFragment.id == chatId) {
                        new Handler(Looper.getMainLooper()).post(() -> MiddleOfGroupViewFragment.updateListOfMessages(getApplicationContext()));
                    } else if (((VchatApplication) getApplicationContext()).isChatActivityStarted()) {
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && GroupViewFragment.id == chatId) {
                            new Handler(Looper.getMainLooper()).post(() -> MiddleOfGroupViewFragment.updateListOfMessages(getApplicationContext()));
                        }
                        new Handler(Looper.getMainLooper()).post(() -> ChatViewFragment.updateListOfChats(getApplicationContext()));
                    }
                }

            });
            socket.on("messageDeleted", args -> {
                Long chatId = Long.valueOf((Integer) args[0]);
                if (((VchatApplication) getApplicationContext()).isGroupViewActivityStarted() && GroupViewFragment.id == chatId) {
                    new Handler(Looper.getMainLooper()).post(() -> MiddleOfGroupViewFragment.updateListOfMessages(getApplicationContext()));
                } else if (((VchatApplication) getApplicationContext()).isChatActivityStarted()) {
                    new Handler(Looper.getMainLooper()).post(() -> ChatViewFragment.updateListOfChats(getApplicationContext()));
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && GroupViewFragment.id == chatId) {
                        new Handler(Looper.getMainLooper()).post(() -> MiddleOfGroupViewFragment.updateListOfMessages(getApplicationContext()));
                    }
                }

            });
            socket.on("chatDeleting", args -> {
                Long chatId = Long.valueOf((Integer) args[0]);
                if (((VchatApplication) getApplicationContext()).isChatActivityStarted()) {
                    new Handler(Looper.getMainLooper()).post(() -> ChatViewFragment.updateListOfChats(getApplicationContext()));
                    if (GroupViewFragment.id == chatId) {
                        ChatViewActivity.id = 0;
                        ChatViewActivity.startSearchChat = false;
                        Intent intent = new Intent(getApplicationContext(), ChatViewActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Intent startIntent = new Intent(getApplicationContext(), ChatViewActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                0, startIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Определяем канал уведомлений
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, "default")
                .setContentTitle(getString(R.string.vchat_started))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (!isServerRunning()) {
            socket.connect();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (isServerRunning()) {
            socket.disconnect();
        }
        stopForeground(true);
    }

    public boolean isServerRunning() {
        return socket != null && socket.connected();
    }

    public class LocalBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }
}