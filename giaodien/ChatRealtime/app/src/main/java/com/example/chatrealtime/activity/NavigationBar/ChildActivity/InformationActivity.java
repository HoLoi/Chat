package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import static android.content.ContentValues.TAG;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.model.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InformationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    EditText editTextNgaySinh;
    ImageView btnBack;

    ImageView imageview_edit_avatar;
    ShapeableImageView imageViewAvatar;
    Button btnCapNhat;
    EditText editTextHoTen, editTextSoDienThoai;
    RadioGroup radioGroupGioiTinh;
    RadioButton radioButtonNam, radioButtonNu;

    SessionManager sessionManager;
    String emailUser;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextNgaySinh = findViewById(R.id.edtNgaySinhI);
        btnBack = findViewById(R.id.btn_back_informationI);

        imageview_edit_avatar = findViewById(R.id.imageviewEditavatarI);
        imageViewAvatar = findViewById(R.id.imageviewavatarI);
        btnCapNhat = findViewById(R.id.btn_capnhat_information);
        editTextHoTen = findViewById(R.id.edt_hotenI);
        editTextSoDienThoai = findViewById(R.id.edt_didongI);
        radioGroupGioiTinh = findViewById(R.id.radioGroupGioiTinhI);
        radioButtonNam = findViewById(R.id.radioBtnNamI);
        radioButtonNu = findViewById(R.id.radioBtnNuI);

        sessionManager = new SessionManager(this);
        emailUser = sessionManager.getEmail();

        if (emailUser == null || emailUser.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Chọn ảnh đại diện
        imageview_edit_avatar.setOnClickListener(v -> chooseImage());

        // 🔹 Tải thông tin hiện tại
        loadCurrentInformation();


        // Quyền đọc ảnh (Android 11 trở xuống)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }

        editTextNgaySinh.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Gán ngày đã chọn vào EditText
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        editTextNgaySinh.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        btnCapNhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadUserInformation();
            }
        });

    }

    /** Chọn ảnh */
    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    /** Nhận ảnh chọn */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imageViewAvatar.setImageURI(selectedImageUri);
            }
        }
    }

    /** Load thông tin người dùng hiện tại */
//    private void loadCurrentInformation() {
//        OkHttpClient client = new OkHttpClient();
//
//        RequestBody formBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("email", emailUser)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(Constants.BASE_URL + "get_information.php")
//                .post(formBody)
//                .build();
//
//        new Thread(() -> {
//            try (Response response = client.newCall(request).execute()) {
//                String res = response.body() != null ? response.body().string() : "null";
//                Log.d(TAG, "📩 Thông tin người dùng: " + res);
//
//                JSONObject json = new JSONObject(res);
//                if (json.getString("status").equals("success")) {
//                    JSONObject data = json.getJSONObject("data");
//
//                    runOnUiThread(() -> {
//                        editTextHoTen.setText(data.optString("tenNguoiDung", ""));
//                        editTextSoDienThoai.setText(data.optString("soDienThoai", ""));
//                        editTextNgaySinh.setText(data.optString("ngaySinh", ""));
//
//                        String gioiTinh = data.optString("gioiTinh", "");
//                        if (gioiTinh.equalsIgnoreCase("Nam")) {
//                            radioButtonNam.setChecked(true);
//                        } else if (gioiTinh.equalsIgnoreCase("Nữ")) {
//                            radioButtonNu.setChecked(true);
//                        }
//
//                        String avatarUrl = data.optString("anhDaiDien_URL", "");
//                        if (!avatarUrl.isEmpty()) {
//                            Glide.with(this).load(avatarUrl)
//                                    .placeholder(R.drawable.avatar_default)
//                                    .into(imageViewAvatar);
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "❌ Lỗi load thông tin: " + e.getMessage());
//            }
//        }).start();
//    }

    private void loadCurrentInformation() {
        OkHttpClient client = new OkHttpClient();

        String url = Constants.BASE_URL
                + "user/get-information?email=" + emailUser;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    Log.e(TAG, "❌ Load info failed: " + response.code());
                    return;
                }

                String res = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "📩 User info: " + res);

                JSONObject json = new JSONObject(res);

                if ("success".equals(json.optString("status"))) {

                    JSONObject data = json.getJSONObject("data");

                    runOnUiThread(() -> {
                        editTextHoTen.setText(data.optString("tenNguoiDung", ""));
                        editTextSoDienThoai.setText(data.optString("soDienThoai", ""));
                        //editTextNgaySinh.setText(data.optString("ngaySinh", ""));
                        editTextNgaySinh.setText(
                                formatDateForDisplay(data.optString("ngaySinh", ""))
                        );


                        String gioiTinh = data.optString("gioiTinh", "");
                        radioButtonNam.setChecked("Nam".equalsIgnoreCase(gioiTinh));
                        radioButtonNu.setChecked("Nữ".equalsIgnoreCase(gioiTinh));

                        String avatar = data.optString("anhDaiDien_URL", "");
                        if (!avatar.isEmpty()) {
                            Glide.with(this)
                                    .load(Constants.BASE_URL.replace("/api/", "") + avatar)
                                    .placeholder(R.drawable.avatar_default)
                                    .into(imageViewAvatar);
                        }
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Load info error", e);
            }
        }).start();
    }

    private String formatDateForDisplay(String input) {
        try {
            String[] p = input.split("-");
            return p[2] + "/" + p[1] + "/" + p[0];
        } catch (Exception e) {
            return input;
        }
    }


    /** Upload thông tin cập nhật */
