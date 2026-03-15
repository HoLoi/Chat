package com.example.chatrealtime.activity.NavigationBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.R;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.ChangePasswordActivity;
import com.example.chatrealtime.model.SessionManager;
import com.example.chatrealtime.activity.NavigationBar.ChildActivity.InformationActivity;
import com.example.chatrealtime.activity.SigninActivity;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    ShapeableImageView avatarImage;
    Button btnThongTin, btnDoiMatKhau, btnDangXuat;
    TextView txtTenUser;
    SessionManager sessionManager;
    int maTaiKhoan;

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){

        // Liên kết với layout fragment_profile.xml
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnThongTin = view.findViewById(R.id.btnThongTinSinhVien);
        btnDoiMatKhau = view.findViewById(R.id.btnDoiMatKhau);
        btnDangXuat = view.findViewById(R.id.btnDangXuat);
        avatarImage = view.findViewById(R.id.avatarImage);
        txtTenUser = view.findViewById(R.id.tv_tenUser);

        sessionManager = new SessionManager(getContext());
        String email = sessionManager.getEmail();

        loadUserProfile(email);

        btnThongTin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Thong tin sinh vien", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), InformationActivity.class);
                startActivity(intent);
            }
        });

        btnDoiMatKhau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(), "Doi mat khau", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        btnDangXuat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionManager sessionManager = new SessionManager(getContext());
                maTaiKhoan = sessionManager.getMaTaiKhoan();

                if (maTaiKhoan == -1) {
                    Toast.makeText(getContext(), "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Gửi request cập nhật trạng thái offline
//                StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "update_status.php",
//                        response -> {
//                            try {
//                                JSONObject json = new JSONObject(response);
//                                if (json.getString("status").equals("success")) {
//                                    // Xóa session và quay lại LoginActivity
//                                    sessionManager.logout();
//                                    Intent intent = new Intent(getActivity(), SigninActivity.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    startActivity(intent);
//                                    Toast.makeText(getContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Toast.makeText(getContext(), "Lỗi: " + json.getString("message"), Toast.LENGTH_SHORT).show();
//                                }
//                            } catch (Exception e) {
//                                Toast.makeText(getContext(), "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
//                            }
//                        },
//                        error -> Toast.makeText(getContext(), "Không thể kết nối server", Toast.LENGTH_SHORT).show()
//                ) {
//                    @Override
//                    protected Map<String, String> getParams() {
//                        Map<String, String> params = new HashMap<>();
//                        params.put("email", email); //
//                        params.put("status", "offline");
//                        return params;
//                    }
//                };
//
//                Volley.newRequestQueue(getContext()).add(request);

                StringRequest request = new StringRequest(
                        Request.Method.POST,
                        Constants.BASE_URL + "user/logout",
                        response -> {
                            try {
                                JSONObject json = new JSONObject(response);
                                if ("success".equals(json.getString("status"))) {
                                    sessionManager.logout();
                                    Intent intent = new Intent(getActivity(), SigninActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    Toast.makeText(getContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            json.optString("message"),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> Toast.makeText(getContext(), "Không thể kết nối server", Toast.LENGTH_SHORT).show()
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("maTaiKhoan", String.valueOf(maTaiKhoan));
                        return params;
                    }
                };

                Volley.newRequestQueue(getContext()).add(request);

            }
        });

        return view;
    }

//    private void loadUserProfile(String email) {
//        StringRequest request = new StringRequest(Request.Method.POST, Constants.BASE_URL + "get_information.php",
//                response -> {
//                    Log.d("PROFILE_RESPONSE", response);
//                    try {
//                        JSONObject json = new JSONObject(response);
//                        if (json.getString("status").equals("success")) {
//                            JSONObject data = json.getJSONObject("data");
//
//                            // Lấy đúng key theo JSON thực tế
//                            String avatarUrl = data.optString("anhDaiDien_URL");
//                            String tenNguoiDung = data.optString("tenNguoiDung");
//
//                            // Hiển thị tên người dùng
//                            txtTenUser.setText(tenNguoiDung.isEmpty() ? "Người dùng" : tenNguoiDung);
//
//                            // Load ảnh vào ImageView bằng Glide
//                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
//                                Glide.with(requireContext())
//                                        .load(avatarUrl) // vì JSON đã chứa URL đầy đủ (http://192.168.x.x/...)
//                                        .placeholder(R.drawable.avatar_default)
//                                        .error(R.drawable.avatar_default)
//                                        .into(avatarImage);
//                            } else {
//                                avatarImage.setImageResource(R.drawable.avatar_default);
//                            }
//                        } else {
//                            Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Toast.makeText(getContext(), "Lỗi xử lý phản hồi", Toast.LENGTH_SHORT).show();
//                    }
//                },
//                error -> Toast.makeText(getContext(), "Không thể kết nối server", Toast.LENGTH_SHORT).show()
//        ) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("email", email);
//                return params;
//            }
//        };
//
//        Volley.newRequestQueue(getContext()).add(request);
//    }

    private void loadUserProfile(String email) {

        String url = Constants.BASE_URL + "user/get-information?email=" + email;

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        if (!"success".equals(json.optString("status"))) {
                            Toast.makeText(getContext(),
                                    json.optString("message"),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject data = json.getJSONObject("data");

                        // ===== TÊN NGƯỜI DÙNG =====
                        txtTenUser.setText(
                                data.optString("tenNguoiDung", "Người dùng")
                        );

                        // ===== AVATAR =====
                        String avatarPath = data.optString("anhDaiDien_URL", "");
                        String avatarUrl = normalizeImageUrl(avatarPath);

                        if (avatarUrl.isEmpty()) {
                            avatarImage.setImageResource(R.drawable.avatar_default);
                            return;
                        }

                        Glide.with(requireContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.avatar_default)
                                .error(R.drawable.avatar_default)
                                .into(avatarImage);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(),
                                "Lỗi xử lý phản hồi",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(),
                        "Không thể kết nối server",
                        Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private String normalizeImageUrl(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) || "/null".equalsIgnoreCase(trimmed)) {
            return "";
        }
        if (trimmed.startsWith("http")) return trimmed;
        if (trimmed.startsWith("/")) return Constants.IMAGE_BASE_URL + trimmed;
        return Constants.IMAGE_BASE_URL + "/" + trimmed;
    }

}
