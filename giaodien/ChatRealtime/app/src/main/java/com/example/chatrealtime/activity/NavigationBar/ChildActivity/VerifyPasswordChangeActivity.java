package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

    String email, oldPassword, newPassword, realOtp;
    ProgressBar progressBar;

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

        // Lấy dữ liệu từ Intent
        email = getIntent().getStringExtra("email");
        oldPassword = getIntent().getStringExtra("oldPassword");
        newPassword = getIntent().getStringExtra("newPassword");
        realOtp = getIntent().getStringExtra("otp");

        // Tự động focus ô tiếp theo
        setAutoMoveOTP();

        btnVerify.setOnClickListener(v -> verifyOtp());
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

        if (!inputOtp.equals(realOtp)) {
            Toast.makeText(this, "Mã OTP không chính xác", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu OTP đúng → gửi request đổi mật khẩu
        changePassword();
    }

    private void changePassword() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constants.BASE_URL + "changePassword_verify.php",
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
                },
                error -> Toast.makeText(this, "Lỗi mạng, vui lòng thử lại", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("oldPassword", oldPassword);
                params.put("newPassword", newPassword);
                return params;
            }
        };

        queue.add(request);
    }

    public void dangxuat() {
        SessionManager sessionManager = new SessionManager(VerifyPasswordChangeActivity.this);
        int maTaiKhoan = sessionManager.getMaTaiKhoan();

        if (maTaiKhoan == -1) {
            Toast.makeText(VerifyPasswordChangeActivity.this, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi request cập nhật trạng thái offline
        StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "update_status.php",
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
                params.put("email", email); //
                params.put("status", "offline");
                return params;
            }
        };

        Volley.newRequestQueue(VerifyPasswordChangeActivity.this).add(request);
    }
}
