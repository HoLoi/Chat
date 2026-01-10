package com.example.chatrealtime.status;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatrealtime.Constants;
import com.example.chatrealtime.model.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class StatusUpdater {
    public static void updateStatus(Context context, String status) {
        SessionManager sessionManager = new SessionManager(context);
        String email = sessionManager.getEmail();
        if (email == null || email.isEmpty()) return;

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.POST,
                Constants.BASE_URL + "user/update-status",
                response -> Log.d("StatusUpdater", "Status updated: " + status),
                error -> Log.e("StatusUpdater", "Error updating status", error)
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("status", status);
                return params;
            }
        };

        queue.add(request);
    }
}
