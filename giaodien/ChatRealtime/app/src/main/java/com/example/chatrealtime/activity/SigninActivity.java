package com.example.chatrealtime.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.model.WebSocketService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SigninActivity extends AppCompatActivity {

    private TextView tv_taotk;
    private TextInputLayout tilEmail, tilMatKhau;
    private TextInputEditText tieEmail, tieMatKhau;
    private Button btnSignin;
    private CheckBox cbGhiNho;
    private SessionManager sessionManager;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        AnhXa();
        KiemTraThayDoi();

        // Nếu đã đăng nhập rồi thì vào thẳng Trang chủ
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, TrangChuActivity.class));
            finish();
            return;
        }

        // su kien chuyen form tao tai khoan
        tv_taotk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        // Sự kiện nút đăng nhập
        btnSignin.setOnClickListener(v -> {
            String email = tieEmail.getText().toString().trim();
            String password = tieMatKhau.getText().toString().trim();

            if (kiemTraNhapLieuEmail() && kiemTraNhapLieuMatKhau()) {
                progressBar.setVisibility(View.VISIBLE);
                dangNhapServer(email, password);
            }
        });
    }

    // 🔹 Gọi API đăng nhập
//    private void dangNhapServer(String email, String password) {
//        StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "login.php",
//                response -> {
//                    try {
//                        progressBar.setVisibility(View.GONE);
//
//                        JSONObject obj = new JSONObject(response);
//                        String status = obj.optString("status");
//
//                        if (status.equals("success")) {
//
//                            JSONObject account = obj.getJSONObject("account");
//
//                            int maTaiKhoan = account.getInt("maTaiKhoan");
//                            String emailServer = account.getString("email");
//                            String token = account.getString("token");
//                            int maNguoiDung = account.isNull("maNguoiDung") ? -1 : account.getInt("maNguoiDung");
//
//                            // ✅ Lưu thông tin session
//                            sessionManager.login(maTaiKhoan, maNguoiDung, emailServer, token, cbGhiNho.isChecked());
//
//                            // ✅ Cập nhật trạng thái online
//                            updateStatus("online");
//
//                            // ✅ Kết nối WebSocket (truyền đúng userId)
//                            connectWebSocket(maTaiKhoan);
//
//                            // ✅ Chuyển hướng
//                            Intent intent = (maNguoiDung == -1)
//                                    ? new Intent(SigninActivity.this, CreateInformationUserActivity.class)
//                                    : new Intent(SigninActivity.this, TrangChuActivity.class);
//
//                            Toast.makeText(SigninActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
//                            startActivity(intent);
//                            finish();
//
//                        } else {
//                            String message = obj.optString("message", "Đăng nhập thất bại");
//                            tilMatKhau.setError(message);
//                            Toast.makeText(SigninActivity.this, message, Toast.LENGTH_SHORT).show();
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Toast.makeText(SigninActivity.this, "Lỗi xử lý JSON", Toast.LENGTH_SHORT).show();
//                        Log.e("LOGIN_JSON", e.getMessage());
//                    }
//                },
//                error -> {
//                    progressBar.setVisibility(View.GONE);
//                    error.printStackTrace();
//                    Toast.makeText(SigninActivity.this, "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
//                }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("email", email);
//                params.put("password", password);
//                return params;
//            }
//        };
//
//        Volley.newRequestQueue(this).add(request);
//    }


    private void dangNhapServer(String email, String password) {

        String url = Constants.BASE_URL + "auth/login";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    progressBar.setVisibility(View.GONE);

                    try {
                        JSONObject obj = new JSONObject(response);

                        if ("success".equals(obj.getString("status"))) {

                            JSONObject account = obj.getJSONObject("account");

                            int maTaiKhoan = account.getInt("maTaiKhoan");
                            String emailServer = account.getString("email");

                            // RẤT QUAN TRỌNG
                            int maNguoiDung = account.isNull("maNguoiDung") ? -1 : account.getInt("maNguoiDung");
                            Log.e("LOGIN_INFO", "Mã người dùng: " + maNguoiDung);

                            // Lưu session
                            sessionManager.login(
                                    maTaiKhoan,
                                    maNguoiDung,
                                    emailServer,
                                    null,
                                    cbGhiNho.isChecked()
                            );

                            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                            updateStatus("online");

                            connectWebSocket(maTaiKhoan);

                            //ĐIỀU HƯỚNG ĐÚNG LOGIC
                            Intent intent;
                            if (maNguoiDung == -1) {
                                // LẦN ĐẦU – CHƯA CÓ THÔNG TIN
                                intent = new Intent(this, CreateInformationUserActivity.class);
                            } else {
                                // ĐÃ CÓ THÔNG TIN
                                intent = new Intent(this, TrangChuActivity.class);
                            }

                            startActivity(intent);
                            finish();

                        } else {
                            tilMatKhau.setError(obj.getString("message"));
                            Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    // 🔹 Cập nhật trạng thái online/offline
//    private void updateStatus(String status) {
//        int maTaiKhoan = sessionManager.getMaTaiKhoan();
//        if (maTaiKhoan == -1) return;
//
//        StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "update_status.php",
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

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constants.BASE_URL + "user/update-status",
                response -> Log.d("STATUS_UPDATE", "Status updated: " + status),
                error -> Log.e("STATUS_UPDATE_ERR", error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("status", status);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    // 🔹 Kết nối WebSocket đúng chuẩn
    private void connectWebSocket(int userId) {
        WebSocketService socketService = WebSocketService.getInstance();
        socketService.connect(Constants.WEBSOCKET_URL, userId);
        Log.d("WebSocket", "🔗 Đang kết nối WebSocket cho user: " + userId);
    }

    // 🔹 Kiểm tra thay đổi input
    private void KiemTraThayDoi() {
        tieEmail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                kiemTraNhapLieuEmail();
            }
        });

        tieMatKhau.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                kiemTraNhapLieuMatKhau();
                tilMatKhau.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            }
        });
    }

    // 🔹 Validate Email
    private boolean kiemTraNhapLieuEmail() {
        String email = tieEmail.getText().toString().trim();
        if (email.isEmpty()) {
            tilEmail.setError("Email không được để trống");
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không đúng định dạng");
            return false;
        }
        tilEmail.setError(null);
        return true;
    }

    // 🔹 Validate Mật khẩu
    private boolean kiemTraNhapLieuMatKhau() {
        String matKhau = tieMatKhau.getText().toString().trim();
        if (matKhau.isEmpty()) {
            tilMatKhau.setError("Mật khẩu không được để trống");

            return false;
        } else if (matKhau.length() < 6) {
            tilMatKhau.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        tilMatKhau.setError(null);
        return true;
    }

    // 🔹 Ánh xạ view
    private void AnhXa() {
        tv_taotk = findViewById(R.id.tv_taotk);
        tilEmail = findViewById(R.id.textInputLayoutEmail);
        tilMatKhau = findViewById(R.id.textInputLayoutPassword);
        tieEmail = findViewById(R.id.textInputEditTextEmail);
        tieMatKhau = findViewById(R.id.textInputEditTextPassword);
        btnSignin = findViewById(R.id.btn_signin);
        cbGhiNho = findViewById(R.id.checkBoxNhoDangNhap);
        progressBar = findViewById(R.id.progressBarSignIn);
    }
}
