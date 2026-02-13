package com.example.chatrealtime.status;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class AppLifecycleObserver implements DefaultLifecycleObserver {
    private final Context context;

    public AppLifecycleObserver(Context context) {
        this.context = context;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d("Lifecycle", "App moved to foreground");
        AppVisibility.setForeground(true);
        StatusUpdater.updateStatus(context, "online");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d("Lifecycle", "App moved to background");
        AppVisibility.setForeground(false);
        StatusUpdater.updateStatus(context, "offline");
    }
}
