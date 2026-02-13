plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.chatrealtime"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.chatrealtime"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // thu vien dung cho viec lam giao dien
    implementation("com.google.android.material:material:1.12.0")

    // thu vien volley de gui nhan du lieu qua lai giua app va server
    implementation("com.android.volley:volley:1.2.1")

    // thu vien okhttp de tang toc do truyen nhan du lieu
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // thu vien picasso de load anh tu url
    implementation("com.squareup.picasso:picasso:2.8")

    // dung de load anh tu url
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // dung de Quản lý lifecycle cấp ứng dụng (toàn app), Update online/offline khi app đóng/mở
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
    // Quản lý lifecycle cơ bản của mọi component,Dùng DefaultLifecycleObserver để bắt sự kiện
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")

    // thu vien firebase
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-analytics")

    // thu vien firebase cloud messaging dung de nhan thong bao push
    implementation("com.google.firebase:firebase-messaging")
}