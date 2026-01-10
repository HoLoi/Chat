//package com.example.chatrealtime.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.android.volley.DefaultRetryPolicy;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import com.example.chatrealtime.Constants;
//import com.example.chatrealtime.R;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//import org.json.JSONObject;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Random;
//
//public class SignupActivity extends AppCompatActivity {
//
//    private TextView tv_dangnhap;
//    private TextInputEditText edtEmail, edtPassword, edtConfirmPassword;
//    private TextInputLayout layoutEmail, layoutPassword, layoutConfirmPassword;
//    private Button btnSignup;
//    private ProgressBar progressBar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_signup);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        Anhxa();
//        textInputEdtChange();
//
//        tv_dangnhap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        btnSignup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //kiem tra loi
//                if (layoutEmail.getError() == null && layoutPassword.getError() == null && layoutConfirmPassword.getError() == null) {
//                    //khong co loi
//                    String email = edtEmail.getText().toString().trim();
//                    String password = edtPassword.getText().toString().trim();
//
//                    //Toast.makeText(SignupActivity.this, "Đang đăng ký...", Toast.LENGTH_SHORT).show();
//                    progressBar.setVisibility(View.VISIBLE);
//
//                    // disable button de tranh nhan nhieu lan
//                    btnSignup.setEnabled(false);
//
//                    // sinh otp ngay trong android
//                    String otp = String.valueOf(new Random().nextInt(900000) + 100000);
//
//                    RequestQueue queue = Volley.newRequestQueue(SignupActivity.this);
//                    StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "register.php",
//                            response -> {
//                                // --- API CHẠY XONG (THÀNH CÔNG) ---
//                                progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
//                                btnSignup.setEnabled(true);
//                                Log.d("API_REPONSE", response);
//                                try {
//                                    //
//                                    JSONObject obj = new JSONObject(response);
//                                    Toast.makeText(SignupActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
//                                    if (obj.getString("status").equals("success")) {
//                                        Intent i = new Intent(SignupActivity.this, SignupVerifyActivity.class);
//                                        i.putExtra("email", edtEmail.getText().toString().trim());
//                                        i.putExtra("password", edtPassword.getText().toString().trim());
//                                        i.putExtra("otp", otp);
//                                        startActivity(i);
//                                    }
//                                } catch (Exception e) {
//                                    Log.e("API_PARSE_ERROR", e.toString());
//                                }
//                            },
//
//                            error -> {
//                                // --- API CHẠY XONG (LỖI) ---
//                                progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
//                                btnSignup.setEnabled(true);
//
//                                Log.e("API_ERROR", "Volley error: " + error.toString());
//                                Toast.makeText(SignupActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
//                            }
//                    ) {
//                        @Override
//                        protected Map<String, String> getParams() {
//                            Map<String, String> params = new HashMap<>();
//                            params.put("email", email);
//                            params.put("otp", otp); //chi gui email + otp cho php
//                            return params;
//                        }
//                    };
//
//                    request.setRetryPolicy(new DefaultRetryPolicy(
//                            5000, // timeout 5 giây
//                            0,    // không retry
//                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//                    ));
//                    queue.add(request);
//                }
//            }
//        });
//    }
//
//    //ham kiem tra thay doi text input
//    public void textInputEdtChange(){
//        edtEmail.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.length()==0){
//                    layoutEmail.setError("Email không được để trống");
//                } else {
//                    layoutEmail.setError(null);
//                    //kiem tra dinh dang email
//                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()){
//                        layoutEmail.setError("Email không đúng định dạng");
//                    } else {
//                        layoutEmail.setError(null);
//                    }
//                }
//            }
//        });
//
//        edtPassword.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.length()==0){
//                    layoutPassword.setError("Mật khẩu không được để trống");
//                } else {
//                    layoutPassword.setError(null);
//                    //kiem tra do dai mat khau
//                    if (charSequence.length()<6){
//                        layoutPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
//                    } else {
//                        layoutPassword.setError(null);
//                    }
//                }
//                layoutPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
//            }
//        });
//
//        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.length()==0){
//                    layoutConfirmPassword.setError("Xác nhận mật khẩu không được để trống");
//                } else {
//                    layoutConfirmPassword.setError(null);
//                    //kiem tra mat khau xac nhan
//                    if (!charSequence.toString().equals(edtPassword.getText().toString())){
//                        layoutConfirmPassword.setError("Mật khẩu xác nhận không khớp");
//                    } else {
//                        layoutConfirmPassword.setError(null);
//                    }
//                }
//                layoutConfirmPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
//            }
//        });
//    }
//
//    //ham anh xa
//    public void Anhxa() {
//        tv_dangnhap = findViewById(R.id.tv_dangnhap);
//        edtEmail = findViewById(R.id.textInputEditTextEmail);
//        edtPassword = findViewById(R.id.textInputEditTextMatkhau);
//        edtConfirmPassword = findViewById(R.id.textInputEditTextXnMatkhau);
//        layoutEmail = findViewById(R.id.textInputLayoutEmail);
//        layoutPassword = findViewById(R.id.textInputLayoutMatkhau);
//        layoutConfirmPassword = findViewById(R.id.textInputLayoutXnMatkhau);
//        btnSignup = findViewById(R.id.btnDangky);
//        progressBar = findViewById(R.id.progressBarSignup);
//    }
//}


