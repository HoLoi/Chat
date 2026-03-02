package com.example.chatrealtime.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.chatrealtime.model.Message;
import com.example.chatrealtime.model.Room;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ChatDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chat_local.db";
    private static final int DATABASE_VERSION = 3;

    // --- BẢNG ROOMS (Danh sách phòng chat) ---
    private static final String TABLE_ROOMS = "rooms";
    private static final String KEY_ROOM_ID = "id";
    private static final String KEY_ROOM_NAME = "name";
    private static final String KEY_LAST_MSG = "last_msg";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_UNREAD = "unread";

    // --- BẢNG MESSAGES (Lịch sử tin nhắn) ---
    private static final String TABLE_MESSAGES = "messages";
    private static final String KEY_MSG_ID = "msg_id";
    private static final String KEY_MSG_CONTENT = "content";
    private static final String KEY_SENDER_ID = "sender_id"; // Lưu maNguoiGui
    private static final String KEY_ROOM_REF = "room_id";
    private static final String KEY_MSG_TYPE = "msg_type";
    private static final String KEY_MSG_FILE = "file_url";
    private static final String KEY_MSG_STATUS = "moderation_status";

    // --- BẢNG FRIENDS (Danh sách bạn bè) ---
    private static final String TABLE_FRIENDS = "friends";
    private static final String KEY_FRIEND_ID = "friend_id";
    private static final String KEY_FRIEND_NAME = "friend_name";
    private static final String KEY_FRIEND_AVATAR = "friend_avatar";

    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Rooms
        String createRoomTable = "CREATE TABLE " + TABLE_ROOMS + "("
                + KEY_ROOM_ID + " INTEGER PRIMARY KEY,"
                + KEY_ROOM_NAME + " TEXT,"
                + KEY_LAST_MSG + " TEXT,"
                + KEY_AVATAR + " TEXT,"
                + KEY_UNREAD + " INTEGER" + ")";
        db.execSQL(createRoomTable);

        // Tạo bảng Messages
        String createMessageTable = "CREATE TABLE " + TABLE_MESSAGES + "(" 
              + KEY_MSG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
              + KEY_ROOM_REF + " INTEGER," 
              + KEY_SENDER_ID + " INTEGER," 
              + KEY_MSG_CONTENT + " TEXT," 
              + KEY_MSG_TYPE + " TEXT," 
              + KEY_MSG_FILE + " TEXT," 
              + KEY_MSG_STATUS + " TEXT" + ")";
        db.execSQL(createMessageTable);

        // Tạo bảng Friends
        String createFriendTable = "CREATE TABLE " + TABLE_FRIENDS + "("
                + KEY_FRIEND_ID + " INTEGER PRIMARY KEY,"
                + KEY_FRIEND_NAME + " TEXT,"
                + KEY_FRIEND_AVATAR + " TEXT" + ")";
        db.execSQL(createFriendTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        onCreate(db);
    }

    // ================== XỬ LÝ ROOMS ==================

    // Lưu danh sách phòng chat (Xóa cũ lưu mới)
    public void saveRooms(List<Room> roomList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM " + TABLE_ROOMS); // Xóa dữ liệu cũ
            for (Room room : roomList) {
                ContentValues values = new ContentValues();
                values.put(KEY_ROOM_ID, room.getId());
                values.put(KEY_ROOM_NAME, room.getName());
                values.put(KEY_LAST_MSG, room.getLastMessage());
                values.put(KEY_AVATAR, room.getImageUrl());
                values.put(KEY_UNREAD, room.getUnreadCount());
                db.insert(TABLE_ROOMS, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DB_ROOM", "Lỗi lưu rooms: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // Lấy danh sách phòng chat
    public List<Room> getRooms() {
        List<Room> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ROOMS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ROOM_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROOM_NAME));
                String lastMsg = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_MSG));
                String avatar = cursor.getString(cursor.getColumnIndexOrThrow(KEY_AVATAR));
                int unread = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_UNREAD));

                Room room = new Room(id, name, lastMsg, avatar);
                room.setUnreadCount(unread);
                list.add(room);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    //Hàm xóa phòng chat và tin nhắn của phòng đó
    public void deleteRoom(int roomId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // 1. Xóa tin nhắn của phòng này trước
            db.delete(TABLE_MESSAGES, KEY_ROOM_REF + "=?", new String[]{String.valueOf(roomId)});

            // 2. Xóa phòng chat
            db.delete(TABLE_ROOMS, KEY_ROOM_ID + "=?", new String[]{String.valueOf(roomId)});

            Log.d("DB_DELETE", "Đã xóa phòng chat ID: " + roomId);
        } catch (Exception e) {
            Log.e("DB_ERROR", "Lỗi xóa phòng: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    // ================== XỬ LÝ MESSAGES ==================

    // Thêm 1 tin nhắn mới (khi gửi hoặc nhận realtime)
    public void addMessage(int roomId, int maNguoiGui, String content, String loaiTinNhan, String fileUrl, String moderationStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROOM_REF, roomId);
        values.put(KEY_SENDER_ID, maNguoiGui);
        values.put(KEY_MSG_CONTENT, content);
        values.put(KEY_MSG_TYPE, loaiTinNhan);
        values.put(KEY_MSG_FILE, fileUrl);
        values.put(KEY_MSG_STATUS, moderationStatus);
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    // Lưu danh sách tin nhắn khi load API (Xóa cũ của phòng đó lưu mới)
    public void saveMessages(int roomId, List<Message> messages) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Chỉ xóa tin nhắn của phòng hiện tại
            db.delete(TABLE_MESSAGES, KEY_ROOM_REF + "=?", new String[]{String.valueOf(roomId)});

            for (Message msg : messages) {
                ContentValues values = new ContentValues();
                values.put(KEY_ROOM_REF, roomId);
                values.put(KEY_SENDER_ID, msg.getMaNguoiGui()); // Sử dụng đúng getter của bạn
                values.put(KEY_MSG_CONTENT, msg.getNoiDung());
                values.put(KEY_MSG_TYPE, msg.getLoaiTinNhan());
                values.put(KEY_MSG_FILE, msg.getDuongDanFile());
                values.put(KEY_MSG_STATUS, msg.getModerationStatus() != null ? msg.getModerationStatus().name() : null);
                db.insert(TABLE_MESSAGES, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // Lấy tin nhắn của 1 phòng
    public List<Message> getMessages(int roomId, int myUserId) {
        List<Message> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MESSAGES, null, KEY_ROOM_REF + "=?",
                new String[]{String.valueOf(roomId)}, null, null, KEY_MSG_ID + " ASC");

        if (cursor.moveToFirst()) {
            do {
                String content = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MSG_CONTENT));
                int senderId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SENDER_ID));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MSG_TYPE));
                String file = cursor.getString(cursor.getColumnIndexOrThrow(KEY_MSG_FILE));
                String statusStr = null;
                int idxStatus = cursor.getColumnIndex(KEY_MSG_STATUS);
                if (idxStatus != -1) {
                    statusStr = cursor.getString(idxStatus);
                }

                boolean isMine = (senderId == myUserId);
                com.example.chatrealtime.model.MessageModerationStatus st = com.example.chatrealtime.model.MessageModerationStatus.from(statusStr);
                Message msg = new Message(content, isMine, senderId, type, file, null, null, st);
                list.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // ================== XỬ LÝ FRIENDS ==================

    // Lưu danh sách bạn bè
    public void saveFriends(List<JSONObject> friendsList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE FROM " + TABLE_FRIENDS);
            for (JSONObject friend : friendsList) {
                ContentValues values = new ContentValues();
                int id = friend.optInt("maTaiKhoan", friend.optInt("maNguoiDung", -1));
                if (id <= 0) continue; // bỏ qua nếu không có mã tài khoản hợp lệ

                String avatar = friend.optString("anhDaiDien_URL", "");
                if ("null".equalsIgnoreCase(avatar)) avatar = "";

                values.put(KEY_FRIEND_ID, id);
                values.put(KEY_FRIEND_NAME, friend.optString("hoTen", friend.optString("tenNguoiDung", "")));
                values.put(KEY_FRIEND_AVATAR, avatar);
                db.insertWithOnConflict(TABLE_FRIENDS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DB_FRIEND", "Lỗi lưu bạn bè: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // Hàm xóa 1 bạn bè khỏi SQLite (Dùng khi Unfriend)
    public void deleteFriend(int friendId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_FRIENDS, KEY_FRIEND_ID + "=?", new String[]{String.valueOf(friendId)});
            Log.d("DB_DELETE", "Đã xóa bạn bè ID: " + friendId + " khỏi SQLite");
        } catch (Exception e) {
            Log.e("DB_ERROR", "Lỗi xóa bạn bè: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    // Lấy danh sách bạn bè (Trả về ArrayList<JSONObject> để khớp với Adapter cũ)
    public ArrayList<JSONObject> getFriends() {
        ArrayList<JSONObject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FRIENDS, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("maNguoiDung", cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FRIEND_ID)));
                    obj.put("hoTen", cursor.getString(cursor.getColumnIndexOrThrow(KEY_FRIEND_NAME)));
                    obj.put("anhDaiDien_URL", cursor.getString(cursor.getColumnIndexOrThrow(KEY_FRIEND_AVATAR)));
                    list.add(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