//    private void uploadUserInformation() {
//        String hoTen = editTextHoTen.getText().toString().trim();
//        String ngaySinh = editTextNgaySinh.getText().toString().trim();
//        String soDienThoai = editTextSoDienThoai.getText().toString().trim();
//        String gioiTinh = radioButtonNam.isChecked() ? "Nam" : "Nữ";
//
//        if (hoTen.isEmpty() || ngaySinh.isEmpty() || soDienThoai.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        OkHttpClient client = new OkHttpClient();
//        MultipartBody.Builder builder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("email", emailUser)
//                .addFormDataPart("tenNguoiDung", hoTen)
//                .addFormDataPart("gioiTinh", gioiTinh)
//                .addFormDataPart("ngaySinh", ngaySinh)
//                .addFormDataPart("soDienThoai", soDienThoai);
//
//        if (selectedImageUri != null) {
//            try {
//                byte[] imageBytes = readBytesFromUri(selectedImageUri);
//                RequestBody fileBody = RequestBody.create(imageBytes, MediaType.parse("image/*"));
//                builder.addFormDataPart("image", "avatar_" + System.currentTimeMillis() + ".jpg", fileBody);
//            } catch (Exception e) {
//                Log.e(TAG, "Lỗi đọc ảnh: " + e.getMessage(), e);
//            }
//        }
//
//        RequestBody requestBody = builder.build();
//
//        Request request = new Request.Builder()
//                .url(Constants.BASE_URL + "update_information.php")
//                .post(requestBody)
//                .build();
//
//        Log.d(TAG, "🚀 Cập nhật thông tin cho " + emailUser);
//
//        new Thread(() -> {
//            try (Response response = client.newCall(request).execute()) {
//                String res = response.body() != null ? response.body().string() : "null";
//                Log.d(TAG, "📥 Phản hồi cập nhật: " + res);
//
//                runOnUiThread(() -> {
//                    if (res.contains("\"success\"")) {
//                        Toast.makeText(this, "✅ Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
//                        //startActivity(new Intent(this, ProfileFragment.class));
//                        finish();
//                    } else {
//                        Toast.makeText(this, "⚠️ Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } catch (IOException e) {
//                Log.e(TAG, "❌ Lỗi khi upload: " + e.getMessage(), e);
//                runOnUiThread(() ->
//                        Toast.makeText(this, "Không thể kết nối server!", Toast.LENGTH_SHORT).show()
//                );
//            }
//        }).start();
//    }

    private void uploadUserInformation() {

        String hoTen = editTextHoTen.getText().toString().trim();
        String ngaySinh = convertDate(editTextNgaySinh.getText().toString().trim()); // yyyy-MM-dd
        String soDienThoai = editTextSoDienThoai.getText().toString().trim();
        String gioiTinh = radioButtonNam.isChecked() ? "Nam" : "Nữ";

        if (hoTen.isEmpty() || soDienThoai.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", emailUser)
                .addFormDataPart("tenNguoiDung", hoTen)
                .addFormDataPart("gioiTinh", gioiTinh)
                .addFormDataPart("soDienThoai", soDienThoai);

        if (!ngaySinh.isEmpty()) {
            builder.addFormDataPart("ngaySinh", ngaySinh);
        }

        // Avatar (nếu có)
        if (selectedImageUri != null) {
            try {
                byte[] imageBytes = readBytesFromUri(selectedImageUri);
                RequestBody imageBody =
                        RequestBody.create(imageBytes, MediaType.parse("image/*"));

                builder.addFormDataPart(
                        "image",
                        "avatar_" + System.currentTimeMillis() + ".jpg",
                        imageBody
                );
            } catch (Exception e) {
                Log.e(TAG, "❌ Read image error", e);
            }
        }

        Request request = new Request.Builder()
                // ✅ GỌI ĐÚNG API UPDATE
                .url(Constants.BASE_URL + "user/update-info")
                .post(builder.build())
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {

                String res = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "📥 Update response: " + res);

                JSONObject json = new JSONObject(res);

                runOnUiThread(() -> {
                    if ("success".equals(json.optString("status"))) {
                        Toast.makeText(this, "✅ Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this,
                                json.optString("message", "Cập nhật thất bại"),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Upload error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Không kết nối được server!", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private String convertDate(String input) {
        if (input == null || input.isEmpty()) return "";

        // Đã đúng yyyy-MM-dd thì trả luôn
        if (input.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return input;
        }

        try {
            String[] p = input.split("/");
            if (p.length == 3) {
                String day = p[0].length() == 1 ? "0" + p[0] : p[0];
                String month = p[1].length() == 1 ? "0" + p[1] : p[1];
                return p[2] + "-" + month + "-" + day;
            }
        } catch (Exception ignored) {}

        return "";
    }



    /** Đọc toàn bộ bytes từ Uri */
    private byte[] readBytesFromUri(Uri uri) throws IOException {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            return buffer.toByteArray();
        }
    }
}