package com.example.chatrealtime.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private TextView tv_dangnhap;
    private TextInputEditText edtEmail, edtPassword, edtConfirmPassword;
    private TextInputLayout layoutEmail, layoutPassword, layoutConfirmPassword;
    private Button btnSignup;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Anhxa();
        textInputEdtChange();

        tv_dangnhap.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, SigninActivity.class));
        });

        btnSignup.setOnClickListener(v -> {
            if (layoutEmail.getError() == null
                    && layoutPassword.getError() == null
                    && layoutConfirmPassword.getError() == null) {

                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                progressBar.setVisibility(View.VISIBLE);
                btnSignup.setEnabled(false);

                RequestQueue queue = Volley.newRequestQueue(SignupActivity.this);

                StringRequest request = new StringRequest(
                        Request.Method.POST,
                        Constants.BASE_URL + "auth/register",
                        response -> {
                            progressBar.setVisibility(View.GONE);
                            btnSignup.setEnabled(true);

                            Log.d("API_RESPONSE", response);

                            try {
                                JSONObject obj = new JSONObject(response);
                                Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();

                                if ("success".equals(obj.getString("status"))) {
                                    Intent intent = new Intent(this, SignupVerifyActivity.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("password", password);
                                    startActivity(intent);
                                }

                            } catch (Exception e) {
                                Log.e("JSON_ERROR", e.toString());
                            }
                        },
                        error -> {
                            progressBar.setVisibility(View.GONE);
                            btnSignup.setEnabled(true);

                            Log.e("API_ERROR", error.toString());
                            Toast.makeText(this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", email); // CHỈ GỬI EMAIL
                        return params;
                    }
                };

                request.setRetryPolicy(new DefaultRetryPolicy(
                        5000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));

                queue.add(request);
            }
        });
    }

    // ===== VALIDATE INPUT =====
    public void textInputEdtChange() {

        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    layoutEmail.setError("Email không được để trống");
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    layoutEmail.setError("Email không đúng định dạng");
                } else {
                    layoutEmail.setError(null);
                }
            }
        });

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    layoutPassword.setError("Mật khẩu ít nhất 6 ký tự");
                } else {
                    layoutPassword.setError(null);
                }
                layoutPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            }
        });

        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(edtPassword.getText().toString())) {
                    layoutConfirmPassword.setError("Mật khẩu xác nhận không khớp");
                } else {
                    layoutConfirmPassword.setError(null);
                }
                layoutConfirmPassword.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            }
        });
    }

    // ===== ÁNH XẠ =====
    public void Anhxa() {
        tv_dangnhap = findViewById(R.id.tv_dangnhap);
        edtEmail = findViewById(R.id.textInputEditTextEmail);
        edtPassword = findViewById(R.id.textInputEditTextMatkhau);
        edtConfirmPassword = findViewById(R.id.textInputEditTextXnMatkhau);
        layoutEmail = findViewById(R.id.textInputLayoutEmail);
        layoutPassword = findViewById(R.id.textInputLayoutMatkhau);
        layoutConfirmPassword = findViewById(R.id.textInputLayoutXnMatkhau);
        btnSignup = findViewById(R.id.btnDangky);
        progressBar = findViewById(R.id.progressBarSignup);
    }
}
