# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.travelfoodie.core.data.local.entity.** { *; }

# Keep Moshi models
-keep class com.travelfoodie.core.data.remote.api.** { *; }
-keep class com.squareup.moshi.** { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static ** bind(android.view.View);
    public static ** inflate(android.view.LayoutInflater);
}

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
