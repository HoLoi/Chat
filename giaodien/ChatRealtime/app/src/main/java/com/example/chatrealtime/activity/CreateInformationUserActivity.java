package com.example.chatrealtime.activity;

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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.model.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateInformationUserActivity extends AppCompatActivity {

    private static final String TAG = "CreateInfoUser";
    private static final int PICK_IMAGE = 1;


    ImageView imageViewBack, imageview_edit_avatar;
    ShapeableImageView imageViewAvatar;
    Button btnXacNhan;
    EditText editTextHoTen, editTextNgaySinh, editTextSoDienThoai;
    RadioGroup radioGroupGioiTinh;
    RadioButton radioButtonNam, radioButtonNu;
    ProgressBar progressBar;

    Uri selectedImageUri;
    String emailUser;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_information_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Anhxa();

        sessionManager = new SessionManager(this);
        emailUser = sessionManager.getEmail();

        if (emailUser == null || emailUser.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔹 Quyền đọc ảnh (Android 11 trở xuống)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        editTextNgaySinh.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

//            DatePickerDialog datePickerDialog = new DatePickerDialog(
//                    this,
//                    (view, selectedYear, selectedMonth, selectedDay) -> {
//                        // Gán ngày đã chọn vào EditText
//                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
//                        editTextNgaySinh.setText(date);
//                    },
//                    year, month, day
//            );

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        // Hiển thị cho người dùng
                        String displayDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        editTextNgaySinh.setText(displayDate);

                        // Format chuẩn để gửi server
                        String apiDate = String.format(
                                "%04d-%02d-%02d",
                                selectedYear,
                                selectedMonth + 1,
                                selectedDay
                        );

                        editTextNgaySinh.setTag(apiDate); // lưu ngầm
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        // 📸 Chọn ảnh đại diện
        imageview_edit_avatar.setOnClickListener(v -> chooseImage());

        btnXacNhan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadUserInformation();
//                Intent intent = new Intent(CreateInformationUserActivity.this, TrangChuActivity.class);
//                startActivity(intent);
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

    /** Gửi thông tin người dùng lên server */
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
//        progressBar.setVisibility(View.VISIBLE);
//        btnXacNhan.setEnabled(false);
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
//        // 🖼️ Nếu có chọn ảnh thì thêm vào multipart
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
//                .url(Constants.BASE_URL+"upload_information.php")
//                .post(requestBody)
//                .build();
//
//        Log.d(TAG, "🚀 Upload thông tin cho " + emailUser);
//
//        new Thread(() -> {
//            try (Response response = client.newCall(request).execute()) {
//                String res = response.body() != null ? response.body().string() : "null";
//                Log.d(TAG, "📥 Phản hồi server: " + res);
//
//                runOnUiThread(() -> {
//                    progressBar.setVisibility(View.GONE);
//                    btnXacNhan.setEnabled(true);
//                    if (res.contains("\"success\"")) {
//                        Toast.makeText(this, "✅ Tạo thông tin thành công!", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(this, TrangChuActivity.class));
//                        finish();
//
//                        updateStatus("online");
//                    } else {
//                        Toast.makeText(this, "⚠️ Lỗi khi tạo thông tin!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            } catch (IOException e) {
//                Log.e(TAG, "❌ Lỗi khi upload: " + e.getMessage(), e);
//                runOnUiThread(() ->{
//                    progressBar.setVisibility(View.GONE);
//                    btnXacNhan.setEnabled(true);
//                    Toast.makeText(this, "Không thể kết nối server!", Toast.LENGTH_SHORT).show();
//                        }
//                );
//            }
//        }).start();
//    }

    private void uploadUserInformation() {
        String hoTen = editTextHoTen.getText().toString().trim();
        //String ngaySinh = editTextNgaySinh.getText().toString().trim();
        String ngaySinh = (String) editTextNgaySinh.getTag();
        String soDienThoai = editTextSoDienThoai.getText().toString().trim();
        String gioiTinh = radioButtonNam.isChecked() ? "Nam" : "Nữ";

        if (hoTen.isEmpty() || ngaySinh.isEmpty() || soDienThoai.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnXacNhan.setEnabled(false);

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", emailUser)
                .addFormDataPart("tenNguoiDung", hoTen)
                .addFormDataPart("gioiTinh", gioiTinh)
                .addFormDataPart("ngaySinh", ngaySinh)
                .addFormDataPart("soDienThoai", soDienThoai);

        // 🖼️ Avatar (nếu có)
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
            } catch (IOException e) {
                Log.e(TAG, "Lỗi đọc ảnh", e);
            }
        }

        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "user/create-info")
                .post(builder.build())
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {

                String res = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnXacNhan.setEnabled(true);

                    try {
                        JSONObject json = new JSONObject(res);
                        String status = json.optString("status");

                        if ("success".equals(status)) {
                            Toast.makeText(this, "✅ Tạo thông tin thành công", Toast.LENGTH_SHORT).show();

                            updateStatus("online");
                            startActivity(new Intent(this, TrangChuActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this,
                                    json.optString("message", "Tạo thông tin thất bại"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi xử lý phản hồi server", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnXacNhan.setEnabled(true);
                    Toast.makeText(this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    // 🔹 Cập nhật trạng thái online/offline
//    private void updateStatus(String status) {
//        int maTaiKhoan = sessionManager.getMaTaiKhoan();
//        if (maTaiKhoan == -1) return;
//
//        StringRequest request = new StringRequest(com.android.volley.Request.Method.POST, Constants.BASE_URL + "update_status.php",
//                response -> Log.d("STATUS_UPDATE", "Cập nhật trạng thái: " + status),
//                error -> Log.e("STATUS_UPDATE_ERR", error.toString())
//        ) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("maTaiKhoan", String.valueOf(maTaiKhoan));
//                params.put("trangThai", status);
//                return params;
//            }
//        };
//
//        Volley.newRequestQueue(this).add(request);
//    }

    private void updateStatus(String status) {
        String email = sessionManager.getEmail();
        if (email == null || email.isEmpty()) return;

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("email", email)
                .addFormDataPart("status", status)
                .build();

        Request request = new Request.Builder()
                .url(Constants.BASE_URL + "user/update-status")
                .post(body)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                String res = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "✅ Update status response: " + res);
            } catch (IOException e) {
                Log.e(TAG, "❌ Update status failed", e);
            }
        }).start();
    }


    /** Đọc toàn bộ bytes từ Uri (API >= 24) */
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

    public void Anhxa(){
        imageViewBack = findViewById(R.id.imageBack);
        imageview_edit_avatar = findViewById(R.id.btn_createInformation_editavatar);
        imageViewAvatar = findViewById(R.id.iv_createInformation_avatar);
        btnXacNhan = findViewById(R.id.btn_xacnhan);
        editTextHoTen = findViewById(R.id.edt_hoten);
        editTextNgaySinh = findViewById(R.id.edtNgaySinh);
        editTextSoDienThoai = findViewById(R.id.edt_didong);
        radioGroupGioiTinh = findViewById(R.id.radioGroupGioiTinh);
        radioButtonNam = findViewById(R.id.radioBtnNam);
        radioButtonNu = findViewById(R.id.radioBtnNu);
        progressBar = findViewById(R.id.progressBarCreateInformationUser);
    }
}