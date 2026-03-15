package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.SigninActivity;
import com.example.chatrealtime.model.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VerifyPasswordChangeActivity extends AppCompatActivity {

    EditText[] otpInputs;
    Button btnVerify;

    String email, oldPassword, newPassword;
    ProgressBar progressBar;
    TextView tvResend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_password_change);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 6 ô OTP
        otpInputs = new EditText[]{
                findViewById(R.id.otp1),
                findViewById(R.id.otp2),
                findViewById(R.id.otp3),
                findViewById(R.id.otp4),
                findViewById(R.id.otp5),
                findViewById(R.id.otp6)
        };

        btnVerify = findViewById(R.id.btnVerify);
        progressBar = findViewById(R.id.progressBarChangePassword);
        tvResend = findViewById(R.id.tvResend);

        // Lấy dữ liệu từ Intent
        email = getIntent().getStringExtra("email");
        oldPassword = getIntent().getStringExtra("oldPassword");
        newPassword = getIntent().getStringExtra("newPassword");

        // Tự động focus ô tiếp theo
        setAutoMoveOTP();

        btnVerify.setOnClickListener(v -> verifyOtp());
        tvResend.setOnClickListener(v -> resendOtp());
    }

    private void setAutoMoveOTP() {
        for (int i = 0; i < otpInputs.length; i++) {
            int next = i + 1;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && next < otpInputs.length) {
                        otpInputs[next].requestFocus();
                    }
                }

                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void verifyOtp() {
        // Ghép 6 số OTP
        StringBuilder sb = new StringBuilder();
        for (EditText e : otpInputs) {
            sb.append(e.getText().toString().trim());
        }
        String inputOtp = sb.toString();

        if (inputOtp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi request xác nhận OTP và đổi mật khẩu
        confirmChangePassword(inputOtp);
    }

    private void confirmChangePassword(String otp) {
        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constants.BASE_URL + "auth/confirm-change-password",
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (obj.getString("status").equals("success")) {
                            new AlertDialog.Builder(VerifyPasswordChangeActivity.this)
                                    .setTitle("Thành công")
                                    .setMessage("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        dangxuat();
                                    })
                                    .show();

                        } else {
                            Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                },
                error -> {
                    Toast.makeText(this, "Lỗi mạng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("oldPassword", oldPassword);
                params.put("newPassword", newPassword);
                params.put("otp", otp);
                return params;
            }
        };

        queue.add(request);
    }

    private void resendOtp() {
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constants.BASE_URL + "auth/request-change-password",
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(this, obj.optString("message", "Đã gửi lại OTP"), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Không thể kết nối server", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("oldPassword", oldPassword);
                return params;
            }
        };

        Volley.newRequestQueue(VerifyPasswordChangeActivity.this).add(request);
    }

    public void dangxuat() {
        SessionManager sessionManager = new SessionManager(VerifyPasswordChangeActivity.this);
        int maTaiKhoan = sessionManager.getMaTaiKhoan();

        if (maTaiKhoan == -1) {
            Toast.makeText(VerifyPasswordChangeActivity.this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi API logout để set offline + xóa token FCM trong DB
        StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "user/logout",
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getString("status").equals("success")) {
                            // Xóa session và quay lại LoginActivity
                            sessionManager.logout();
                            Intent intent = new Intent(VerifyPasswordChangeActivity.this, SigninActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Toast.makeText(VerifyPasswordChangeActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VerifyPasswordChangeActivity.this, "Lỗi: " + json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(VerifyPasswordChangeActivity.this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(VerifyPasswordChangeActivity.this, "Không thể kết nối server", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("maTaiKhoan", String.valueOf(maTaiKhoan));
                return params;
            }
        };

        Volley.newRequestQueue(VerifyPasswordChangeActivity.this).add(request);
    }
}
