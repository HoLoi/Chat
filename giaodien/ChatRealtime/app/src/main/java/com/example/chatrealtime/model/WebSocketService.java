package com.example.chatrealtime.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * WebSocketService - Quản lý kết nối WebSocket duy nhất cho toàn app
 * - Kết nối một lần duy nhất theo userId
 * - Gửi / nhận tin nhắn dạng JSON
 * - Phát realtime qua LiveData để UI (Activity / Fragment) lắng nghe
 */
public class WebSocketService extends WebSocketListener {

    private static final String TAG = "WebSocketService";

    private static WebSocketService instance;
    private WebSocket webSocket;
    private OkHttpClient client;
    private int currentUserId = -1;
    private boolean isConnected = false;

    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    private WebSocketService() {}

    // Singleton pattern
    public static synchronized WebSocketService getInstance() {
        if (instance == null) instance = new WebSocketService();
        return instance;
    }

    /**
     * Kết nối WebSocket đến server
     * @param url: URL WebSocket (ws:// hoặc wss://)
     * @param userId: ID người dùng hiện tại
     */
    public void connect(String url, int userId) {
        if (isConnected && webSocket != null) {
            Log.d(TAG, "⚠️ Already connected. Skipping reconnect.");
            return;
        }

        this.currentUserId = userId;

        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, this);

        Log.d(TAG, " Connecting to WebSocket: " + url);
    }

    /**
     * Gửi JSON qua WebSocket
     */
    public void sendJson(JSONObject json) {
        if (webSocket != null && isConnected) {
            webSocket.send(json.toString());
            Log.d(TAG, " Sent: " + json);
        } else {
            Log.e(TAG, " WebSocket not connected. Message not sent!");
        }
    }

    /**
     * LiveData để UI lắng nghe tin nhắn realtime
     */
    public LiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    /**
     * Khi kết nối thành công
     */
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        isConnected = true;
        Log.d(TAG, " Connected to WebSocket server");

        if (currentUserId != -1) {
            try {
                JSONObject init = new JSONObject();
                init.put("type", "init");
                init.put("userId", currentUserId);
                sendJson(init);
                Log.d(TAG, " Sent init for user: " + currentUserId);
            } catch (JSONException e) {
                Log.e(TAG, " JSON error in onOpen", e);
            }
        }
    }

    /**
     * Khi nhận được tin nhắn từ server
     */
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "Received: " + text);
        try {
            JSONObject json = new JSONObject(text);
            String type = json.optString("type", "");

            switch (type) {
                case "init":
                    Log.d(TAG, " Init confirmed: " + json.optString("message"));
                    break;

                case "chat_message":
                    Log.d(TAG, " Chat message received: " + json);

//                    int senderId = json.optInt("maTaiKhoanGui");
//
//                    //  BỎ QUA tin nhắn của chính mình (đã add UI rồi)
//                    if (senderId == currentUserId) {
//                        Log.d(TAG, "⏭ Bỏ qua tin nhắn của chính mình.");
//                        return;
//                    }

                    messageLiveData.postValue(text);
                    break;

                case "message_status_update":
                    Log.d(TAG, " Message status update: " + json);
                    messageLiveData.postValue(text);
                    break;

                case "friend_request":
                case "friend_cancel":
//                    Log.d(TAG, "👥 Friend request update: " + json);
//                    messageLiveData.postValue(text);
//                    break;
                case "friend_accepted":
                    Log.d(TAG, " Friend update: " + json);
                    messageLiveData.postValue(text);
                    break;

                default:
                    Log.w(TAG, " Unknown message type: " + type);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, " Error parsing message", e);
        }
    }

    /**
     * Khi kết nối bị đóng
     */
    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        isConnected = false;
        this.webSocket = null;
        Log.w(TAG, " WebSocket closed: " + reason);
    }

    /**
     * Khi gặp lỗi kết nối
     */
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        isConnected = false;
        this.webSocket = null;
        Log.e(TAG, " WebSocket error: " + t.getMessage());

        // Tự động reconnect nhẹ nhàng
        reconnect();
    }

    /**
     * Hàm reconnect nhẹ nếu mất kết nối
     */
    private void reconnect() {
        if (currentUserId != -1 && client != null) {
            Log.d(TAG, " Attempting reconnect...");
            // Ví dụ: dùng lại URL cũ nếu cần
            // Hoặc bạn có thể lưu URL tạm khi connect
        }
    }

    /**
     * Kiểm tra kết nối WebSocket
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Ngắt kết nối
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User disconnect");
            webSocket = null;
        }
        isConnected = false;
        Log.d(TAG, "🔌 Disconnected from WebSocket.");
    }
}
