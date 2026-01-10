//package com.example.chatrealtime.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import com.example.chatrealtime.Constants;
//import com.example.chatrealtime.R;
//
//import org.json.JSONObject;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class SignupVerifyActivity extends AppCompatActivity {
//
//    EditText edtOtp;
//    Button btnVerify;
//    String email, password, realOtp;
//    EditText[] otpInputs;
//    ProgressBar progressBar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_signup_verify);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        btnVerify = findViewById(R.id.btnVerify);
//        progressBar = findViewById(R.id.progressBarVerify);
//
//        //edtOtp = findViewById(R.id.edtOtp);
//        otpInputs = new EditText[]{
//                findViewById(R.id.otp1),
//                findViewById(R.id.otp2),
//                findViewById(R.id.otp3),
//                findViewById(R.id.otp4),
//                findViewById(R.id.otp5),
//                findViewById(R.id.otp6)
//        };
//
//        // Chuyển focus khi nhập
//        for (int i = 0; i < otpInputs.length; i++) {
//            int next = i + 1;
//            otpInputs[i].addTextChangedListener(new TextWatcher() {
//                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    if (s.length() == 1 && next < otpInputs.length) {
//                        otpInputs[next].requestFocus();
//                    }
//                }
//                @Override public void afterTextChanged(Editable s) {}
//            });
//        }
//
//        email = getIntent().getStringExtra("email");
//        password = getIntent().getStringExtra("password");
//        realOtp = getIntent().getStringExtra("otp");
//
//        btnVerify.setOnClickListener(view -> {
////            String inputOtp = edtOtp.getText().toString().trim();
////            if (inputOtp.isEmpty()) {
////                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
////                return;
////            }
////            if (!inputOtp.equals(realOtp)) {
////                Toast.makeText(this, "Mã OTP không chính xác", Toast.LENGTH_SHORT).show();
////                return;
////            }
//
//            // Ghép 6 ô OTP
//            StringBuilder sb = new StringBuilder();
//            for (EditText e : otpInputs) {
//                sb.append(e.getText().toString().trim());
//            }
//            String inputOtp = sb.toString();
//
//            if (inputOtp.length() < 6) {
//                Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (!inputOtp.equals(realOtp)) {
//                Toast.makeText(this, "Mã OTP không chính xác", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            verifyAccount(email, password);
//            progressBar.setVisibility(View.VISIBLE);
//            btnVerify.setEnabled(false);
//        });
//    }
//
//    private void verifyAccount(String email, String password) {
//        RequestQueue queue = Volley.newRequestQueue(this);
//        StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "verify.php",
//                response -> {
//                    progressBar.setVisibility(View.GONE);
//                    try {
//                        JSONObject obj = new JSONObject(response);
//                        if (obj.getString("status").equals("success")) {
//                            // ✅ Hiển thị hộp thoại xác nhận
//                            new AlertDialog.Builder(SignupVerifyActivity.this)
//                                    .setTitle("Thành công")
//                                    .setMessage("Tài khoản của bạn đã được xác nhận! Hãy đăng nhập ngay.")
//                                    .setCancelable(false)
//                                    .setPositiveButton("OK", (dialog, which) -> {
//                                        // Quay lại form đăng nhập
//                                        Intent intent = new Intent(SignupVerifyActivity.this, SigninActivity.class);
//                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        startActivity(intent);
//                                        finish();
//                                    })
//                                    .show();
//                        } else {
//                            Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Toast.makeText(this, "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
//                    }
//                },
//                error -> {
//                    progressBar.setVisibility(View.GONE);
//                    Toast.makeText(this, "Lỗi mạng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
//                }
//        ) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("email", email);
//                params.put("password", password);
//                return params;
//            }
//        };
//
//        queue.add(request);
//    }
//}






package com.example.chatrealtime.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignupVerifyActivity extends AppCompatActivity {

    Button btnVerify;
    EditText[] otpInputs;
    ProgressBar progressBar;

    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_verify);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnVerify = findViewById(R.id.btnVerify);
        progressBar = findViewById(R.id.progressBarVerify);

        otpInputs = new EditText[]{
                findViewById(R.id.otp1),
                findViewById(R.id.otp2),
                findViewById(R.id.otp3),
                findViewById(R.id.otp4),
                findViewById(R.id.otp5),
                findViewById(R.id.otp6)
        };

        // auto focus
        for (int i = 0; i < otpInputs.length; i++) {
            int next = i + 1;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && next < otpInputs.length) {
                        otpInputs[next].requestFocus();
                    }
                }
            });
        }

        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        btnVerify.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            for (EditText e : otpInputs) {
                sb.append(e.getText().toString().trim());
            }
            String otp = sb.toString();

            if (otp.length() < 6) {
                Toast.makeText(this, "Vui lòng nhập đủ 6 số OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnVerify.setEnabled(false);

            verifyAccount(email, password, otp);
        });
    }

    private void verifyAccount(String email, String password, String otp) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constants.BASE_URL + "auth/verify",
                response -> {
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);

                    try {
                        JSONObject obj = new JSONObject(response);

                        if ("success".equals(obj.getString("status"))) {
                            new AlertDialog.Builder(this)
                                    .setTitle("Thành công")
                                    .setMessage("Tạo tài khoản thành công! Hãy đăng nhập.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (d, w) -> {
                                        Intent i = new Intent(this, SigninActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();
                                    })
                                    .show();
                        } else {
                            Toast.makeText(this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                    Toast.makeText(this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                params.put("otp", otp);
                return params;
            }
        };

        queue.add(request);
    }
}
