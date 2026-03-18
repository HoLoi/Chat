package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.model.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final int OTP_REQUEST_TIMEOUT_MS = 15000;

    EditText edtOldPassword, edtNewPassword, edtConfirmNewPassword;
    Button btnChangePassword;
    ImageView btnback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AnhXa();
        CheckChangePassword();

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitChangePassword();
            }
        });
    }

    private void submitChangePassword() {
        SessionManager sessionManager = new SessionManager(ChangePasswordActivity.this);
        String email = sessionManager.getEmail();

        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isInputValid()) {
            return;
        }

        String oldPassword = edtOldPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();

        btnChangePassword.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "auth/request-change-password", response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String status = jsonObject.optString("status", "error");
                String message = jsonObject.optString("message", "Có lỗi xảy ra");

                if ("success".equals(status)) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ChangePasswordActivity.this, VerifyPasswordChangeActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("oldPassword", oldPassword);
                    intent.putExtra("newPassword", newPassword);
                    startActivity(intent);
                    btnChangePassword.setEnabled(true);
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    btnChangePassword.setEnabled(true);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                btnChangePassword.setEnabled(true);
            }
        }, error -> {
            Toast.makeText(this, "Không thể kết nối server", Toast.LENGTH_SHORT).show();
            btnChangePassword.setEnabled(true);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("oldPassword", oldPassword);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
            OTP_REQUEST_TIMEOUT_MS,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        request.setShouldRetryServerErrors(false);
        request.setShouldCache(false);

        Volley.newRequestQueue(ChangePasswordActivity.this).add(request);
    }

    private boolean isInputValid() {
        String oldPassword = edtOldPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();

        boolean isValid = true;

        if (oldPassword.isEmpty()) {
            edtOldPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            isValid = false;
        } else {
            edtOldPassword.setError(null);
        }

        if (newPassword.length() < 6) {
            edtNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        } else if (newPassword.equals(oldPassword)) {
            edtNewPassword.setError("Mật khẩu mới không được trùng mật khẩu cũ");
            isValid = false;
        } else {
            edtNewPassword.setError(null);
        }

        if (confirmNewPassword.isEmpty() || !newPassword.equals(confirmNewPassword)) {
            edtConfirmNewPassword.setError("Mật khẩu không khớp");
            isValid = false;
        } else {
            edtConfirmNewPassword.setError(null);
        }

        btnChangePassword.setEnabled(isValid);
        return isValid;
    }

    public void CheckChangePassword() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isInputValid();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        edtOldPassword.addTextChangedListener(validationWatcher);
        edtNewPassword.addTextChangedListener(validationWatcher);
        edtConfirmNewPassword.addTextChangedListener(validationWatcher);

        btnChangePassword.setEnabled(false);
    }

    public void AnhXa() {
        edtOldPassword = findViewById(R.id.et_current_password);
        edtNewPassword = findViewById(R.id.et_new_password);
        edtConfirmNewPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_update_password);
        btnback = findViewById(R.id.btn_exitChangePassword);
    }
}