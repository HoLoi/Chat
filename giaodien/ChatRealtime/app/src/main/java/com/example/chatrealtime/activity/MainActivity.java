package com.example.chatrealtime.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatrealtime.R;
import com.example.chatrealtime.model.SessionManager;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_POST_NOTIFICATIONS = 1001;
    private Button btnSignup, btnSignin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //  Kiểm tra nếu đã đăng nhập thì chuyển thẳng sang Trang chủ
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn() && sessionManager.getMaNguoiDung() != -1) {
            Intent intent = new Intent(MainActivity.this, TrangChuActivity.class);
            startActivity(intent);
            finish(); // Đóng MainActivity để không quay lại
            return;
        }

        requestNotificationPermissionIfNeeded();
        //promptEnableBubblesIfNeeded();

        btnSignin = findViewById(R.id.btn_signin);
        btnSignup = findViewById(R.id.btn_signup);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, SigninActivity.class);
                    startActivity(intent);
            }
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return; // Không cần runtime permission dưới Android 13
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    REQ_POST_NOTIFICATIONS
            );
        }
    }

    private void promptEnableBubblesIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return; // Bubbles chuẩn bắt đầu tốt từ Android 11
        }

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) return;

        int pref = nm.getBubblePreference();
        if (pref == NotificationManager.BUBBLE_PREFERENCE_NONE) {
            new AlertDialog.Builder(this)
                    .setTitle("Bật bong bóng chat")
                    .setMessage("Để hiện bong bóng như Zalo/Messenger, vui lòng bật Bubbles trong cài đặt thông báo.")
                    .setPositiveButton("Mở cài đặt", (d, which) -> openBubbleSettings())
                    .setNegativeButton("Để sau", null)
                    .show();
        }
    }

    private void openBubbleSettings() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            intent = new Intent(Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        }
        startActivity(intent);
    }
}