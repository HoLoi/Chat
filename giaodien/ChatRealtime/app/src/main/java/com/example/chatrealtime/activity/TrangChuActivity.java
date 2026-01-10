package com.example.chatrealtime.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.NavigationBar.FriendsFragment;
import com.example.chatrealtime.activity.NavigationBar.MessageFragment;
import com.example.chatrealtime.activity.NavigationBar.ProfileFragment;
import com.example.chatrealtime.model.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class TrangChuActivity extends AppCompatActivity {

    private int currentTabIndex = 0; // 0 = Tin nhắn, 1 = Bạn bè, 2 = Cá nhân

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trang_chu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        SessionManager sessionManager = new SessionManager(this);
        int maNguoiDung = sessionManager.getMaTaiKhoan();
        Log.d("FriendsFragment", "Mã người dùng hiện tại: " + maNguoiDung);

        //mac dinh hien thi MessageFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, new MessageFragment())
                .commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                int newTabIndex = 0;

                if (menuItem.getItemId() == R.id.nav_message) {
                    selectedFragment = new MessageFragment();
                    newTabIndex = 0;
                } else if (menuItem.getItemId() == R.id.nav_friends) {
                    selectedFragment = new FriendsFragment();
                    newTabIndex = 1;
                } else if (menuItem.getItemId() == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                    newTabIndex = 2;
                }

                // Quyết định hướng hiệu ứng
                if (newTabIndex > currentTabIndex) {
                    // Đi tới bên phải → animation trượt từ phải sang trái
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                            )
                            .replace(R.id.frame_layout, selectedFragment)
                            .commit();
                } else if (newTabIndex < currentTabIndex) {
                    // Quay lại bên trái → animation trượt từ trái sang phải
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_left,
                                    R.anim.slide_out_right
                            )
                            .replace(R.id.frame_layout, selectedFragment)
                            .commit();
                }

                currentTabIndex = newTabIndex;
                return true;
            }
        });


    }
}