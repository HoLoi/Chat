package com.example.chatrealtime.status;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleObserver(this));
    }
}
