package com.example.chatrealtime.activity.NavigationBar.ChildActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatrealtime.R;

public class activity_terms_policy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_terms_policy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CheckBox cbAgree = findViewById(R.id.cbAgree);
        Button btnAgree = findViewById(R.id.btnAgree);
        ImageView btnBack = findViewById(R.id.btnBackTerms);
        TextView tvFeedback = findViewById(R.id.tvFeedbackLink);

        btnAgree.setEnabled(false);

        cbAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAgree.setEnabled(isChecked);
        });


        tvFeedback.setOnClickListener(v -> {
            String url = "https://docs.google.com/forms/d/e/1FAIpQLScnP3ok82WK8CcxPLOVw5kU-5Nxa6oGQcPVSu4QQo550ECjAg/viewform";

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                Toast.makeText(activity_terms_policy.this, "Bạn đã đồng ý với Điều khoản và Chính sách", Toast.LENGTH_SHORT).show();
            }
        });
    }
}