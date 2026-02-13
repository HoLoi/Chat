package com.example.chatrealtime.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.Person;

import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.ChatActivity;
import com.example.chatrealtime.status.AppVisibility;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // Giữ tối đa 5 thông báo gần nhất; quá 5 thì hủy thông báo cũ nhất
    private static final int MAX_NOTIFICATIONS = 5;
    private static final Deque<Integer> recentNotificationIds = new ArrayDeque<>();
    private static final AtomicInteger nextId = new AtomicInteger(1000);

    @Override
    public void onMessageReceived(RemoteMessage message) {
        try {
            // Ưu tiên dùng data payload
            String chatId = firstNonEmpty(
                message.getData().get("chatId"),
                message.getData().get("maPhongChat"),
                message.getData().get("maPhong"),
                "chat_default"
            );

            String senderName = firstNonEmpty(
                message.getData().get("senderName"),
                message.getData().get("tenNguoiGui"),
                message.getData().get("maTaiKhoanGui"),
                message.getNotification() != null ? message.getNotification().getTitle() : null,
                "Tin nhắn mới"
            );

            String text = firstNonEmpty(
                message.getData().get("text"),
                message.getData().get("noiDung"),
                message.getData().get("body"),
                message.getNotification() != null ? message.getNotification().getBody() : null,
                "Bạn có tin nhắn mới"
            );

            String roomName = firstNonEmpty(
                message.getData().get("roomName"),
                message.getData().get("tenPhong"),
                senderName
            );

            int roomIdInt = safeParseInt(chatId, -1);

            if ((chatId == null || chatId.isEmpty()) && roomIdInt != -1) {
                chatId = String.valueOf(roomIdInt);
            }

            Log.i("FCM_MSG", "recv data chatId=" + chatId + ", roomIdInt=" + roomIdInt + ", sender=" + senderName + ", roomName=" + roomName + ", body=" + text + ", notifTitle=" + (message.getNotification() != null ? message.getNotification().getTitle() : "null") + ", notifBody=" + (message.getNotification() != null ? message.getNotification().getBody() : "null"));

            if (AppVisibility.isForeground()) {
                Log.i("FCM_MSG", "app in foreground -> skip notification");
                return; // Đang mở app thì không hiện thông báo
            }

            showPlainNotification(chatId, roomIdInt, senderName, roomName, text);
        } catch (Exception e) {
            // tránh sập app khi đang foreground
            Log.e("FCM_MSG", "onMessageReceived error", e);
        }
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isEmpty()) return v;
        }
        return "";
    }

    private int safeParseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void showPlainNotification(String chatId, int roomId, String senderName, String roomName, String text) {
        String channelId = "CHAT_CHANNEL";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String appName = getString(R.string.app_name);
        String title = senderName != null && !senderName.isEmpty() ? senderName : appName;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Chat Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("maPhong", roomId);
        intent.putExtra("maPhongChat", roomId);
        intent.putExtra("roomName", roomName);
        intent.setAction("OPEN_CHAT_" + chatId + System.currentTimeMillis()); // unique để PendingIntent không reuse sai
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
            this,
            roomId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText(roomName)
            .setNumber(0)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setShortcutId(chatId)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(contentIntent)
            .setShowWhen(true)
            .setAutoCancel(true);

        int notificationId = nextId.getAndIncrement();
        synchronized (recentNotificationIds) {
            recentNotificationIds.addLast(notificationId);
            if (recentNotificationIds.size() > MAX_NOTIFICATIONS) {
                Integer oldId = recentNotificationIds.pollFirst();
                if (oldId != null) {
                    manager.cancel(oldId);
                }
            }
        }

        Log.d("FCM_MSG", "notify id=" + notificationId + " chatId=" + chatId + " roomId=" + roomId + " sender=" + senderName + " roomName=" + roomName);
        manager.notify(notificationId, builder.build());
    }
}
