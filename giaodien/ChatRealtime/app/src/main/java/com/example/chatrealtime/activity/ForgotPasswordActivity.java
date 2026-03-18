package com.example.chatrealtime.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final int OTP_REQUEST_TIMEOUT_MS = 15000;

    private ImageView btnBack;
    private EditText edtEmailReset;
    private Button btnSendReset;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        edtEmailReset = findViewById(R.id.edtEmailReset);
        btnSendReset = findViewById(R.id.btnSendReset);
        tvMessage = findViewById(R.id.tvMessage);

        btnBack.setOnClickListener(v -> finish());

        btnSendReset.setOnClickListener(v -> {
            String email = edtEmailReset.getText().toString().trim();
            if (!kiemTraEmail(email)) {
                return;
            }

            btnSendReset.setEnabled(false);
            guiOtpQuenMatKhau(email);
        });
    }

    private boolean kiemTraEmail(String email) {
        if (email.isEmpty()) {
            tvMessage.setText("Email khong duoc de trong");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvMessage.setText("Email khong dung dinh dang");
            return false;
        }
        tvMessage.setText("");
        return true;
    }

    private void guiOtpQuenMatKhau(String email) {
        btnSendReset.setEnabled(false);
        tvMessage.setText("");

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constants.BASE_URL + "auth/request-forgot-password",
                response -> {
                    btnSendReset.setEnabled(true);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if ("success".equals(obj.optString("status"))) {
                            Toast.makeText(this, obj.optString("message", "Da gui OTP"), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, VerifyForgotPasswordActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        } else {
                            tvMessage.setText(obj.optString("message", "Gui OTP that bai"));
                        }
                    } catch (Exception e) {
                        tvMessage.setText("Loi xu ly phan hoi");
                    }
                },
                error -> {
                    btnSendReset.setEnabled(true);
                    tvMessage.setText("Khong ket noi duoc server");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
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