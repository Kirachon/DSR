# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# DSR Mobile App ProGuard Configuration
# Optimized for security, performance, and compatibility

# Basic optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Keep line numbers for debugging crashes
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# React Native specific rules
-keep class com.facebook.react.** { *; }
-keep class com.facebook.hermes.** { *; }
-keep class com.facebook.jni.** { *; }

# Keep React Native bridge methods
-keepclassmembers class * {
    @com.facebook.react.uimanager.annotations.ReactProp <methods>;
}
-keepclassmembers class * {
    @com.facebook.react.uimanager.annotations.ReactPropGroup <methods>;
}

# Keep React Native module classes
-keep class * extends com.facebook.react.bridge.ReactContextBaseJavaModule { *; }
-keep class * extends com.facebook.react.bridge.BaseJavaModule { *; }

# Keep native modules
-keep class * implements com.facebook.react.bridge.NativeModule { *; }

# Keep JavaScript interface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Firebase rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }

# Biometric authentication
-keep class androidx.biometric.** { *; }
-keep class android.hardware.biometrics.** { *; }
-keep class android.hardware.fingerprint.** { *; }

# Keychain/Keystore
-keep class android.security.keystore.** { *; }
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }

# SQLite and database
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# OkHttp and networking
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Retrofit (if used)
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson (if used)
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Jackson (if used)
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**

# Keep model classes (adjust package names as needed)
-keep class ph.gov.dsr.mobile.models.** { *; }
-keep class ph.gov.dsr.mobile.dto.** { *; }
-keep class ph.gov.dsr.mobile.entities.** { *; }

# Keep API interfaces
-keep interface ph.gov.dsr.mobile.api.** { *; }
-keep interface ph.gov.dsr.mobile.services.** { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
    *** get*();
}

# Keep activity classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep fragment classes
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Fragment

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove debug code
-assumenosideeffects class java.lang.System {
    public static void out.println(...);
    public static void err.println(...);
}

# Optimize string concatenation
-optimizations !code/simplification/string

# Keep annotation classes
-keep class * extends java.lang.annotation.Annotation { *; }

# Keep reflection-based code
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}

# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Camera and media
-keep class android.hardware.camera2.** { *; }
-keep class androidx.camera.** { *; }

# Location services
-keep class com.google.android.gms.location.** { *; }
-keep class android.location.** { *; }

# Push notifications
-keep class com.google.firebase.messaging.** { *; }
-keep class androidx.work.** { *; }

# Accessibility
-keep class androidx.core.view.accessibility.** { *; }
-keep class android.view.accessibility.** { *; }

# Security-specific rules
-keep class ph.gov.dsr.mobile.security.** { *; }
-keep class ph.gov.dsr.mobile.crypto.** { *; }
-keep class ph.gov.dsr.mobile.auth.** { *; }

# Performance monitoring
-keep class com.google.firebase.perf.** { *; }

# Analytics
-keep class com.google.firebase.analytics.** { *; }

# Multidex
-keep class androidx.multidex.** { *; }

# Support library
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Material Design
-keep class com.google.android.material.** { *; }

# Warnings to ignore
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.**

# Additional optimizations for smaller APK
-repackageclasses ''
-allowaccessmodification
-printmapping mapping.txt

# Keep BuildConfig
-keep class ph.gov.dsr.mobile.BuildConfig { *; }

# Keep R class
-keep class ph.gov.dsr.mobile.R
-keep class ph.gov.dsr.mobile.R$* {
    <fields>;
}

# Final optimization settings
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
