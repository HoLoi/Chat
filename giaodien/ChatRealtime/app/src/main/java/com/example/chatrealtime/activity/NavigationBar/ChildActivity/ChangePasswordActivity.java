package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChangePasswordActivity extends AppCompatActivity {

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
                Log.e("CLICK_CHANGE_PASSWORD", "Button Change Password clicked");
                SessionManager sessionManager = new SessionManager(ChangePasswordActivity.this);
                String email = sessionManager.getEmail();

                String newPassword = edtNewPassword.getText().toString().trim();

                // disable button de tranh nhan nhieu lan
                btnChangePassword.setEnabled(false);

                // sinh otp ngay trong android
                String otp = String.valueOf(new Random().nextInt(900000) + 100000);

                // Thực hiện thay đổi mật khẩu (gọi API hoặc cập nhật cơ sở dữ liệu)
                StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "changePassword.php", response -> {
                    // Xử lý phản hồi từ server
                    Log.e("API_RESPONSE_CHANGE_PASSWORD", response);
                    try {
                        // Phân tích phản hồi JSON
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            // Thay đổi mật khẩu thành công
                            Intent intent = new Intent(ChangePasswordActivity.this, VerifyPasswordChangeActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("oldPassword", edtOldPassword.getText().toString().trim());
                            intent.putExtra("newPassword", newPassword);
                            intent.putExtra("otp", otp);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        } else {
                            // Thay đổi mật khẩu thất bại
                            btnChangePassword.setEnabled(true);
                            Log.e("CHANGE_PASSWORD_ERROR", "Thay đổi mật khẩu thất bại");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("CHANGE_PASSWORD_EXCEPTION", e.toString());
                    }
                    btnChangePassword.setEnabled(true);
                }, error -> {
                    // Xử lý lỗi
                    Log.e("CHANGE_PASSWORD_NETWORK_ERROR", error.toString());
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Log.e("CHANGE_PASSWORD_PARAMS", "email: " + email + ", otp: " + otp);
                        Map<String, String> params = new HashMap<>();
                        params.put("email", email);
                        params.put("otp", otp);
                        return params;
                    }
                };

                Volley.newRequestQueue(ChangePasswordActivity.this).add(request);
            }
        });
    }

    public void CheckChangePassword() {
        // kiem tra 2 mat khau moi co giong nhau khong
        edtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String newPassword = edtNewPassword.getText().toString().trim();
                String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();
                if(newPassword.length() >= 6){
                    edtNewPassword.setError(null);
                    if (newPassword.equals(confirmNewPassword)) {
                        btnChangePassword.setEnabled(true);
                        edtConfirmNewPassword.setError(null);
                        edtNewPassword.setError(null);
                    } else {
                        btnChangePassword.setEnabled(false);
                        edtConfirmNewPassword.setError("Mật khẩu không khớp");
                        if(newPassword.equals(edtOldPassword.getText().toString().trim())){
                            edtNewPassword.setError("Mật khẩu mới không được trùng với mật khẩu cũ");
                        }
                    }
                } else {
                    btnChangePassword.setEnabled(false);
                    edtNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                }
            }
        });

        edtConfirmNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String newPassword = edtNewPassword.getText().toString().trim();
                String confirmNewPassword = edtConfirmNewPassword.getText().toString().trim();
                if (newPassword.equals(confirmNewPassword)) {
                    btnChangePassword.setEnabled(true);
                    edtConfirmNewPassword.setError(null);
                    edtNewPassword.setError(null);
                } else {
                    btnChangePassword.setEnabled(false);
                    edtConfirmNewPassword.setError("Mật khẩu không khớp");
                }
            }
        });
    }

    public void AnhXa()
    {
        edtOldPassword = findViewById(R.id.et_current_password);
        edtNewPassword = findViewById(R.id.et_new_password);
        edtConfirmNewPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_update_password);
        btnback = findViewById(R.id.btn_exitChangePassword);
    }
}