package com.example.chatrealtime.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VerifyForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "VerifyForgotPwd";
    private static final int OTP_REQUEST_TIMEOUT_MS = 15000;
    private static final int OTP_VERIFY_TIMEOUT_MS = 25000;

    private EditText[] otpInputs;
    private Button btnVerify;
    private TextView tvResend;
    private ProgressBar progressBar;
    private String email;
    private boolean isVerifying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        otpInputs = new EditText[]{
                findViewById(R.id.otp1),
                findViewById(R.id.otp2),
                findViewById(R.id.otp3),
                findViewById(R.id.otp4),
                findViewById(R.id.otp5),
                findViewById(R.id.otp6)
        };

        btnVerify = findViewById(R.id.btnVerify);
        tvResend = findViewById(R.id.tvResend);
        progressBar = findViewById(R.id.progressBarVerify);

        email = getIntent().getStringExtra("email");
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy email", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setAutoMoveOTP();

        btnVerify.setOnClickListener(v -> xacNhanOtp());
        tvResend.setOnClickListener(v -> guiLaiOtp());
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

    private void xacNhanOtp() {
        if (isVerifying) {
            Log.d(TAG, "xacNhanOtp: blocked because a request is already in progress");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (EditText e : otpInputs) {
            sb.append(e.getText().toString().trim());
        }
        String otp = sb.toString();

        if (!otp.matches("\\d{6}")) {
            Log.d(TAG, "xacNhanOtp: invalid otp format, valueLength=" + otp.length());
            Toast.makeText(this, "Vui long nhap dung 6 chu so OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "xacNhanOtp: start verify for email=" + email + ", otpLength=" + otp.length());
        xacNhanQuenMatKhau(otp);
    }

    private void xacNhanQuenMatKhau(String otp) {
        isVerifying = true;
        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setEnabled(false);
        tvResend.setEnabled(false);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constants.BASE_URL + "auth/confirm-forgot-password",
                response -> {
                    Log.d(TAG, "xacNhanQuenMatKhau: rawResponse=" + response);
                    isVerifying = false;
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                    tvResend.setEnabled(true);
                    try {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.optString("status", "").trim().toLowerCase();
                        String message = obj.optString("message", "");
                        Log.d(TAG, "xacNhanQuenMatKhau: parsedStatus=" + status + ", message=" + message);

                        if ("success".equals(status)) {
                            new AlertDialog.Builder(this)
                                    .setTitle("Thanh cong")
                                    .setMessage(obj.optString("message", "Mat khau moi da duoc gui qua email"))
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        Intent intent = new Intent(this, SigninActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .show();
                        } else {
                            //Toast.makeText(this, obj.optString("message", "OTP khong dung"), Toast.LENGTH_SHORT).show();
                            Toast.makeText(this, "OTP khong dung", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "xacNhanQuenMatKhau: parse response failed", e);
                        Toast.makeText(this, "Loi xu ly phan hoi", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "xacNhanQuenMatKhau: network error=" + error);
                    isVerifying = false;
                    progressBar.setVisibility(View.GONE);
                    btnVerify.setEnabled(true);
                    tvResend.setEnabled(true);
                    Toast.makeText(this, "Khong ket noi duoc server", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("otp", otp);
                Log.d(TAG, "xacNhanQuenMatKhau: requestParams email=" + email + ", otpLength=" + otp.length());
                return params;
            }
        };

        // Tranh gui lai request confirm OTP tu dong khi server phan hoi cham.
        request.setRetryPolicy(new DefaultRetryPolicy(
            OTP_VERIFY_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        request.setShouldRetryServerErrors(false);
        request.setShouldCache(false);
        Log.d(TAG, "xacNhanQuenMatKhau: retryPolicy timeoutMs=" + OTP_VERIFY_TIMEOUT_MS + ", maxRetries=0");

        Volley.newRequestQueue(this).add(request);
    }

    private void guiLaiOtp() {
        Log.d(TAG, "guiLaiOtp: start resend for email=" + email);
        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constants.BASE_URL + "auth/request-forgot-password",
                response -> {
                    Log.d(TAG, "guiLaiOtp: rawResponse=" + response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(this,
                                com.example.chatrealtime.network.ServerMessageDecoder.normalize(obj.optString("message", "Da gui lai OTP")),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "guiLaiOtp: parse response failed", e);
                        Toast.makeText(this, "Loi xu ly phan hoi", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "guiLaiOtp: network error=" + error);
                    Toast.makeText(this, "Khong ket noi duoc server", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                Log.d(TAG, "guiLaiOtp: requestParams email=" + email);
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

        Volley.newRequestQueue(this).add(request);
    }
}